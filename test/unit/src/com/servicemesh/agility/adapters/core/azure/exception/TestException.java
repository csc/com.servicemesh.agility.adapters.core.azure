/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.exception;

import org.junit.Assert;
import org.junit.Test;

public class TestException
{
    @Test
    public void testAzureAdapterException() throws Exception
    {
        int code = 451;
        String codeStr = Integer.toString(code);
        String msg = "AdapterMessage";
        AzureAdapterException aae = new AzureAdapterException(code, msg);
        Assert.assertEquals(codeStr, aae.getCode());
        Assert.assertEquals(msg, aae.getMessage());

        aae = new AzureAdapterException(codeStr, msg);
        Assert.assertEquals(codeStr, aae.getCode());
        Assert.assertEquals(msg, aae.getMessage());

        AzureAdapterException aae2 = new AzureAdapterException(aae);
        Assert.assertNull(aae2.getCode());
        Assert.assertTrue(aae2.getMessage().contains(msg));

        String msg2 = "MessageTwo";
        aae2 = new AzureAdapterException(msg2, aae);
        Assert.assertNull(aae2.getCode());
        Assert.assertEquals(msg2, aae2.getMessage());

        aae2 = new AzureAdapterException(code, msg2, aae);
        Assert.assertEquals(codeStr, aae2.getCode());
        Assert.assertEquals(msg2, aae2.getMessage());

        aae2 = new AzureAdapterException(codeStr, msg2, aae);
        Assert.assertEquals(codeStr, aae2.getCode());
        Assert.assertEquals(msg2, aae2.getMessage());
    }

    @Test
    public void testAzureErrorException() throws Exception
    {
        String msErrorObj = "hello";
        String msg = "ErrorMsg";
        AzureErrorException aee = new AzureErrorException(msErrorObj, msg);
        Assert.assertEquals(msg, aee.getMessage());

        String sObj = aee.getError(String.class);
        Assert.assertNotNull(sObj);
        Assert.assertSame(msErrorObj, sObj);

        Integer iObj = aee.getError(Integer.class);
        Assert.assertNull(iObj);
    }

    @Test
    public void testSignatureException() throws Exception
    {
        int code = 893;
        String codeStr = Integer.toString(code);
        String msg = "SigMessage";
        SignatureException se = new SignatureException(msg);
        Assert.assertNull(se.getCode());
        Assert.assertEquals(msg, se.getMessage());

        se = new SignatureException(codeStr, msg);
        Assert.assertEquals(codeStr, se.getCode());
        Assert.assertEquals(msg, se.getMessage());

        SignatureException se2 = new SignatureException(se);
        Assert.assertNull(se2.getCode());
        Assert.assertTrue(se2.getMessage().contains(msg));

        String msg2 = "MessageTwo";
        se2 = new SignatureException(msg2, se);
        Assert.assertNull(se2.getCode());
        Assert.assertEquals(msg2, se2.getMessage());

        se2 = new SignatureException(codeStr, msg2, se);
        Assert.assertEquals(codeStr, se2.getCode());
        Assert.assertEquals(msg2, se2.getMessage());
    }
}
