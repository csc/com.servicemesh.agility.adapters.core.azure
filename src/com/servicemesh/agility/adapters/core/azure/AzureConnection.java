/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure;

import com.servicemesh.core.async.Promise;
import com.servicemesh.io.http.IHttpResponse;
import com.servicemesh.io.http.QueryParams;

/**
 * Provides CRUD operations for Microsoft Azure
 */
public interface AzureConnection
{
    /**
     * Returns the endpoint associated with this connection
     */
    public AzureEndpoint getEndpoint();

    /**
     * Retrieves an Azure resource.
     *
     * @param requestURI
     *            The URI specific to retrieving a resource
     * @param params
     *            Query parameters. Optional - may be null or empty.
     * @param responseClass
     *            The class of resource to be retrieved.
     * @return A Promise for the retrieved resource
     */
    public <T> Promise<T> get(String requestURI, QueryParams params, final Class<T> responseClass);

    /**
     * Creates an Azure resource.
     *
     * @param requestURI
     *            The URI specific to creating a resource
     * @param resource
     *            The data to be posted. If type is String it is directly used while any other type is encoded.
     * @param responseClass
     *            The class of the response. Typically either the class of the resource to be created or IHttpResponse if the
     *            response has no body.
     * @return A Promise for the created resource
     */
    public <T> Promise<T> post(String requestURI, Object resource, final Class<T> responseClass);

    /**
     * Updates an Azure resource.
     *
     * @param requestURI
     *            The URI specific to updating a resource
     * @param resource
     *            The data to be posted. If type is String it is directly used while any other type is encoded.
     * @param responseClass
     *            The class of the response. Typically either the class of the resource to be updated or IHttpResponse if the
     *            response has no body.
     * @return A Promise for the updated resource
     */
    public <T> Promise<T> put(String requestURI, Object resource, final Class<T> responseClass);

    /**
     * Deletes an Azure resource
     *
     * @param requestURI
     *            The URI specific to deleting a resource
     * @return A Promise for the response to the delete request
     */
    public Promise<IHttpResponse> delete(String requestURI);
}
