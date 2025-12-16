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

public class AdGroupTestJig extends BaseTestJig {

    public static void main(String[] args) {
        AdGroupTestJig theOne = new AdGroupTestJig();
        theOne.run();
    }

    @Override
    public void run() {

        try {
            IDMEntitlementCollectionService service = createService();

            JSONObject configData = getJSONDoc("/com/pointbluetech/ida/collector/idm/entitlement/offline/adgroup-config.json");
            String authInfo = "cn=admin,ou=sa,o=system:dittibop";

            //TODO: try it without this
            service.setConfigData("traceFileName", 3, "instanceID", configData);
            JSONObject request = getJSONDoc("/com/pointbluetech/ida/collector/idm/entitlement/offline/adgroup-request.json");
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
