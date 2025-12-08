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

package com.pointbluetech.ida.collector.idm.entitlement.offline;

import com.netiq.daas.common.DaaSException;
import com.pointbluetech.ida.collector.idm.entitlement.Collector;
import com.pointbluetech.ida.collector.idm.entitlement.DirXMLClient;

import javax.naming.ldap.LdapContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class DXMLTest {

    public static void main(String[] args){
        int code = run(args);
        System.exit(code);
    }

    /**
     * Executes the DXMLTest logic and returns a process-style exit code instead of exiting the JVM.
     * 0 = success, non-zero = error.
     */
    public static int run(String[] args){
        // Usage: host port userDN password driverDN inputFile [ssl=true|false] [trustAll=true|false] [readTimeout]
        // New behavior: if no arguments are provided, read parameters from dxmlConfig.properties in the working directory.

        String host;
        int port;
        String userDN;
        String password;
        String driverDN;
        String inputFile;
        boolean ssl = true;       // defaults retained for both CLI and properties
        boolean trustAll = true;  // defaults retained for both CLI and properties
        int readTimeout = 1;      // DirXMLClient often uses 1

        if (args.length == 0) {
            // Load from default dxmlConfig.properties
            Properties props = new Properties();
            String cfgPath = "dxmlConfig.properties";
            try (FileInputStream fis = new FileInputStream(cfgPath)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Could not read properties file '" + cfgPath + "': " + e.getMessage());
                printUsage();
                return 1;
            }

            host = require(props, "host");
            port = parseInt(require(props, "port"), "port");
            userDN = require(props, "userDN");
            password = require(props, "password");
            driverDN = require(props, "driverDN");
            inputFile = require(props, "inputFile");
            // optional
            ssl = parseBool(props.getProperty("ssl"), true);
            trustAll = parseBool(props.getProperty("trustAll"), true);
            String rt = props.getProperty("readTimeout");
            if (rt != null && !rt.trim().isEmpty()) {
                readTimeout = parseInt(rt, "readTimeout");
            }
        } else if (args.length == 1) {
            // Load from the provided properties file path
            Properties props = new Properties();
            String cfgPath = args[0];
            try (FileInputStream fis = new FileInputStream(cfgPath)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Could not read properties file '" + cfgPath + "': " + e.getMessage());
                printUsage();
                return 1;
            }

            host = require(props, "host");
            port = parseInt(require(props, "port"), "port");
            userDN = require(props, "userDN");
            password = require(props, "password");
            driverDN = require(props, "driverDN");
            inputFile = require(props, "inputFile");
            // optional
            ssl = parseBool(props.getProperty("ssl"), true);
            trustAll = parseBool(props.getProperty("trustAll"), true);
            String rt = props.getProperty("readTimeout");
            if (rt != null && !rt.trim().isEmpty()) {
                readTimeout = parseInt(rt, "readTimeout");
            }
        } else if (args.length >= 6) {
            host = args[0];
            port = Integer.parseInt(args[1]);
            userDN = args[2];
            password = args[3];
            driverDN = args[4];
            inputFile = args[5];
            ssl = args.length > 6 ? Boolean.parseBoolean(args[6]) : true;
            trustAll = args.length > 7 ? Boolean.parseBoolean(args[7]) : true;
            readTimeout = args.length > 8 ? Integer.parseInt(args[8]) : 1; // DirXMLClient often uses 1
        } else {
            printUsage();
            return 1;
        }

        LdapContext ctx = null;
        try {
            byte[] requestBytes = Files.readAllBytes(Paths.get(inputFile));

            ctx = Collector.getLdapCtx(host, userDN, password, ssl, port, trustAll);
            DirXMLClient client = new DirXMLClient(ctx, driverDN, readTimeout);

            byte[] response = client.submitXDSCommand(requestBytes);
            if (response != null) {
                String out = new String(response, StandardCharsets.UTF_8);
                System.out.println(out);
            } else {
                System.out.println("<no response>");
            }
            return 0;
        } catch (DaaSException e) {
            System.err.println("DaaS error: " + e.getMessage());
            e.printStackTrace(System.err);
            return 2;
        } catch (Exception e) {
            System.err.println("Error running DXMLTest: " + e.getMessage());
            e.printStackTrace(System.err);
            return 3;
        } finally {
            if (ctx != null) {
                try { ctx.close(); } catch (Exception ignore) { }
            }
        }
    }

    private static void printUsage() {
        System.err.println("Usage: java " + DXMLTest.class.getName() +
                " <host> <port> <userDN> <password> <driverDN> <inputFile> [ssl=true|false] [trustAll=true|false] [readTimeout]\\n" +
                "Or run with no arguments to load from dxmlConfig.properties in the current directory.\\n" +
                "Or run with a single argument pointing to a properties file to override the default (e.g., dxmlConfig-dev.properties).\\n" +
                "Expected properties keys: host, port, userDN, password, driverDN, inputFile, [ssl], [trustAll], [readTimeout]");
    }

    private static String require(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.trim().isEmpty()) {
            System.err.println("Missing required property: " + key);
            printUsage();
            return ""; // caller will likely fail parsing and return non-zero
        }
        return v.trim();
    }

    private static int parseInt(String s, String name) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            System.err.println("Invalid integer for '" + name + "': " + s);
            printUsage();
            return 0; // caller decides exit code
        }
    }

    private static boolean parseBool(String s, boolean dflt) {
        if (s == null || s.trim().isEmpty()) return dflt;
        return Boolean.parseBoolean(s.trim());
    }
}
