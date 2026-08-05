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


import com.netiq.daas.api.IDaaSService;
import com.netiq.daas.api.IDataSourceService;
import com.netiq.daas.common.CommonImpl;
import com.netiq.daas.common.DaaSException;
import com.netiq.daas.common.UUID;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class IDMEntitlementCollectionService implements IDaaSService, IDataSourceService {
    static final Logger LOGGER = LoggerFactory.getLogger(IDMEntitlementCollectionService.class.getName());
    private static final Map<String, Session> g_collections = new ConcurrentHashMap<>();
    private boolean m_cancel = false;

   private Collector m_collector = null;

   /** Token of the collection this service instance is currently driving, if any. */
   private String m_token = null;

   private ServiceParams serviceParams = null;

    /**
     * A cached in-flight chunked collection: the {@link Collector} (which owns an
     * open LDAP connection) plus the time it was last touched.
     * <p>
     * Identity Governance abandons a batched collection after {@code chunk-request-ttl}
     * seconds of idle time, but it has no way to tell us it did so. Without an
     * expiry of our own, every collection that times out mid-run strands its
     * Collector in this map forever, holding an eDirectory connection open — so
     * each timeout makes the next collection more likely to time out too.
     */
    private static final class Session
    {
        final Collector collector;
        final long ttlNanos;
        volatile long lastAccessNanos;

        Session(Collector collector, long ttlNanos)
        {
            this.collector = collector;
            this.ttlNanos = ttlNanos;
            touch();
        }

        void touch()
        {
            this.lastAccessNanos = System.nanoTime();
        }

        boolean isExpired(long nowNanos)
        {
            return (nowNanos - lastAccessNanos) > ttlNanos;
        }
    }

    /**
     * This method shuts down the service, releasing this instance's collection
     * and its LDAP connection.
     * <p>
     * It must only ever touch its <em>own</em> session. {@link #g_collections} is
     * static and shared by every service instance in the agent, so releasing the
     * whole map here would close other, still-running collections' LDAP contexts
     * mid-request — surfacing as {@code CommunicationException: Request N cancelled}
     * followed by a NullPointerException on the next page. Sessions abandoned by
     * Identity Governance are reclaimed by {@link #evictExpiredSessions()} instead,
     * which is idle-time guarded and therefore cannot disturb a live collection.
     */
    @Override
    public void shutdown() {
        LOGGER.debug("In shutdown...");
        releaseSession(m_token);
        m_token = null;
        if (m_collector != null)
        {
            closeQuietly(m_collector);
            m_collector = null;
        }
    }

    /**
     * Release a cached collection session and close its LDAP connection.
     * A no-op for a null/unknown token.
     */
    private static void releaseSession(String token)
    {
        if (token == null || token.isEmpty())
        {
            return;
        }
        Session session = g_collections.remove(token);
        if (session != null)
        {
            LOGGER.debug("Releasing collection session " + token);
            closeQuietly(session.collector);
        }
    }

    /**
     * Shut a Collector down without letting a cleanup failure mask the real error.
     * Collector.shutdown() is idempotent, so calling this on an already-closed
     * collector is safe.
     */
    private static void closeQuietly(Collector collector)
    {
        if (collector == null)
        {
            return;
        }
        try
        {
            collector.shutdown();
        }
        catch (Exception e)
        {
            LOGGER.warn("Error releasing collector resources: " + e.getLocalizedMessage());
        }
    }

    /**
     * Drop any cached collection that Identity Governance has clearly abandoned,
     * closing its LDAP connection.
     */
    private static void evictExpiredSessions()
    {
        long now = System.nanoTime();
        for (Map.Entry<String, Session> entry : g_collections.entrySet())
        {
            Session session = entry.getValue();
            // remove(key, value) so we never evict a session another thread has
            // just replaced under the same token.
            if (session.isExpired(now) && g_collections.remove(entry.getKey(), session))
            {
                LOGGER.warn("Collection session " + entry.getKey()
                        + " exceeded its idle timeout and was abandoned; releasing its LDAP connection.");
                closeQuietly(session.collector);
            }
        }
    }

    /**
     * How long we hold an idle collection before reclaiming it. Deliberately
     * later than IG's own {@code chunk-request-ttl} so we can never reclaim a
     * session IG still considers live.
     */
    private long sessionGraceNanos()
    {
        int ttl = (serviceParams != null) ? serviceParams.getCollectionTtlSecs() : 60;
        if (ttl <= 0)
        {
            ttl = 60;
        }
        return TimeUnit.SECONDS.toNanos(Math.max(2L * ttl, ttl + 60L));
    }

    /**
     * This method sets the configuration data for the service.
     * It first logs that it is in the setConfigData method.
     * It then creates a new ServiceParams object with the configData parameter.
     * If there is an exception during the operation, it throws a DaaSException with the exception and the TYPE_INVALID_PARAMETER error code.
     *
     * @param traceFileName The name of the trace file.
     * @param traceLevel The level of the trace.
     * @param instanceID The ID of the instance.
     * @param configData The configuration data as a JSONObject.
     * @throws DaaSException If there is an error during the operation.
     */
    @Override
    public void setConfigData(String traceFileName, int traceLevel, String instanceID, JSONObject configData) throws DaaSException {

        try
        {
            LOGGER.info("Setting config data for IDMEntitlementCollectionService version 4.5.0.0");
            serviceParams = new ServiceParams(configData);

        }
        catch (Exception thr)
        {
            throw new DaaSException(thr, CommonImpl.TYPE_INVALID_PARAMETER);
        }
    }

    /**
     * This method tests the service by setting up a Collector and testing the connection.
     * It first logs that it is in the serviceTest method.
     * It then creates a new Collector object with the serviceParams parameter.
     * It sets the credentials for the Collector with the authInfo parameter.
     * It logs that the credentials have been set successfully.
     * It then tests the connection of the Collector.
     * If there is an exception during the operation, it throws a DaaSException.
     *
     * @param s The authentication information for the Collector.
     * @throws DaaSException If there is an error during the operation.
     */
    @Override
    public void serviceTest(String s) throws DaaSException {
        LOGGER.debug("In serviceTest...");
        m_collector = new Collector(serviceParams);
        setConnectorCredentials(s);
        LOGGER.debug("serviceTest set Credentials successful");
        m_collector.testConnection();

    }

    /**
     * This method tests the view by executing a JSON chunk request and checking the results.
     * It first logs that it is in the viewTest method.
     * It then executes a JSON chunk request with the jsonObject parameter, a null token, and a chunk size of 1.
     * It gets the results from the response as a JSONArray.
     * If the results array has a length greater than 0, it logs that the view test was successful.
     * If the results array does not have a length greater than 0, it throws a DaaSException with the DAAS_ERROR_CONNECTION_FAILURE error code.
     * If there is a JSONException during the operation, it throws a DaaSException with the TYPE_DAAS_GENERAL error code.
     *
     * @param jsonObject The JSON object to test the view with.
     * @throws DaaSException If the results array does not have a length greater than 0 or if there is a JSONException during the operation.
     */
    @Override
    public void viewTest(JSONObject jsonObject) throws DaaSException {
        LOGGER.debug("In viewTest...");

        JSONObject resObj = executeJSONChunkRequest(jsonObject, null, 1);
        try
        {
            // A view test only ever reads the first page. If the driver signalled
            // more, nothing will ever come back for the rest of it, so release the
            // session here instead of leaking its connection until the sweep runs.
            releaseSession(resObj.optString(CommonImpl.MORE_TOKEN, null));

            JSONArray results = resObj.getJSONArray("Results");
            if(results.length() > 0)
            {
                LOGGER.debug("View Test successful");
            }
            else
            {
                throw new DaaSException("View Test failed", CommonImpl.DAAS_ERROR_CONNECTION_FAILURE);
            }
        }
        catch (JSONException je)
        {
            throw new DaaSException(je, CommonImpl.TYPE_DAAS_GENERAL);
        }

    }


    /**
     * Implementation of IDataSourceService.executeJSONRequest non-chunked data collection API NOT USED by Identity
     * Governance product
     *
     * @param type - Type of request. For collection, value is "find"
     * @param jsonRequest - Find command parameters
     * @return JSONObject containing query status, size, and results
     * @throws DaaSException thrown if errors occur
     */
    @Override
    public JSONObject executeJSONRequest(String type, JSONObject jsonRequest) throws DaaSException {



            throw new DaaSException("executeJSONRequest not implemented", CommonImpl.TYPE_INVALID_REQUEST);

    }

    /**
     * Implementation of IDataSourceService.executeJSONChunkRequest Chunked data collection API
     *
     * @param jsonRequest - Find command parameters
     * @param token - Chunked request continuation token
     * @param chunkSize - Initial request chunk size
     * @return JSONObject containing query status, size, and results
     * @throws DaaSException thrown if errors occur
     */
    @Override
    public JSONObject executeJSONChunkRequest(JSONObject jsonRequest, String token, int chunkSize) throws DaaSException {
        String newToken = token;

        if (LOGGER.isDebugEnabled())
        {
            try
            {
                // Copy before redacting so we don't mutate Identity Governance's
                // request; the request carries the collection password in the clear.
                JSONObject loggable = new JSONObject(jsonRequest.toString());
                if (loggable.has(CommonImpl.DAAS_AUTH_ATTR))
                {
                    loggable.put(CommonImpl.DAAS_AUTH_ATTR, "****");
                }
                LOGGER.debug(loggable.toString(2));
            }catch (JSONException je)
            {
                throw new DaaSException(je, CommonImpl.TYPE_DAAS_GENERAL);
            }
        }

        // Reclaim anything Identity Governance abandoned on an earlier run before
        // we add another connection of our own.
        evictExpiredSessions();

        Session session;

        try
        {
            LOGGER.trace("In executeJSONChunkRequest...");
            if (newToken == null)
            {
                newToken = UUID.generate().toString();

                // Get the view parameters for the query

                LOGGER.debug("searchClass: "+ serviceParams.getSearchClass());

                // A cancel applies to one collection, not to every collection this
                // service instance handles afterwards.
                m_cancel = false;

                // Initialize a new collector for chunk request
                m_collector = new Collector(serviceParams);
                m_collector.setCollectionPageSize(chunkSize);
                // Set (or override) any default credentials set in service-parms
                // with collection-specific values if present.
                // NOTE: This is the standard method for passing back-end application
                // credentials from the Identity Governance product
                if (jsonRequest.has(CommonImpl.DAAS_AUTH_ATTR))
                {
                    LOGGER.debug("Setting credentials1");
                    setConnectorCredentials(jsonRequest.getString(CommonImpl.DAAS_AUTH_ATTR));
                }
                session = new Session(m_collector, sessionGraceNanos());
                g_collections.put(newToken, session);
                m_token = newToken;

            }
            else
            {
                // Get the existing Collector
                session = g_collections.get(token);

                if (session == null)
                {
                    // The session was reclaimed after IG stopped asking for pages.
                    // Fail loudly rather than throwing an opaque NPE below.
                    throw new DaaSException("Collection session " + token
                            + " is no longer active; it exceeded the configured "
                            + CommonImpl.COLLECTION_TTL + " idle timeout. Raise that value "
                            + "or reduce the collection page size.",
                            CommonImpl.TYPE_INVALID_REQUEST, CommonImpl.STATUS_ERROR);
                }

                session.touch();
                m_collector = session.collector;
                m_token = token;

                if (jsonRequest.has(CommonImpl.CANCEL))
                {
                    m_cancel = jsonRequest.optBoolean(CommonImpl.CANCEL);
                }
            }

            // build reply header
            JSONObject resObj = new JSONObject();
            resObj.put(CommonImpl.STATUS, CommonImpl.STATUS_SUCCESS);
            resObj.put(CommonImpl.STATUS_TEXT, CommonImpl.STATUS_SUCCESS);

            // If requests have been canceled, clean up and return
            if (m_cancel)
            {
                LOGGER.debug("Collection canceled.  Release Collector");
                releaseSession(newToken);
                m_token = null;
                m_collector = null;
                return resObj;
            }

            JSONArray results;
            try
            {
                results = m_collector.getChunkResults(jsonRequest);
            }
            catch (DaaSException | RuntimeException e)
            {
                // A failed page ends the collection; don't strand its connection
                // waiting for the idle sweep to notice.
                releaseSession(newToken);
                m_token = null;
                m_collector = null;
                throw e;
            }

            // Build a standard DaaS reply
            int count = results.length();

            resObj.put(CommonImpl.STATUS_TEXT, "RESULT COUNT: "+ count);
            resObj.put(CommonImpl.SIZE, count);

            if (m_collector.hasMore())
            {
                // keep the collector cached, and reset its idle clock
                resObj.put(CommonImpl.MORE_TOKEN, newToken);
                session.touch();
                g_collections.put(newToken, session);
            }
            else
            {
                // Release resources. The Collector closes its own connection when a
                // paged collection runs out of pages, but a legacy single-shot
                // collection never does, so close it here either way.
                LOGGER.debug("All results obtained.  Release Collector");
                releaseSession(newToken);
                m_token = null;
                m_collector = null;
            }

            resObj.put(CommonImpl.RESULTS, results);
            return resObj;

        }
        catch (JSONException je)
        {
            throw new DaaSException("COMMAND_ERROR: " + je.getLocalizedMessage());
        }
    }

    private void setConnectorCredentials(String authInfo)
    {
        // TODO: Depending on application requirements, an exception may be thrown for
        // null credentials on a request. For other applications, no authInfo may be acceptable
        // (eg: File-input applications)
        if (authInfo == null || authInfo.isEmpty())
        {
            return;
        }

        // Authentication info arrives in user:password format
        String user = authInfo.substring(0, authInfo.indexOf(':'));
        String password = authInfo.substring(authInfo.indexOf(':') + 1);
        m_collector.setCredentials(user, password);
    }

}
