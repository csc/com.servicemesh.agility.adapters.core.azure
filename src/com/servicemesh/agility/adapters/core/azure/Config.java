/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure;

import java.util.List;

import com.servicemesh.agility.api.AssetProperty;
import com.servicemesh.agility.api.Property;

/**
 * Provides configuration settings for Microsoft Azure connections
 */
public class Config
{
    public static final String POLL_RETRIES = "AgilityManager.azure.PollRetries";

    public static final String HTTP_RETRIES = "AgilityManager.azure.HttpRetries";

    public static final String HTTP_TIMEOUT = "AgilityManager.azure.HttpTimeoutMillis";

    public static final String SOCKET_TIMEOUT = "AgilityManager.azure.SocketTimeoutMillis";

    public static final int POLL_RETRIES_DEFAULT = 30;
    public static final int HTTP_RETRIES_DEFAULT = 2;
    public static final int HTTP_TIMEOUT_DEFAULT_SECS = 240;
    public static final int SOCKET_TIMEOUT_DEFAULT_SECS = 20;

    public static final String CONFIG_SUBSCRIPTION = "subscription";
    public static final String CONFIG_CERTIFICATE = "certificate";
    public static final String CONFIG_PRIVATE_KEY = "private-key";

    /**
     * Returns the number of retry attempts while waiting for an operation to complete
     *
     * @param settings
     *            Configuration data - if empty or null a default value is returned.
     */
    public static int getPollRetries(List<Property> settings)
    {
        return Config.getProperty(Config.POLL_RETRIES, settings, Config.POLL_RETRIES_DEFAULT);
    }

    /**
     * Returns the number of retries upon failure of an HTTP request
     *
     * @param settings
     *            Configuration data - if empty or null a default value is returned.
     */
    public static int getHttpRetries(List<Property> settings)
    {
        return Config.getProperty(Config.HTTP_RETRIES, settings, Config.HTTP_RETRIES_DEFAULT);
    }

    /**
     * Returns the number of milliseconds to wait for a successful HTTP connection/response
     *
     * @param settings
     *            Configuration data - if empty or null a default value is returned.
     */
    public static int getHttpTimeout(List<Property> settings)
    {
        return Config.getProperty(Config.HTTP_TIMEOUT, settings, Config.HTTP_TIMEOUT_DEFAULT_SECS * 1000);
    }

    /**
     * Returns the number of milliseconds to wait for a successful HTTP socket connection
     *
     * @param settings
     *            Configuration data - if empty or null a default value is returned.
     */
    public static int getSocketTimeout(List<Property> settings)
    {
        return Config.getProperty(Config.SOCKET_TIMEOUT, settings, Config.SOCKET_TIMEOUT_DEFAULT_SECS * 1000);
    }

    /**
     * Returns the requested property
     *
     * @param name
     *            The name of a property
     * @param properties
     *            Configuration data - may be empty or null
     * @param defaultValue
     *            The default value to return if property is not found in settings parameter
     */
    public static int getProperty(String name, List<Property> properties, int defaultValue)
    {
        int value = defaultValue;

        if (properties != null) {
            for (Property property : properties) {
                if (property.getName().equals(name)) {
                    value = Integer.parseInt(property.getValue());
                    break;
                }
            }
        }
        return value;
    }

    public static AssetProperty getAssetProperty(String name, List<AssetProperty> properties)
    {
        if (properties != null) {
            for (AssetProperty property : properties) {
                if (property.getName().equals(name)) {
                    return property;
                }
            }
        }
        return null;
    }

    public static String getAssetPropertyAsString(String name, List<AssetProperty> properties)
    {
        AssetProperty prop = Config.getAssetProperty(name, properties);
        return prop != null ? prop.getStringValue() : null;
    }

    public static int getAssetPropertyAsInteger(String name, List<AssetProperty> properties)
    {
        AssetProperty prop = Config.getAssetProperty(name, properties);
        return prop != null ? prop.getIntValue() : 0;
    }

    public static byte[] getAssetPropertyAsBytes(String name, List<AssetProperty> properties)
    {
        AssetProperty prop = Config.getAssetProperty(name, properties);
        return prop != null ? prop.getByteValue() : null;
    }

    public static String getAssetPropertyAsString(String name, List<AssetProperty> properties, String defaultValue)
    {
        AssetProperty prop = Config.getAssetProperty(name, properties);
        return prop != null ? prop.getStringValue() : defaultValue;
    }

    public static int getAssetPropertyAsInteger(String name, List<AssetProperty> properties, int defaultValue)
    {
        AssetProperty prop = Config.getAssetProperty(name, properties);
        return prop != null ? prop.getIntValue() : defaultValue;
    }
}
