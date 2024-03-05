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

package com.pointbluetech.ida.collector.idm.entitlement;


import com.netiq.daas.api.IDaaSService;
import com.netiq.daas.api.IDataSourceService;
import com.netiq.daas.common.CommonImpl;
import com.netiq.daas.common.DaaSException;
import com.netiq.daas.common.UUID;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IDMEntitlementCollectionService implements IDaaSService, IDataSourceService {
    static final Logger LOGGER = LoggerFactory.getLogger(IDMEntitlementCollectionService.class.getName());
    private static final Map<String, Collector> g_collections = new ConcurrentHashMap<>();
    private boolean m_cancel = false;

   private Collector m_collector = null;

   private ServiceParams serviceParams = null;

    /**
     * This method shuts down the service.
     * It first logs that it is in the shutdown method.
     * If the Collector object is not null, it calls the shutdown method on the Collector.
     */
    @Override
    public void shutdown() {
        LOGGER.debug("In shutdown...");
        if (m_collector != null)
        {
            m_collector.shutdown();
        }
    }

    /**
     * This method sets the configuration data for the service.
     * It first logs that it is in the setConfigData method.
     * It then creates a new ServiceParams object with the configData parameter.
     * If there is an exception during the operation, it throws a DaaSException with the exception and the TYPE_INVALID_PARAMETER error code.
     *
     * @param traceFileName The name of the trace file.
     * @param traceLevel The level of the trace.
     * @param instanceID The ID of the instance.
     * @param configData The configuration data as a JSONObject.
     * @throws DaaSException If there is an error during the operation.
     */
    @Override
    public void setConfigData(String traceFileName, int traceLevel, String instanceID, JSONObject configData) throws DaaSException {

        try
        {
            LOGGER.debug("In setConfigData...");
            serviceParams = new ServiceParams(configData);

        }
        catch (Exception thr)
        {
            throw new DaaSException(thr, CommonImpl.TYPE_INVALID_PARAMETER);
        }
    }

    /**
     * This method tests the service by setting up a Collector and testing the connection.
     * It first logs that it is in the serviceTest method.
     * It then creates a new Collector object with the serviceParams parameter.
     * It sets the credentials for the Collector with the authInfo parameter.
     * It logs that the credentials have been set successfully.
     * It then tests the connection of the Collector.
     * If there is an exception during the operation, it throws a DaaSException.
     *
     * @param s The authentication information for the Collector.
     * @throws DaaSException If there is an error during the operation.
     */
    @Override
    public void serviceTest(String s) throws DaaSException {
        LOGGER.debug("In serviceTest...");
        m_collector = new Collector(serviceParams);
        setConnectorCredentials(s);
        LOGGER.debug("serviceTest set Credentials successful");
        m_collector.testConnection();

    }

    /**
     * This method tests the view by executing a JSON chunk request and checking the results.
     * It first logs that it is in the viewTest method.
     * It then executes a JSON chunk request with the jsonObject parameter, a null token, and a chunk size of 1.
     * It gets the results from the response as a JSONArray.
     * If the results array has a length greater than 0, it logs that the view test was successful.
     * If the results array does not have a length greater than 0, it throws a DaaSException with the DAAS_ERROR_CONNECTION_FAILURE error code.
     * If there is a JSONException during the operation, it throws a DaaSException with the TYPE_DAAS_GENERAL error code.
     *
     * @param jsonObject The JSON object to test the view with.
     * @throws DaaSException If the results array does not have a length greater than 0 or if there is a JSONException during the operation.
     */
    @Override
    public void viewTest(JSONObject jsonObject) throws DaaSException {
        LOGGER.debug("In viewTest...");

        JSONObject resObj = executeJSONChunkRequest(jsonObject, null, 1);
        try
        {
            JSONArray results = resObj.getJSONArray("Results");
            if(results.length() > 0)
            {
                LOGGER.debug("View Test successful");
            }
            else
            {
                throw new DaaSException("View Test failed", CommonImpl.DAAS_ERROR_CONNECTION_FAILURE);
            }
        }
        catch (JSONException je)
        {
            throw new DaaSException(je, CommonImpl.TYPE_DAAS_GENERAL);
        }

    }


    /**
     * Implementation of IDataSourceService.executeJSONRequest non-chunked data collection API NOT USED by Identity
     * Governance product
     *
     * @param type - Type of request. For collection, value is "find"
     * @param jsonRequest - Find command parameters
     * @return JSONObject containing query status, size, and results
     * @throws DaaSException thrown if errors occur
     */
    @Override
    public JSONObject executeJSONRequest(String type, JSONObject jsonRequest) throws DaaSException {



            throw new DaaSException("executeJSONRequest not implemented", CommonImpl.TYPE_INVALID_REQUEST);

    }

    /**
     * Implementation of IDataSourceService.executeJSONChunkRequest Chunked data collection API
     *
     * @param jsonRequest - Find command parameters
     * @param token - Chunked request continuation token
     * @param chunkSize - Initial request chunk size
     * @return JSONObject containing query status, size, and results
     * @throws DaaSException thrown if errors occur
     */
    @Override
    public JSONObject executeJSONChunkRequest(JSONObject jsonRequest, String token, int chunkSize) throws DaaSException {
        String newToken = token;

        try
        {
            LOGGER.debug(jsonRequest.toString(2));

        }catch (JSONException je)
        {
            throw new DaaSException(je, CommonImpl.TYPE_DAAS_GENERAL);
        }

        try
        {
            LOGGER.trace("In executeJSONChunkRequest...");
            if (newToken == null)
            {
                newToken = UUID.generate().toString();

                // Get the view parameters for the query

                LOGGER.debug("searchClass: "+ serviceParams.getSearchClass());

                // Initialize a new collector for chunk request
                m_collector = new Collector(serviceParams);
                m_collector.setCollectionPageSize(chunkSize);
                // Set (or override) any default credentials set in service-parms
                // with collection-specific values if present.
                // NOTE: This is the standard method for passing back-end application
                // credentials from the Identity Governance product
                if (jsonRequest.has(CommonImpl.DAAS_AUTH_ATTR))
                {
                    LOGGER.debug("Setting credentials1");
                    String authInfo = jsonRequest.getString(CommonImpl.DAAS_AUTH_ATTR);
                    System.out.println("authInfo: "+authInfo);
                    setConnectorCredentials(authInfo);
                }
                g_collections.put(newToken, m_collector);

            }
            else
            {
                // Get the existing Collector
                m_collector = g_collections.get(token);

                if (jsonRequest.has(CommonImpl.CANCEL))
                {
                    m_cancel = jsonRequest.optBoolean(CommonImpl.CANCEL);
                }
            }

            // build reply header
            JSONObject resObj = new JSONObject();
            resObj.put(CommonImpl.STATUS, CommonImpl.STATUS_SUCCESS);
            resObj.put(CommonImpl.STATUS_TEXT, CommonImpl.STATUS_SUCCESS);

            // If requests have been canceled, clean up and return
            if (m_cancel)
            {
                m_collector = null;
                g_collections.remove(newToken);
                return resObj;
            }

            JSONArray results = m_collector.getChunkResults(jsonRequest);

            // Build a standard DaaS reply
            int count = results.length();

            resObj.put(CommonImpl.STATUS_TEXT, "RESULT COUNT: "+ Integer.toString(count));
            resObj.put(CommonImpl.SIZE, count);

            if (m_collector.hasMore())
            {
                // cache/re-cache the collector if still needed
                resObj.put(CommonImpl.MORE_TOKEN, newToken);
                g_collections.put(newToken, m_collector);
            }
            else
            {
                // Release resources
                LOGGER.debug("All results obtained.  Release Collector");
                g_collections.remove(newToken);
            }

            resObj.put(CommonImpl.RESULTS, results);
            return resObj;

        }
        catch (JSONException je)
        {
            throw new DaaSException("COMMAND_ERROR: " + je.getLocalizedMessage());
        }
    }

    private void setConnectorCredentials(String authInfo)
    {
        // TODO: Depending on application requirements, an exception may be thrown for
        // null credentials on a request. For other applications, no authInfo may be acceptable
        // (eg: File-input applications)
        if (authInfo == null || authInfo.isEmpty())
        {
            return;
        }

        // Authentication info arrives in user:password format
        String user = authInfo.substring(0, authInfo.indexOf(':'));
        String password = authInfo.substring(authInfo.indexOf(':') + 1);
        m_collector.setCredentials(user, password);
    }

}
