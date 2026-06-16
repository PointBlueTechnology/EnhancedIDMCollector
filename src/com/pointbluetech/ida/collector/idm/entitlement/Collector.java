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

import com.netiq.daas.common.CommonImpl;
import com.netiq.daas.common.DaaSException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

public class Collector {

    static final Logger LOGGER = LoggerFactory.getLogger(Collector.class.getName());

    private ServiceParams m_serviceParams;
    private int m_pageSize = 0;
    private int m_counter = 0;

    private int m_readTimeout;

    public static final String CERTIFICATE_PARAM = "service-cert";

    private String m_user;
    private String m_password;
    private String m_hostName;
    public String m_b64Cert;
    private int m_port;
    private String m_keyStoreName;
    private String searchClass;
    private String m_sessionToken;

    private  LdapContext myLdapCTX;

    private JSONObject jsonRequest;

    // --- query-ex chunked-collection state (only used when paging is enabled) ---
    /** Connection reused across page requests for a single collection. */
    private DirXMLClient udClient;
    /** Continuation token returned by the driver; null when the set is complete. */
    private String m_idmQueryToken;
    /** Whether the driver signalled more pages on the last response. */
    private boolean m_hasMore = false;
    /** Resolve the view's query parameters once, on the first page. */
    private boolean m_started = false;
    private String m_searchClass;
    private String m_customQuery;
    private boolean m_accountCollection;
    /** Driver query-ex capability, detected once after the connection is open. */
    private boolean m_capsResolved = false;
    private boolean m_driverSupportsQueryEx = false;


    public final void setCredentials(String user, String password)
    {
        LOGGER.debug("Setting Credentials");
        System.out.println("user: " + user + " password: " + password);
        m_user = user;
        m_password = password;
    }

    public Collector(ServiceParams params) {
        m_serviceParams = params;
       // this.jsonRequest = jsonRequest;
    }


    public void setCollectionPageSize(int pageSize) throws DaaSException {
        if (pageSize < 0)
        {
            throw new DaaSException("INVALID_PAGE_SIZE: " + pageSize, CommonImpl.TYPE_INVALID_REQUEST, CommonImpl.STATUS_ERROR);

        }

        m_pageSize = pageSize;
    }

    public final int getCollectionPageSize()
    {
        return m_pageSize;
    }

    public ServiceParams getViewParams()
    {
        return m_serviceParams;
    }

    /**
     * This method retrieves chunk results from a JSON request.
     * It first logs the JSON request.
     * It then creates an LdapContext object with the host, user, password, SSL, port, and trustAllCerts parameters.
     * It creates a DirXMLClient object with the LdapContext, driver name, and read timeout parameters.
     * It checks if the search class in the JSON request is "Account". If it is, it generates a query with the search class, custom query, and account collection parameters.
     * If the search class is not "Account", it gets the search class and custom query from the JSON request and generates a query with these parameters and the account collection parameter.
     * It then submits the query to the DirXMLClient and gets the response.
     * It parses the response into a JSONArray and returns it.
     *
     * @param jsonRequest The JSON request to get chunk results from.
     * @return The chunk results as a JSONArray.
     * @throws DaaSException If there is an error during the operation.
     */
    public JSONArray getChunkResults(JSONObject jsonRequest) throws DaaSException {
        try{
        LOGGER.debug("jsonRequest: " + jsonRequest.toString(2));
        }catch (Exception e)
        {
            LOGGER.error("Error getting jsonRequest", e);
        }

        // Resolve the view's query parameters once, on the first page, and keep
        // them for any continuation pages so we don't depend on the continuation
        // request echoing the same view fields back to us.
        if (!m_started)
        {
            if (jsonRequest.optString("search-class").equals("Account"))
            {
                m_accountCollection = true;
                m_searchClass = m_serviceParams.getSearchClass();
                m_customQuery = m_serviceParams.getCustomQuery();
                LOGGER.debug("Account collection; using search class: " + m_searchClass);
            }
            else
            {
                m_accountCollection = false;
                m_searchClass = jsonRequest.optString("view-dxml-search-class");
                m_customQuery = jsonRequest.optString("view-custom-query");
                LOGGER.debug("Not an account collection; using search class: " + m_searchClass);
                m_serviceParams.setAttributeForAssociation(
                        jsonRequest.optString(ServiceParams.ATTRIBUTE_FOR_ASSOCIATION, "Member"));
            }
            m_started = true;
        }

        // Connect once and reuse the context across pages of a single collection.
        if (udClient == null)
        {
            myLdapCTX = getLdapCtx(m_serviceParams.getHost(), m_user, m_password, true,
                    m_serviceParams.getPort(), m_serviceParams.getTrustAllCerts());
            udClient = new DirXMLClient(myLdapCTX, m_serviceParams.getDriverName(),
                    m_serviceParams.getReadTimeout());
        }

        // Decide whether to page. In "auto" mode (the default) we ask the driver
        // once whether it supports query-ex; "on"/"off" force the choice.
        boolean paged = useQueryEx();
        int maxResultCount = paged ? effectivePageSize() : 0;

        byte[] queryXDS = getQuery(m_searchClass, m_customQuery, m_accountCollection,
                maxResultCount, paged ? m_idmQueryToken : null);

        LOGGER.debug("Driver running: " + udClient.isDriverRunning());
        byte[] response = udClient.submitXDSCommand(queryXDS);

        ResultParser parser = new ResultParser();
        JSONArray results = parser.parse(new String(response), m_serviceParams);

        if (paged)
        {
            // A returned <query-token> means more pages remain; its absence means
            // the result set is complete.
            m_idmQueryToken = parser.getQueryToken();
            m_hasMore = (m_idmQueryToken != null);
            LOGGER.debug("query-ex page returned " + results.length() + " results; hasMore=" + m_hasMore);
            if (!m_hasMore)
            {
                closeConnection();
            }
        }

        try{
        LOGGER.debug("Results: " + results.toString(2));
        }catch (Exception e)
        {
            LOGGER.error("Error logging results", e);

        }
        return results;
    }

    /**
     * Decide whether this collection should use {@code <query-ex>} chunking.
     * <ul>
     *   <li>{@code off} - never; always the legacy plain {@code <query>}.</li>
     *   <li>{@code on}  - always (the driver is assumed query-ex capable).</li>
     *   <li>{@code auto} (default) - detect once by asking the driver whether it
     *       advertises {@code query-ex-supported}; cached for the collection.</li>
     * </ul>
     */
    private boolean useQueryEx()
    {
        String mode = m_serviceParams.getQueryExMode();
        if ("off".equalsIgnoreCase(mode))
        {
            return false;
        }
        if ("on".equalsIgnoreCase(mode))
        {
            return true;
        }
        // auto: probe the driver once, then reuse the answer for every page.
        if (!m_capsResolved)
        {
            m_driverSupportsQueryEx = udClient.driverSupportsQueryEx();
            m_capsResolved = true;
            LOGGER.debug("Auto query-ex: driver supports query-ex = " + m_driverSupportsQueryEx);
        }
        return m_driverSupportsQueryEx;
    }

    /**
     * Effective query-ex page size: the chunk size requested by Identity
     * Governance, bounded by the configured {@code page-size-limit}.
     */
    private int effectivePageSize()
    {
        int limit = m_serviceParams.getPageSizeLimit();
        int size = m_pageSize > 0 ? m_pageSize : limit;
        if (limit > 0 && size > limit)
        {
            size = limit;
        }
        return size > 0 ? size : 100;
    }

    private void closeConnection()
    {
        udClient = null;
        if (myLdapCTX != null)
        {
            try
            {
                myLdapCTX.close();
            }
            catch (Exception e)
            {
                LOGGER.error("Error closing LDAP Context", e);
            }
            myLdapCTX = null;
        }
    }

    /**
     * This method tests the connection to the LDAP server and the IDM driver.
     * It first logs that it is testing the connection.
     * It then creates an LdapContext object with the host, user, password, SSL, port, and trustAllCerts parameters.
     * It logs that it has gotten the LdapContext.
     * It then creates a DirXMLClient object with the LdapContext, driver name, and read timeout parameters.
     * It logs that it has gotten the DirXMLClient.
     * If the IDM driver is running, it logs that the driver is running and returns.
     * If the IDM driver is not running, it throws a DaaSException.
     * If there is an exception during the operation, it logs the error and throws a DaaSException.
     *
     * @throws DaaSException If the IDM driver is not running or if there is an error during the operation.
     */
    public void testConnection() throws DaaSException {

        try{
            LOGGER.debug("Testing connection");
            LdapContext myLdapCTX = getLdapCtx(m_serviceParams.getHost(), m_user, m_password, true, m_serviceParams.getPort(), m_serviceParams.getTrustAllCerts());
            LOGGER.debug("Got LDAP Context");

            DirXMLClient udClient = new DirXMLClient(myLdapCTX, m_serviceParams.getDriverName(), m_serviceParams.getReadTimeout());
            LOGGER.debug("Got DirXMLClient");

            if(udClient.isDriverRunning())
            {
                LOGGER.info("Driver is running");
            }
            else
            {
                throw new DaaSException("IDM Driver is not running");
            }

        }catch (Exception e)
        {
            LOGGER.error("Error testing connection", e);
            throw new DaaSException(e.getLocalizedMessage());
        }


    }

    public boolean hasMore() {
        // True only while the driver keeps returning a continuation <query-token>
        // (chunked collection). Legacy single-shot collections leave this false.
        return m_hasMore;
    }



    public void shutdown() {
        closeConnection();
    }

    /**
     * This method generates a query in XML format for a given search class and query text.
     * It first creates a default XML document with placeholders for the search class.
     * If the query is for an account, it sets the ig-account-collection-query attribute to true.
     * If a custom query text is provided, it replaces the default XML document with the custom query text.
     * It then converts the XML document to a byte array and parses it into a Document object.
     * The Document object is then converted into an XDSQueryDocument object.
     * The XDSQueryDocument object is serialized back into a byte array and returned.
     *
     * @param searchClass The search class for the query.
     * @param queryText The custom query text, if any.
     * @param accountCollection A boolean indicating if the query is for an account.
     * @return The query as a byte array.
     * @throws DaaSException If there is an error during the operation.
     */
    public static byte[] getQuery(String searchClass, String queryText, boolean accountCollection,
                                  int maxResultCount, String queryToken) throws DaaSException {

        boolean paged = maxResultCount > 0;
        String tokenDoc;

        if (queryText != null && !queryText.isEmpty())
        {
            // Custom query: forwarded verbatim. To participate in chunking it must
            // itself be authored as <query-ex> with the IG collection markers and a
            // max-result-count; on a continuation page we splice in the token.
            tokenDoc = (paged && queryToken != null) ? injectQueryToken(queryText, queryToken) : queryText;
        }
        else if (paged)
        {
            // Built-in chunked collection query (<query-ex> + max-result-count).
            tokenDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                    + "<nds dtdversion=\"2.0\"><input>"
                    + "<query-ex class-name=\"" + searchClass + "\" event-id=\"IG:query\" scope=\"subtree\""
                    + " max-result-count=\"" + maxResultCount + "\">"
                    + "<search-class class-name=\"" + searchClass + "\"/>"
                    + (queryToken != null ? "<query-token>" + queryToken + "</query-token>" : "")
                    + "<operation-data ig-account-collection-query=\"" + accountCollection + "\""
                    + " ig-collection-query=\"true\"/>"
                    + "</query-ex></input></nds>";
        }
        else
        {
            // Legacy single-shot plain <query> (unchanged behavior).
            tokenDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><nds dtdversion=\"2.0\"><input><query class-name=\""+searchClass+"\" event-id=\"IG:query\" scope=\"subtree\"><search-class class-name=\""+searchClass+"\"/><operation-data ig-account-collection-query=\"false\" ig-collection-query=\"true\"/></query></input></nds>";
            if (accountCollection)
            {
                tokenDoc = tokenDoc.replace("ig-account-collection-query=\"false\"", "ig-account-collection-query=\"true\"");
            }
        }

        LOGGER.debug("Query: " + tokenDoc);
        return tokenDoc.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Splice a {@code <query-token>} into a custom {@code <query-ex>} document for a
     * continuation page. Returns the input unchanged (with a warning) if the custom
     * query is not a {@code <query-ex>}.
     */
    private static String injectQueryToken(String queryXml, String token)
    {
        String tokenEl = "<query-token>" + token + "</query-token>";
        int idx = queryXml.indexOf("</query-ex>");
        if (idx < 0)
        {
            LOGGER.warn("Custom query is not a <query-ex>; cannot resume paging with a query-token");
            return queryXml;
        }
        return queryXml.substring(0, idx) + tokenEl + queryXml.substring(idx);
    }

    /**
     * This method creates and returns an LdapContext object for the given parameters.
     * It first creates a Hashtable and sets the necessary environment variables for the LdapContext.
     * If SSL is enabled, it sets the security protocol to SSL and, if trustAllCerts is true, it sets the socket factory to a custom socket factory.
     * It then sets the initial context factory, provider URL, security authentication, security principal, and security credentials in the environment.
     * It then tries to create an LdapContext with the environment variables.
     * If there is a NamingException, it logs the error and throws a DaaSException.
     *
     * @param ldapHost The host name of the LDAP server.
     * @param loginDN The distinguished name of the user to login with.
     * @param pwd The password of the user to login with.
     * @param ssl A boolean indicating if SSL should be used.
     * @param ldapPort The port number of the LDAP server.
     * @param trustAllCerts A boolean indicating if all certificates should be trusted.
     * @return An LdapContext object for the given parameters.
     * @throws DaaSException If there is an error creating the LdapContext.
     */
    public static LdapContext getLdapCtx(String ldapHost, String loginDN, String pwd,
                                         boolean ssl, int ldapPort, boolean trustAllCerts) throws DaaSException {
        LdapContext ldapCtx;

        LOGGER.debug("Getting LdapCtx: "+ldapHost+" "+loginDN+" "+ldapPort+" "+ssl+" "+trustAllCerts);

        try {
            // Create a Hashtable object.
            Hashtable<String,String> env = new Hashtable<>(5, 0.75f);

            if (ssl) {
                // ldapPort     = LdapCtx.DEFAULT_SSL_PORT;
                env.put(javax.naming.Context.SECURITY_PROTOCOL, "ssl");

                if (trustAllCerts) {
                    env.put("java.naming.ldap.factory.socket",
                            "com.pointbluetech.ida.collector.idm.entitlement.JndiSocketFactory");
                }
            }
            LOGGER.debug("Setting up environment");

            env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(javax.naming.Context.PROVIDER_URL, "ldap://" + ldapHost + ":"
                    + ldapPort);
            env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");
            env.put(javax.naming.Context.SECURITY_PRINCIPAL, loginDN);
            env.put(javax.naming.Context.SECURITY_CREDENTIALS, pwd);
            LOGGER.debug("Getting LdapCtx: "+env.toString());
            // Construct an LdapContext object.
            ldapCtx = new InitialLdapContext(env, null);
        } catch (NamingException e) {
            System.out.println("Error getting LdapCtx:  ");
            e.printStackTrace();
            LOGGER.error("Error getting LdapCtx:  ", e);
            throw new DaaSException("Error getting LdapCtx:  " + e.getLocalizedMessage());
        }
        LOGGER.debug("Got LdapCtx");

        return ldapCtx;
    }

}
