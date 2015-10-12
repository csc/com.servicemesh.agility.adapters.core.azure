/**
 *              Copyright (c) 2008-2013 ServiceMesh, Incorporated; All Rights Reserved
 *              Copyright (c) 2013-Present Computer Sciences Corporation
 */
package com.servicemesh.agility.adapters.core.azure;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;

import com.servicemesh.agility.adapters.core.azure.exception.AzureAdapterException;
import com.servicemesh.agility.adapters.core.azure.exception.AzureErrorException;
import com.servicemesh.agility.api.Credential;
import com.servicemesh.core.async.Promise;
import com.servicemesh.io.http.HttpStatus;
import com.servicemesh.io.http.IHttpHeader;
import com.servicemesh.io.http.IHttpResponse;

public class TestHelpers
{
    private static final Logger _logger = Logger.getLogger(TestHelpers.class);
    private static final String _certFile = System.getProperty("azure_certificate");
    private static final String _certPasswd = System.getProperty("azure_certificate_password");
    private static final String _subscription = System.getProperty("azure_subscription");

    public static void initLogger(Level level)
    {
        ConsoleAppender console = new ConsoleAppender();
        console.setLayout(new PatternLayout("%d [%p|%c|%C{1}] %m%n"));
        console.setThreshold(level);
        console.activateOptions();

        Logger logger = Logger.getLogger(TestHelpers.class.getPackage().getName());
        logger.removeAllAppenders();
        logger.setLevel(level);
        logger.addAppender(console);
    }

    public static Logger setLogLevel(String loggerName, Level level)
    {
        Logger logger = Logger.getLogger(loggerName);
        logger.setLevel(level);
        return logger;
    }

    public static Credential getAzureCredential()
    {
        Credential cred = null;
        byte[] cert = null;
        StringBuilder sb = new StringBuilder();
        if ((_certFile == null) || _certFile.equals("${azure_certificate}")) {
            sb.append(" azure_certificate is not defined\n");
        }
        else {
            try {
                Path path = Paths.get(_certFile);
                cert = Files.readAllBytes(path);

                if (cert.length == 0)
                    sb.append("Empty certificate file " + _certFile);
            }
            catch (Exception e) {
                sb.append(" Exception reading certificate file " + _certFile +
                          ": " + e);
            }
        }
        if ((_certPasswd == null) || _certPasswd.equals("${azure_certificate_password}")) {
            sb.append(" azure_certificate_password is not defined\n");
        }
        if (sb.length() > 0) {
            System.out.println("No Azure Credentials:" + sb.toString());
        }
        if (sb.length() == 0) {
            cred = new Credential();
            cred.setCertificate(cert);
            cred.setPrivateKey(_certPasswd);
        }
        return cred;
    }

    public static String getAzureSubscription()
    {
        if ((_subscription == null) || _subscription.equals("${azure_subscription}")) {
            System.out.println("No Azure Subscription: azure_subscription is not defined");
        }
        return _subscription;
    }

    public static <T> T completePromise(Promise<T> promise, AzureEndpoint endpoint, boolean expectSuccess) throws Exception
    {
        T obj = null;
        StringBuilder err = new StringBuilder();
        try {
            // No Reactor so just wait for completion
            obj = promise.get();
            if (obj instanceof IHttpResponse) {
                IHttpResponse response = (IHttpResponse) obj;
                HttpStatus status = response.getStatus();
                String reason = null;
                if (status.getReason() != null) {
                    reason = ", reason=" + status.getReason();
                }
                int code = response.getStatusCode();
                if (_logger.isTraceEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("completePromise: IHttpResponse code=").append(code).append(reason)
                            .append(headersToString(response.getHeaders()));
                    _logger.trace(sb.toString());
                }
                if (expectSuccess && (code >= 400))
                    err.append("IHttpResponse code=").append(code).append("\n").append(response.getContent());
            }
            else {
                System.out.println("completePromise: obj=:\n" + endpoint.encode(obj));
            }
            if ((!expectSuccess) && (promise.isCompleted())) {
                err.append("completePromise: Expected failure");
            }
        }
        catch (AzureErrorException aex) {
            if (expectSuccess) {
                err.append("completePromise: AzureErrorException=" + aex.toString());
            }
            else {
                System.out.println("completePromise: Failed as expected: " + aex.toString());
            }
        }
        catch (AzureAdapterException aax) {
            if (expectSuccess) {
                err.append("completePromise: AzureAdapterException=" + aax.toString());
            }
            else {
                System.out.println("completePromise: Failed as expected: " + aax.toString());
            }
        }
        catch (Exception ex) {
            err.append("completePromise: Exception=" + ex);
        }
        catch (Throwable t) {
            err.append("completePromise: Throwable=" + t);
        }
        if (err.length() > 0) {
            if (_logger.isTraceEnabled()) {
                _logger.trace("completePromise err=" + err.toString());
            }
            Assert.fail(err.toString());
        }
        return obj;
    }

    public static String headersToString(List<IHttpHeader> headers)
    {
        StringBuilder sb = new StringBuilder();
        if (headers != null) {
            for (IHttpHeader header : headers) {
                sb.append("\n").append(header.getName()).append(":");

                String value = header.getValue();
                if ((value != null) && (!value.isEmpty()))
                    sb.append(value);
                else {
                    List<String> values = header.getValues();
                    boolean isFirst = true;
                    if (values != null) {
                        for (String v : values) {
                            if (isFirst) {
                                isFirst = false;
                                sb.append(v);
                            }
                            else
                                sb.append(",").append(v);
                        }
                    }
                }
            }
        }
        return sb.toString();
    }
}
