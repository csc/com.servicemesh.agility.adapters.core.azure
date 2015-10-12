/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.servicemesh.agility.adapters.core.azure.exception.SignatureException;
import com.servicemesh.io.http.IHttpHeader;
import com.servicemesh.io.http.QueryParams;

public abstract class AzureUtil implements Serializable
{
    private static final Logger logger = Logger.getLogger(AzureUtil.class);
    private static final long serialVersionUID = 20150114;

    private static final Level DEFAULT_LEVEL = Level.TRACE;

    /**
     * All headers will be canonicalized.
     *
     * @param headers
     *            All header defined for the request
     * @return String - canonical representation of headers
     */
    public static String canonicalizeHeaders(IHttpHeader[] headers)
    {
        return AzureUtil.canonicalizeHeaders(headers, null);
    }

    /**
     * This method will convert the request headers to a canonical version for use in Azure REST calls. See the Microsoft
     * documentation at http://msdn.microsoft.com/en-us/library/azure/dd179428.aspx#Constructing_Element
     *
     * @param headers
     *            All header defined for the request
     * @param headerPrefix
     *            Restricts the headers that will be processed
     * @return String - canonical representation of headers
     */
    public static String canonicalizeHeaders(IHttpHeader[] headers, String headerPrefix)
    {
        StringBuilder result = new StringBuilder();
        List<IHttpHeader> headersToProcess = new ArrayList<IHttpHeader>();
        String prefix = headerPrefix != null ? headerPrefix.trim().toLowerCase() : null;

        if (headers != null) {
            if (prefix != null && !prefix.isEmpty()) {
                for (IHttpHeader header : headers) {
                    String headerName = header.getName().toLowerCase().trim();

                    if (headerName.startsWith(prefix)) {
                        headersToProcess.add(header);
                    }
                }
            }
            else {
                for (IHttpHeader header : headers) {
                    headersToProcess.add(header);
                }
            }

            HashMap<String, KeyValues<String>> headerMap = new HashMap<String, KeyValues<String>>();

            for (IHttpHeader header : headersToProcess) {
                String headerName = header.getName().toLowerCase().trim();
                String headerValue = header.getValue().trim().replace("\n", " ");
                KeyValues<String> values = headerMap.get(headerName);

                if (values == null) {
                    values = new KeyValues<String>(headerName);
                }

                values.addValue(headerValue);
                headerMap.put(headerName, values);
            }

            String sep = "";
            for (String key : new TreeSet<String>(headerMap.keySet())) {
                result.append(sep + headerMap.get(key).asMessage());
                sep = "\n";
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("\nCanonicalized Header: [\n" + result.toString() + "\n]\n");
        }
        return result.toString();
    }

    /**
     * This method will convert resources to a canonical version for use in Azure REST calls.
     *
     * @param account
     *            Account name
     * @param uri
     *            Uniform Resource Identifier
     * @param params
     *            Query parameters
     * @return String - canonical representation of headers
     */
    public static String canonicalizeResource(String account, String uri, QueryParams params)
    {
        StringBuilder result = new StringBuilder();

        if (account != null) {
            result.append("/" + account + "/");
        }

        result.append(uri != null ? uri + "\n" : "\n");

        if (params != null) {
            boolean isCaseSensitiveOriginal = params.isCaseSensitive();
            boolean isMaintainOrderOriginal = params.isMaintainOrder();

            // param names must be lower case and sorted by name
            params.setCaseSensitive(false);
            params.setMaintainOrder(false);

            // calculate the query string and get rid of the question mark
            String queryString = params.asQueryString(true).replaceAll("\\?", "");

            // the param names must be lower case
            StringBuilder buf = new StringBuilder();

            if (AzureUtil.isValued(queryString)) {
                String sep = "";
                String[] qParams = queryString.split("&");

                for (String qParam : qParams) {
                    String[] nameValuePair = qParam.split("=");
                    int len = nameValuePair.length;

                    buf.append(sep);

                    if (len == 1) { // name with no value
                        buf.append(nameValuePair[0].toLowerCase());
                    }
                    else if (len > 1) { // normal format name=value
                        buf.append(nameValuePair[0].toLowerCase() + ":" + nameValuePair[1]);
                    }

                    sep = "\n";
                }
            }

            result.append(buf);
            //result.append(params.asQueryString().replaceAll("=", ":").replaceAll("&", "\n").replaceAll("\\?", ""));

            // reset the case sensitive value
            params.setCaseSensitive(isCaseSensitiveOriginal);
            params.setMaintainOrder(isMaintainOrderOriginal);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("\nCanonicalized Resource: [\n" + result.toString() + "\n]\n");
        }
        return result.toString();
    }

    /**
     * This method will use reflection to log an object to the specified logger using the default level of TRACE.
     *
     * @param obj
     *            Oobject to be logged
     * @param logger
     *            The logger to which the information will be written
     * @return String - string representation of the object
     */
    public static String logObject(Object obj, Logger logger)
    {
        return AzureUtil.logObject(obj, logger, null, false, 1);
    }

    /**
     * This method will use reflection to log an object to the specified logger using the default level of TRACE.
     *
     * @param obj
     *            Object to be logged
     * @param logger
     *            The logger to which the information will be written
     * @param level
     *            Logging level to be used; if null, the DEFAULT_LEVEL value will be used
     * @return String - string representation of the object
     */
    public static String logObject(Object obj, Logger logger, Level level)
    {
        return AzureUtil.logObject(obj, logger, level, false, 1);
    }

    /**
     * This method will use reflection to log an object to the specified logger using the provided log level. The object is
     * reflected upon looking for "get*" and "is*" methods JavaBean pattern. The methods are invoked and the results written to
     * the log. If the result is another object, the reference information will be displayed - all this is good for is to
     * determine if the property is null or not. If the logger parameter is not enabled for the level requested, nothing will be
     * written.
     *
     * @param obj
     *            Object to be logged
     * @param logger
     *            The logger to which the information will be written
     * @param level
     *            Logging level to be used; if null, the DEFAULT_LEVEL value will be used
     * @param recursive
     *            If true, recurse to log contained objects
     * @param indentLevel
     *            Number of levels to indent for printing
     * @return String - string representation of the object
     */
    public static String logObject(Object obj, Logger logger, Level level, boolean recursive, int indentLevel)
    {
        StringBuilder buf = new StringBuilder();
        String indentStr = "    "; // used for formatting pretty printing
        String indent = ""; // used for formatting pretty printing
        String prefix = "--- "; // used for formatting pretty printing
        String seperator = ": "; // used for formatting pretty printing

        indentLevel = indentLevel < 1 ? 1 : indentLevel;

        for (int i = 1; i < indentLevel; i++) {
            indent += indentStr;
        }

        indent += indentLevel;

        // if no level is provided, it will use the default
        level = level == null ? AzureUtil.DEFAULT_LEVEL : level;

        boolean isEnabled = logger != null ? logger.isEnabledFor(level) : false;
        if (obj != null && logger != null && isEnabled) {
            buf.append("\n" + indent + prefix + "Logging Information for class: " + obj.getClass().getName() + "\n");

            Method[] methods = obj.getClass().getMethods();

            for (Method m : methods) {
                String methodName = m.getName();
                boolean hasParams = m.getParameterTypes().length > 0;

                // just process methods that match the javaBean pattern
                if ((methodName.startsWith("get") || methodName.startsWith("is")) && !hasParams) {
                    int start = methodName.startsWith("get") ? 3 : 2;
                    String label = indent + prefix + methodName.substring(start) + seperator;

                    try {
                        Object o = m.invoke(obj, new Object[] {});

                        if (recursive && o != null && o.getClass().getName().startsWith("com.servicemesh")) {
                            buf.append(label + "\n");
                            buf.append(AzureUtil.logObject(o, logger, level, recursive, indentLevel + 1));
                        }
                        else {
                            // do not print private key value to log
                            if (methodName.toLowerCase().contains("privatekey")) {
                                buf.append(label);
                                buf.append(AzureUtil.maskPrivateKey((String) o));
                            }
                            else {
                                buf.append(label);
                                buf.append(o);
                            }
                        }
                    }
                    catch (Exception e) {
                        buf.append(e.getMessage());
                    }

                    buf.append("\n");
                }
            }

            logger.log(level, buf.toString());
        }

        return buf.toString();
    }

    /**
     * This method will mask the value of a private key. If the masking is dependent on the length of the key value. The purpose
     * is to mask part of a key that may be displayed during logging.
     *
     * @param privateKey
     *            The key to be masked
     * @return String - key with part of the value masked
     */
    public static String maskPrivateKey(String privateKey)
    {
        String retval = null;

        if (AzureUtil.isValued(privateKey)) {
            if (privateKey.length() > 10) {
                String prefix = privateKey.substring(0, 4);
                String suffix = privateKey.substring(privateKey.length() - 5);

                return prefix + "*****" + suffix;
            }
            else if (privateKey.length() > 5) {
                String prefix = privateKey.substring(0, 2);
                String suffix = privateKey.substring(privateKey.length() - 1);

                return prefix + "*****" + suffix;
            }
            else {
                retval = "*****";
            }
        }

        return retval;
    }

    /**
     * This method will convert a string that represents an integer to an integer value. If it cannot be converted, 0 will be
     * returned.
     *
     * @param value
     *            The value to be converted
     * @return int - the converted value; 0 if conversion fails or the value is empty
     */
    public static int parseInt(String value)
    {
        int retval = 0; // default value if it cannot be converted

        if (isValued(value)) {
            try {
                retval = Integer.parseInt(value);
            }
            catch (Exception e) {
                logger.error("The value '" + value + "' could not be converted to an integer.", e);
            }
        }

        return retval;
    }

    /**
     * This method will process a CIDR block and create network components. The code was pulled from the old EC2Cloud class.
     *
     * @param cidrBlock
     *            The CIDR block to be processed
     * @return NetworkContext - object with network components computed from the CIDR block
     */
    public static NetworkContext parseCidrBlock(String cidrBlock)
    {
        NetworkContext retval = null;

        if (cidrBlock != null) {
            String[] parts = cidrBlock.split("/");
            String ip = parts[0];
            int prefix = 0;

            try {
                if (parts.length >= 2) {
                    prefix = Integer.parseInt(parts[1]);
                }

                int mask = 0xffffffff << 32 - prefix;
                byte[] bytes =
                        new byte[] { (byte) (mask >>> 24), (byte) (mask >> 16 & 0xff), (byte) (mask >> 8 & 0xff),
                                (byte) (mask & 0xff) };

                InetAddress netAddr = InetAddress.getByAddress(bytes);

                retval = new NetworkContext(cidrBlock, ip, netAddr != null ? netAddr.getHostAddress() : null, prefix, mask);

                if (logger.isTraceEnabled()) {
                    logger.trace("NetworkContext for CIDR " + cidrBlock + "\n"
                                 + retval.toString());
                }
            }
            catch (Exception e) {
                AzureUtil.logger.error("An exception occurred while processing CIDR block " + cidrBlock, e);
            }
        }
        return retval;
    }

    /**
     * This method will translate an array of strings into a single string with the values separated by the provided separator.
     *
     * @param array
     *            The array to be translated to string
     * @param separator
     *            The value to use between array values
     * @return String - string representation of the array. If the array is null or empty, an empty string is returned.
     */
    public static String arrayToString(String[] array, String separator)
    {
        StringBuilder sb = new StringBuilder();

        if (array != null && array.length > 0) {
            String sepString = AzureUtil.isValued(separator) ? separator : ",";
            String sep = "";

            for (String element : array) {
                sb.append(sep + element);
                sep = sepString;
            }
        }

        return sb.toString();
    }

    /**
     * This method will translate an array of strings into a single string with the values separated by a comma.
     *
     * @param array
     *            The array to be translated to string
     * @return String - string representation of the array. If the array is null or empty, an empty string is returned.
     */
    public static String arrayToString(String[] array)
    {
        return AzureUtil.arrayToString(array, ",");
    }

    /**
     * This method will return true if the object is not null and not empty.
     *
     * @param obj
     *            The object to check
     * @return boolean - true if the instance is not empty and not null
     */
    public static boolean isValued(Object obj)
    {
        boolean retval = false;

        if (obj != null) {
            if (obj instanceof String) {
                retval = !((String) obj).trim().isEmpty();
            }
            else if (obj instanceof StringBuilder) {
                retval = ((StringBuilder) obj).length() > 0;
            }
            else if (obj instanceof Collection) {
                retval = !((Collection<?>) obj).isEmpty();
            }
            else {
                retval = true;
            }
        }

        return retval;
    }

    /**
     * This method will return a "SharedKey" Microsoft Azure authorization string. The expectation is that any string that
     * requires URL encoding is already encoded before this method is called.
     *
     * @param stringToSign
     *            The input string that is to be signed
     * @param signingKey
     *            The key that is to be used for encryption. this will be the Microsoft Azure key associated with the
     *            entity.
     * @param account
     *            Microsoft Azure account name
     * @return String - encrypted Microsoft Azure "SharedKey" authorization string
     */
    public static String createSharedKeyAuthorization(String stringToSign, String signingKey, String account)
    {
        String result = null;

        if (AzureUtil.isValued(stringToSign) && AzureUtil.isValued(signingKey) && AzureUtil.isValued(account)) {
            try {
                Mac mac = Mac.getInstance(AzureConstants.SIGNING_ALGORITHM);

                mac.init(new SecretKeySpec(Base64.decodeBase64(signingKey), AzureConstants.SIGNING_ALGORITHM));

                String encodedString =
                        new String(Base64.encodeBase64(mac.doFinal(stringToSign.getBytes(AzureConstants.SIGNING_CHAR_SET))));

                result = "SharedKey " + account + ":" + encodedString;
            }
            catch (Exception e) {
                AzureUtil.logger.error("An exception occurred while creating the authorization signature.", e);
                throw new SignatureException(e);
            }
        }
        else {
            AzureUtil.logger
                    .error("A share key authorization calculation is missing one or more parameter values.  StringToSign["
                            + stringToSign + "] signingKey[" + AzureUtil.maskPrivateKey(signingKey) + "] account[" + account
                            + "].");
        }

        return result;
    }

    /**
     * Performs a reverse DNS lookup on an IP address
     *
     * @param ip
     *            The IP address to be queried
     * @return The fully qualified domain name or null if the lookup did not yield a FQDN
     */
    public static String ipToFqdn(String ip)
    {
        String fqdn = null;
        if (ip != null && !ip.isEmpty()) {
            try {
                InetAddress ia = InetAddress.getByName(ip);
                fqdn = ia.getCanonicalHostName();

                if (ip.equals(fqdn)) {
                    // There's no reverse DNS record for this IP
                    fqdn = null;
                }
            }
            catch (UnknownHostException uhe) {
            }
            catch (Exception e) {
                AzureUtil.logger.error("ipToFqdn exception: " + ip, e);
            }
        }
        return fqdn;
    }

    /**
     * Performs a DNS lookup for a qualified domain name
     *
     * @param domain
     *            The domain name to be queried
     * @return The IP addresses for the domain name
     */
    public static List<String> domainNameToIp(String domain)
    {
        List<String> ips = new ArrayList<String>();
        if (domain != null && !domain.isEmpty()) {
            try {
                InetAddress[] ias = InetAddress.getAllByName(domain);
                if (ias != null) {
                    for (InetAddress ia : ias) {
                        String address = ia.getHostAddress();
                        if (address != null && !address.isEmpty()) {
                            ips.add(address);
                        }
                    }
                }
            }
            catch (UnknownHostException uhe) {
            }
            catch (Exception e) {
                AzureUtil.logger.error("domainNameToIp exception: " + domain, e);
            }
        }
        return ips;
    }
}
