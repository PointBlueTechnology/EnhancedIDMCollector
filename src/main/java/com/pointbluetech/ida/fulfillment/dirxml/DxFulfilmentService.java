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

package com.pointbluetech.ida.fulfillment.dirxml;

import com.netiq.daas.api.IDaaSService;
import com.netiq.daas.api.IFulfillmentService;
import com.netiq.daas.common.DaaSException;
import com.pointbluetech.ida.collector.idm.entitlement.Collector;
import com.pointbluetech.ida.collector.idm.entitlement.DirXMLClient;
import com.pointbluetech.ida.fulfillment.Config;
import com.pointbluetech.ida.fulfillment.LDAPClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ldap.LdapContext;

public class DxFulfilmentService implements IDaaSService, IFulfillmentService {
    static final Logger LOGGER = LoggerFactory.getLogger(DxFulfilmentService.class);
    private Config config;
    private LDAPClient ldapClient;

    String loginDN;
    String loginPWD;


    @Override
    public void shutdown() {
        LOGGER.info("Shutting down FulfilmentService");
    }

    @Override
    public void setConfigData(String logFileName, int logLevel, String instanceID, JSONObject jsonObject) throws DaaSException {

        LOGGER.info("Setting config data for FulfilmentService version 4.2.2.0");
        this.config =new Config(jsonObject);
        //Dump out for dev purposes
        try
        {
            LOGGER.debug("Config data: " + jsonObject.toString(2));
        } catch (JSONException e)
        {
            throw new DaaSException(e);
        }


    }

    @Override
    public void serviceTest(String authInfo) throws DaaSException {
   //test connection
        ldapClient = new LDAPClient(config, authInfo);
        ldapClient.testConnection();
    }

    @Override
    public JSONObject executeChangesetRequest(JSONObject request) throws DaaSException {
        LOGGER.info("Executing change set request");
        try
        {
            LOGGER.debug("Request: " + request.toString(2));
        } catch (JSONException e)
        {
            throw new DaaSException(e);
        }
//        ldapClient = new LDAPClient(config, request);
//        return ldapClient.executeChangeSet();
        try
        {
            parseAuthInfo(request.optString("DAAS_AUTH_INFO"));
            LOGGER.debug("Before getting LDAP context");
            LdapContext ldapContext = Collector.getLdapCtx(config.getHost(), loginDN, loginPWD, true, config.getPort(), true);
            LOGGER.debug("After getting LDAP context");
            DirXMLClient dirXMLClient = new DirXMLClient(ldapContext, config.getDriverDN(), 1);
            LOGGER.debug("After creating DirXMLClient");
            IDVLoopbackDocBuilder idvLoopbackDocBuilder = new IDVLoopbackDocBuilder(config, request);

            byte[] xds = idvLoopbackDocBuilder.buildXDS();
            LOGGER.debug("XDS: " + new String(xds));
            byte[] response = dirXMLClient.submitXDSCommand(xds);
            String responseStr = new String(response);
            LOGGER.debug("Response: " +responseStr);

            if(responseStr.contains("level=\"retry") || responseStr.contains("level=\"error"))
            {
                LOGGER.error("Error executing change set request: " + responseStr);
                throw new DaaSException("Error executing change set request: " + responseStr);
            }

            //comment,
            //fullfillmentId get from changeItemId
            //STATUS "success"
            //STATUS_TEXT "success"
            JSONObject responseJson = new JSONObject();
            responseJson.put("fulfillmentId", request.get("changeItemId"));
            responseJson.put("STATUS", "success");
            responseJson.put("STATUS_TEXT", "success");
            responseJson.put("comment", responseStr);

            return responseJson;
        }catch (Exception e)
        {
            if(e instanceof DaaSException)
                throw (DaaSException)e;
            //TODO: need to catch common exceptions and provide meaningful response

            LOGGER.error("Error executing change set request: " + e.getMessage());
            throw new DaaSException("Error executing change set request: " + e.getMessage());
        }
    }

    private void parseAuthInfo(String authInfo)
    {
        // Depending on application requirements, an exception may be thrown for
        // null credentials on a request. For other applications, no authInfo may be acceptable
        // (eg: File-input applications)
        if (authInfo == null || authInfo.isEmpty())
        {
            LOGGER.error("No authentication info provided with request");
            return;
        }

        // Authentication info arrives in user:password format
         loginDN = authInfo.substring(0, authInfo.indexOf(':'));
         loginPWD = authInfo.substring(authInfo.indexOf(':') + 1);

    }


}


