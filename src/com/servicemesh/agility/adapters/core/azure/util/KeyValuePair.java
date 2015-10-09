/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.util;

import java.io.Serializable;

/**
 * This class is a simple mapping of a key to a value.
 *
 * @author henry
 */
public class KeyValuePair implements Serializable
{
    private static final long serialVersionUID = 20131204;

    private String key;
    private String value;

    /**
     * Default construction.
     */
    public KeyValuePair()
    {
    }

    /**
     * Constructor using all properties.
     *
     * @param key
     *            The key value of the pair
     * @param value
     *            The data value of the pair
     * @throws IllegalArgumentException
     *             - if the key has no value
     */
    public KeyValuePair(String key, String value)
    {
        if (key != null && !key.isEmpty()) {
            this.key = key;
            this.value = value;
        }
        else {
            throw new IllegalArgumentException("The key parameter should not be null or empty.");
        }
    }

    /**
     * This method will create a human-friendly representation of the object.
     *
     * @return String - string representation of the object
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append("key = " + key);
        sb.append(", ");
        sb.append("value = " + value);
        sb.append("]");

        return sb.toString();
    }

    /**
     * This method will return the pair as a message style format such as "key:value"
     *
     * @return A string representation of the pair.
     */
    public String asMessage()
    {
        return (KeyValuePair.isValued(key) ? key : "empty") + ":" + (KeyValuePair.isValued(value) ? value : "empty");
    }

    /**
     * This method provides an easy way to verify a string has data.
     *
     * @param s
     *            The value to be checked
     * @return boolean - true if the string is not null or not empty
     */
    private static boolean isValued(String s)
    {
        return s != null && !s.isEmpty();
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

}
