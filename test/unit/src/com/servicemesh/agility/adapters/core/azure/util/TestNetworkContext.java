/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.util;

import org.junit.Assert;
import org.junit.Test;

public class TestNetworkContext
{
    @Test
    public void testConstructor() throws Exception
    {
        String cidrBlock = "192.168.1.0/24";
        String ip = "192.168.1.1";
        String hostAddress = "192.168.1.2";
        int ipPrefix = 8;
        int mask = 0x000000ff;
        NetworkContext nc =
            new NetworkContext(cidrBlock, ip, hostAddress, ipPrefix, mask);

        String asString = nc.toString();
        Assert.assertNotNull(asString);
        Assert.assertTrue(asString.contains(cidrBlock));
        Assert.assertTrue(asString.contains(ip));
        Assert.assertTrue(asString.contains(hostAddress));

        NetworkContext nc2 = new NetworkContext("", "", "", 0, 0);
        nc2.setCidrBlock(nc.getCidrBlock());
        nc2.setIp(nc.getIp());
        nc2.setHostAddress(nc.getHostAddress());
        nc2.setIpPrefix(nc.getIpPrefix());
        nc2.setMask(nc.getMask());

        String asString2 = nc2.toString();
        Assert.assertEquals(asString, asString2);
    }
}
