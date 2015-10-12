/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */
package com.servicemesh.agility.adapters.core.azure;

import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import org.apache.log4j.Level;

import com.microsoft.schemas.azure.trafficmgr.Error;
import com.microsoft.schemas.azure.trafficmgr.Profile;
import com.microsoft.schemas.azure.trafficmgr.Status;
import com.microsoft.schemas.azure.trafficmgr.StatusDetails;

import com.servicemesh.agility.adapters.core.azure.AzureConnection;
import com.servicemesh.agility.adapters.core.azure.AzureConnectionFactory;
import com.servicemesh.agility.adapters.core.azure.AzureEndpoint;
import com.servicemesh.agility.adapters.core.azure.AzureEndpointFactory;

import com.servicemesh.agility.api.Credential;
import com.servicemesh.agility.api.Property;
import com.servicemesh.core.async.Promise;
import com.servicemesh.io.http.IHttpResponse;

public class TestTrafficMgrIntegration
{
    // Per Microsoft, Traffic Manager version should be set to 2011-10-01 or
    // higher. For now, using same value as Cloud async adapter.
    private static final String MS_VERSION = "2012-08-01";

    private static final String BASE_TRAFFIC_MGR_URI = "services/WATM/profiles";

    private Credential _credential;
    private String _subscription;
    private AzureEndpoint _endpoint;

    @Before
    public void before()
    {
        // Only run these tests if Azure credentials and subscription
        // have been provided
        _credential = TestHelpers.getAzureCredential();
        Assume.assumeTrue(_credential != null);

        _subscription = TestHelpers.getAzureSubscription();
        Assume.assumeTrue((_subscription != null) &&
                          (! _subscription.isEmpty()));

        TestHelpers.initLogger(Level.TRACE);
    }

    @Test
    public void testTrafficMgrIntegration() throws Exception
    {
        String profileName = "core-azure-" + System.currentTimeMillis();
        boolean cleanup = false;
        try {
            _endpoint = getEndpoint();
            Assert.assertNotNull(_endpoint);

            retrieveProfile(profileName, false);
            createProfile(profileName);
            cleanup = true;
            Profile profile = retrieveProfile(profileName, true);
            updateProfile(profile);
            retrieveProfile(profileName, true);
            deleteProfile(profileName);
            cleanup = false;
            retrieveProfile(profileName, false);
        }
        finally {
            if (cleanup) {
                try {
                    cleanupProfile(profileName);
                }
                catch (Exception e) {}
            }
        }
    }

    private AzureEndpoint getEndpoint() throws Exception
    {
        return AzureEndpointFactory.getInstance()
            .getEndpoint(_subscription, MS_VERSION,
                         Error.class.getPackage().getName(), Error.class);
    }

    private AzureConnection getConnection() throws Exception
    {
        List<Property> settings = null; // Just use defaults for now
        return AzureConnectionFactory.getInstance()
            .getConnection(settings, _credential, null, _endpoint);
    }

    private void createProfile(String profileName) throws Exception
    {
        System.out.println("Creating Profile " + profileName);
        String uri = BASE_TRAFFIC_MGR_URI;
        Profile profile = new Profile();
        profile.setDomainName(profileName + ".trafficmanager.net");
        profile.setName(profileName);

        AzureConnection conn = getConnection();
        // Traffic Manager Create Profile has no response body
        // (although Create Definition, not modeled here, does)
        Promise<IHttpResponse> promise =
            conn.post(uri, profile, IHttpResponse.class);
        TestHelpers.completePromise(promise, _endpoint, true);
    }

    private Profile retrieveProfile(String profileName, boolean expectSuccess) throws Exception
    {
        System.out.println("Retrieving Profile " + profileName);
        String uri = BASE_TRAFFIC_MGR_URI + "/" + profileName;
        AzureConnection conn = getConnection();
        Promise<Profile> promise = conn.get(uri, null, Profile.class);
        Profile profile = TestHelpers.completePromise(promise, _endpoint,
                                                      expectSuccess);
        return profile;
    }

    private void updateProfile(Profile profile) throws Exception
    {
        System.out.println("Updating Profile " + profile.getName());
        String uri = BASE_TRAFFIC_MGR_URI + "/" + profile.getName();
        // With status=enabled, the request will return a 200 but then still
        // show as disabled on a subsequently retrieved profile. This is also
        // seen when doing an independent update via cURL. Assuming it may
        // be due to not having any Definition in the Profile. However, for
        // our purposes we just want to demo update/put plumbing so will
        // ignore this unexpected outcome.
        profile.setStatus(Status.ENABLED);

        // API not used but still requires it be set
        StatusDetails details = new StatusDetails();
        details.setEnabledVersion(2);
        profile.setStatusDetails(details);

        AzureConnection conn = getConnection();
        // Update Profile has no response body
        Promise<IHttpResponse> promise =
            conn.put(uri, profile, IHttpResponse.class);
        TestHelpers.completePromise(promise, _endpoint, true);
    }

    private void deleteProfile(String profileName) throws Exception
    {
        System.out.println("Deleting Profile " + profileName);
        String uri = BASE_TRAFFIC_MGR_URI + "/" + profileName;

        AzureConnection conn = getConnection();
        Promise<IHttpResponse> promise = conn.delete(uri);
        TestHelpers.completePromise(promise, _endpoint, true);
    }

    private IHttpResponse cleanupProfile(String profileName) throws Exception
    {
        String uri = BASE_TRAFFIC_MGR_URI + "/" + profileName;
        AzureConnection conn = getConnection();
        Promise<IHttpResponse> promise = conn.delete(uri);
        try {
            return promise.get();
        }
        catch (Throwable t) {
            return null;
        }
    }
}

