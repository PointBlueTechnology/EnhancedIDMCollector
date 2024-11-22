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

import com.netiq.daas.common.DaaSException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Base64;
import org.slf4j.Logger;

public class ResultParser {

    static final Logger LOGGER = LoggerFactory.getLogger(ResultParser.class.getName());

    /**
     * Parses the result of a query.
     *
     * @param result The result of the query.
     * @param entitlementDn The distinguished name of the entitlement.
     * @return A JSONArray representing the parsed result.
     * @throws DaaSException If an error occurs during parsing.
     */
    public  JSONArray parse(String result, ServiceParams params) throws DaaSException {
        try {
            LOGGER.debug("Parsing result: " + result);
            InputSource is = new InputSource(new StringReader(result));
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            QueryResultHandler handler = new QueryResultHandler();
            InputSource inputSource = new InputSource(new StringReader(result));

            handler.setEntitlementDn(params.getEntitlementName());
            handler.setIdmAccountID(params.getIdmAccountID());
            handler.setAttributeForAssociation(params.getAttributeForAssociation());
            saxParser.parse(is, handler);
            return handler.getResultArray();
        } catch (Exception e) {
            e.printStackTrace();
           StackTraceElement[] sta = e.getStackTrace();
            for (StackTraceElement st : sta) {
                LOGGER.error(st.toString());
            }
            LOGGER.error("Error parsing result", e.getMessage());
            throw new DaaSException("Error parsing result", e);
        }

    }

    class QueryResultHandler extends DefaultHandler {

        final Logger LOGGER = LoggerFactory.getLogger(QueryResultHandler.class.getName());
        private StringBuilder elementValue;
        JSONArray resultArray = new JSONArray();
        JSONArray attrValues;

        JSONObject instance;

        String association;

        String id;
        String id2;
        String className;
        String idmAccountID;
        String assocRef;
        String valueAssociation;
        String attributeForAssociation;

        /*
        the name of an attribute that is returned with associations or association reference
        values that should be returned as attribute values
         */
        String attrNameForAssocProcessing;

        String attrName;
        private String entitlementDn;
        private String status;

        //StringBuilder assocValue;
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(qName.equalsIgnoreCase("instance")) {
                instance = new JSONObject();
                id2 = attributes.getValue("src-dn");
                className = attributes.getValue("class-name");
            }
            if(qName.equalsIgnoreCase("attr")) {
                attrValues = new JSONArray();
                attrName = attributes.getValue("attr-name");
                assocRef=null;
                valueAssociation=null;
            }
            if(qName.equalsIgnoreCase("value")) {
                elementValue = new StringBuilder();

                if(attrName != null && attrName.equals(attributeForAssociation))
                {
                    assocRef = attributes.getValue("association-ref");
                    valueAssociation = attributes.getValue("association");
                }

            }
            if(qName.equalsIgnoreCase("association")) {
                elementValue = new StringBuilder();
            }

            if(qName.equalsIgnoreCase("status")) {

                status = attributes.getValue("level");
                elementValue = new StringBuilder();

            }

        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(qName.equalsIgnoreCase("instance")) {

                try
                {
                    //instance.put("id", id);
                    instance.put("id2", id2);
                    instance.put("class", className);
                    instance.put("association", association);
                    instance.put("entitlementDn", entitlementDn);
                    instance.put("idmAccountID", idmAccountID);
                } catch (JSONException e)
                {
                    e.printStackTrace();

                }


                resultArray.put(instance);
            }
            //Need to see how IG handles this. It may be that we need to handle multi-valued attributes differently
            if(qName.equalsIgnoreCase("attr")) {
                try {
                    if (attrValues.length() == 1) {
                        instance.put(attrName, attrValues.get(0));
                    } else{
                        instance.put(attrName, attrValues);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(qName.equalsIgnoreCase("value")) {
                attrValues.put(elementValue.toString());
                /*
                This should only occur for things like group membership where the driver adds a reference to the association
                This is used for things like AD group membership so that you have both the DN and the guid of the group member
                The AD driver with the Ent collection package transforms association to association-ref but we handle it either way
                This should make the expensive transformation unnecessary.
                Adding the additional value should do no harm as the only use for such attributes is permission to account matching.
                values that don't match are ignored.
                */
                if(assocRef != null) {
                   attrValues.put(assocRef);
                }
                if(valueAssociation != null) {
                    attrValues.put(valueAssociation);
                }
            }
            if(qName.equalsIgnoreCase("association")) {
                association = elementValue.toString();
            }

            if(qName.equalsIgnoreCase("status")) {
             if(status != null && !status.equalsIgnoreCase("success")){
                 throw new SAXException("Status: "+ status + " Message:  " + elementValue.toString());
                }
            }


        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if (elementValue == null) {
                elementValue = new StringBuilder();
            } else {

                elementValue.append(ch, start, length);

            }
        }

        public JSONArray getResultArray() {
            return resultArray;
        }

        public void setEntitlementDn(String entitlementDn) {
            this.entitlementDn = entitlementDn;
        }

        public void setIdmAccountID(String idmAccountID) {
            this.idmAccountID = idmAccountID;
        }

        public void setAttributeForAssociation(String attributeForAssociation) {
            this.attributeForAssociation = attributeForAssociation;
        }


    }



    public static String guidToString(String guid) {
        if (guid !=null && guid.length() >0)
            return "";
        byte[] decoded = Base64.getDecoder().decode(guid);
        String hex = String.format("%x", new Object[] { new BigInteger(1, decoded) });
        String guidStr = String.format("%32s", new Object[] { hex }).replace(' ', '0');
        return guidStr.toUpperCase();
    }

}
