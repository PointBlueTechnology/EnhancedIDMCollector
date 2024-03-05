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

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * This class implements the X509TrustManager interface and is used to provide custom trust management.
 * It does not perform any checks for client or server trust, and accepts all issuers.
 * This is typically used for testing purposes and should not be used in a production environment due to the security risks.
 */
public class DummyTrustManager implements X509TrustManager
{


    /**
     * This method is called to check if the client is trusted.
     * In this implementation, it does not perform any checks and always trusts the client.
     *
     * @param chain the certificate chain of the client
     * @param authType the authentication type used
     * @throws CertificateException if there is an error processing the certificate
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
    {
        return;
    }

    /**
     * This method is called to check if the server is trusted.
     * In this implementation, it does not perform any checks and always trusts the server.
     *
     * @param chain the certificate chain of the server
     * @param authType the authentication type used
     * @throws CertificateException if there is an error processing the certificate
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
    {
        return;
    }

    /**
     * This method returns the list of certificate issuer authorities which are trusted for authentication.
     * In this implementation, it returns an empty array, indicating that all issuers are accepted.
     *
     * @return an array of X509Certificate representing the issuers
     */
    public X509Certificate[] getAcceptedIssuers()
    {
        //throw new RuntimeException("NOT IMPLEMENTED");
        return new X509Certificate[0];
    }
}
