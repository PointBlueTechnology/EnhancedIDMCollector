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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Base64;

public class ResultParser {



    /**
     * Parses the result of a query.
     *
     * @param result The result of the query.
     * @param entitlementDn The distinguished name of the entitlement.
     * @return A JSONArray representing the parsed result.
     * @throws DaaSException If an error occurs during parsing.
     */
    public  JSONArray parse(String result, String entitlementDn, String idmAccountID) throws DaaSException {
        try {
            InputSource is = new InputSource(new StringReader(result));
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            QueryResultHandler handler = new QueryResultHandler();
            InputSource inputSource = new InputSource(new StringReader(result));

            handler.setEntitlementDn(entitlementDn);
            handler.setIdmAccountID(idmAccountID);
            saxParser.parse(is, handler);
            return handler.getResultArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaaSException("Error parsing result", e);
        }

    }

    class QueryResultHandler extends DefaultHandler {

        private StringBuilder elementValue;
        JSONArray resultArray = new JSONArray();
        JSONArray attrValues;

        JSONObject instance;

        String association;

        String id;
        String id2;
        String className;
        String idmAccountID;

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
            }
            if(qName.equalsIgnoreCase("value")) {
                elementValue = new StringBuilder();
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
