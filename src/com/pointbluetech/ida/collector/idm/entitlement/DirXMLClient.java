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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

public class DirXMLClient {

    static final Logger LOGGER = LoggerFactory.getLogger(DirXMLClient.class.getName());

    private static final int DRIVER_STATE_RUNNING = 2;

    private String m_driverDn;
    private int m_ldapReadTimeout;
    private LdapContext lctx;


    public DirXMLClient(LdapContext lctx, String driverDN, int ldapReadTimeout) {
        this.lctx = lctx;
        this.m_driverDn = driverDN;
        this.m_ldapReadTimeout = ldapReadTimeout;
        LOGGER.debug("DirXMLClient created with driverDN: " + driverDN + " and ldapReadTimeout: " + ldapReadTimeout);

        //Uncomment to enable LDAP tracing
        //System.setProperty("com.sun.jndi.ldap.trace.ber", "ldap_trace.txt");
    }

    public String getDriverDn() {
        return this.m_driverDn;
    }

    public LdapContext getLdapContext() {
        return this.lctx;
    }

    /**
     * Submits an XDS command to the driver and retrieves the response.
     * If the driver is not running, it throws a DaaSException.
     *
     * @param xds The XDS command to be submitted.
     * @return The response data from the driver.
     * @throws DaaSException If the driver is not running or if there is an error during the operation.
     */
    public byte[] submitXDSCommand(byte[] xds) throws DaaSException {
        try {
            LOGGER.debug("submitXDSCommand: " + this.m_driverDn + " : " + this.m_ldapReadTimeout + " : " + xds.length);
            //System.out.println(this.m_driverDn+" : "+this.m_ldapReadTimeout+" : "+xds.length);
            //System.out.println(new String(xds));
            LOGGER.debug(new String(xds));
            //TODO: had to use 1 for timeout to get it to work? figure out why

            SubmitCommandRequest request = new SubmitCommandRequest(this.m_driverDn, 1, xds);
            LOGGER.debug(request.toString());
            if (!this.isDriverRunning()) {
                throw new DaaSException("Driver is not running.");
            } else {
                SubmitCommandResponse response = (SubmitCommandResponse)this.lctx.extendedOperation((ExtendedRequest) request);
                byte[] responseData = this.getChunkedResponse(response.getDataSize(), response.getDataHandle());
                return responseData;
            }
        } catch (NamingException nex) {
            LOGGER.error("Error submitting XDS command: " +nex.getMessage());

            throw new DaaSException(nex);
        } catch (LDAPException lex) {
            LOGGER.error("Error submitting XDS command: " + lex.getMessage());
            throw new DaaSException(lex);
        }
    }

    /**
     * Asks the driver whether it supports {@code <query-ex>} chunked queries.
     * <p>
     * This issues the standard {@code __driver_identification_class__} query and
     * looks for a {@code query-ex-supported} attribute with value {@code true} —
     * the same capability the engine reads from a shim's driver identification.
     * Any failure (driver doesn't answer, attribute absent, parse error) is
     * treated as "not supported" so the collector safely falls back to a plain
     * {@code <query>}.
     *
     * @return true only if the driver explicitly advertises query-ex support.
     */
    public boolean driverSupportsQueryEx() {
        try {
            String idQuery = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<nds dtdversion=\"2.0\"><input>"
                    + "<query event-id=\"query-ex-detect\" scope=\"entry\">"
                    + "<search-class class-name=\"__driver_identification_class__\"/>"
                    + "</query></input></nds>";
            byte[] response = submitXDSCommand(idQuery.getBytes(StandardCharsets.UTF_8));
            boolean supported = parseQueryExSupported(new String(response, StandardCharsets.UTF_8));
            LOGGER.debug("Driver " + this.m_driverDn + " query-ex-supported=" + supported);
            return supported;
        } catch (Exception e) {
            LOGGER.warn("Could not determine driver query-ex support; assuming not supported: "
                    + e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Parse a driver-identification response for {@code query-ex-supported=true}.
     * Exposed so it can be unit-tested without an LDAP connection.
     */
    public static boolean parseQueryExSupported(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document doc = docFactory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        NodeList attrs = doc.getElementsByTagName("attr");
        for (int i = 0; i < attrs.getLength(); i++) {
            Element attr = (Element) attrs.item(i);
            if ("query-ex-supported".equals(attr.getAttribute("attr-name"))) {
                NodeList values = attr.getElementsByTagName("value");
                if (values.getLength() > 0) {
                    return "true".equalsIgnoreCase(values.item(0).getTextContent().trim());
                }
            }
        }
        return false;
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
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setReturningAttributes(new String[]{"DirXML-State"});

        NamingEnumeration<SearchResult> results = null;
        try {
            results = this.lctx.search(this.m_driverDn, "(objectClass=*)", sc);
            int state = Integer.parseInt((String)((SearchResult)results.nextElement()).getAttributes().get("DirXML-State").get());
            return state == DRIVER_STATE_RUNNING;
        } catch (NamingException nex) {
            LOGGER.error("Error checking if driver is running", nex);
            throw new DaaSException(nex);
        } catch (NoSuchElementException nse) {
            throw new DaaSException(nse);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (NamingException e) {
                    LOGGER.debug("Error closing NamingEnumeration", e);
                }
            }
        }
    }

    /**
     * This method is used to submit an XDS event to the driver.
     * It first checks if the driver is running, if not, it throws a DaaSException.
     * If the driver is running, it creates a SubmitEventRequest and sends it to the driver.
     * It then retrieves the response data in chunks and returns it.
     *
     * @param xds The XDS event to be submitted.
     * @return The response data from the driver.
     * @throws DaaSException If the driver is not running or if there is an error during the operation.
     */
    public byte[] submitXDSEvent(byte[] xds) throws DaaSException {
        try {
            LOGGER.debug(this.m_driverDn+" : "+this.m_ldapReadTimeout+" : "+xds.length);
            SubmitEventRequest request = new SubmitEventRequest(this.m_driverDn, this.m_ldapReadTimeout, xds);

            if (!this.isDriverRunning()) {
                throw new DaaSException("Driver is not running.");
            } else {
                SubmitEventResponse response = (SubmitEventResponse)this.lctx.extendedOperation(request);
                byte[] responseData = this.getChunkedResponse(response.getDataSize(), response.getDataHandle());
                return responseData;
            }
        } catch (NamingException nex) {
            LOGGER.error("Error submitting XDS event: " + nex.getLocalizedMessage());
            throw new DaaSException(nex);
        } catch (LDAPException lex) {
            LOGGER.error("Error submitting XDS event: " + lex.getLocalizedMessage());
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
            int sizeRemaining = size;
            int chunkSize = Math.min(size, 10240);
            while (sizeRemaining > 0) {
                //Additional timeout was added to overcome the error 80 issue
                GetChunkedResultRequest request = new GetChunkedResultRequest(handle, chunkSize, 10);
                GetChunkedResultResponse response = (GetChunkedResultResponse) this.lctx.extendedOperation(request);
                byte[] reply = response.getData();
                if (reply == null || reply.length == 0) {
                    throw new DaaSException("Received empty chunk with " + sizeRemaining + " bytes remaining; aborting.");
                }
                sizeRemaining -= reply.length;
                os.write(reply);
            }

            this.lctx.extendedOperation(new CloseChunkedResultRequest(handle));
            return os.toByteArray();
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
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new DaaSException(ex);
        }
    }

    public boolean find(String dn) throws DaaSException {
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setReturningAttributes(new String[0]);

        NamingEnumeration<SearchResult> entlSearch = null;
        try {
            entlSearch = this.lctx.search(dn, "(objectclass=*)", sc);
            return entlSearch.hasMore();
        } catch (NamingException namingEx) {
            if (namingEx instanceof NameNotFoundException) {
                return false;
            } else {
                throw new DaaSException(namingEx);
            }
        } finally {
            if (entlSearch != null) {
                try {
                    entlSearch.close();
                } catch (NamingException e) {
                    LOGGER.debug("Error closing NamingEnumeration", e);
                }
            }
        }
    }
}
