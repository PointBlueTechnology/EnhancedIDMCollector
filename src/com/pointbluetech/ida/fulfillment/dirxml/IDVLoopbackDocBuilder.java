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

import com.netiq.daas.common.DaaSException;
import com.pointbluetech.ida.fulfillment.LDAPClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.pointbluetech.ida.fulfillment.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class IDVLoopbackDocBuilder {
    static final Logger LOGGER = LoggerFactory.getLogger(IDVLoopbackDocBuilder.class);

    Config config;
    JSONObject request;

    public IDVLoopbackDocBuilder(Config config, JSONObject request) {
        this.config = config;
        this.request = request;

    }

   public byte[] buildXDS() throws DaaSException{
         final String USER_MOD_CMD = "<nds>\r\n<input>\r\n<modify class-name=\"User\" event-id=\"%s#%s\" dest-dn=\"%s\">\r\n<association>%s</association>\r\n<modify-attr attr-name=\"%s\">\r\n%s\r\n%s\r\n</modify-attr>\r\n</modify>\r\n</input>\r\n</nds>";
         //TODO:
       String xds = null;

       try
       {
           xds = String.format(USER_MOD_CMD, "IG REQUEST", getChangeItemId(), getDestDN(), getAssociation(), getAttributeName(), getRemoveValue(), getAddValue());
              LOGGER.debug("XDS: " + xds);
           return xds.getBytes(StandardCharsets.UTF_8);
       } catch (Exception e)
       {
           e.printStackTrace();
           LOGGER.error("Error parsing request: " + e.getLocalizedMessage());
           throw new DaaSException("Error parsing request: " + e.getLocalizedMessage());
       }
    }

    private String getDestDN() throws JSONException {
        String destDN = "\\"+ config.getTreeName() + "\\";
        LOGGER.debug("Provisioning ID: " + getProvisioningId());
        String[] parts = getProvisioningId().split(",");
        LOGGER.debug("Parts: " + parts.length);
        for(int i=parts.length; i>0; i--)
        {
            String part = parts[i-1];
            String name = part.replaceFirst(".*=", "");

            destDN = destDN + name;
            if(i>1)
                destDN = destDN + "\\";
        }
      return destDN;

//return "\\IDM_IG4_TREE\\data\\users\\test1";
 }

    private String getChangeItemId() throws JSONException {
        return request.getString("changeItemId");
    }

    private String getAssociation() throws JSONException {
        //return "6b7d257a-525a-af43-86e2-6b7d257a525a";
        String capitalizedGUID = request.getJSONObject("userProfile").getString("guid");  //it is also in userID
        String guid = capitalizedGUID.toLowerCase();
        guid = guid.substring(0,8) + "-" + guid.substring(8,12) + "-" + guid.substring(12,16) + "-" + guid.substring(16,20) + "-" + guid.substring(20);
        return guid;
    }
    private String getAttributeName() throws JSONException {
        JSONArray userAttributeModifications = request.getJSONArray("userAttributeModifications");
        String attrName = null;
        for (int i = 0; i < userAttributeModifications.length(); i++) {
            JSONObject userAttributeModification = userAttributeModifications.getJSONObject(i);
           attrName = userAttributeModification.getString("attributeName");
           break;
        }

        if(attrName == null)
            throw new JSONException("Attribute name not found in request");

        return attrName;
    }

    private String getProvisioningId() throws JSONException {
        return request.getJSONObject("userProfile").getString("provisioningId");
    }
    private String getRemoveValue() throws JSONException {
        String removeValue = "";
        JSONArray userAttributeModifications = request.getJSONArray("userAttributeModifications");
        for (int i = 0; i < userAttributeModifications.length(); i++) {
            JSONObject userAttributeModification = userAttributeModifications.getJSONObject(i);
            int changeType = userAttributeModification.getInt("changeType");
            if(changeType == 2)
            {
              removeValue = "<remove-value><value>"+userAttributeModification.getString("attributeValue")+"</value></remove-value>";
                break;
            }
        }
        return removeValue;
    }
    private String getAddValue() throws JSONException {
       String addValue="";
        JSONArray userAttributeModifications = request.getJSONArray("userAttributeModifications");
        for (int i = 0; i < userAttributeModifications.length(); i++) {
            JSONObject userAttributeModification = userAttributeModifications.getJSONObject(i);
            int changeType = userAttributeModification.getInt("changeType");
            if(changeType == 1)
            {
                addValue = "<add-value><value>"+userAttributeModification.getString("attributeValue")+"</value></add-value>";
                break;
            }
        }
        return addValue;
    }


}
