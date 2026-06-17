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

public class CyberArkTestJig extends BaseTestJig {

    public static void main(String[] args) {
        CyberArkTestJig theOne = new CyberArkTestJig();
        theOne.run();
    }

    @Override
    public void run() {
        try {
            IDMEntitlementCollectionService service = createService();

            JSONObject serviceParams = getJSONDoc("/com/pointbluetech/ida/collector/idm/entitlement/offline/cyberark-serviceParams.json");

            service.setConfigData("traceFileName", 3, "instanceID", serviceParams);
            JSONObject request = getJSONDoc("/com/pointbluetech/ida/collector/idm/entitlement/offline/cyberark-request.json");

            String authInfo = "cn=admin,ou=sa,o=system:am1c@1n53cu435t@g3";
            // request.put(CommonImpl.DAAS_AUTH_ATTR, authInfo);
            // service.serviceTest("cn=admin,ou=sa,o=system:am1c@1n53cu435t@g3");
            // System.out.println(serviceParams.toString());

            executeRequest(service, request, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
