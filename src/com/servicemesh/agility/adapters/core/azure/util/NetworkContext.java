/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.util;

import java.io.Serializable;

/**
 * This class is used to store information from a parsed CIDR block.
 *
 * @author henry
 */
public class NetworkContext implements Serializable
{
    private static final long serialVersionUID = 20140508;

    private String cidrBlock;
    private String ip;
    private String hostAddress;
    private int ipPrefix;
    private int mask;

    /**
     * Constructor.
     *
     * @param cidrBlock
     *            The CIDR block that was parsed
     * @param ip
     *            IP address extracted from CIDR
     * @param hostAddress
     *            Host IP address extracted from CIDR
     * @param ipPrefix
     *            Number of CIDR range parts
     * @param mask
     *            Mask value give the ipPrefix
     */
    public NetworkContext(String cidrBlock, String ip, String hostAddress, int ipPrefix, int mask)
    {
        this.cidrBlock = cidrBlock;
        this.ip = ip;
        this.hostAddress = hostAddress;
        this.ipPrefix = ipPrefix;
        this.mask = mask;
    }

    /**
     * This method will create a string representation of the object.
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("[");
        buf.append("cidrBlock: " + cidrBlock + "; ");
        buf.append("ip: " + ip + "; ");
        buf.append("hostAddress: " + hostAddress + "; ");
        buf.append("ipPrefix: " + ipPrefix + "; ");
        buf.append("mask: " + mask);
        buf.append("]");

        return buf.toString();
    }

    public String getCidrBlock()
    {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock)
    {
        this.cidrBlock = cidrBlock;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public String getHostAddress()
    {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress)
    {
        this.hostAddress = hostAddress;
    }

    public int getIpPrefix()
    {
        return ipPrefix;
    }

    public void setIpPrefix(int ipPrefix)
    {
        this.ipPrefix = ipPrefix;
    }

    public int getMask()
    {
        return mask;
    }

    public void setMask(int mask)
    {
        this.mask = mask;
    }

}
