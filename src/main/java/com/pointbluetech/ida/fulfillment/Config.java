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

public class Config {
    private String host;
    private int port;
    private String driverDN;
    private String securityCertificate;
    private String treeName;


    public Config(JSONObject jsonObject) throws DaaSException {
        try
        {
            JSONArray serviceParms = jsonObject.getJSONArray("service-parms");
            for (int i = 0; i < serviceParms.length(); i++)
            {
                JSONObject parm = serviceParms.getJSONObject(i);
                String name = parm.getString("name");
                String value = parm.getString("value");
                switch (name)
                {
                    case "server":
                        host = value;
                        break;
                    case "port":
                        port = Integer.parseInt(value);
                        break;
                    case "driver_dn":
                        //TODO: get this in the config template
                        driverDN = value;
                        break;
                    case "treeName":
                        treeName = value;
                        break;
                    case "securityCertificate":
                        securityCertificate = value;
                        break;
                    default:

                }
            }
        } catch (Exception e)
        {
            throw new DaaSException("Error parsing config data: " + e.getMessage());
        }


    }

    public String getDriverDN() {
        return driverDN;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSecurityCertificate() {
        return securityCertificate;
    }

    public String getTreeName() {
        return treeName;
    }
}
