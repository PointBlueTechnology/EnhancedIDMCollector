/*
 * Copyright (C) 2025 Pointblue Technology LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pointbluetech.ida.fulfillment.dirxml.test;

import com.pointbluetech.ida.collector.idm.entitlement.JndiSocketFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.text.DecimalFormat;
import java.util.Hashtable;

/**
 * Simple utility to bulk-create user entries in LDAP using LdapContext (JNDI).
 *
 * Defaults:
 * - Base container: o=testData
 * - ObjectClass: User
 * - Count: 100000
 * - Attribute population: unique cn and sn per entry
 *
 * Usage:
 *   java com.pointbluetech.ida.fulfillment.dirxml.test.CreateBulkUsers \
 *       <ldapUrl> <bindDn> <password> [count] [baseDn]
 *
 * Examples:
 *   java ... CreateBulkUsers ldap://localhost:389 "cn=admin,ou=sa,o=system" secret 100000 o=testData
 */
public class CreateBulkUsers {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java " + CreateBulkUsers.class.getName() +
                    " <ldapUrl> <bindDn> <password> [count=100000] [baseDn=o=testData]");
            System.exit(64); // EX_USAGE
        }

        final String ldapUrl = args[0];
        final String bindDn = args[1];
        final String password = args[2];
        final int count = (args.length >= 4) ? Integer.parseInt(args[3]) : 100000;
        final String baseDn = (args.length >= 5) ? args[4] : "o=testData";

        LdapContext ctx = null;
        try {
            ctx = connect(ldapUrl, bindDn, password);
            ensureBaseContainer(ctx, baseDn);
            createUsers(ctx, baseDn, count);
            System.out.println("Completed creating " + count + " user entries under " + baseDn);
        } catch (Exception e) {
            System.err.println("Bulk user creation failed: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        } finally {
            if (ctx != null) try { ctx.close(); } catch (NamingException ignore) {}
        }
    }

    private static LdapContext connect(String url, String bindDn, String password) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(javax.naming.Context.PROVIDER_URL, url);
        env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");
        env.put(javax.naming.Context.SECURITY_PRINCIPAL, bindDn);
        env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);

        // If using LDAPS and trust-all is enabled, use the same DummyTrustManager-based socket factory
        boolean isLdaps = url != null && url.toLowerCase().startsWith("ldaps://");
        boolean trustAll = Boolean.parseBoolean(System.getProperty("ida.trustAll", "true"));
        if (isLdaps && trustAll) {
            env.put("java.naming.ldap.factory.socket", JndiSocketFactory.class.getName());
        }
        return new InitialLdapContext(env, null);
    }

    private static void ensureBaseContainer(LdapContext ctx, String baseDn) throws NamingException {
        try {
            // Probe if baseDn exists
            Attributes attrs = ctx.getAttributes(baseDn, new String[] {"objectClass"});
            if (attrs != null) return;
        } catch (NamingException e) {
            // Create base if it doesn't exist; if base is of the form o=xxx, use organization
            String rdnAttr = baseDn.split("=", 2)[0].toLowerCase();
            String rdnValue = baseDn.split("=", 2)[1];

            BasicAttributes attrs = new BasicAttributes(true);
            BasicAttribute oc = new BasicAttribute("objectClass");
            oc.add("top");
            if ("o".equals(rdnAttr)) {
                oc.add("organization");
                attrs.put("o", rdnValue);
            } else if ("ou".equals(rdnAttr)) {
                oc.add("organizationalUnit");
                attrs.put("ou", rdnValue);
            } else if ("cn".equals(rdnAttr)) {
                oc.add("container");
                attrs.put("cn", rdnValue);
            } else {
                // Fallback generic container
                oc.add("container");
                attrs.put(rdnAttr, rdnValue);
            }
            attrs.put(oc);

            System.out.println("Creating base container: " + baseDn);
            ctx.createSubcontext(baseDn, attrs).close();
        }
    }

    private static void createUsers(LdapContext ctx, String baseDn, int count) throws NamingException {
        DecimalFormat df = new DecimalFormat("000000");
        int created = 0;
        long start = System.currentTimeMillis();

        for (int i = 1; i <= count; i++) {
            String suffix = df.format(i);
            String cn = "TestUser-" + suffix;
            String sn = "User-" + suffix;
            String dn = "cn=" + escapeRdnValue(cn) + "," + baseDn;

            Attributes attrs = new BasicAttributes(true);
            BasicAttribute oc = new BasicAttribute("objectClass");
            oc.add("top");
            oc.add("User"); // per requirement
            attrs.put(oc);
            attrs.put("cn", cn);
            attrs.put("sn", sn);

            try {
                ctx.createSubcontext(dn, attrs).close();
                created++;
            } catch (NameAlreadyBoundException nab) {
                // Skip existing entries
            }

            if (i % 1000 == 0) {
                long elapsed = System.currentTimeMillis() - start;
                System.out.println("Progress: " + i + "/" + count + " (created=" + created + ") in " + elapsed + " ms");
            }
        }
    }

    // Minimal RDN escaping for commas and plus signs, per RFC 2253 basics
    private static String escapeRdnValue(String value) {
        return value.replace("\\", "\\\\").replace(",", "\\,").replace("+", "\\+");
    }
}
