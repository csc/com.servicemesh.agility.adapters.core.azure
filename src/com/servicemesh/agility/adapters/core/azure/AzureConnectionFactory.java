/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure;

import java.util.List;

import com.servicemesh.agility.adapters.core.azure.impl.AzureConnectionImpl;
import com.servicemesh.agility.api.Cloud;
import com.servicemesh.agility.api.Credential;
import com.servicemesh.agility.api.Link;
import com.servicemesh.agility.api.Property;
import com.servicemesh.agility.api.ServiceProvider;
import com.servicemesh.io.proxy.Proxy;

/**
 * Provides a connection to a Microsoft Azure service
 */
public class AzureConnectionFactory
{
    private AzureConnectionFactory()
    {
    }

    private static class Holder
    {
        private static final AzureConnectionFactory _instance = new AzureConnectionFactory();
    }

    /**
     * Gets a connection factory
     */
    public static AzureConnectionFactory getInstance()
    {
        return Holder._instance;
    }

    /**
     * Gets an Azure connection
     *
     * @param settings
     *            The configuration settings for the connection. Optional - may be empty or null.
     * @param credentials
     *            All available connections - the first credential with a certificate will be utilized. The credential must also
     *            contain a private key for the certificate.
     * @param proxy
     *            The proxy to be utilized. Optional - may be null.
     * @param endpoint
     *            Provides data specific to an Azure service.
     * @see com.servicemesh.agility.adapters.core.azure.Config
     * @return An Azure connection
     */
    public AzureConnection getConnection(List<Property> settings, List<Credential> credentials, Proxy proxy,
            AzureEndpoint endpoint) throws Exception
    {
        return new AzureConnectionImpl(settings, credentials, proxy, endpoint);
    }

    /**
     * Gets an Azure connection
     *
     * @param settings
     *            The configuration settings for the connection. Optional - may be empty or null.
     * @param certificate
     *            Must be a credential that contains a certificate and a private key.
     * @param proxy
     *            The proxy to be utilized. Optional - may be null.
     * @param endpoint
     *            Provides data specific to an Azure service.
     * @see com.servicemesh.agility.adapters.core.azure.Config
     * @return An Azure connection
     */
    public AzureConnection getConnection(List<Property> settings, Credential certificate, Proxy proxy, AzureEndpoint endpoint)
            throws Exception
    {
        return new AzureConnectionImpl(settings, certificate, proxy, endpoint);
    }

    public static String getSubscription(ServiceProvider provider, List<Cloud> clouds)
    {
        // get from service provider asset properties, otherwise fall back to cloud provider
        String subscription = Config.getAssetPropertyAsString(Config.CONFIG_SUBSCRIPTION, provider.getProperties());
        if (subscription == null || subscription.length() == 0) {
            subscription = null;
            Link providerCloud = provider.getCloud();
            if (providerCloud != null) {
                if (clouds != null) {
                    // Find the subscription for the provider's cloud
                    for (Cloud cloud : clouds) {
                        if (cloud.getId() == providerCloud.getId()) {
                            subscription = cloud.getSubscription();
                            break;
                        }
                    }
                }
            }
        }
        return subscription;
    }

    public static Credential getCredentials(ServiceProvider provider, List<Cloud> clouds)
    {
        // get from service provider, otherwise fall back to cloud provider
        Credential cred = null;
        Credential serviceCandidate = provider.getCredentials();
        if (serviceCandidate != null && serviceCandidate.getCertificate() != null && serviceCandidate.getPrivateKey() != null) {
            cred = serviceCandidate;
        }
        // check properties
        if (cred == null) {
            byte[] certificate = Config.getAssetPropertyAsBytes(Config.CONFIG_CERTIFICATE, provider.getProperties());
            String privateKey = Config.getAssetPropertyAsString(Config.CONFIG_PRIVATE_KEY, provider.getProperties());
            if (certificate != null && privateKey != null && privateKey.length() > 0) {
                cred = new Credential();
                cred.setCertificate(certificate);
                cred.setPrivateKey(privateKey);
            }
        }
        // check in cloud provider
        if (cred == null && provider.getCloud() != null && clouds != null) {
            for (Cloud cloud : clouds) {
                if (cloud.getId() == provider.getCloud().getId()) {
                    Credential cloudCandidate = cloud.getCloudCredentials();
                    if (cloudCandidate != null && cloudCandidate.getCertificate() != null
                            && cloudCandidate.getPrivateKey() != null) {
                        cred = cloudCandidate;
                    }
                    break;
                }
            }
        }

        return cred;
    }
}
