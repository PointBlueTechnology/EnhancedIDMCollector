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
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceParams {


    /**
     * Constant for view parameter 'search-class'
     */

    private static final String HOST_PARAM = "server";
    private static final String PORT_PARAM = "port";

    private static final String PAGE_SIZE_LIMIT = "page-size-limit";
    private static final String SEARCH_CLASS = "dxml-search-class";
    private static final String DRIVER_NAME = "driver-name";
    private static final String ENT_NAME = "entitlement_dn";

    public static final String CERTIFICATE_PARAM = "service-cert";
    public static final String CUSTOM_QUERY = "view-custom-query";


    private int readTimeout =  600;

    private String searchClass;

    private String driverName;

    private String entitlementName;
    private String host;
    private int port;
    private int pageSizeLimit;

    private String customQuery;

    public boolean accountCollection = false;

    static final Logger LOGGER = LoggerFactory.getLogger(ServiceParams.class.getName());


    /**
     * Constructor
     *
     * @param jsonRequest JSON request object
     * @throws DaaSException thrown if errors occur
     */
    public ServiceParams(JSONObject jsonRequest)
            throws DaaSException
    {


        LOGGER.debug("Service Params"+jsonRequest.toString());

        String collectionClass = CommonImpl.validateRequestParamString(jsonRequest, "collectorType");
        if(collectionClass != null && collectionClass.equals("ACCOUNT"))
        {
            accountCollection = true;
        }
        LOGGER.debug("accountCollection: " + accountCollection);

        // MANDATORY parameters
        setSearchClass(CommonImpl.validateServiceParamString(jsonRequest, SEARCH_CLASS));

//        if(searchClass == null || searchClass.isEmpty())
//        {
//            throw new DaaSException("Invalid searchClass", CommonImpl.TYPE_INVALID_PARAMETER, CommonImpl.STATUS_ERROR);
//        }
        LOGGER.debug("searchClass: " + searchClass);

        //TODO: there may be other params for these
        this.setHost(CommonImpl.validateServiceParamString(jsonRequest, HOST_PARAM));
        LOGGER.debug("host: " + host);
        this.setPort(CommonImpl.validateServiceParamInt(jsonRequest, PORT_PARAM).intValue());
        LOGGER.debug("port: " + port);
        this.setEntitlementName(CommonImpl.validateServiceParamString(jsonRequest, ENT_NAME));
        LOGGER.debug("entitlementName: " + entitlementName);

        this.setPageSizeLimit(CommonImpl.getServiceParamInt(jsonRequest, PAGE_SIZE_LIMIT, 0));
        LOGGER.debug("pageSizeLimit: " + pageSizeLimit);
        this.setReadTimeout(CommonImpl.getServiceParamInt(jsonRequest, "read-timeout-secs", 1).intValue());
        LOGGER.debug("readTimeout: " + readTimeout);

        this.customQuery = CommonImpl.getServiceParamString(jsonRequest, CUSTOM_QUERY, null);
        LOGGER.debug("customQuery: " + customQuery);

        setDriverName(CommonImpl.validateServiceParamString(jsonRequest, DRIVER_NAME));
        LOGGER.debug("driverName: " + driverName);


//        if(driverName == null || driverName.isEmpty())
//        {
//            throw new DaaSException("Invalid driverName", CommonImpl.TYPE_INVALID_PARAMETER, CommonImpl.STATUS_ERROR);
//        }

        // OPTIONAL parameters
    }

    private void setEntitlementName(String s) {
        this.entitlementName = s;
    }

    public String getEntitlementName() {
        return entitlementName;
    }

    private void setReadTimeout(int i) {
        this.readTimeout = i;
    }

    private void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public final String getSearchClass()
    {
        return this.searchClass;
    }

    private void setSearchClass(String searchClass) throws DaaSException
    {

        this.searchClass = searchClass;

    }

    public String getDriverName() {
        return driverName;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPageSizeLimit(int pageSizeLimit) {
        this.pageSizeLimit = pageSizeLimit;
    }

    public int getPageSizeLimit() {
        return pageSizeLimit;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getCustomQuery() {
        return customQuery;
    }


}
