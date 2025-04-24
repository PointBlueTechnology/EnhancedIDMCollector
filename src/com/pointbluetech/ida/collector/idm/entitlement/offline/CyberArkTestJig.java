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

package com.pointbluetech.ida.collector.idm.entitlement.offline;

import com.netiq.daas.common.CommonImpl;
import com.pointbluetech.ida.collector.idm.entitlement.IDMEntitlementCollectionService;
import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CyberArkTestJig {

    private static final String HOST_PARAM = "server";
    private static final String PORT_PARAM = "port";

    private static final String PAGE_SIZE_LIMIT = "page-size-limit";
    public static final String CERTIFICATE_PARAM = "service-cert";
    public static void main(String[] args) {
        CyberArkTestJig theOne = new CyberArkTestJig();
        theOne.run();
    }

    public void run() {

        try {
            IDMEntitlementCollectionService service = new IDMEntitlementCollectionService();

            JSONObject serviceParams = getJSONDoc("/com/pointbluetech/ida/collector/idm/entitlement/offline/cyberark-serviceParams.json");


            service.setConfigData("traceFileName", 3, "instanceID",serviceParams);
            JSONObject request = new JSONObject("{\n" +
                    "\n" +
                    " \n" +
                    "\n" +
                    " \n" +
                    "\n" +
                    "  \"search-class\": \"Account\",\n" +
                    "\n" +
                    "  \"read-attrs\": [\n" +
                    "\n" +
                    "    \"association\",\n" +
                    "\n" +
                    "    \"userType\",\n" +
                    "\n" +
                    "    \"calcName\",\n" +
                    "\n" +
                    "    \"displayName\",\n" +
                    "\n" +
                    "    \"active\",\n" +
                    "\n" +
                    "    \"entryDn\",\n" +
                    "\n" +
                    "    \"liid\",\n" +
                    "\n" +
                    "    \"nativeIdentifier\",\n" +
                    "\n" +
                    "    \"directoryType\"\n" +
                    "\n" +
                    "  ],\n" +
                    "\n" +
                    "  \"DAAS_AUTH_INFO\": \"cn=prodig,ou=sa,o=system:Xu)[dtP082V4x=]'\",\n" +
                    "\n" +
                    "  \"view-name\": \"account\"\n" +
                    "\n" +
                    "}");

            String authInfo = "cn=admin,ou=sa,o=system:am1c@1n53cu435t@g3";
           // request.put(CommonImpl.DAAS_AUTH_ATTR, authInfo);
          //  service.serviceTest("cn=admin,ou=sa,o=system:am1c@1n53cu435t@g3");
       // System.out.println(serviceParams.toString());


            JSONObject result = service.executeJSONChunkRequest(request, null, 100);
            System.out.println(result.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static JSONObject getJSONDoc(String path) throws Exception{
        InputStream istream = TestJig.class.getResourceAsStream(path);

        String initString = getStringFromInputStream(istream);
        //System.out.println(initString);
        return new JSONObject(initString);
    }


    public static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try
        {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }


}
