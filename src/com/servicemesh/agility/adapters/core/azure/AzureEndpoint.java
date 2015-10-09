/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure;

import javax.xml.bind.JAXBContext;

import com.servicemesh.io.http.IHttpResponse;

/**
 * Provides data and serialization required for operations on a Microsoft Azure service
 */
public interface AzureEndpoint
{
    public static final String DEFAULT_ADDRESS = "https://management.core.windows.net/";

    /** Media types supported by an AzureEndpoint */
    public enum MediaType
    {
        XML("application/xml"),
        JSON("application/json");

        private final String value;

        MediaType(String mediaTypeValue) { this.value = mediaTypeValue; }

        /** Returns the HTTP header value for this media type */
        public String getValue() { return value; }
    }

    public static final MediaType DEFAULT_MEDIA_TYPE = MediaType.XML;

    /** Returns the base address */
    public String getAddress();

    /** Returns the media type value */
    public String getContentType();

    /** Returns the Microsoft Azure subscription */
    public String getSubscription();

    /** Returns the Microsoft version for a x-ms-version header */
    public String getMsVersion();

    /** Returns the default JAXB context */
    public JAXBContext getContext();

    /**
     * Returns the JAXB context for a specified context path. Must use the same class loader as the default context
     */
    public JAXBContext getContext(String contextPath);

    /**
     * Decodes a HTTP response
     *
     * @param response
     *            An HTTP response
     * @param responseClass
     *            The class contained in the body of a succcessful response
     * @return An object of type responseClass for a successful response.
     */
    public <T> T decode(IHttpResponse response, Class<T> responseClass);

    /**
     * Decodes a HTTP response
     *
     * @param response
     *            An HTTP response
     * @param responseClass
     *            The class contained in the body of a succcessful response
     * @param responseContextPath
     *            The context path for the responseClass if the default JAXB context is not to be used. Must use the same class
     *            loader as the default context.
     * @return An object of type responseClass for a successful response.
     */
    public <T> T decode(IHttpResponse response, String responseContextPath, Class<T> responseClass);

    /**
     * Encodes an object
     *
     * @param obj
     *            The object to be encoded
     * @return The object encoded as an XML string
     */
    public String encode(Object obj);

    /**
     * Encodes an object
     *
     * @param objContextPath
     *            The context path for the object if the default JAXB context is not to be used. Must use the same class loader as
     *            the default context.
     * @param obj
     *            The object to be encoded
     * @return The object encoded as an XML string
     */
    public String encode(String objContextPath, Object obj);
}
