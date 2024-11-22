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

import com.netiq.daas.api.IDaaSService;
import com.netiq.daas.api.IFulfillmentService;
import com.netiq.daas.common.DaaSException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: implement this to set entitlements directly via LDAP

public class FulfilmentService implements IDaaSService, IFulfillmentService {
    static final Logger LOGGER = LoggerFactory.getLogger(FulfilmentService.class);
    private Config config;
    private LDAPClient ldapClient;

    @Override
    public void shutdown() {
        LOGGER.info("Shutting down FulfilmentService");
    }

    @Override
    public void setConfigData(String logFileName, int logLevel, String instanceID, JSONObject jsonObject) throws DaaSException {

        LOGGER.info("Setting config data for FulfilmentService");
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
        ldapClient = new LDAPClient(config, request);
        return ldapClient.executeChangeSet();
    }


}


