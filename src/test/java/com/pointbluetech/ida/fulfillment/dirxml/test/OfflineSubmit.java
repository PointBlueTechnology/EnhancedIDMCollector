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

package com.pointbluetech.ida.fulfillment.dirxml.test;

import com.pointbluetech.ida.fulfillment.dirxml.DxFulfilmentService;
import org.codehaus.jettison.json.JSONObject;

public class OfflineSubmit {
DxFulfilmentService dxFulfilmentService = new DxFulfilmentService();

    public static void main(String[] args) throws Exception{
        System.out.println("OfflineSubmit");

        OfflineSubmit offlineSubmit = new OfflineSubmit();
        offlineSubmit.init();
        offlineSubmit.submitEvent();
        //offlineSubmit.submitCommand();
    }

    public void submitEvent() throws Exception{
        System.out.println("submit");
        JSONObject response = dxFulfilmentService.executeChangesetRequest(getRequestJSON());
        System.out.println(response.toString(2));

    }

    public void submitCommand() {
        System.out.println("command");
    }


    public void init() throws Exception{
        System.out.println("init");
        dxFulfilmentService.setConfigData("logFile", 1, "instanceID", getConfigJSON());

    }

    private JSONObject getRequestJSON() throws Exception{
        String requestString = "{\n" +
                "  \"changeSetId\": 24,\n" +
                "  \"changeItemId\": 56,\n" +
                "  \"changeRequestType\": \"MODIFY_USER_PROFILE\",\n" +
                "  \"userName\": \"JoeX Smith\",\n" +
                "  \"userUniqueUserId\": \"67c32c51507040309230adfcb870d360\",\n" +
                "  \"reason\": \"Request for profile attribute change(s) on user JoeX Smith requested by  admin. \",\n" +
                "  \"requesterName\": \" admin\",\n" +
                "  \"requesterUniqueUserId\": \"06ac2803a3324d6f9a48a7e8ff677570\",\n" +
                "  \"userProfile\": \"{\"accountProvId\":\"cn=test1,ou=users,o=data\",\"cn\":\"j\",\"sn\":\"\",\"fullName\":\"JoeX \",\"givenName\":\"JoeX\",\"title\":\"\",\"workforceID\":\"12345\",\"targetContainer\":\"ou=Users,o=Test\",\"password\":\"Password123!\"}\",\n" +
                "  \"requesterProfile\": {\n" +
                "    \"userId\": \"9275FCE50D4669428B179275FCE50D46\",\n" +
                "    \"emails\": [],\n" +
                "    \"idmDn\": \"cn=admin,ou=sa,o=system\"\n" +
                "  },\n" +
                "  \"userAttributeModifications\": [\n" +
                "    {\n" +
                "      \"attributeDefinitionId\": 839,\n" +
                "      \"changeType\": 1,\n" +
                "      \"attributeValue\": \"1718350820000\",\n" +
                "      \"attributeName\": \"ext_rAccountExpirationDate\",\n" +
                "      \"attributeDisplayName\": {\n" +
                "        \"en\": \"R Account Expiration Date\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"attributeDefinitionId\": 839,\n" +
                "      \"changeType\": 2,\n" +
                "      \"attributeValue\": \"1719350820000\",\n" +
                "      \"attributeName\": \"ext_rAccountExpirationDate\",\n" +
                "      \"attributeDisplayName\": {\n" +
                "        \"en\": \"R Account Expiration Date\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"serviceName\": \"EEProfileFulfillmentTemplate-24-50-bdad1168a8874d27976fa7a1076ab879\",\n" +
                "  \"viewName\": \"fulfillment-configuration\",\n" +
                "  \"DAAS_AUTH_INFO\": \"cn=admin,ou=sa,o=system:dittibop\"\n" +
                "}";

        String requestString2 ="{\n" +
                "  \"changeSetId\": 24,\n" +
                "  \"changeItemId\": 56,\n" +
                "  \"changeRequestType\": \"MODIFY_USER_PROFILE\",\n" +
                "  \"userName\": \"JoeX Smith\",\n" +
                "  \"userUniqueUserId\": \"67c32c51507040309230adfcb870d360\",\n" +
                "  \"reason\": \"Request for profile attribute change(s) on user JoeX Smith requested by  admin. \",\n" +
                "  \"requesterName\": \" admin\",\n" +
                "  \"requesterUniqueUserId\": \"06ac2803a3324d6f9a48a7e8ff677570\",\n" +
                "  \"userProfile\": {\n" +
                "    \"emails\": [\"test11@pointbluetech.com\"],\n" +
                "    \"firstName\": \"JoeX\",\n" +
                "    \"uniqueUserId\": \"67c32c51507040309230adfcb870d360\",\n" +
                "    \"dn\": \"cn=test1,ou=users,o=data\",\n" +
                "    \"idmDn\": \"cn=test1,ou=users,o=data\",\n" +
                "    \"provisioningId\": \"cn=test1,ou=users,o=data\",\n" +
                "    \"guid\": \"6B7D257A525AAF4386E26B7D257A525A\",\n" +
                "    \"userId\": \"6B7D257A525AAF4386E26B7D257A525A\",\n" +
                "    \"workforceId\": \"12345\",\n" +
                "    \"ext_rAccountExpirationDate\": \"1718054820000\"\n" +
                "  },\n" +
                "  \"requesterProfile\": {\n" +
                "    \"userId\": \"9275FCE50D4669428B179275FCE50D46\",\n" +
                "    \"emails\": [],\n" +
                "    \"idmDn\": \"cn=admin,ou=sa,o=system\"\n" +
                "  },\n" +
                "  \"userAttributeModifications\": [\n" +
                "    {\n" +
                "      \"attributeDefinitionId\": 839,\n" +
                "      \"changeType\": 1,\n" +
                "      \"attributeValue\": \"1719350820000\",\n" +
                "      \"attributeName\": \"ext_rAccountExpirationDate\",\n" +
                "      \"attributeDisplayName\": {\n" +
                "        \"en\": \"R Account Expiration Date\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"attributeDefinitionId\": 839,\n" +
                "      \"changeType\": 2,\n" +
                "      \"attributeValue\": \"1718054820000\",\n" +
                "      \"attributeName\": \"ext_rAccountExpirationDate\",\n" +
                "      \"attributeDisplayName\": {\n" +
                "        \"en\": \"R Account Expiration Date\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"serviceName\": \"EEProfileFulfillmentTemplate-24-50-bdad1168a8874d27976fa7a1076ab879\",\n" +
                "  \"viewName\": \"fulfillment-configuration\",\n" +
                "  \"DAAS_AUTH_INFO\": \"cn=admin,ou=sa,o=system:dittibop\"\n" +
                "}";

       String requestString3 ="{\n" +
                "  \"changeSetId\": 32,\n" +
                "  \"changeItemId\": 64,\n" +
                "  \"changeRequestType\": \"MODIFY_USER_PROFILE\",\n" +
                "  \"userName\": \"JoeX Smith\",\n" +
                "  \"userUniqueUserId\": \"67c32c51507040309230adfcb870d360\",\n" +
                "  \"reason\": \"Request for profile attribute change(s) on user JoeX Smith requested by  admin. \",\n" +
                "  \"requesterName\": \" admin\",\n" +
                "  \"requesterUniqueUserId\": \"06ac2803a3324d6f9a48a7e8ff677570\",\n" +
                "  \"userProfile\": {\n" +
                "    \"emails\": [\"test11@pointbluetech.com\"],\n" +
                "    \"firstName\": \"JoeX\",\n" +
                "    \"uniqueUserId\": \"67c32c51507040309230adfcb870d360\",\n" +
                "    \"dn\": \"cn=test1,ou=users,o=data\",\n" +
                "    \"idmDn\": \"cn=test1,ou=users,o=data\",\n" +
                "    \"provisioningId\": \"cn=test1,ou=users,o=data\",\n" +
                "    \"guid\": \"6B7D257A525AAF4386E26B7D257A525A\",\n" +
                "    \"userId\": \"6B7D257A525AAF4386E26B7D257A525A\",\n" +
                "    \"workforceId\": \"12345\",\n" +
                "    \"ext_rAccountExpirationDate\": \"1718832420000\"\n" +
                "  },\n" +
                "  \"requesterProfile\": {\n" +
                "    \"userId\": \"9275FCE50D4669428B179275FCE50D46\",\n" +
                "    \"emails\": [],\n" +
                "    \"idmDn\": \"cn=admin,ou=sa,o=system\"\n" +
                "  },\n" +
                "  \"userAttributeModifications\": [\n" +
                "    {\n" +
                "      \"attributeDefinitionId\": 839,\n" +
                "      \"changeType\": 1,\n" +
                "      \"attributeValue\": \"1719005220000\",\n" +
                "      \"attributeName\": \"ext_rAccountExpirationDate\",\n" +
                "      \"attributeDisplayName\": {\n" +
                "        \"en\": \"R Account Expiration Date\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"attributeDefinitionId\": 839,\n" +
                "      \"changeType\": 2,\n" +
                "      \"attributeValue\": \"1718832420000\",\n" +
                "      \"attributeName\": \"ext_rAccountExpirationDate\",\n" +
                "      \"attributeDisplayName\": {\n" +
                "        \"en\": \"R Account Expiration Date\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"serviceName\": \"EEProfileFulfillmentTemplate2-30-59-216a377b5d6a421da629d7052a976619\",\n" +
                "  \"viewName\": \"fulfillment-configuration\",\n" +
                "  \"DAAS_AUTH_INFO\": \"cn=admin,ou=sa,o=system:dittibop\"\n" +
                "}";


        return new JSONObject(requestString3);
    }

    private JSONObject getConfigJSON() throws Exception{
    String configString = " {\n" +
            "  \"data-source-name\": \"Enhanced Entitlement Fulfillment\",\n" +
            "  \"service-identifier\": \"EEProfileFulfillmentTemplate-25-54-a363d82eb9f3470d80ac453ad3bc5ea7\",\n" +
            "  \"class\": \"com.pointbluetech.ida.fulfillment.dirxml.DxFulfilmentService\",\n" +
            "  \"allow-connection-test\": true,\n" +
            "  \"version\": \"4.2.2\",\n" +
            "  \"ecma-scripts\": [{\n" +
            "    \"name\": \"PassThrough\",\n" +
            "    \"description\": \"Passes the input value to the output value\",\n" +
            "    \"script\": \"if (inputValue === null || inputValue.length === 0) {\\n\\toutputValue = '';\\n} else {\\n\\noutputValue = JSON.parse(inputValue);\\n\\n}\",\n" +
            "    \"display-name\": \"User Profile script\"\n" +
            "  }],\n" +
            "  \"service-parms\": [\n" +
            "    {\n" +
            "      \"name\": \"use_bridge_connector\",\n" +
            "      \"display-name\": \"Use Cloud Bridge connector?\",\n" +
            "      \"description\": \"Select 'No' if the collector will connect directly to the eDirectory system.  Select 'Yes' if the collector will access eDirectory via the Cloud Bridge.\",\n" +
            "      \"data-type\": \"string\",\n" +
            "      \"default-value\": 0,\n" +
            "      \"value\": 0,\n" +
            "      \"required\": true,\n" +
            "      \"options\": [\n" +
            "        {\n" +
            "          \"display-name\": \"Yes\",\n" +
            "          \"value\": 1\n" +
            "        },\n" +
            "        {\n" +
            "          \"display-name\": \"No\",\n" +
            "          \"value\": 0\n" +
            "        }\n" +
            "      ],\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"data_source_conn_id\",\n" +
            "      \"display-name\": \"Select a Cloud Bridge Data Source Connection\",\n" +
            "      \"description\": \"Choose the Cloud Bridge Data Source Connection that provides connectivity for this collector\",\n" +
            "      \"data-type\": \"string\",\n" +
            "      \"default-value\": 0,\n" +
            "      \"value\": 0,\n" +
            "      \"required\": true,\n" +
            "      \"hidden\": true,\n" +
            "      \"options\": [{\n" +
            "        \"display-name\": \"(Select a Data Source)\",\n" +
            "        \"value\": 0\n" +
            "      }],\n" +
            "      \"conditional_param\": {\n" +
            "        \"use_bridge_connector\": 1\n" +
            "      },\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"server\",\n" +
            "      \"display-name\": \"Host\",\n" +
            "      \"description\": \"IP or DNS address of eDirectory server\",\n" +
            "      \"data-type\": \"string\",\n" +
            "      \"value\": \"172.17.2.81\",\n" +
            "      \"required\": true,\n" +
            "      \"options\": null,\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"port\",\n" +
            "      \"display-name\": \"Port\",\n" +
            "      \"description\": \"LDAP Service Port Number\",\n" +
            "      \"data-type\": \"numeric\",\n" +
            "      \"default-value\": 389,\n" +
            "      \"value\": \"636\",\n" +
            "      \"required\": true,\n" +
            "      \"options\": null,\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"security-certificate\",\n" +
            "      \"display-name\": \"Server Certificate\",\n" +
            "      \"description\": \"Base-64 encoded certificate from target eDirectory Server\",\n" +
            "      \"data-type\": \"string\",\n" +
            "      \"default-value\": \"\",\n" +
            "      \"value\": \"MIII0zCCBrugAwIBAgIUVEPbvtxj8qBXkqGrMgYDoJwGwHQwDQYJKoZIhvcNAQEMBQAwMzEaMBgGA1UECxMRT3JnYW5pemF0aW9uYWwgQ0ExFTATBgNVBAoUDElETV9JRzRfVFJFRTAeFw0yNDAxMjAxODQ5NDBaFw0zNDAxMTcxODQ5NDBaMDsxFTATBgNVBAoUDElETV9JRzRfVFJFRTEiMCAGA1UEAxMZaWRtLWlnNC5wb2ludGJsdWV0ZWNoLm5ldDCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBANUX13fWs0SB1SqI33uY1abBsLVh0GXNG5\\/7Oa4\\/RHb\\/bjAHVYa7\\/Lyq2AK8Onzi98rVyNcDROfPphc5epLtNoYH16W18ky5YeZnPreHpEJ4URX+1yqqW9Itp7JCqSLqVqifV2liROUP8jN6ORWBR2IBiImfzGefRulK3WJcQjKYuZXdxpCdW8peYW15uTMTb094WprR\\/rBDI08JQ7FCrz1GVTlAQZ7Q1SPbZDxfxj3gtb2SlHycExjrsLf9Bgy3g7PExkDtCkl2HY7X\\/pW7UxRYT3TM\\/KHdW53fKNnOqn3xXXY\\/WYmZPdMMiVUJF4hfz85VyesxK723dV25+PkeIxMNp39Ljm1Ji6ysKezJbJ4SSIpJj3Kx80GMUwqf\\/IoYXNgQAsGiP3aMGiL49vzPvQvt1MfVtFBk6yuJklNxpin2eZkRt2aFR2GATwtTj4wvBCskbboJSY2BKG6xFIMKQAzw5GygG7zS4jY4K9FHJXFZDeUY2ZrO2IBSFj7D9xYZxMH4Twvkzk2XATYpFHuTzKBfCEMP6c3BMuwm5EG5ZxweJ8A2XTJl+\\/OCbGpiCojxaTA6MokNHdB\\/XxnQkf8AlxGz+V+Tu1Y7rdt\\/I50YlNgYe3ldtSpROHshmP3f\\/rPJ\\/QByVhp4En8S5HlVq+fn3+xadQA0MTO+WgX4JFTRcVWhAgMBAAGjggPVMIID0TAdBgNVHQ4EFgQUpyMYL9D+BiDFebjsPdci3CBpodswHwYDVR0jBBgwFoAU5T2ehZkMCYVUbiiFtVLbQM7gGcwwKgYDVR0RBCMwIYcErBECUYIZaWRtLWlnNC5wb2ludGJsdWV0ZWNoLm5ldDALBgNVHQ8EBAMCBaAwggHMBgtghkgBhvg3AQkEAQSCAbswggG3BAIBAAEB\\/xMdTm92ZWxsIFNlY3VyaXR5IEF0dHJpYnV0ZSh0bSkWQ2h0dHA6Ly9kZXZlbG9wZXIubm92ZWxsLmNvbS9yZXBvc2l0b3J5L2F0dHJpYnV0ZXMvY2VydGF0dHJzX3YxMC5odG0wggFIoBoBAQAwCDAGAgEBAgFGMAgwBgIBAQIBCgIBaaEaAQEAMAgwBgIBAQIBADAIMAYCAQECAQACAQCiBgIBFwEB\\/6OCAQSgWAIBAgICAP8CAQADDQCAAAAAAAAAAAAAAAADCQCAAAAAAAAAADAYMBACAQACCH\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/AQEAAgQG8N9IMBgwEAIBAAIIf\\/\\/\\/\\/\\/\\/\\/\\/\\/8BAQACBAbw30ihWAIBAgICAP8CAQADDQBAAAAAAAAAAAAAAAADCQBAAAAAAAAAADAYMBACAQACCH\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/AQEAAgQFYsaBMBgwEAIBAAIIf\\/\\/\\/\\/\\/\\/\\/\\/\\/8BAQACBAVixoGiTjBMAgECAgEAAgIA\\/wMNAIAAAAAAAAAAAAAAAAMJAIAAAAAAAAAAMBIwEAIBAAIIf\\/\\/\\/\\/\\/\\/\\/\\/\\/8BAQAwEjAQAgEAAgh\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/wEBADCCAYQGA1UdHwSCAXswggF3MCmgJ6AlhiNodHRwOi8vMTcyLjE3LjIuODE6ODAyOC9jcmwvb25lLmNybDBdoFugWYZXbGRhcDovLzE3Mi4xNy4yLjgxOjM4OS9DTj1PbmUsQ049T25lJTIwLSUyMENvbmZpZ3VyYXRpb24sQ049Q1JMJTIwQ29udGFpbmVyLENOPVNlY3VyaXR5MCqgKKAmhiRodHRwczovLzE3Mi4xNy4yLjgxOjgwMzAvY3JsL29uZS5jcmwwXqBcoFqGWGxkYXBzOi8vMTcyLjE3LjIuODE6NjM2L0NOPU9uZSxDTj1PbmUlMjAtJTIwQ29uZmlndXJhdGlvbixDTj1DUkwlMjBDb250YWluZXIsQ049U2VjdXJpdHkwX6BdoFukWTBXMQwwCgYDVQQDEwNPbmUxHDAaBgNVBAMTE09uZSAtIENvbmZpZ3VyYXRpb24xFjAUBgNVBAMTDUNSTCBDb250YWluZXIxETAPBgNVBAMTCFNlY3VyaXR5MA0GCSqGSIb3DQEBDAUAA4ICAQBBAgAGO135DolZj\\/AJhbS3DvNYq93y\\/GaUOoGA0hPp+8\\/IsdhZsbXLGg6jCe1O2Ulr\\/zVnMTfIUa1nmDhOBAHWqY2oSWfzKhVH3LJKjzmnJuEXTrwUi8PTb+PwfRnAcjKlflY\\/IebiJqHovQ3FLp0ZibLLPHn7X5ASSnG8s4nY+EDBHcijd77FYo0nuZaBcx\\/95qAiDrsPAcvWnO+RDP7paHIuVwVlyxconFNXp0nQYtM\\/uNZRBjklZNl4Xnkr8xYBcG0\\/6vMMQ0RKpyPEzKzibMHZn4Maij5HccKlnC7SY2HpN6NMMK7phEcsxDB7hVRIVnumoCL0pUV66pWgnA1X3mggbnMHszoVM\\/rhjUEswEewwNyf37LIp8qQ5PwQPwxzbYyAz8WY5K84nQTBNaMxKDgBu8Ckcy9Vz0uIKJPdpz3L0yJfCQsGIY20\\/M4oUZONRhMbSMyIMO\\/SSqKjmwehimQv+M2eq63E8u9KM17XxX8S4cuQ\\/pXzCA0Fjd620srxqrAmTq\\/Ex4ue8Dn5h8wkd6X49dm1ViAAAPvx5+kDh83Fs2XOPNoC8RCE7J7waYRO0p+k\\/16inJ3DrMqlm0Xf8aDQnQIcmq+vkPMrrmtKLsPszPVV431Nj+wKczo6SouyFRYP7DKfFyz37NqEndf9JSyilU2UlCtu8psgbQarrA==\",\n" +
            "      \"required\": false,\n" +
            "      \"conn-parms\": \"[\\\"server\\\",\\\"port\\\"]\",\n" +
            "      \"certificate-parm\": true,\n" +
            "      \"options\": null,\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"reciprocal-attrs\",\n" +
            "      \"display-name\": \"Set reciprocal attributes?\",\n" +
            "      \"description\": \"Select 'Yes' to set User and Group security attributes. Select 'No' to only set LDAP membership\",\n" +
            "      \"data-type\": \"string\",\n" +
            "      \"default-value\": 1,\n" +
            "      \"value\": 1,\n" +
            "      \"required\": false,\n" +
            "      \"options\": [\n" +
            "        {\n" +
            "          \"display-name\": \"Yes\",\n" +
            "          \"value\": 1\n" +
            "        },\n" +
            "        {\n" +
            "          \"display-name\": \"No\",\n" +
            "          \"value\": 0\n" +
            "        }\n" +
            "      ],\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"server-type\",\n" +
            "      \"display-name\": \"Server Type\",\n" +
            "      \"description\": \"Type of LDAP Server\",\n" +
            "      \"data-type\": \"string\",\n" +
            "      \"default-value\": \"EDIR\",\n" +
            "      \"value\": \"EDIR\",\n" +
            "      \"required\": true,\n" +
            "      \"hidden\": true,\n" +
            "      \"options\": null,\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"driver_dn\",\n" +
            "      \"display-name\": \"Driver Distinguished Name\",\n" +
            "      \"description\": \"LDAP Distinguished Name of IDM Provisioning Driver\",\n" +
            "      \"data-type\": \"string\",\n" +
            "      \"default-value\": \"cn=IG Update,cn=driverset1,o=system\",\n" +
            "      \"value\": \"cn=IG Update,cn=driverset1,o=system\",\n" +
            "      \"required\": true,\n" +
            "      \"options\": null,\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"treeName\",\n" +
            "      \"display-name\": \"eDirectory Tree Name\",\n" +
            "      \"description\": \"LDAP format tree Name\",\n" +
            "      \"data-type\": \"string\",\n" +
            "      \"default-value\": \"IDM_IG4_TREE\",\n" +
            "      \"value\": \"IDM_IG4_TREE\",\n" +
            "      \"required\": true,\n" +
            "      \"options\": null,\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"chunk-request-ttl\",\n" +
            "      \"display-name\": \"Batch Fulfillment Session Timeout Value\",\n" +
            "      \"description\": \"Number of seconds of idle time before a batched fulfillment session is terminated. The value should not be set to less than 10 seconds\",\n" +
            "      \"data-type\": \"numeric\",\n" +
            "      \"default-value\": 30,\n" +
            "      \"value\": 30,\n" +
            "      \"required\": true,\n" +
            "      \"options\": null,\n" +
            "      \"data_source_connection_parm\": false\n" +
            "    }\n" +
            "  ],\n" +
            "  \"views\": [{\n" +
            "    \"name\": \"fulfillment-configuration\",\n" +
            "    \"display-name\": \"Fulfillment Item configuration and mapping\",\n" +
            "    \"input-transforms\": [],\n" +
            "    \"output-transforms\": [{\n" +
            "      \"app-name\": \"userProfile\",\n" +
            "      \"script-name\": \"schema_map_profile\"\n" +
            "    }],\n" +
            "    \"view-parms\": [],\n" +
            "    \"schema-map-filter\": {\n" +
            "      \"generic-map\": [\n" +
            "        {\n" +
            "          \"view-name\": \"comment\",\n" +
            "          \"app-name\": \"comment\",\n" +
            "          \"required\": true\n" +
            "        },\n" +
            "        {\n" +
            "          \"view-name\": \"fulfillmentId\",\n" +
            "          \"app-name\": \"fulfillmentId\",\n" +
            "          \"required\": true\n" +
            "        }\n" +
            "      ],\n" +
            "      \"fulfillment-parms\": [\n" +
            "        {\n" +
            "          \"display-name\": \"Fulfillment payload\",\n" +
            "          \"description\": \"Fulfillment payload\",\n" +
            "          \"data-type\": \"string\",\n" +
            "          \"app-name\": \"provPayload\",\n" +
            "          \"required\": true,\n" +
            "          \"view-name\": \"[\\\"flowdata\\\",\\\"changeItemId\\\",\\\"changeRequestType\\\",\\\"accountProvId\\\",\\\"permProvId\\\",\\\"permProvAttr\\\",\\\"userProfile\\\"]\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"display-name\": \"Account name generation payload\",\n" +
            "          \"description\": \"User Profile attributes used for creating new account names or matching to existing identities. Account provisioning prohibited if left blank\",\n" +
            "          \"data-type\": \"string\",\n" +
            "          \"app-name\": \"userProfile\",\n" +
            "          \"required\": false,\n" +
            "          \"view-name\": \"userProfile\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }],\n" +
            "  \"supportedChangeItems\": {\n" +
            "    \"REMOVE_PERMISSION_ASSIGNMENT\": true,\n" +
            "    \"ADD_PERMISSION_TO_USER\": true,\n" +
            "    \"REMOVE_ACCOUNT\": true,\n" +
            "    \"ADD_APPLICATION_TO_USER\": true,\n" +
            "    \"REMOVE_ACCOUNT_PERMISSION\": true\n" +
            "  }\n" +
            "}" ;



    JSONObject configJSON = new JSONObject(configString);
    return configJSON;

    }


}
