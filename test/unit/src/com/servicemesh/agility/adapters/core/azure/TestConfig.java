/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.servicemesh.agility.api.AssetProperty;
import com.servicemesh.agility.api.Property;

public class TestConfig
{
    @Test
    public void testProperty() throws Exception
    {
        Config cfg = new Config();
        Assert.assertNotNull(cfg);

        List<Property> settings = null;
        Assert.assertEquals(Config.POLL_RETRIES_DEFAULT,
                            Config.getPollRetries(settings));
        Assert.assertEquals(Config.HTTP_RETRIES_DEFAULT,
                            Config.getHttpRetries(settings));
        Assert.assertEquals(Config.HTTP_TIMEOUT_DEFAULT_SECS * 1000,
                            Config.getHttpTimeout(settings));
        Assert.assertEquals(Config.SOCKET_TIMEOUT_DEFAULT_SECS * 1000,
                            Config.getSocketTimeout(settings));

        settings = new ArrayList<Property>();
        Assert.assertEquals(Config.POLL_RETRIES_DEFAULT,
                            Config.getPollRetries(settings));
        Assert.assertEquals(Config.HTTP_RETRIES_DEFAULT,
                            Config.getHttpRetries(settings));
        Assert.assertEquals(Config.HTTP_TIMEOUT_DEFAULT_SECS * 1000,
                            Config.getHttpTimeout(settings));
        Assert.assertEquals(Config.SOCKET_TIMEOUT_DEFAULT_SECS * 1000,
                            Config.getSocketTimeout(settings));

        Property pollRetries = new Property();
        pollRetries.setName(Config.POLL_RETRIES);
        pollRetries.setValue("100");
        settings.add(pollRetries);

        Property httpRetries = new Property();
        httpRetries.setName(Config.HTTP_RETRIES);
        httpRetries.setValue("101");
        settings.add(httpRetries);

        Property httpTimeout = new Property();
        httpTimeout.setName(Config.HTTP_TIMEOUT);
        httpTimeout.setValue("102");
        settings.add(httpTimeout);

        Property socketTimeout = new Property();
        socketTimeout.setName(Config.SOCKET_TIMEOUT);
        socketTimeout.setValue("103");
        settings.add(socketTimeout);

        Assert.assertEquals(Integer.parseInt(pollRetries.getValue()),
                            Config.getPollRetries(settings));
        Assert.assertEquals(Integer.parseInt(httpRetries.getValue()),
                            Config.getHttpRetries(settings));
        Assert.assertEquals(Integer.parseInt(httpTimeout.getValue()),
                            Config.getHttpTimeout(settings));
        Assert.assertEquals(Integer.parseInt(socketTimeout.getValue()),
                            Config.getSocketTimeout(settings));
    }

    @Test
    public void testAssetProperty() throws Exception
    {
        String propName = "foo";
        List<AssetProperty> props = null;
        AssetProperty prop = Config.getAssetProperty(propName, props);
        Assert.assertNull(prop);

        props = new ArrayList<AssetProperty>();
        prop = Config.getAssetProperty(propName, props);
        Assert.assertNull(prop);

        AssetProperty apstr = new AssetProperty();
        apstr.setName("apstr");
        apstr.setStringValue("apstr-value");
        String strPropValue = "strPropValue";
        String asString =
            Config.getAssetPropertyAsString(apstr.getName(), props,
                                            strPropValue);
        Assert.assertEquals(strPropValue, asString);

        props.add(apstr);
        asString = Config.getAssetPropertyAsString(apstr.getName(), props,
                                                   strPropValue);
        Assert.assertEquals(apstr.getStringValue(), asString);

        AssetProperty apint = new AssetProperty();
        apint.setName("apint");
        apint.setIntValue(42);
        int intPropValue = 159;
        int asInt = Config.getAssetPropertyAsInteger(apint.getName(), props);
        Assert.assertEquals(0, asInt);

        asInt = Config.getAssetPropertyAsInteger(apint.getName(), props,
                                                 intPropValue);
        Assert.assertEquals(intPropValue, asInt);

        props.add(apint);
        asInt = Config.getAssetPropertyAsInteger(apint.getName(), props);
        Assert.assertEquals(apint.getIntValue().intValue(), asInt);
        asInt = Config.getAssetPropertyAsInteger(apint.getName(), props,
                                                 intPropValue);
        Assert.assertEquals(apint.getIntValue().intValue(), asInt);
    }
}
