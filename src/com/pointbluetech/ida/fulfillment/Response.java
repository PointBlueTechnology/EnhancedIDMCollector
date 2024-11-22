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

import com.netiq.daas.common.DaaSException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public  class Response {
    private String fulfillmentId;
    private JSONObject response;

    public Response(String changeItemId, String changeRequestType, String objectDN,  String message) throws DaaSException {

        response = new JSONObject();
        try {
            response.put("fulfillmentId", changeItemId);
            JSONArray results = new JSONArray();
            JSONObject result = new JSONObject();
            result.put("fulfillmentId", changeItemId);
            StringBuilder sb = new StringBuilder();
            sb.append("Change Item '" + changeItemId + "' Fulfilled. Type: ");
            sb.append(changeRequestType);
            sb.append(", Target Object: " + objectDN);
            if (message != null)
                sb.append(", " + message);
            result.put("comment", sb.toString());
            results.put(result);
            response.put("Results", results);
        } catch (Exception e) {
            e.printStackTrace();
           throw new DaaSException("Error executing change request: "+e.getMessage());
        }

    }

    public JSONObject getResponse() {
        return response;
    }


}