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
import com.novell.ldap.LDAPException;
import com.novell.nds.dirxml.ldap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.LdapContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DirXMLClient {

    static final Logger LOGGER = LoggerFactory.getLogger(DirXMLClient.class.getName());

    public static final int CHUNK_SIZE = 10240;
    private String m_driverDn;
    private int m_ldapReadTimeout;
    private LdapContext lctx;


    public DirXMLClient(LdapContext lctx, String driverDN, int ldapReadTimeout) {
        this.lctx = lctx;
        this.m_driverDn = driverDN;
        this.m_ldapReadTimeout = ldapReadTimeout;
        LOGGER.debug("DirXMLClient created with driverDN: " + driverDN + " and ldapReadTimeout: " + ldapReadTimeout);

    }

    public String getDriverDn() {
        return this.m_driverDn;
    }

    public LdapContext getLdapContext() {
        return this.lctx;
    }

    /**
     * This method is used to submit an XDS command to the driver.
     * It first prints out the driver's distinguished name, the LDAP read timeout, and the length of the XDS command.
     * It then creates a SubmitCommandRequest with the driver's distinguished name, a timeout of 1, and the XDS command.
     * If the driver is not running, it throws a DaaSException.
     * If the driver is running, it sends the SubmitCommandRequest to the driver and retrieves the response.
     * It then retrieves the response data in chunks and returns it.
     *
     * @param xds The XDS command to be submitted.
     * @return The response data from the driver.
     * @throws DaaSException If the driver is not running or if there is an error during the operation.
     */
    public byte[] submitXDSCommand(byte[] xds) throws DaaSException {
        try {

            System.out.println(this.m_driverDn+" : "+this.m_ldapReadTimeout+" : "+xds.length);
            System.out.println(new String(xds));
            //TODO: had to use 1 for timeout to get it to work? figure out why
            SubmitCommandRequest request = new SubmitCommandRequest(this.m_driverDn, 1, xds);
            System.out.println(request.toString());
            if (!this.isDriverRunning()) {
                throw new DaaSException("Driver is not running.");
            } else {
                SubmitCommandResponse response = (SubmitCommandResponse)this.lctx.extendedOperation((ExtendedRequest) request);
                byte[] responseData = this.getChunkedResponse(response.getDataSize(), response.getDataHandle());
                return responseData;
            }
        } catch (NamingException nex) {
            throw new DaaSException(nex);
        } catch (LDAPException lex) {
            throw new DaaSException(lex);
        }
    }

    /**
     * This method checks if the driver is running.
     * It sets up search controls and specifies the attribute to return ("DirXML-State").
     * It then performs a search on the LDAP context using the driver's distinguished name and the search controls.
     * The state of the driver is retrieved from the search results and checked if it equals 2 (indicating the driver is running).
     *
     * @return true if the driver is running, false otherwise.
     * @throws DaaSException if there is an error during the operation.
     */
    public boolean isDriverRunning() throws DaaSException {
        SearchControls sc = new SearchControls();
        sc.setSearchScope(2);
        sc.setReturningAttributes(new String[]{"DirXML-State"});

        try {
            NamingEnumeration<SearchResult> results = this.lctx.search(this.m_driverDn, "(objectClass=*)", sc);
            int state = Integer.parseInt((String)((SearchResult)results.nextElement()).getAttributes().get("DirXML-State").get());
            return state == 2;
        } catch (NamingException nex) {
            LOGGER.error("Error checking if driver is running", nex.getLocalizedMessage());
            throw new DaaSException(nex);
        }
    }

/**
 * This method is used to submit an XDS event to the driver.
 * It first checks if the driver is running, if not, it throws a DaaSException.
 * If the driver is running, it creates a SubmitEventRequest and sends it to the driver.
 * It then retrieves the response data in chunks and returns it.
 * NOT USED IN THIS PROJECT
 *
 * @param driverDn The distinguished name of the driver.
 * @param xds The XDS event to be submitted.
 * @return The response data from the driver.
 * @throws DaaSException If the driver is not running or if there is an error during the operation.
 */
public byte[] submitXDSEvent(String driverDn, byte[] xds) throws DaaSException {
    try {
        System.out.println(this.m_driverDn+" : "+this.m_ldapReadTimeout+" : "+xds.length);
        SubmitEventRequest request = new SubmitEventRequest(this.m_driverDn, this.m_ldapReadTimeout, xds);

        if (!this.isDriverRunning()) {
            throw new DaaSException("Driver is not running.");
        } else {
            SubmitEventResponse response = (SubmitEventResponse)this.lctx.extendedOperation(request);
            byte[] responseData = this.getChunkedResponse(response.getDataSize(), response.getDataHandle());
            return responseData;
        }
    } catch (NamingException nex) {
        throw new DaaSException(nex);
    } catch (LDAPException lex) {
        throw new DaaSException(lex);
    }
}

    /**
     * This method retrieves a response in chunks from the driver.
     * It first creates a ByteArrayOutputStream to store the response data.
     * It then calculates the chunk size (either the size of the response or 10240, whichever is smaller).
     * It then enters a loop where it sends a GetChunkedResultRequest to the driver and retrieves the response data.
     * The response data is written to the ByteArrayOutputStream and the size remaining is updated.
     * Once all the data has been retrieved, it sends a CloseChunkedResultRequest to the driver.
     * The ByteArrayOutputStream is then converted to a byte array and returned.
     *
     * @param size The size of the response data.
     * @param handle The handle of the response data.
     * @return The response data as a byte array.
     * @throws DaaSException If there is an error during the operation.
     */
    private byte[] getChunkedResponse(int size, int handle) throws DaaSException {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Throwable var4 = null;

            try {
                int sizeRemaining = size;
                int chunkSize = size > 10240 ? 10240 : size;

                byte[] reply;
                while(sizeRemaining > 0) {
                    GetChunkedResultRequest request = new GetChunkedResultRequest(handle, chunkSize, 0);
                    GetChunkedResultResponse response = (GetChunkedResultResponse)this.lctx.extendedOperation(request);
                    reply = response.getData();
                    sizeRemaining -= reply.length;
                    os.write(reply);
                }

                this.lctx.extendedOperation(new CloseChunkedResultRequest(handle));
                reply = os.toByteArray();
                return reply;
            } catch (Throwable var18) {
                var4 = var18;
                throw var18;
            } finally {
                if (os != null) {
                    if (var4 != null) {
                        try {
                            os.close();
                        } catch (Throwable var17) {
                            var4.addSuppressed(var17);
                        }
                    } else {
                        os.close();
                    }
                }

            }
        } catch (LDAPException | IOException | NamingException lex) {
            throw new DaaSException(lex);
        }
    }


    /**
     * This method reads an LDAP attribute from a given LDAP object.
     * It first tries to get the attributes of the LDAP object.
     * If the attributes exist and there is at least one attribute, it gets the specified attribute.
     * If the attribute exists and it contains exactly one value, it returns the value as a string.
     * If the attribute does not exist or it does not contain exactly one value, it throws a DaaSException.
     *
     * @param ldapObjectDn The distinguished name of the LDAP object.
     * @param attrName The name of the attribute to read.
     * @return The value of the attribute as a string.
     * @throws DaaSException If the attribute does not exist, does not contain exactly one value, or if there is an error during the operation.
     */
    public String readLdapAttribute(String ldapObjectDn, String attrName) throws DaaSException {
        Attributes attrs;
        try {
            attrs = this.lctx.getAttributes(ldapObjectDn, new String[]{attrName});
        } catch (NamingException var7) {
            throw new DaaSException(var7);
        }

        if (null != attrs && attrs.size() > 0) {
            Attribute attr = attrs.get(attrName);
            if (null != attr && attr.size() == 1) {
                try {
                    return (String)attr.get(0);
                } catch (NamingException var6) {
                    throw new DaaSException(var6);
                }
            } else {
                throw new DaaSException(attrName + " attribute on " + ldapObjectDn + " Object doesn't contain exactly 1 value.");
            }
        } else {
            throw new DaaSException(attrName + " attribute on " + ldapObjectDn + " Object doesn't exist.");
        }
    }

    public Document readLdapAttributeAsXML(String ldapObjectDn, String attrName) throws DaaSException {
        String xml = this.readLdapAttribute(ldapObjectDn, attrName);

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder builder = docFactory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.getBytes()));
        } catch (ParserConfigurationException pcex) {
            throw new DaaSException(pcex);
        } catch (SAXException saxex) {
            throw new DaaSException(saxex);
        } catch (IOException ioex) {
            throw new DaaSException(ioex);
        }
    }

    public boolean find(String dn) throws DaaSException {
        SearchControls sc = new SearchControls();
        sc.setSearchScope(2);
        sc.setReturningAttributes(new String[0]);

        try {
            NamingEnumeration<SearchResult> entlSearch = this.lctx.search(dn, "(objectclass=*)", sc);
            return entlSearch.hasMore();
        } catch (NamingException var4) {
            if (var4 instanceof NameNotFoundException) {
                return false;
            } else {
                throw new DaaSException(var4);
            }
        }
    }




}
