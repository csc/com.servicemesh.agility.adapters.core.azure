/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.servicemesh.agility.adapters.core.azure.impl.AzureConnectionImpl;
import com.servicemesh.agility.adapters.core.azure.impl.AzureEndpointImpl;
import com.servicemesh.agility.api.AssetProperty;
import com.servicemesh.agility.api.Cloud;
import com.servicemesh.agility.api.Credential;
import com.servicemesh.agility.api.Link;
import com.servicemesh.agility.api.Property;
import com.servicemesh.agility.api.ServiceProvider;
import com.servicemesh.core.async.Promise;
import com.servicemesh.io.http.IHttpResponse;
import com.servicemesh.io.http.QueryParams;
import com.servicemesh.io.proxy.Host;
import com.servicemesh.io.proxy.Proxy;
import com.servicemesh.io.proxy.ProxyType;

// Unable to use PowerMock - causes KeyManagerFactory.getInstance() to
// fail with "class configured for KeyManagerFactory:
// sun.security.ssl.KeyManagerFactoryImpl$SunX509 not a KeyManagerFactory".
// Possible workaround would be to switch to PowerMock/EasyMock -
// see https://code.google.com/p/powermock/wiki/MockSystem.
public class TestAzureConnection
{
    @Before
    public void before()
    {
        TestHelpers.initLogger(Level.TRACE);
    }

    @Test
    public void testConnectionFactorySubscription() throws Exception
    {
        Cloud dummyCloud = new Cloud();
        dummyCloud.setId(35);
        dummyCloud.setSubscription("ghi");
        Cloud cloud = new Cloud();
        cloud.setId(3);
        cloud.setSubscription("abcde");
        ServiceProvider provider = new ServiceProvider();
        Link cloudLink = new Link();
        cloudLink.setId(cloud.getId());
        cloudLink.setType("application/" + Cloud.class.getName() + "+xml");
        List<Cloud> clouds = new ArrayList<Cloud>();
        String subscription = AzureConnectionFactory.getSubscription(provider, clouds);
        Assert.assertNull(subscription);

        provider.setCloud(cloudLink);
        subscription = AzureConnectionFactory.getSubscription(provider, clouds);
        Assert.assertNull(subscription);

        // Find the subscription for the provider's cloud
        clouds.add(dummyCloud);
        clouds.add(cloud);
        subscription = AzureConnectionFactory.getSubscription(provider, clouds);
        Assert.assertNotNull(subscription);
        Assert.assertEquals(cloud.getSubscription(), subscription);

        subscription = AzureConnectionFactory.getSubscription(provider, null);
        Assert.assertNull(subscription);

        // Find the subscription from the provider's asset properties
        AssetProperty property = new AssetProperty();
        property.setName(Config.CONFIG_SUBSCRIPTION);
        property.setStringValue(null);
        provider.getProperties().add(property);
        subscription = AzureConnectionFactory.getSubscription(provider, null);
        Assert.assertNull(subscription);

        property.setStringValue("");
        subscription = AzureConnectionFactory.getSubscription(provider, null);
        Assert.assertNull(subscription);

        property.setStringValue("fghij");
        subscription = AzureConnectionFactory.getSubscription(provider, clouds);
        Assert.assertNotNull(subscription);
        Assert.assertEquals(property.getStringValue(), subscription);
    }

    @Test
    public void testConnectionFactoryCredentials() throws Exception
    {
        ServiceProvider provider = new ServiceProvider();
        Credential cred = AzureConnectionFactory.getCredentials(provider, null);
        Assert.assertNull(cred);

        // Scenarios related to cloud provider
        Credential cloudCred = new Credential();
        List<Cloud> clouds = new ArrayList<Cloud>();
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNull(cred);

        Cloud dummyCloud = new Cloud();
        dummyCloud.setId(541);
        Cloud cloud = new Cloud();
        cloud.setId(4);
        Link cloudLink = new Link();
        cloudLink.setId(cloud.getId());
        cloudLink.setType("application/" + Cloud.class.getName() + "+xml");
        provider.setCloud(cloudLink);
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNull(cred);

        clouds.add(dummyCloud);
        clouds.add(cloud);
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNull(cred);

        cloud.setCloudCredentials(cloudCred);
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNull(cred);

        byte[] cloudCert = "CloudCert".getBytes();
        cloudCred.setCertificate(cloudCert);
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNull(cred);

        cloudCred.setPrivateKey("cloudKey");
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNotNull(cred);
        Assert.assertArrayEquals(cloudCred.getCertificate(), cred.getCertificate());
        Assert.assertEquals(cloudCred.getPrivateKey(), cred.getPrivateKey());

        // Scenarios related to provider asset properties
        AssetProperty certProperty = new AssetProperty();
        certProperty.setName(Config.CONFIG_CERTIFICATE);
        provider.getProperties().add(certProperty);
        Assert.assertNotNull(cred);
        Assert.assertArrayEquals(cloudCred.getCertificate(), cred.getCertificate());
        Assert.assertEquals(cloudCred.getPrivateKey(), cred.getPrivateKey());

        certProperty.setByteValue("AssetPropertyCert".getBytes());
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNotNull(cred);
        Assert.assertArrayEquals(cloudCred.getCertificate(), cred.getCertificate());
        Assert.assertEquals(cloudCred.getPrivateKey(), cred.getPrivateKey());

        AssetProperty keyProperty = new AssetProperty();
        keyProperty.setName(Config.CONFIG_PRIVATE_KEY);
        provider.getProperties().add(keyProperty);
        Assert.assertNotNull(cred);
        Assert.assertArrayEquals(cloudCred.getCertificate(), cred.getCertificate());
        Assert.assertEquals(cloudCred.getPrivateKey(), cred.getPrivateKey());

        keyProperty.setStringValue("");
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNotNull(cred);
        Assert.assertArrayEquals(cloudCred.getCertificate(), cred.getCertificate());
        Assert.assertEquals(cloudCred.getPrivateKey(), cred.getPrivateKey());

        keyProperty.setStringValue("propKey");
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNotNull(cred);
        Assert.assertArrayEquals(certProperty.getByteValue(), cred.getCertificate());
        Assert.assertEquals(keyProperty.getStringValue(), cred.getPrivateKey());
        provider.getProperties().clear();

        // Scenarios related to credentials being directly on provider
        Credential provCred = new Credential();
        provider.setCredentials(provCred);
        cred = AzureConnectionFactory.getCredentials(provider, null);
        Assert.assertNull(cred);

        byte[] provCert = "ProviderCert".getBytes();
        provCred.setCertificate(provCert);
        cred = AzureConnectionFactory.getCredentials(provider, null);
        Assert.assertNull(cred);

        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNotNull(cred);
        Assert.assertArrayEquals(cloudCred.getCertificate(), cred.getCertificate());
        Assert.assertEquals(cloudCred.getPrivateKey(), cred.getPrivateKey());

        provCred.setPrivateKey("provKey");
        cred = AzureConnectionFactory.getCredentials(provider, clouds);
        Assert.assertNotNull(cred);
        Assert.assertArrayEquals(provCred.getCertificate(), cred.getCertificate());
        Assert.assertEquals(provCred.getPrivateKey(), cred.getPrivateKey());
    }

    @Test
    public void testConnectionFactory() throws Exception
    {
        /**
         * Would like to mock HttpClient classes but conflict between PowerMock and KeyManagerFactory. See note above class
         * declaration. HttpClientFactory mockFactory = mock(HttpClientFactory.class);
         * PowerMockito.mockStatic(HttpClientFactory.class); PowerMockito.when(HttpClientFactory.getInstance())
         * .thenReturn(mockFactory); IHttpClientConfigBuilder mockCB = mock(IHttpClientConfigBuilder.class);
         * when(mockFactory.getConfigBuilder()).thenReturn(mockCB); IHttpClient mockClient = mock(IHttpClient.class);
         * when(mockFactory.getClient(any(IHttpClientConfig.class))) .thenReturn(mockClient);
         */
        AzureEndpoint mockEndpoint = Mockito.mock(AzureEndpointImpl.class);
        Mockito.when(mockEndpoint.getAddress()).thenReturn("1.2.3.4");
        AzureConnectionFactory factory = AzureConnectionFactory.getInstance();
        AzureConnection conn;
        List<Property> settings = new ArrayList<Property>();
        Credential cred = new Credential();
        List<Credential> credentials = new ArrayList<Credential>();
        credentials.add(cred);
        Proxy proxy = null;

        try {
            factory.getConnection(settings, credentials, proxy, mockEndpoint);
            Assert.fail("Expected exception for no certificate");
        }
        catch (Exception ex) {
        }

        try {
            factory.getConnection(settings, cred, null, mockEndpoint);
            Assert.fail("Expected exception for no certificate");
        }
        catch (Exception ex) {
        }

        cred.setCertificate(Base64.decodeBase64(TestAzureConnection.TEST_PKCS12));
        try {
            factory.getConnection(settings, credentials, proxy, mockEndpoint);
            Assert.fail("Expected exception for no password");
        }
        catch (Exception ex) {
        }

        try {
            factory.getConnection(settings, cred, proxy, mockEndpoint);
            Assert.fail("Expected exception for no password");
        }
        catch (Exception ex) {
        }

        cred.setPrivateKey(TestAzureConnection.TEST_PKCS12_PASSWORD + "bad");
        try {
            factory.getConnection(settings, cred, proxy, mockEndpoint);
            Assert.fail("Expected exception for wrong password");
        }
        catch (Exception ex) {
        }

        cred.setPrivateKey(TestAzureConnection.TEST_PKCS12_PASSWORD);
        conn = factory.getConnection(settings, credentials, proxy, mockEndpoint);
        Assert.assertNotNull(conn);

        Host host = new Host("bar.com", 153);
        proxy = new Proxy("foo.com", 4223, ProxyType.HTTP_PROXY, host);
        conn = factory.getConnection(settings, cred, proxy, null);
        Assert.assertNotNull(conn);

        // No endpoint so will exercise exception path
        TestHelpers.setLogLevel(AzureConnectionImpl.class.getName(),
                                Level.OFF);
        Promise<IHttpResponse> promise = conn.delete("/deleteIt");
        Assert.assertNotNull(promise);
    }

    @Test
    public void testConnection() throws Exception
    {
        AzureEndpoint mockEndpoint = Mockito.mock(AzureEndpointImpl.class);
        Mockito.when(mockEndpoint.getAddress()).thenReturn(AzureEndpoint.DEFAULT_ADDRESS);
        Mockito.when(mockEndpoint.getContentType()).thenReturn(AzureEndpoint.DEFAULT_MEDIA_TYPE.getValue());
        String subscription = "abcde";
        Mockito.when(mockEndpoint.getSubscription()).thenReturn(subscription);
        String encoded = "encodedObject";
        Mockito.when(mockEndpoint.encode(Matchers.anyObject())).thenReturn(encoded);

        AzureConnectionFactory factory = AzureConnectionFactory.getInstance();
        AzureConnection conn;
        List<Property> settings = new ArrayList<Property>();
        Credential cred = new Credential();
        cred.setCertificate(Base64.decodeBase64(TestAzureConnection.TEST_PKCS12));
        cred.setPrivateKey(TestAzureConnection.TEST_PKCS12_PASSWORD);

        conn = factory.getConnection(settings, cred, null, mockEndpoint);
        Assert.assertNotNull(conn);

        AzureEndpoint endpoint = conn.getEndpoint();
        Assert.assertSame(mockEndpoint, endpoint);

        // Not being able to mock HttpClient classes doesn't let us do
        // much other than to see that there's no exceptions.
        String requestURI = "mypath/here";
        QueryParams params = new QueryParams();
        Promise<IHttpResponse> promise = conn.get(requestURI, params, IHttpResponse.class);
        Assert.assertNotNull(promise);

        Promise<Integer> ipromise = conn.get(requestURI, params, Integer.class);
        Assert.assertNotNull(ipromise);

        // Just use a String resource
        promise = conn.post(requestURI, encoded, IHttpResponse.class);
        Assert.assertNotNull(promise);

        TestHelpers.setLogLevel(AzureConnectionImpl.class.getName(), Level.TRACE);
        Cloud cloud = new Cloud();
        Promise<Cloud> cPromise = conn.put(requestURI, cloud, Cloud.class);
        Assert.assertNotNull(cPromise);

        TestHelpers.setLogLevel(AzureConnectionImpl.class.getName(), Level.INFO);
        byte[] byteResource = "ByteResource".getBytes();
        promise = conn.put(requestURI, byteResource, IHttpResponse.class);

        TestHelpers.setLogLevel(AzureConnectionImpl.class.getName(), Level.TRACE);
        requestURI = null;
        promise = conn.delete(requestURI);
        Assert.assertNotNull(promise);

        String msVersion = "123";
        Mockito.when(mockEndpoint.getMsVersion()).thenReturn(msVersion);

        requestURI = "";
        promise = conn.delete(requestURI);
        Assert.assertNotNull(promise);

        requestURI = "/deleteIt";
        promise = conn.delete(requestURI);
        Assert.assertNotNull(promise);
    }

    private static final String TEST_PKCS12_PASSWORD = "foobar";

    private static final String TEST_PKCS12 = "MIIJYQIBAzCCCScGCSqGSIb3DQEHAaCCCRgEggkUMIIJEDCCA8cGCSqGSIb3DQEHB"
            + "qCCA7gwggO0AgEAMIIDrQYJKoZIhvcNAQcBMBwGCiqGSIb3DQEMAQYwDgQIvg4hIs"
            + "e281cCAggAgIIDgE457/YCGIFdsZW7ykstWYKzu7GDCo4RlQQUVpIiWIRZFC5SbBW"
            + "QJSBOR06ybGPOQnOzhIFmublJgeTbeJnlqSXMl1gtlQEwbLuNUr7NCg/kwom63TqQ"
            + "b8Fig3BfADUM2q3rxlKgqB5t6SrNE1qe3eRkPVAQqrtvgyB068IuhQNFlhMAaVzaV"
            + "h+PwZ0iMkqN281QYUxzHZp6ZCf2F1QLksO+b4zUlpKPu2xTpn+4EjY4Hy/BlCqZNG"
            + "fM07tJ9o3Dltt6YGgWKI4HCdgy/xsAOtY2skhjJ1PdTF1wp6stsipUYC9INWjXhDV"
            + "I5kSeCphUxfN+Q3CJN6G6N3lqJCeu0z+X52zEUvCXYJ2HAUQU19AsE1fsYWft2Ikr"
            + "Vx48myblAwztqK+WgOIE/t2IdFYLhu2DuVfW8Qh/BPGUl6VqDE6cbuqVcgVHRF658"
            + "koqfanTj9VHWx1pnMi2GJYCprhmblBQ7sf4XgZkB4Ab6McxtZuljycGny/fCzTu1V"
            + "WSiJAYuNNqlXmk7qAfLHga6QjWz9tebb03madGcGQGlSBd4Q+XIR6vv2q0N+80WpL"
            + "QQv74/CuF18+L8l2shIsf7QysHBaNeENiLBknLSz8zdWIoGeBcrruT/WkUtsS0iNi"
            + "ZYiWYQ7+EEycCrWp4ND4RH0czjnuk6l/lx4kO0k96A4XFwQYXiShTVHwy3/9H6IUI"
            + "PRrApVxEdRgmkQ6HBRBPJ6uQTPcSVvcYavRDSgZIu7XOr0N0vD6ARau0wrC1Xe5YH"
            + "H3H2FfPkP0h1WYIq1eERXotnrIxOlMnwxq7dbqrP69R5wk2YY8bJgthJ+j/dTsqBO"
            + "Tm175PR0s9n1XTuXg7fMoW/spzVZMdVUHUeW2EwIvnEpx526NM7H8Jda5sqTpxEvb"
            + "XeZBEiSnSfhaOzgSMtkbGIi6b1Vs/Gj1wul+SiwY9rhvdDavfcJQwcpTdheti94x4"
            + "9YWosmXiL+ADu2GWsxCz8dmIruwt0T4IrlAfv3XiWO3r0Dzcs2xxmDjO9r+8w83XA"
            + "0kN8SKB4LLL1r2slnnsMwjpOdqib3naQ++97vpS9qZaDIX4wHIQ6t8XHusX5riKL3"
            + "hLkiocgttvRnXHKszTXSuRyfPKnXfEMqyQCv74M4TtPKeoqDAL0tUrZzMeAvrwYx+"
            + "kKb5f4j+tc6gQ0vZ2JkFSvlNnDcot52Z4ZVeencFMIIFQQYJKoZIhvcNAQcBoIIFM"
            + "gSCBS4wggUqMIIFJgYLKoZIhvcNAQwKAQKgggTuMIIE6jAcBgoqhkiG9w0BDAEDMA"
            + "4ECBrnTyiiDPP+AgIIAASCBMjcLtU52W8Drp5CJR6N6B2vuimJy1q2IpdcUSSB3fd"
            + "O30Uee5WVglh9tUaSAPDTH5YSvGy76d3jawlgQ14Y3mK/U1BtdJi2jw8tn4msJ1Gx"
            + "+vXw4Vr3uLaMIJjIdEwdwdSK+MdpY983Jwt40x6NYZB4NDPHFuUs1CIP9fAFI95XJ"
            + "LAJXQAz1rA1qAOUwkmK0aIHSTluAlCPYofQllM9eqVg8j09wTioiOh2FISM794GsP"
            + "5XiyQdnjmOW/vTHyeo8TS2DwL5ohUWdTpG3p3JDkeHSWKiQuHP12IfvjdxDJR7w88"
            + "iDCfYhjVk5QoEzUH46NgkRlbTmzBfKXfTGKPuBbxis+m2DN+GE1256gQMcijDHFJW"
            + "/oOlKiovfh/o3DJ7ST9xVD6ty8d27TReiX1t7zkR4pnwuKhYeFUeXsomNtXiVXM9P"
            + "DeF7T9wkJiWXZ5cDhCZdd/5YQzNR9b7qdDrX1vB2FWDADtjpRxdHzmtiJH2fawqAp"
            + "F0+Ml64TTmot2Z4iVN26HHfnhKQOT+S19vdSXGe2+6EhiQbVf/RkkEamU60spVBtC"
            + "5pqLwwpuXPBQqtCthJctK3+buhx14KdFJ45dcUg2A8TxW4ZpBbX+JQpCLYG5U06eZ"
            + "lE8y8Rg0y6nf3vnkcnpbH+A70+/Ex7VFL88DkyK1b3rrhVgu/8iXrrBMGEGYLarpa"
            + "ERfJ2uuSWIBM+BU0mSQJaBURoimnDcpd1JHBtgAyowAZEp7Ln5cYdJeyhxz46WeGT"
            + "RULCVRL1BDfyRdhdgdGP3XqM6ptSg+P3B4wMcIiLBDpzn+uLkIkpfua5sooutwzhB"
            + "7vhDMG8mF0+y/Q+QXT3XVlvjBmckRiG8TUeA9Xyq193kspm9AAbGm9kINaSz2lFF/"
            + "9Jcr8qQRPu/YzBRhhrccmbexnLM3CsZizlhytFzMAS48ivhatFHIXY3FmbljnUsXz"
            + "o2YNmUVx34teiFHEMAg01S8/4joWFU8Frgnld+VGEiqMu5vGOWAhyBuKpz/z8iHBf"
            + "OFrzKKoTTflqn3n6Z/+ozbHVX2QhAda05tWhgKcAIeUfBMCyICPRFKc3ktNUxDmF4"
            + "tOyHq4Saqbun4/UqG4l0Lj3dQd+n2nojaBodCFbM4NMHc0T7Lpta/sYZz1+5DXk6z"
            + "Ato6T8eG9f5c2UCQXnexW+OtUTRHWAGnU6vFKjqd3Jp+tNn10Y59ahL72leGpErU6"
            + "a5BgecmhAIW1P1FZYyHp+CWJs8UZuuSOgxbhRsbmLA6N20dQkoFf3fHu4BkWPgT8p"
            + "3mKbh7U7itz24g120g4LScumVhDLGtEyMIiwmwbqAidALBKcLBbZUu2sqSl79B1VG"
            + "TYYqv8WgWU46MlAjLjfpYTzgiBdYz87FeoRSyR3yYZp+K4dO8TnRc0u8meOls0TIy"
            + "6jVJ1audOJLYl1YLKSBZpB18dnY1Fii6odnXaFaqXdH7p1hzGugTLc9r6ltCaWsFx"
            + "3Op0zeI22nncLFTPenp+MUfYiyXBQLDDN0ZPS44bDDOlfRur4FD8SzP1J/ZFjCG3P"
            + "76lEWFv6t/dHNH6DcD60XhIo8uKXu9vN2E/uY7ZSS7poYKMNze7sA0RUsfLtl7+De"
            + "Y++Z7BrhTNXiHFGYe7LpHT1bKNHcU2kAxJTAjBgkqhkiG9w0BCRUxFgQULsraD8sB"
            + "hRrqSoPM1rWho2dwfdgwMTAhMAkGBSsOAwIaBQAEFLdTv57o/NtP6IQRviZ/QF7cp" + "JbpBAg1CsYOpUzuOQICCAA=";
}
