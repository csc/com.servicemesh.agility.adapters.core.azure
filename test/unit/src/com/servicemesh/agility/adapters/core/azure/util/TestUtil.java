/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */
package com.servicemesh.agility.adapters.core.azure.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.servicemesh.agility.adapters.core.azure.TestHelpers;
import com.servicemesh.agility.api.Cloud;
import com.servicemesh.agility.api.Credential;

import com.servicemesh.io.http.IHttpHeader;
import com.servicemesh.io.http.QueryParam;
import com.servicemesh.io.http.QueryParams;

public class TestUtil
{
    private static Logger rootLogger;

    @Before
    public void before()
    {
        TestHelpers.initLogger(Level.TRACE);
    }

    @After
    public void after()
    {
        TestHelpers.setLogLevel(AzureUtil.class.getName(), Level.INFO);
    }

    private class ChildAzureConstants extends AzureConstants
    {
        private static final long serialVersionUID = 20150714;
        ChildAzureConstants() {}
    }

    private class ChildAzureUtil extends AzureUtil
    {
        private static final long serialVersionUID = 20150714;
        ChildAzureUtil() {}
    }

    private Logger setUtilLogLevel(Level level)
    {
        return TestHelpers.setLogLevel(AzureUtil.class.getName(), level);
    }

    @BeforeClass
    public static void init() throws Exception
    {
        PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");

        TestUtil.rootLogger = Logger.getRootLogger();

        TestUtil.rootLogger.setLevel(Level.DEBUG);
        TestUtil.rootLogger.addAppender(new ConsoleAppender(layout));
    }

    @AfterClass
    public static void cleanup() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testChildren() throws Exception
    {
        ChildAzureConstants cac = new ChildAzureConstants();
        Assert.assertNotNull(cac);
        ChildAzureUtil cau = new ChildAzureUtil();
        Assert.assertNotNull(cau);
    }

    @Test
    public void testIsValued() throws Exception
    {
        Object o = null;
        String s = null;
        StringBuilder sb = null;
        List<String> list = null;
        Collection<String> col = null;

        Assert.assertFalse(AzureUtil.isValued(o));
        Assert.assertFalse(AzureUtil.isValued(s));
        Assert.assertFalse(AzureUtil.isValued(sb));
        Assert.assertFalse(AzureUtil.isValued(list));
        Assert.assertFalse(AzureUtil.isValued(col));

        list = new ArrayList<String>();
        col = new TreeSet<String>();
        s = "";
        sb = new StringBuilder();

        Assert.assertFalse(AzureUtil.isValued(list));
        Assert.assertFalse(AzureUtil.isValued(col));
        Assert.assertFalse(AzureUtil.isValued(s));
        Assert.assertFalse(AzureUtil.isValued(sb));

        o = new Object();
        s = "string";
        sb = new StringBuilder("stringbuilder");
        list.add("some string");
        col.add("some string");

        Assert.assertTrue(AzureUtil.isValued(o));
        Assert.assertTrue(AzureUtil.isValued(s));
        Assert.assertTrue(AzureUtil.isValued(sb));
        Assert.assertTrue(AzureUtil.isValued(list));
        Assert.assertTrue(AzureUtil.isValued(col));
    }

    @Test
    public void testCanonicalizeHeaders() throws Exception
    {
        String result = AzureUtil.canonicalizeHeaders(null, "prefix");
        Assert.assertTrue(result.isEmpty());

        HttpHeader[] httpHeaders =
                { new HttpHeader("header2", "value2"), new HttpHeader("header1", "value1"),
                        new HttpHeader("x-ms-version", "versionValue"), new HttpHeader("x-ms-date", "dateValue") };

        setUtilLogLevel(Level.DEBUG);
        Assert.assertEquals("header1:value1\nheader2:value2\nx-ms-date:dateValue\nx-ms-version:versionValue",
                AzureUtil.canonicalizeHeaders(httpHeaders));
        setUtilLogLevel(Level.TRACE);
        Assert.assertEquals("x-ms-date:dateValue\nx-ms-version:versionValue", AzureUtil.canonicalizeHeaders(httpHeaders, "x-ms-"));

        HttpHeader[] dupHeader = { new HttpHeader("x-ms-head", "3"),
                                   new HttpHeader("x-ms-head", "4") };
        Assert.assertEquals("x-ms-head:3,4",
                            AzureUtil.canonicalizeHeaders(dupHeader, "x-ms-"));

        Assert.assertEquals("x-ms-head:3,4",
                            AzureUtil.canonicalizeHeaders(dupHeader, null));
        Assert.assertEquals("x-ms-head:3,4",
                            AzureUtil.canonicalizeHeaders(dupHeader, ""));
    }

    @Test
    public void testCanonicalizeResource() throws Exception
    {
        QueryParams params = new QueryParams();
        String accountName = "myAccountName";
        String account = "/" + accountName + "/";
        String uri = "uriPrefix/uriSuffix";
        String paramStr = "aparam3:value3\nparam1:value1\nparam2:value2\nzparam4:my%2Fanswer+value4";
        String expectedValue = account + uri + "\n" + paramStr;

        params.setCaseSensitive(true);
        params.setMaintainOrder(true);

        params.add(new QueryParam("param2", "value2"));
        params.add(new QueryParam("Param1", "value1"));
        params.add(new QueryParam("zPaRaM4", "my/answer value4"));
        params.add(new QueryParam("APARAM3", "value3"));

        setUtilLogLevel(Level.DEBUG);
        String result = AzureUtil.canonicalizeResource(accountName, uri, params);

        Assert.assertEquals(result, expectedValue);

        setUtilLogLevel(Level.TRACE);
        String noAcctValue = expectedValue.replace(account, "");
        result = AzureUtil.canonicalizeResource(null, uri, params);
        Assert.assertEquals(noAcctValue, result);

        String noUriValue = expectedValue.replace(uri, "");
        result = AzureUtil.canonicalizeResource(accountName, null, params);
        Assert.assertEquals(noUriValue, result);

        QueryParams minParams = null;
        String minParamsValue = expectedValue.replace(paramStr, "");
        result = AzureUtil.canonicalizeResource(accountName, uri, minParams);
        Assert.assertEquals(minParamsValue, result);
        minParams = new QueryParams();
        result = AzureUtil.canonicalizeResource(accountName, uri, minParams);
        Assert.assertEquals(minParamsValue, result);

        minParams.add(new QueryParam("paramNoValue"));
        minParamsValue = expectedValue.replace(paramStr, "paramnovalue");
        result = AzureUtil.canonicalizeResource(accountName, uri, minParams);
        Assert.assertEquals(minParamsValue, result);
    }

    @Test
    public void testMaskPrivateKey() throws Exception
    {
        String completelyMasked = "*****";

        Assert.assertNull(AzureUtil.maskPrivateKey(null));
        Assert.assertNull(AzureUtil.maskPrivateKey(""));
        Assert.assertEquals(completelyMasked, AzureUtil.maskPrivateKey("a"));
        Assert.assertEquals(completelyMasked, AzureUtil.maskPrivateKey("ab"));
        Assert.assertEquals(completelyMasked, AzureUtil.maskPrivateKey("abc"));
        Assert.assertEquals(completelyMasked, AzureUtil.maskPrivateKey("abcd"));
        Assert.assertEquals(completelyMasked, AzureUtil.maskPrivateKey("abcde"));
        Assert.assertEquals("ab*****f", AzureUtil.maskPrivateKey("abcdef"));
        Assert.assertEquals("ab*****g", AzureUtil.maskPrivateKey("abcdefg"));
        Assert.assertEquals("ab*****h", AzureUtil.maskPrivateKey("abcdefgh"));
        Assert.assertEquals("ab*****i", AzureUtil.maskPrivateKey("abcdefghi"));
        Assert.assertEquals("ab*****j", AzureUtil.maskPrivateKey("abcdefghij"));
        Assert.assertEquals("abcd*****ghijk", AzureUtil.maskPrivateKey("abcdefghijk"));
        Assert.assertEquals("abcd*****hijkl", AzureUtil.maskPrivateKey("abcdefghijkl"));
        Assert.assertEquals("abcd*****ijklm", AzureUtil.maskPrivateKey("abcdefghijklm"));
        Assert.assertEquals("abcd*****jklmn", AzureUtil.maskPrivateKey("abcdefghijklmn"));
    }

    @Test
    public void testParseCidrBlock() throws Exception
    {
        Assert.assertNull(AzureUtil.parseCidrBlock(null));
        setUtilLogLevel(Level.OFF);
        Assert.assertNull(AzureUtil.parseCidrBlock("abc/def"));
        setUtilLogLevel(Level.DEBUG);

        NetworkContext ctx = AzureUtil.parseCidrBlock("10.0.0.0/16");
        Assert.assertEquals("10.0.0.0", ctx.getIp());
        Assert.assertEquals("255.255.0.0", ctx.getHostAddress());
        Assert.assertEquals(16, ctx.getIpPrefix());
        Assert.assertEquals(-65536, ctx.getMask());

        setUtilLogLevel(Level.TRACE);
        ctx = AzureUtil.parseCidrBlock("192.192.0.0/24");
        Assert.assertEquals("192.192.0.0", ctx.getIp());
        Assert.assertEquals("255.255.255.0", ctx.getHostAddress());
        Assert.assertEquals(24, ctx.getIpPrefix());
        Assert.assertEquals(-256, ctx.getMask());

        ctx = AzureUtil.parseCidrBlock("192.168.1.0");
        Assert.assertEquals(0, ctx.getIpPrefix());
    }

    @Test
    public void testCreateSharedKeyAuthorization() throws Exception
    {
        String stringToSign =
                "GET\n\n\n\n\n\n\n\n\n\n\n\nx-ms-date:Thu, 15 Jan 2015 20:46:23 GMT\nx-ms-version:2009-09-19\n/henrytest/\ncomp:list";
        String account = "henrytest";

        String signingKey = "ulGbaoSjXPozzkW9AwjLaxw4/hR5hmnE2oO/GMHa5Jr2ZWnRjUT/ZFHob9M6j5XB24APdPJvv0doXzHo6KCA3A==";
        String expectedAuthorization = "SharedKey henrytest:7CW/gaz7s2Ee27gLzvixIa4C0H2ScD8tw6NgAZ25tNM=";
        Assert.assertEquals(expectedAuthorization, AzureUtil.createSharedKeyAuthorization(stringToSign, signingKey, account));

        signingKey = "BMTLi7fiBu2iLyFp5vPArVFAF2GWMpwlbq3+4bAEsOROmsCIOOv/ESa4zx1+/WgaH9M+KIBa61kC9aqOuYhgYg==";
        expectedAuthorization = "SharedKey henrytest:FMzI3KO8bSmyuUNOnoXiMiORZBHwZV/2UrBETQzDRpg=";
        Assert.assertEquals(expectedAuthorization, AzureUtil.createSharedKeyAuthorization(stringToSign, signingKey, account));

        Assert.assertNull(AzureUtil.createSharedKeyAuthorization(null, signingKey, account));
        Assert.assertNull(AzureUtil.createSharedKeyAuthorization(stringToSign, null, account));
        Assert.assertNull(expectedAuthorization, AzureUtil.createSharedKeyAuthorization(stringToSign, signingKey, null));
    }

    private class HttpHeader implements IHttpHeader
    {
        String name;
        String value;

        public HttpHeader(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getValue()
        {
            return value;
        }

        @Override
        public List<String> getValues()
        {
            return null;
        }

    }

    @Test
    public void testDNS() throws Exception
    {
        List<String> ips = AzureUtil.domainNameToIp("www.csc.com");
        Assert.assertFalse(ips.isEmpty());

        // Try IP lookups until one succeeds
        String fqdn = null;
        for (String ip : ips) {
            fqdn = AzureUtil.ipToFqdn(ip);
            if (fqdn != null) {
                break;
            }
        }
        Assert.assertNotNull(fqdn);

        ips = AzureUtil.domainNameToIp(".x");
        Assert.assertTrue(ips.isEmpty());
        ips = AzureUtil.domainNameToIp(null);
        Assert.assertTrue(ips.isEmpty());
        ips = AzureUtil.domainNameToIp("");
        Assert.assertTrue(ips.isEmpty());

        fqdn = AzureUtil.ipToFqdn("10.0.0.255");
        Assert.assertNull(fqdn);
        Assert.assertNull(AzureUtil.ipToFqdn(null));
        Assert.assertNull(AzureUtil.ipToFqdn(""));
        Assert.assertNull(AzureUtil.ipToFqdn("xyz"));
    }

    @Test
    public void testLogObject() throws Exception
    {
        Credential cred = new Credential();
        cred.setPublicKey("BruceWayne");
        cred.setPrivateKey("Batman");
        Cloud cloud = new Cloud();
        cloud.setCloudCredentials(cred);
        cloud.setName("hello, cloud");

        Logger logger = setUtilLogLevel(Level.TRACE);
        String s = AzureUtil.logObject(cloud, logger);
        Assert.assertFalse(s.contains(cred.getPublicKey()));
        Assert.assertTrue(s.contains(cloud.getName()));
        logger.setLevel(Level.WARN);
        s = AzureUtil.logObject(cloud, logger, Level.FATAL);
        Assert.assertFalse(s.contains(cred.getPublicKey()));
        Assert.assertTrue(s.contains(cloud.getName()));

        boolean recurse = true;
        int indent = 0;
        s = AzureUtil.logObject(null, null, Level.DEBUG, recurse, indent);
        Assert.assertTrue(s.isEmpty());
        indent = 2;
        s = AzureUtil.logObject(cloud, null, Level.DEBUG, recurse, indent);
        Assert.assertTrue(s.isEmpty());
        s = AzureUtil.logObject(cloud, logger, Level.DEBUG, recurse, indent);
        Assert.assertTrue(s.isEmpty());

        logger.setLevel(Level.TRACE);
        s = AzureUtil.logObject(cloud, logger, Level.DEBUG, recurse, indent);
        Assert.assertTrue(s.contains(cred.getPublicKey()));
        Assert.assertTrue(s.contains(cloud.getName()));

        recurse = false;
        s = AzureUtil.logObject(cloud, logger, Level.DEBUG, recurse, indent);
        Assert.assertFalse(s.contains(cred.getPublicKey()));
        Assert.assertTrue(s.contains(cloud.getName()));
    }

    @Test
    public void testParseInt() throws Exception
    {
        String istr = null;
        Assert.assertEquals(0, AzureUtil.parseInt(istr));

        istr = "32";
        Assert.assertEquals(istr, Integer.toString(AzureUtil.parseInt(istr)));

        istr = "abc";
        setUtilLogLevel(Level.OFF);
        Assert.assertEquals(0, AzureUtil.parseInt(istr));
    }

    @Test
    public void testArrayToString() throws Exception
    {
        String[] array = null;
        Assert.assertTrue(AzureUtil.arrayToString(array).isEmpty());
        array = new String[0];
        Assert.assertTrue(AzureUtil.arrayToString(array).isEmpty());

        array = new String[] { "1", "2", "3" };
        String s= AzureUtil.arrayToString(array, "");
        Assert.assertEquals("1,2,3", s);
        s= AzureUtil.arrayToString(array, "-");
        Assert.assertEquals("1-2-3", s);
    }
}
