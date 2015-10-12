/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure;

import javax.xml.bind.JAXBContext;

import com.servicemesh.agility.adapters.core.azure.impl.AzureEndpointImpl;

/**
 * Provides an endpoint for a Microsoft Azure service
 */
public class AzureEndpointFactory
{
    private AzureEndpointFactory()
    {
    }

    private static class Holder
    {
        private static final AzureEndpointFactory _instance = new AzureEndpointFactory();
    }

    /**
     * Gets a connection factory
     */
    public static AzureEndpointFactory getInstance()
    {
        return Holder._instance;
    }

    /**
     * Gets an Azure endpoint
     *
     * @param subscription
     *            The Microsoft Azure subscription
     * @param msVersion
     *            The Microsoft version for a x-ms-version header
     * @param msContextPath
     *            The namespace containing JAXB classes for the Azure API
     * @param msErrorClass
     *            The Microsoft Azure Error class contained in the body of a failed response
     * @return An Azure endpoint
     */
    public <E> AzureEndpoint getEndpoint(String subscription, String msVersion, String msContextPath, Class<E> msErrorClass)
            throws Exception
    {
        return getEndpoint(subscription, msVersion, msContextPath, msErrorClass, AzureEndpoint.DEFAULT_MEDIA_TYPE);
    }

    /**
     * Gets an Azure endpoint
     *
     * @param subscription
     *            The Microsoft Azure subscription
     * @param msVersion
     *            The Microsoft version for a x-ms-version header
     * @param msContextPath
     *            The namespace containing JAXB classes for the Azure API
     * @param msErrorClass
     *            The Microsoft Azure Error class contained in the body of a failed response
     * @param mediaType
     *            The endpoint's HTTP media type
     * @return An Azure endpoint
     */
    public <E> AzureEndpoint getEndpoint(String subscription, String msVersion, String msContextPath, Class<E> msErrorClass, AzureEndpoint.MediaType mediaType)
            throws Exception
    {
        return new AzureEndpointImpl(subscription, msVersion, msContextPath, msErrorClass, mediaType);
    }

    /**
     * Returns the JAXB context for a namespace
     *
     * @param msContextPath
     *            The namespace containing JAXB classes for the Azure API
     */
    public JAXBContext lookupContext(String msContextPath)
    {
        return AzureEndpointImpl.lookupContext(msContextPath);
    }

    /**
     * Unregisters a namespace
     *
     * @param msContextPath
     *            The namespace containing JAXB classes for the Azure API
     */
    public void unregisterContext(String msContextPath)
    {
        AzureEndpointImpl.unregisterContext(msContextPath);
    }
}
