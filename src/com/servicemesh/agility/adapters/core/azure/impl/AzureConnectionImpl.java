/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import org.apache.log4j.Logger;

import com.servicemesh.agility.adapters.core.azure.AzureConnection;
import com.servicemesh.agility.adapters.core.azure.AzureEndpoint;
import com.servicemesh.agility.adapters.core.azure.Config;
import com.servicemesh.agility.api.Credential;
import com.servicemesh.agility.api.Property;
import com.servicemesh.core.async.Function;
import com.servicemesh.core.async.Promise;
import com.servicemesh.io.http.HttpClientFactory;
import com.servicemesh.io.http.HttpMethod;
import com.servicemesh.io.http.IHttpClient;
import com.servicemesh.io.http.IHttpClientConfigBuilder;
import com.servicemesh.io.http.IHttpHeader;
import com.servicemesh.io.http.IHttpRequest;
import com.servicemesh.io.http.IHttpResponse;
import com.servicemesh.io.http.QueryParams;
import com.servicemesh.io.proxy.Proxy;

public class AzureConnectionImpl implements AzureConnection
{
    private static final Logger _logger = Logger.getLogger(AzureConnectionImpl.class);

    private IHttpClient _httpClient;
    private AzureEndpoint _endpoint;

    public AzureConnectionImpl(List<Property> settings, List<Credential> credentials, Proxy proxy, AzureEndpoint endpoint)
            throws Exception
    {
        byte[] certificate = null;
        String certificatePassword = null;

        for (Credential credential : credentials) {
            certificate = credential.getCertificate();

            // use the first credential that has a cert
            if (certificate != null) {
                certificatePassword = credential.getPrivateKey();
                break;
            }
        }
        init(settings, certificate, certificatePassword, proxy, endpoint);
    }

    public AzureConnectionImpl(List<Property> settings, Credential certificate, Proxy proxy, AzureEndpoint endpoint)
            throws Exception
    {
        init(settings, certificate.getCertificate(), certificate.getPrivateKey(), proxy, endpoint);
    }

    private void init(List<Property> settings, byte[] certificate, String certificatePassword, Proxy proxy, AzureEndpoint endpoint)
            throws Exception
    {
        if (certificate == null) {
            throw new Exception("No certificate");
        }
        if (certificatePassword == null) {
            throw new Exception("No certificate password");
        }
        IHttpClientConfigBuilder cb = HttpClientFactory.getInstance().getConfigBuilder();
        cb.setConnectionTimeout(Config.getHttpTimeout(settings));
        cb.setRetries(Config.getHttpRetries(settings));
        cb.setSocketTimeout(Config.getSocketTimeout(settings));
        cb.setKeyManagers(getKeyManagers(certificate, certificatePassword));
        if (proxy != null) {
            cb.setProxy(proxy);
        }

        _httpClient = HttpClientFactory.getInstance().getClient(cb.build());
        _endpoint = endpoint;
    }

    private KeyManager[] getKeyManagers(byte[] certificate, String certificatePassword) throws Exception
    {
        InputStream cert = new ByteArrayInputStream(certificate);
        char[] secretPassword = certificatePassword.toCharArray();
        KeyStore keyStore = KeyStore.getInstance("pkcs12");

        try {
            keyStore.load(cert, secretPassword);
        }
        catch (Exception ex) {
            String err = "Certificate/Secret Password error: " + ex.getMessage();
            AzureConnectionImpl._logger.error(err);
            throw new Exception(err);
        }

        KeyManagerFactory fac = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        fac.init(keyStore, secretPassword);
        return fac.getKeyManagers();
    }

    @Override
    public AzureEndpoint getEndpoint()
    {
        return _endpoint;
    }

    //-------------------------------------------------------------------------
    // HTTP methods
    //-------------------------------------------------------------------------

    @Override
    public <T> Promise<T> get(String requestURI, QueryParams params, final Class<T> responseClass)
    {
        return execute(HttpMethod.GET, requestURI, params, null, responseClass);
    }

    @Override
    public <T> Promise<T> post(String requestURI, Object resource, final Class<T> responseClass)
    {
        return execute(HttpMethod.POST, requestURI, null, resource, responseClass);
    }

    @Override
    public <T> Promise<T> put(String requestURI, Object resource, final Class<T> responseClass)
    {
        return execute(HttpMethod.PUT, requestURI, null, resource, responseClass);
    }

    @Override
    public Promise<IHttpResponse> delete(String requestURI)
    {
        return execute(HttpMethod.DELETE, requestURI, null, null, IHttpResponse.class);
    }

    @SuppressWarnings("unchecked")
    private <T> Promise<T> execute(HttpMethod method, String requestURI, QueryParams params, Object resource,
            final Class<T> responseClass)
    {
        URI uri = null;
        try {
            uri = getURI(requestURI, params);
            IHttpRequest request = HttpClientFactory.getInstance().createRequest(method, uri);
            addMsVersionHeader(request);
            addAcceptHeader(request);

            if (resource != null) {
                if (resource instanceof java.lang.String) {
                    request.setContent((String)resource);
                }
                else if (resource instanceof byte[]) {
                    request.setContent((byte[])resource);
                }
                else {
                    request.setContent(_endpoint.encode(resource));
                    addContentTypeHeader(request);
                }
            }

            if (AzureConnectionImpl._logger.isDebugEnabled()) {
                AzureConnectionImpl._logger.debug(method.getName() + " " + uri);
            }
            Promise<IHttpResponse> promise = _httpClient.promise(request);

            if (responseClass.getCanonicalName().equals(IHttpResponse.class.getCanonicalName())) {
                return (Promise<T>) promise;
            }
            else {
                return promise.map(new Function<IHttpResponse, T>() {
                    @Override
                    public T invoke(IHttpResponse response)
                    {
                        return _endpoint.decode(response, responseClass);
                    }
                });
            }
        }
        catch (Exception e) {
            String err = "Exception for " + method.getName() + "'" + uri + "': " + e.toString();
            AzureConnectionImpl._logger.error(err, e);
            return Promise.pure(new Exception(err));
        }
    }

    //-------------------------------------------------------------------------
    // Utility methods
    //-------------------------------------------------------------------------

    private URI getURI(String resourceString, QueryParams params) throws Exception
    {
        StringBuilder sb = new StringBuilder(_endpoint.getAddress());
        sb.append(_endpoint.getSubscription());

        if (resourceString != null && !resourceString.isEmpty()) {
            if (resourceString.charAt(0) != '/') {
                sb.append("/");
            }
            sb.append(resourceString);
        }
        if (params != null) {
            sb.append(params.asQueryString());
        }

        return new URI(sb.toString());
    }

    private void addMsVersionHeader(IHttpRequest request)
    {
        addHeader(request, "x-ms-version", _endpoint.getMsVersion());
    }

    private void addContentTypeHeader(IHttpRequest request)
    {
        addHeader(request, "Content-Type", _endpoint.getContentType());
    }

    private void addAcceptHeader(IHttpRequest request)
    {
        addHeader(request, "Accept", _endpoint.getContentType());
    }
    private void addHeader(IHttpRequest request, String name, String value)
    {
        if (value != null) {
            IHttpHeader header = HttpClientFactory.getInstance().createHeader(name, value);
            request.setHeader(header);
        }
    }
}
