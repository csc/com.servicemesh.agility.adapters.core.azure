/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.exception;

public class AzureAdapterException extends RuntimeException
{
    private static final long serialVersionUID = 20150114;

    String code;

    public AzureAdapterException(int code, String message)
    {
        this(Integer.toString(code), message);
    }

    public AzureAdapterException(String code, String message)
    {
        super(message);
        this.code = code;
    }

    public AzureAdapterException(String message)
    {
        super(message);
    }

    public AzureAdapterException(Throwable arg0)
    {
        super(arg0);
    }

    public AzureAdapterException(String message, Throwable arg1)
    {
        super(message, arg1);
    }

    public AzureAdapterException(int code, String message, Throwable arg1)
    {
        this(Integer.toString(code), message, arg1);
    }

    public AzureAdapterException(String code, String message, Throwable arg1)
    {
        super(message, arg1);
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }
}
