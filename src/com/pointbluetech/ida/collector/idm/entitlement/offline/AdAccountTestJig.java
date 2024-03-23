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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class AdAccountTestJig {

    private static final String HOST_PARAM = "server";
    private static final String PORT_PARAM = "port";

    private static final String PAGE_SIZE_LIMIT = "page-size-limit";
    public static final String CERTIFICATE_PARAM = "service-cert";
    public static void main(String[] args) {
        AdAccountTestJig theOne = new AdAccountTestJig();
        theOne.run();
    }

    public void run() {

        try {
            IDMEntitlementCollectionService service = new IDMEntitlementCollectionService();

            JSONObject configData = new JSONObject();
            JSONObject hostParm = new JSONObject();
            hostParm.put("name", HOST_PARAM);
            hostParm.put("data-type", "string");
            hostParm.put("required", true);
            hostParm.put("value", "172.17.2.81");

            String cert = "-----BEGIN CERTIFICATE-----MIIFZzCCBO6gAwIBAgIQAnSH9ILQfLDoQc8KKTV+mzAKBggqhkjOPQQDAzBWMQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMTAwLgYDVQQDEydEaWdpQ2VydCBUTFMgSHlicmlkIEVDQyBTSEEzODQgMjAyMCBDQTEwHhcNMjIwMzE2MDAwMDAwWhcNMjMwMzE2MjM1OTU5WjBoMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZyYW5jaXNjbzEVMBMGA1UEChMMR2l0SHViLCBJbmMuMRUwEwYDVQQDDAwqLmdpdGh1Yi5jb20wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAARIb1gNoZrYHCt+5vNwBAU6Mx3vXDPyh+KkTCfkI3UYCvn93lPgXyQbTtqmy/y3l6afkW5+b+4xbWIle6D/3r9Bo4IDijCCA4YwHwYDVR0jBBgwFoAUCrwIKReMpTlteg7OM8cus+37w3owHQYDVR0OBBYEFPxlb3xHAP8XtGvY1qhzy2K99THoMCMGA1UdEQQcMBqCDCouZ2l0aHViLmNvbYIKZ2l0aHViLmNvbTAOBgNVHQ8BAf8EBAMCB4AwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMIGbBgNVHR8EgZMwgZAwRqBEoEKGQGh0dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydFRMU0h5YnJpZEVDQ1NIQTM4NDIwMjBDQTEtMS5jcmwwRqBEoEKGQGh0dHA6Ly9jcmw0LmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydFRMU0h5YnJpZEVDQ1NIQTM4NDIwMjBDQTEtMS5jcmwwPgYDVR0gBDcwNTAzBgZngQwBAgIwKTAnBggrBgEFBQcCARYbaHR0cDovL3d3dy5kaWdpY2VydC5jb20vQ1BTMIGFBggrBgEFBQcBAQR5MHcwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2ljZXJ0LmNvbTBPBggrBgEFBQcwAoZDaHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0VExTSHlicmlkRUNDU0hBMzg0MjAyMENBMS0xLmNydDAJBgNVHRMEAjAAMIIBfQYKKwYBBAHWeQIEAgSCAW0EggFpAWcAdQCt9776fP8QyIudPZwePhhqtGcpXc+xDCTKhYY069yCigAAAX+SOmyXAAAEAwBGMEQCIACuiIEAOhMFn4ippfskCwgwcUCWfGTc+bwID8xLbueRAiBBLeHu31IreBwZl3ugr6/KfW9IqA8OQ7XgGlBUWErxxAB3ADXPGRu/sWxXvw+tTG1Cy7u2JyAmUeo/4SrvqAPDO9ZMAAABf5I6bJcAAAQDAEgwRgIhAI/nbP4LpHcB1dlxle1FIblULpEZQkkEQLBT6ZOBNeaWAiEA7PsoTFPIJuD+Eo245DSCCfu0cZKmGwX8VZtHRBFPlX8AdQCzc3cH4YRQ+GOG1gWp3BEJSnktsWcMC4fc8AMOeTalmgAAAX+SOmzEAAAEAwBGMEQCIDqisYrdFm0laYJiZqZII6D2N9qmy1E42DvY178dZE92AiBGAE8xPDVm23lfvQRCLO1PO5GxHsZq0tl/gjBS1dYzpDAKBggqhkjOPQQDAwNnADBkAjB4nbrDy4J4BtaUrquwkgQtOboZ4UaO0kDlNbP/AvJ+NuJejyCQ/e3eV3cpGDJTHY8CMDNQXbqwimW1ipqB2Zn9r8IVBqfkAaaBDSeb49ZHOtJqX0m+5wgG/LXCjhGCWJX9PQ==-----END CERTIFICATE-----";
            JSONObject certParm = new JSONObject();
            certParm.put("name", CERTIFICATE_PARAM);
            certParm.put("data-type", "string");
            certParm.put("required", false);
            certParm.put("value", "");

            JSONObject collClassParam = new JSONObject();
            collClassParam.put("name", "search-class");
            collClassParam.put("data-type", "string");
            collClassParam.put("required", true);
            collClassParam.put("value", "Account");

            JSONObject v_searchClassParam = new JSONObject();
            v_searchClassParam.put("name", "view-dxml-search-class");
            v_searchClassParam.put("data-type", "string");
            v_searchClassParam.put("required", true);
            v_searchClassParam.put("value", "User");

            JSONObject searchClassParam = new JSONObject();
            searchClassParam.put("name", "dxml-search-class");
            searchClassParam.put("data-type", "string");
            searchClassParam.put("required", true);
            searchClassParam.put("value", "User");

            JSONObject portParam = new JSONObject();
            portParam.put("name", PORT_PARAM);
            portParam.put("data-type", "string");
            portParam.put("required", true);
            portParam.put("value", "636");

            JSONObject driverNameParam = new JSONObject();
            driverNameParam.put("name", "driver-name");
            driverNameParam.put("data-type", "string");
            driverNameParam.put("required", true);
            driverNameParam.put("value", "cn=Active Directory Driver,cn=driverset1,o=system");

            JSONObject entNameParam = new JSONObject();
            entNameParam.put("name", "entitlement_dn");
            entNameParam.put("data-type", "string");
            entNameParam.put("required", true);
            entNameParam.put("value", "cn=Account,cn=Active Directory Driver,cn=driverset1,o=system");


            JSONObject customQueryParam = new JSONObject();
            customQueryParam.put("name", "custom-query");
            customQueryParam.put("data-type", "string");
            customQueryParam.put("required", false);
            //customQueryParam.put("value", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><nds dtdversion=\"2.0\"><input><query class-name=\"User\" event-id=\"IG:query\" scope=\"subtree\"><search-class class-name=\"User\"/><read-attr attr-name=\"dirxml-uACAccountDisable\"/><read-attr attr-name=\"userPrincipalName\"/><read-attr attr-name=\"sAMAccountName\"/><read-attr attr-name=\"given name\"/><read-attr attr-name=\"surname\"/><operation-data ig-account-collection-query=\"true\" ig-collection-query=\"true\"/></query></input></nds>");
//           customQueryParam.put("value", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><nds dtdversion=\"2.0\"><input>" +
//
//                    "<query class-name=\"User\" event-id=\"IG:query\" scope=\"subtree\">" +
//                   "<search-class class-name=\"User\"/>"+
//                    "<read-attr attr-name=\"dirxml-uACAccountDisable\"/>" +
//                    "<read-attr attr-name=\"userPrincipalName\"/>" +
//                    "<read-attr attr-name=\"sAMAccountName\"/>" +
//                    "<read-attr attr-name=\"given name\"/>" +
//                    "<read-attr attr-name=\"surname\"/>" +
//                    "<read-attr attr-name=\"name\"/>\n" +
//                    "<read-attr attr-name=\"displayName\"/>" +
//                    "<read-attr attr-name=\"userAccountControl\"/>" +
//                    "<read-attr attr-name=\"title\"/>" +
//                    "<read-attr attr-name=\"lastLogon\"/>" +
//                    "<read-attr attr-name=\"accountExpires\"/>" +
//                    "<read-attr attr-name=\"distinguishedName\"/>" +
//                    "<read-attr attr-name=\"objectGUID\"/>" +
//                    "<read-attr attr-name=\"objectSid\"/>" +
//                    "<read-attr attr-name=\"whenCreated\"/>" +
//                    "<operation-data ig-account-collection-query=\"true\" ig-collection-query=\"true\"/></query></input></nds>");
//

customQueryParam.put("value", "");

            String authInfo = "cn=admin,ou=sa,o=system:dittibop";
            configData.put(CommonImpl.DAAS_AUTH_ATTR, authInfo);


            JSONArray serviceParams = new JSONArray();
            serviceParams.put(0, hostParm);
            serviceParams.put(1, certParm);
            serviceParams.put(2, searchClassParam);
            serviceParams.put(3, portParam);
            serviceParams.put(4, driverNameParam);
            serviceParams.put(5, customQueryParam);
            serviceParams.put(6, entNameParam);
            serviceParams.put(7, collClassParam);
            serviceParams.put(8, v_searchClassParam);
            configData.put("service-parms", serviceParams);
            //System.out.println(configData.getString(HOST_PARAM));
            configData.put("organization", "PointblueTechnology");


            //TODO: try it without this
            service.setConfigData("traceFileName", 3, "instanceID",configData);
            JSONObject request = new JSONObject("{\"search-class\":\"Account\",\"view-dxml-search-class\":\"Account\",\"view-custom-query\":\"\",\"read-attrs\":[\"entitlementAction\",\"holderId\",\"entitlementDn\"]}");
            request.put(CommonImpl.DAAS_AUTH_ATTR, authInfo);
            service.serviceTest("cn=admin,ou=sa,o=system:dittibop");
       // System.out.println(serviceParams.toString());


            JSONObject result = service.executeJSONChunkRequest(request, null, 100);
            System.out.println(result.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
