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

import com.netiq.daas.common.CommonImpl;
import com.pointbluetech.ida.collector.idm.entitlement.IDMEntitlementCollectionService;
import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Base class for test jigs that can be extended for future test jigs.
 * Provides common functionality for testing IDM entitlement collection.
 */
public abstract class BaseTestJig {

    // Common constants used across all test jigs
    protected static final String HOST_PARAM = "server";
    protected static final String PORT_PARAM = "port";
    protected static final String PAGE_SIZE_LIMIT = "page-size-limit";
    protected static final String CERTIFICATE_PARAM = "service-cert";

    /**
     * The main entry point for the test jig.
     * Creates an instance of the test jig and calls its run method.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // This method should be overridden in subclasses to create the appropriate instance
    }

    /**
     * Runs the test jig. This method should be implemented by subclasses
     * to set up the specific test scenario.
     */
    public abstract void run();

    /**
     * Loads a JSON document from a resource path.
     * 
     * @param path The resource path to the JSON document
     * @return The loaded JSON document as a JSONObject
     * @throws Exception If there is an error loading or parsing the JSON
     */
    public static JSONObject getJSONDoc(String path) throws Exception {
        InputStream istream = BaseTestJig.class.getResourceAsStream(path);
        String initString = getStringFromInputStream(istream);
        return new JSONObject(initString);
    }

    /**
     * Reads an InputStream and converts it to a String.
     * 
     * @param is The InputStream to read
     * @return The contents of the InputStream as a String
     */
    public static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    /**
     * Creates and configures an IDMEntitlementCollectionService.
     * 
     * @return A configured IDMEntitlementCollectionService
     */
    protected IDMEntitlementCollectionService createService() {
        return new IDMEntitlementCollectionService();
    }

    /**
     * Executes a request against the service and prints the result.
     * 
     * @param service The service to execute the request against
     * @param request The request to execute
     * @param chunkSize The chunk size for the request
     * @throws Exception If there is an error executing the request
     */
    protected void executeRequest(IDMEntitlementCollectionService service, JSONObject request, int chunkSize) throws Exception {
        JSONObject result = service.executeJSONChunkRequest(request, null, chunkSize);
        System.out.println(result.toString(2));
    }
}