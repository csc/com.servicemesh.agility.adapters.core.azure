/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.exception;

public class SignatureException extends AzureAdapterException
{
    private static final long serialVersionUID = 20150114;

    public SignatureException(String code, String msg)
    {
        super(code, msg);
    }

    public SignatureException(String code, String msg, Throwable e)
    {
        super(code, msg, e);
    }

    public SignatureException(String msg, Throwable arg1)
    {
        super(msg, arg1);
    }

    public SignatureException(String msg)
    {
        super(msg);
    }

    public SignatureException(Throwable arg0)
    {
        super(arg0);
    }

}
