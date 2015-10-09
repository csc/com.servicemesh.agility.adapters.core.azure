/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */
package com.servicemesh.agility.adapters.core.azure;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.microsoft.schemas.azure.trafficmgr.Definition;
import com.microsoft.schemas.azure.trafficmgr.Definitions;
import com.microsoft.schemas.azure.trafficmgr.Error;
import com.microsoft.schemas.azure.trafficmgr.Profile;
import com.microsoft.schemas.azure.trafficmgr.Status;
import com.microsoft.schemas.azure.trafficmgr.StatusDetails;
import com.servicemesh.agility.adapters.core.azure.exception.AzureAdapterException;
import com.servicemesh.agility.adapters.core.azure.exception.AzureErrorException;
import com.servicemesh.agility.adapters.core.azure.impl.AzureEndpointImpl;
import com.servicemesh.agility.api.Credential;
import com.servicemesh.agility.api.Property;
import com.servicemesh.io.http.impl.DefaultHttpResponse;

public class TestAzureEndpoint
{
    final private static String SUBSCRIPTION = "2351";
    final private static String MS_VERSION = "2012-08-01";

    @Before
    public void before()
    {
        TestHelpers.initLogger(Level.TRACE);
    }

    @Test
    public void testEndpointFactory() throws Exception
    {
        // Try invalid contexts
        createEndpoint("foo.bar", DefaultHttpResponse.class, true);
        createEndpoint(null, java.lang.Error.class, AzureEndpoint.MediaType.XML, true);

        // Good contexts
        AzureEndpoint ep = createEndpoint(null, Error.class, false);
        Assert.assertEquals(AzureEndpoint.DEFAULT_ADDRESS, ep.getAddress());
        Assert.assertEquals(AzureEndpoint.DEFAULT_MEDIA_TYPE.getValue(), ep.getContentType());
        Assert.assertNotNull(ep.getContext());
        Assert.assertEquals(TestAzureEndpoint.MS_VERSION, ep.getMsVersion());
        Assert.assertEquals(TestAzureEndpoint.SUBSCRIPTION, ep.getSubscription());

        // Non-default content type
        ep = createEndpoint(null, Error.class, AzureEndpoint.MediaType.JSON, false);
        Assert.assertEquals(AzureEndpoint.DEFAULT_ADDRESS, ep.getAddress());
        Assert.assertEquals(AzureEndpoint.MediaType.JSON.getValue(), ep.getContentType());
        Assert.assertNotNull(ep.getContext());
        Assert.assertEquals(TestAzureEndpoint.MS_VERSION, ep.getMsVersion());
        Assert.assertEquals(TestAzureEndpoint.SUBSCRIPTION, ep.getSubscription());
    }

    @Test
    public void testEndpointSerialization() throws Exception
    {
        AzureEndpoint ep = createEndpoint(null, Error.class, false);
        doSerialization(ep);
    }

    @Test
    public void testJson() throws Exception
    {
        AzureEndpoint ep = createEndpoint(null, Error.class,
                                          AzureEndpoint.MediaType.JSON, false);
        doSerialization(ep);
    }

    private void doSerialization(AzureEndpoint ep) throws Exception
    {
        // Negative cases of trying to serialize a non-schema class
        try {
            doEncode(ep, ep);
            Assert.fail("encode error expected for invalid class");
        }
        catch (AzureAdapterException aee) {
            Assert.assertNull(aee.getCode());
        }
        catch (Exception ex) {
            Assert.fail("Unexected exception message: " + ex.getMessage());
        }
        try {
            doDecode(ep, AzureEndpoint.class, "<foo/>");
            Assert.fail("decode error expected for invalid class");
        }
        catch (AzureAdapterException aee) {
        }
        catch (Exception ex) {
            Assert.fail("Unexected exception message: " + ex.getMessage());
        }

        // Serialize/deserialize a Profile
        TestHelpers.setLogLevel(AzureEndpointImpl.class.getName(), Level.TRACE);
        Profile expProfile = createProfile();
        Profile actProfile = (Profile) cycleObject(ep, expProfile);
        verifyProfile(expProfile, actProfile);

        // An IHttpResponse can also be requested back from decode()
        DefaultHttpResponse response = new DefaultHttpResponse();
        DefaultHttpResponse cyResponse = ep.decode(response, response.getClass());
        Assert.assertSame(response, cyResponse);

        // Serialize/deserialize an Error
        Error error = new Error();
        error.setCode("1441");
        error.setMessage("My Error Message");

        Error cyError = (Error) cycleObject(ep, error);
        Assert.assertEquals(error.getCode(), cyError.getCode());
        Assert.assertEquals(error.getMessage(), cyError.getMessage());

        // Simulate REST call failure by having the Error returned when a good
        // response should have a Profile
        String encodedError = doEncode(ep, error);
        try {
            doDecode(ep, expProfile.getClass(), encodedError);
            if (AzureEndpoint.MediaType.XML
                .getValue().equals(ep.getContentType()))
                Assert.fail("Expected exception for returned Error");
        }
        catch (AzureErrorException aex) {
            Error exError = aex.getError(Error.class);
            Assert.assertNotNull(exError);
            Assert.assertEquals(error.getCode(), exError.getCode());
            Assert.assertEquals(error.getMessage(), exError.getMessage());
        }
        catch (Exception ex) {
            Assert.fail("Unexected exception message: " + ex.getMessage());
        }

        // REST service is behaving entirely wrong
        String encodedDetails = doEncode(ep, expProfile.getStatusDetails());
        try {
            doDecode(ep, expProfile.getClass(), encodedDetails);
            if (AzureEndpoint.MediaType.XML
                .getValue().equals(ep.getContentType()))            
                Assert.fail("Expected exception for wrong class returned");
        }
        catch (Exception ex) {
        }
    }

    private Profile createProfile()
    {
        Profile profile = new Profile();
        profile.setDomainName("testGoodEndpoint.trafficmanager.net");
        profile.setName("testGoodEndpoint");
        profile.setStatus(Status.ENABLED);

        Definitions definitions = new Definitions();
        Definition definition = new Definition();
        definition.setStatus("a-okay");
        definition.setVersion("abc");
        definitions.getDefinitions().add(definition);
        profile.setDefinitions(definitions);

        StatusDetails details = new StatusDetails();
        details.setEnabledVersion(42);
        profile.setStatusDetails(details);
        return profile;
    }

    private void verifyProfile(Profile expected, Profile actual)
        throws Exception
    {
        Assert.assertEquals(expected.getDomainName(), actual.getDomainName());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getStatus(), actual.getStatus());

        Definitions expDefs = expected.getDefinitions();
        Definitions actDefs = actual.getDefinitions();
        Assert.assertNotNull(actDefs);
        Assert.assertEquals(expDefs.getDefinitions().size(),
                            actDefs.getDefinitions().size());
        Definition expDef = expDefs.getDefinitions().get(0);
        Definition actDef = actDefs.getDefinitions().get(0);
        Assert.assertEquals(expDef.getStatus(), actDef.getStatus());
        Assert.assertEquals(expDef.getVersion(), actDef.getVersion());

        StatusDetails expDetails = expected.getStatusDetails();
        StatusDetails actDetails = actual.getStatusDetails();
        Assert.assertNotNull(actDetails);
        Assert.assertEquals(expDetails.getEnabledVersion(), actDetails.getEnabledVersion());
    }

    @Test
    public void testMultiContext() throws Exception
    {
        // Currently only have one namespace available, but using it will
        // still exercise code paths
        TestHelpers.setLogLevel(AzureEndpointImpl.class.getName(), Level.INFO);
        String pkgName = Error.class.getPackage().getName();
        AzureEndpoint ep = createEndpoint(null, Error.class, false);
        Assert.assertNotNull(ep);
        Assert.assertNotNull(ep.getContext(pkgName));

        Profile profile = new Profile();
        profile.setDomainName("multiContext.trafficmanager.net");
        profile.setName("multiContext");

        String encoded = ep.encode(pkgName, profile);
        Assert.assertNotNull(encoded);

        DefaultHttpResponse response = new DefaultHttpResponse();
        response.setContent(new ByteArrayInputStream(encoded.getBytes(StandardCharsets.UTF_8)));
        Profile decoded = ep.decode(response, pkgName, Profile.class);
        Assert.assertNotNull(decoded);
        Assert.assertEquals(profile.getDomainName(), decoded.getDomainName());
        Assert.assertEquals(profile.getName(), decoded.getName());

        try {
            ep.encode("bad.context", profile);
            Assert.fail("Expected exception for bad encode context");
        }
        catch (Exception ex) {
        }

        try {
            ep.decode(response, "bad.context", Profile.class);
            Assert.fail("Expected exception for bad decode context");
        }
        catch (Exception ex) {
        }

        AzureEndpointFactory factory = AzureEndpointFactory.getInstance();
        JAXBContext context = factory.lookupContext(pkgName);
        Assert.assertNotNull(context);

        Assert.assertTrue(ep instanceof AzureEndpointImpl);
        AzureEndpointImpl aei = (AzureEndpointImpl)ep;

        Class<?> holder =
            Whitebox.getInnerClassType(AzureEndpointImpl.class, "Holder");
        Assert.assertNotNull(holder);

        String credPath = Credential.class.getPackage().getName();
        JAXBContext apiContext =
            (JAXBContext)Whitebox.invokeMethod(aei, holder, "createContext",
                                               credPath,
                                               Credential.class.getClassLoader());
        Assert.assertNotNull(apiContext);

        Credential cred = new Credential();
        cred.setPublicKey("foo");
        cred.setPrivateKey("bar");
        String encodedCred = ep.encode(credPath, cred);
        Assert.assertNotNull(encodedCred);
        response.setContent(encodedCred.getBytes());

        try {
            Whitebox.invokeMethod(aei, "doDecode", response, Property.class,
                                  apiContext, ep.getContext());
            Assert.fail("Expected exception for decode of wrong class");
        }
        catch (AzureAdapterException aae) {
        }

        Error myError = new Error();
        myError.setCode("2623");
        myError.setMessage("Decode test");
        String encodedError = ep.encode(myError);
        Assert.assertNotNull(encodedError);
        response.setContent(encodedError.getBytes());

        try {
            Whitebox.invokeMethod(aei, "doDecode", response, Property.class,
                                  apiContext, ep.getContext());
        }
        catch (AzureErrorException aee) {
            String exError = aee.getMessage();
            Assert.assertNotNull(exError);
            Assert.assertTrue(exError.contains(encodedError));
        }

        factory.unregisterContext(pkgName);

        context = factory.lookupContext(pkgName);
        Assert.assertNull(context);
    }

    private <E> AzureEndpoint createEndpoint(String msContextPath, Class<E> msErrorClass, boolean throwing) throws Exception
    {
        return createEndpoint(msContextPath, msErrorClass,
                              AzureEndpoint.MediaType.XML, throwing);
    }

    private <E> AzureEndpoint createEndpoint(String msContextPath, Class<E> msErrorClass, AzureEndpoint.MediaType mediaType, boolean throwing) throws Exception
    {
        String pkgName = getPackageName(msContextPath, msErrorClass);
        AzureEndpoint endpoint = null;
        try {
            if (AzureEndpoint.DEFAULT_MEDIA_TYPE == mediaType) {
                endpoint = AzureEndpointFactory.getInstance()
                    .getEndpoint(TestAzureEndpoint.SUBSCRIPTION,
                                 TestAzureEndpoint.MS_VERSION,
                                 pkgName, msErrorClass);
            }
            else {
                endpoint = AzureEndpointFactory.getInstance()
                    .getEndpoint(TestAzureEndpoint.SUBSCRIPTION,
                                 TestAzureEndpoint.MS_VERSION,
                                 pkgName, msErrorClass, mediaType);
            }
            if (throwing) {
                Assert.fail("Expected exception: path=" + msContextPath +
                            ", class=" + msErrorClass.getName());
            }
        }
        catch (AzureAdapterException aae) {
            StringBuilder sb = new StringBuilder();
            sb.append("path=").append(msContextPath).append(", class=")
                .append(msErrorClass.getName()).append(", exc=").append(aae);
            if (throwing) {
                System.out.println("Got expected exception: " + sb.toString());
            }
            else {
                Assert.fail("Unexpected exception: " + sb.toString());
            }
        }
        return endpoint;
    }

    private <E> String getPackageName(String msContextPath, Class<E> msErrorClass)
    {
        String pkgName = msContextPath;
        if (pkgName == null) {
            pkgName = msErrorClass.getPackage().getName();
        }
        return pkgName;
    }

    private Object cycleObject(AzureEndpoint ep, Object schemaObject) throws Exception
    {
        String encoded = doEncode(ep, schemaObject);
        Object decodedObject = doDecode(ep, schemaObject.getClass(), encoded);
        if (!schemaObject.getClass().isInstance(decodedObject)) {
            Assert.fail("Decode returned unexpected: " + decodedObject.getClass().getName());
        }
        System.out.println("cycleObject: encoded=\n" + encoded);
        return decodedObject;
    }

    private String doEncode(AzureEndpoint ep, Object schemaObject)
    {
        String encoded = ep.encode(schemaObject);
        if (encoded == null || encoded.isEmpty()) {
            Assert.fail("Profile encoding failed");
        }
        return encoded;
    }

    private Object doDecode(AzureEndpoint ep, Class<?> responseClass, String encoded)
    {
        DefaultHttpResponse response = new DefaultHttpResponse();
        response.setContent(new ByteArrayInputStream(encoded.getBytes(StandardCharsets.UTF_8)));
        Object decodedObject = ep.decode(response, responseClass);
        Assert.assertNotNull(decodedObject);
        return decodedObject;
    }
}
