/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */

package com.servicemesh.agility.adapters.core.azure.impl;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.servicemesh.agility.adapters.core.azure.AzureEndpoint;
import com.servicemesh.agility.adapters.core.azure.exception.AzureAdapterException;
import com.servicemesh.agility.adapters.core.azure.exception.AzureErrorException;
import com.servicemesh.io.http.HttpUtil;
import com.servicemesh.io.http.IHttpResponse;

public class AzureEndpointImpl implements AzureEndpoint
{
    private static final Logger _logger = Logger.getLogger(AzureEndpointImpl.class);

    private final String _subscription;
    private final String _msVersion;
    private final MediaType _mediaType;
    private final Class<?> _msErrorClass;
    private final ClassLoader _msErrorLoader;
    private final JAXBContext _context;

    private static class Holder
    {
        private static HashMap<String, JAXBContext> contextMap = new HashMap<String, JAXBContext>();
        private static final Object lock = new Object();

        private static JAXBContext getContext(String contextPath, ClassLoader loader)
        {
            JAXBContext context;
            synchronized (Holder.lock) {
                context = Holder.contextMap.get(contextPath);
                if (context == null) {
                    context = Holder.createContext(contextPath, loader);
                    Holder.contextMap.put(contextPath, context);
                }
            }
            return context;
        }

        private static JAXBContext createContext(String contextPath, ClassLoader loader)
        {
            try {
                return JAXBContext.newInstance(contextPath, loader);
            }
            catch (Exception ex) {
                AzureEndpointImpl._logger.error("getContext: " + contextPath + ", exception=" + ex);
                return null;
            }
        }

        private static JAXBContext lookupContext(String contextPath)
        {
            JAXBContext context = null;
            synchronized (Holder.lock) {
                context = Holder.contextMap.get(contextPath);
            }
            return context;
        }

        private static void unregisterContext(String contextPath)
        {
            synchronized (Holder.lock) {
                Holder.contextMap.remove(contextPath);
            }
        }
    }

    public static JAXBContext lookupContext(String msContextPath)
    {
        return Holder.lookupContext(msContextPath);
    }

    public static void unregisterContext(String msContextPath)
    {
        Holder.unregisterContext(msContextPath);
    }

    public <E> AzureEndpointImpl(String subscription, String msVersion, String msContextPath, Class<E> msErrorClass, MediaType mediaType)
    {
        _subscription = subscription;
        _msVersion = msVersion;
        _mediaType = mediaType;
        _msErrorClass = msErrorClass;
        _msErrorLoader = msErrorClass.getClassLoader();
        _context = Holder.getContext(msContextPath, _msErrorLoader);

        if (_context == null) {
            throw new AzureAdapterException("No context for path=" + msContextPath + ", class=" + msErrorClass.getName());
        }
    }

    @Override
    public String getAddress()
    {
        return AzureEndpoint.DEFAULT_ADDRESS;
    }

    @Override
    public String getContentType()
    {
        return _mediaType.getValue();
    }

    @Override
    public String getSubscription()
    {
        return _subscription;
    }

    @Override
    public String getMsVersion()
    {
        return _msVersion;
    }

    @Override
    public JAXBContext getContext()
    {
        return _context;
    }

    @Override
    public JAXBContext getContext(String contextPath)
    {
        return Holder.getContext(contextPath, _msErrorLoader);
    }

    @Override
    public <T> T decode(IHttpResponse response, Class<T> responseClass)
    {
        return doDecode(response, responseClass, _context, _context);
    }

    @Override
    public <T> T decode(IHttpResponse response, String responseClassPath, Class<T> responseClass)
    {
        JAXBContext responseContext = getContext(responseClassPath);
        if (responseContext == null) {
            throw new AzureAdapterException("No context for response path: " + responseClassPath);
        }
        return doDecode(response, responseClass, responseContext, _context);
    }

    private <T> T doDecode(IHttpResponse response, Class<T> responseClass, JAXBContext responseContext, JAXBContext errorContext)
    {
        if (responseClass.isInstance(response)) {
            // We already have the return object
            return responseClass.cast(response);
        }
        StringBuilder err = new StringBuilder();
        String content = response.getContent();
        Object object = decodeContent(_mediaType, content, responseClass, responseContext, err);

        if (responseClass.isInstance(object)) {
            return responseClass.cast(object);
        }
        else {
            if (responseContext != errorContext) {
                object = decodeContent(_mediaType, content, _msErrorClass, errorContext, err);
            }
            if (_msErrorClass.isInstance(object)) {
                throw new AzureErrorException(object, content);
            }
        }
        throw new AzureAdapterException(err.toString());
    }

    private <T> Object decodeContent(MediaType mediaType, String content,
                                     Class<T> clazz, JAXBContext context,
                                     StringBuilder err)
    {
        Object object = null;
        try {
            if (MediaType.XML == mediaType) {
                object = HttpUtil.decodeObject(content, null, context);
            }
            else { // JSON decoding
                Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                    .create();
                object = gson.fromJson(content, clazz);
                if (object == null)
                    err.append("Unable to decode object");
            }
            if (_logger.isTraceEnabled()) {
                _logger.trace("decoded: " + content);
            }
        }
        catch (Exception ex) {
            err.append("Unable to decode object: " + ex);
        }
        return object;
    }

    @Override
    public String encode(Object obj)
    {
        return doEncode(obj, _context);
    }

    @Override
    public String encode(String objClassPath, Object obj)
    {
        JAXBContext objContext = getContext(objClassPath);
        if (objContext == null) {
            throw new AzureAdapterException("No context for object path: " + objClassPath);
        }
        return doEncode(obj, objContext);
    }

    private String doEncode(Object obj, JAXBContext objContext)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            Marshaller marshaller = objContext.createMarshaller();

            if (MediaType.XML == _mediaType) {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(obj, os);
            }
            else { // JSON encoding
                Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
                String json = gson.toJson(obj);
                os.write(json.getBytes(), 0, json.length());
            }
            if (_logger.isTraceEnabled()) {
                _logger.trace("encoded: " + os.toString());
            }
            return os.toString();
        }
        catch (Exception ex) {
            AzureEndpointImpl._logger.error("encode Exception: " + ex);
            throw new AzureAdapterException("Unable to encode object: " + ex);
        }
    }
}
