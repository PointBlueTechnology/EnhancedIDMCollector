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
import com.novell.nds.dirxml.driver.XmlDocument;
import com.novell.nds.dirxml.driver.xds.*;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {

    static final Logger LOGGER = LoggerFactory.getLogger(Utility.class.getName());



    public static String serializeXMLDocument(Document xmlDocument, boolean indent, boolean writeDecl) {
        String xmlString = null;
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            transfac.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            transfac.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
            transfac.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");
            Transformer trans = transfac.newTransformer();
            if (!writeDecl) {
                trans.setOutputProperty("omit-xml-declaration", "yes");
            } else {
                trans.setOutputProperty("omit-xml-declaration", "no");
            }
            if (indent) {
                trans.setOutputProperty("indent", "yes");
            } else {
                trans.setOutputProperty("indent", "no");
            }
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(xmlDocument);
            trans.transform(source, result);
            xmlString = sw.toString();
        } catch (Exception exception) {

        } finally {
            if (null == xmlString)
                xmlString = "";
        }
        return xmlString;
    }


//
//    public static JSONArray parseResults(byte[] response, ServiceParams serviceParams) throws DaaSException {
//
//        JSONArray resultArray = new JSONArray();
//        String results = new String(response);
//        LOGGER.debug("Results: " + results);
//        XDSQueryResultDocument queryResultDocument;
//
//        Document resultDocument = null;
//
//        //Turn raw response into a DOM Document
//        try{
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
//            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            resultDocument = db.parse(new ByteArrayInputStream(response));
//        } catch (Exception e) {
//            throw new DaaSException("Failed to parse XDS Query Response: \n" + results);
//        }
//
//        Element docElement = resultDocument.getDocumentElement();
//        NodeList instanceList = docElement.getElementsByTagName("instance");
//        if (null != instanceList && instanceList.getLength() > 0)
//            for (int instanceCount = 0; instanceCount < instanceList.getLength(); instanceCount++) {
//                Element instanceElement = (Element)instanceList.item(instanceCount);
//                //dropEmptyNodes
//                NodeList nodes = instanceElement.getElementsByTagName("attr");
//                for (int i = 0; i < nodes.getLength(); i++) {
//                    Node node = nodes.item(i);
//                    if (!node.hasAttributes() || !node.hasChildNodes())
//                        instanceElement.removeChild(node);
//                }
//
//            }
//
//
//        //Convert the DOM Document into an XDSQueryResultDocument
//        try {
//            queryResultDocument = new XDSQueryResultDocument(new XmlDocument(resultDocument));
//        } catch (XDSParseException e) {
//            throw new DaaSException("Failed to parse XDS Query Response: \n" + new String(results));
//        }
//
//        checkForErrors(queryResultDocument);
//
//        List instances = queryResultDocument.extractInstanceElements();
//
//        //Iterate through the instances and build a JSON array
//        try
//        {
//            for(Object instance : instances) {
//                XDSInstanceElement instanceElement = (XDSInstanceElement) instance;
//
//                JSONObject instanceObj = new JSONObject();
//                XDSAssociationElement assoc = instanceElement.extractAssociationElement();
//                String assocText = instanceElement.extractAssociationText();
//                //System.out.println("Association: " + assocText);
//                instanceObj.put("id", assocText);
//                instanceObj.put("association", assocText);
//                String qualSrcDn = instanceElement.getQualifiedSrcDN();
//                //System.out.println("QualifiedSrcDN: " + qualSrcDn);
//                String srcDN = instanceElement.getSrcDN();
//                //System.out.println("SrcDN: " + srcDN);
//                instanceObj.put("id2", srcDN);
//
//                String className = instanceElement.getClassName();
//                //System.out.println("ClassName: " + className);
//                instanceObj.put("className", className);
//                instanceObj.put("entitlementDn", serviceParams.getEntitlementName());
//                List<XDSAttrElement> attrList = instanceElement.extractAttrElements();
//                for(XDSAttrElement attr : attrList) {
//                    JSONArray valueArray = new JSONArray();
//                    String attrName = attr.getAttrName();
//                    //System.out.println("Attribute: " + attrName );
//
//                    List<XDSValueElement> valueList = attr.extractValueElements();
//                    for(XDSValueElement value : valueList) {
//                        String valueText = value.extractText();
//                        //System.out.println("Value: " + valueText);
//                        //This is required to convert the edir guid to a string.
//                        //TODO: change the attribute name to something less likely to conflict
//                        //TODO://is this needed beyond AD? Seems like it could lead to a nasty bug if another driver returned a guid that was already string and not Base64
//                        if(attrName.equals("GUID"))
//                            valueText = guidToString(valueText);
//                        valueArray.put(valueText);
//                    }
//
//                    instanceObj.put(attrName, valueArray);
//                }
//
//                //System.out.println("Instance: " + instanceElement);
//
//                resultArray.put(instanceObj);
//                LOGGER.debug(instanceObj.toString(2));
//            }
//        }catch (JSONException e)
//        {
//            throw new DaaSException("Failed to parse XDS Query Response: " + e.getLocalizedMessage());
//        }
//
//        return resultArray;
//    }

    private static void checkForErrors(XDSQueryResultDocument queryResultDocument) throws DaaSException {
        List<XDSStatusElement> queryStatuses = queryResultDocument.extractStatusElements();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (XDSStatusElement status : queryStatuses) {
            StatusLevel level = status.getLevel();
            String msg = getStatusMessage(status);
            if (level == StatusLevel.ERROR || level == StatusLevel.FATAL)
                errors.add(msg);
            if (level == StatusLevel.WARNING || level == StatusLevel.RETRY)
                warnings.add(msg);
        }
        String allWarnings = warnings.stream().collect(Collectors.joining(", "));
        String allErrors = errors.stream().collect(Collectors.joining(", "));
        if (warnings.size() > 0)
            LOGGER.warn("Warning from query result", allWarnings);
        if (errors.size() > 0)
            throw new DaaSException(allErrors);
    }


    private static String getStatusMessage(XDSStatusElement status) {
        String text = status.extractText();
        if (text != null && text.length() > 0)
            return text;
        List<ElementImpl> childElements = status.childElements();
        text = "";
        for (ElementImpl child : childElements)
            text = text + text;
        if (text != null && text.length() > 0)
            return text;
        //return XmlUtils.elementToString(status.domElement());

        DOMImplementationLS lsImpl = (DOMImplementationLS)(status.domElement()).getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer serializer = lsImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
        String str = serializer.writeToString(status.domElement());
        return str;
    }

    //TODO: this is a hack to convert the AD (or eDir??) guid to a string. This is only needed for AD. It should be removed and done in the collector transform
    //edir GUID is used in OOTB collector for acct-user matching
    public static String guidToString(String guid) {
        if (guid !=null && guid.length() >0)
            return "";
        byte[] decoded = Base64.getDecoder().decode(guid);
        String hex = String.format("%x", new Object[] { new BigInteger(1, decoded) });
        String guidStr = String.format("%32s", new Object[] { hex }).replace(' ', '0');
        return guidStr.toUpperCase();
    }

}
