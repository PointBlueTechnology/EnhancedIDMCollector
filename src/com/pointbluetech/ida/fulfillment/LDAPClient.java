/*
 * Copyright (C) 2024 Pointblue Technology LLC.
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

package com.pointbluetech.ida.fulfillment;

import com.pointbluetech.ida.collector.idm.entitlement.JndiSocketFactory;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class LDAPClient {
    static final Logger LOGGER = LoggerFactory.getLogger(LDAPClient.class);
    JSONObject request;
    Map<String,String> configMap = new HashMap<>();


    public LDAPClient(Config config, String authInfo) {
        LOGGER.debug("Creating LDAPClient with authInfo");
        parseAuthInfo(authInfo);
    }

    public LDAPClient(Config config, JSONObject request) {
        LOGGER.debug("Creating LDAPClient with request");
        this.request = request;

        if (request.has("DAAS_AUTH_INFO")) {
            String authInfo = request.optString("DAAS_AUTH_INFO");
            parseAuthInfo(authInfo);
            //request.remove("DAAS_AUTH_INFO");  //Why?
        }
    }

    public void testConnection() {
        LOGGER.info("Testing connection to LDAP server");
    }

    public JSONObject executeChangeSet() {
        LOGGER.info("Executing change set");



        return null;
    }

    private void parseAuthInfo(String authInfo) {
        String user = authInfo.substring(0, authInfo.indexOf(':'));
        String password = authInfo.substring(authInfo.indexOf(':') + 1);

        configMap.put("java.naming.security.principal", user);
        configMap.put("java.naming.security.credentials", password);

    }

    private DirContext getConnection() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        // Load any provided properties into the environment
        Iterator<String> pit = configMap.keySet().iterator();
        while (pit.hasNext()) {
            String propertyKey = pit.next();
            env.put(propertyKey, configMap.get(propertyKey));
        }

        // Honor insecure trust-all for LDAPS the same way as DirXMLClient via JndiSocketFactory
        // Enable by setting -Dida.trustAll=true (default true) or putting "trustAll=true" into configMap
        boolean trustAll = true;
        String trustAllStr = System.getProperty("ida.trustAll");
        if (trustAllStr != null) {
            trustAll = Boolean.parseBoolean(trustAllStr);
        } else if (configMap.containsKey("trustAll")) {
            trustAll = Boolean.parseBoolean(configMap.get("trustAll"));
        }

        String providerUrl = env.get("java.naming.provider.url");
        String protocol = env.get("java.naming.security.protocol");
        boolean isLdaps = (providerUrl != null && providerUrl.toLowerCase().startsWith("ldaps://"))
                || (protocol != null && protocol.equalsIgnoreCase("ssl"));
        if (isLdaps && trustAll) {
            env.put("java.naming.ldap.factory.socket", JndiSocketFactory.class.getName());
        }

        return new InitialDirContext(env);
    }
}
