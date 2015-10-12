/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.exception;

public class AzureErrorException extends RuntimeException
{
    private static final long serialVersionUID = 20150114;

    String code;
    Object msError;

    public AzureErrorException(Object msErrorObj, String message)
    {
        super(message);
        msError = msErrorObj;
    }

    /**
     * Returns the Microsoft Error object associated with this exception.
     */
    public <T> T getError(Class<T> errorClass)
    {
        return errorClass.isInstance(msError) ? errorClass.cast(msError) : null;
    }
}
