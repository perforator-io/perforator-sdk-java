/*
 * Copyright Perforator, Inc. and contributors. All rights reserved.
 *
 * Use of this software is governed by the Business Source License
 * included in the LICENSE file.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0.
 */
package io.perforator.sdk.loadgenerator.core.internal;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.loadgenerator.core.AbstractLoadGenerator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import static io.perforator.sdk.loadgenerator.core.Threaded.sleep;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RemoteWebDriverHttpClient implements HttpClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteWebDriverHttpClient.class);
    
    private static final long DEFAULT_RETRY_DELAY = 1000l;
    private static final long MIN_RETRY_DELAY = 250l;
    private static final long MAX_RETRY_DELAY = 5000l;

    private final SuiteInstanceContextImpl suiteInstanceContext;
    private final AsyncHttpClient client;
    private final URL baseUrl;
    private final String userAgent;
    private final long createSessionRetryTimeout;
    private final long deleteSessionRetryTimeout;

    public RemoteWebDriverHttpClient(SuiteInstanceContextImpl suiteInstanceContext) {
        this.suiteInstanceContext = suiteInstanceContext;
        this.client = suiteInstanceContext.getLoadGeneratorContext().getAsyncHttpClient();
        this.baseUrl = suiteInstanceContext.getLoadGeneratorContext().getBrowserCloudContext().getSeleniumHubURL();
        this.userAgent = ApiClientBuilder.DEFAULT_USER_AGENT;
        this.createSessionRetryTimeout = suiteInstanceContext.getSuiteConfigContext().getSuiteConfig().getWebDriverCreateSessionRetryTimeout().toMillis();
        this.deleteSessionRetryTimeout = suiteInstanceContext.getSuiteConfigContext().getSuiteConfig().getWebDriverDeleteSessionRetryTimeout().toMillis();
    }

    @Override
    public HttpResponse execute(HttpRequest request) throws IOException {
        if (isNewSessionRequest(request)) {
            return processNewSessionRequest(request);
        } else if (isTerminateSessionRequest(request)) {
            return processTerminateSessionRequest(request);
        } else {
            return executeInternal(request);
        }
    }

    private boolean isNewSessionRequest(HttpRequest request) {
        return request.getMethod() == HttpMethod.POST && request.getUri().endsWith("/session");
    }

    private boolean isTerminateSessionRequest(HttpRequest request) {
        if (request.getMethod() != HttpMethod.DELETE) {
            return false;
        }

        String[] segments = request.getUri().split("/");
        if (segments.length < 2) {
            return false;
        }

        return "session".equals(segments[segments.length - 2]);
    }

    private HttpResponse processNewSessionRequest(HttpRequest request) throws IOException {
        long maxRetryTime = System.currentTimeMillis() + createSessionRetryTimeout;
        
        while (true) {
            try {
                HttpResponse response = executeInternal(request);

                if (response.getStatus() < 400) {
                    return response;
                }
                
                if(System.currentTimeMillis() > maxRetryTime) {
                    return response;
                }
                
                long retryDelay = computeDelayFromResponse(response);
                LOGGER.debug(
                        "Unexpected response code {} received while creating session, retrying in {}ms",
                        response.getStatus(),
                        retryDelay
                );
                
                sleep(retryDelay);
            } catch (IOException e) {
                if(System.currentTimeMillis() > maxRetryTime) {
                    throw e;
                } else {
                    if(LOGGER.isDebugEnabled()) {
                        LOGGER.error(
                                "IOException happened while creating session, retrying in {}ms",
                                DEFAULT_RETRY_DELAY,
                                e
                        );
                    }
                    sleep(DEFAULT_RETRY_DELAY);
                }
            }
        }
    }

    private HttpResponse processTerminateSessionRequest(HttpRequest request) throws IOException {
        long maxRetryTime = System.currentTimeMillis() + deleteSessionRetryTimeout;
        
        while (true) {
            try {
                HttpResponse response = executeInternal(request);
                if (response.getStatus() < 400) {
                    return response;
                }

                if (response.getStatus() == 404) {
                    response.setStatus(200);
                    return response;
                }
                
                if(System.currentTimeMillis() > maxRetryTime) {
                    return response;
                }
                
                long retryDelay = computeDelayFromResponse(response);
                LOGGER.debug(
                        "Unexpected response code {} received while terminating session, retrying in {}ms",
                        response.getStatus(),
                        retryDelay
                );
                sleep(retryDelay);
            } catch (IOException e) {
                if(System.currentTimeMillis() > maxRetryTime) {
                    throw e;
                } else {
                    if(LOGGER.isDebugEnabled()) {
                        LOGGER.error(
                                "IOException happened while terminating session, retrying in {}ms",
                                DEFAULT_RETRY_DELAY,
                                e
                        );
                    }
                    sleep(DEFAULT_RETRY_DELAY);
                }
            }
        }
    }
    
    private long computeDelayFromResponse(HttpResponse response) {
        if(response.getStatus() != 429) {
            return DEFAULT_RETRY_DELAY;
        }
        
        String retryAfter = response.getHeader("Retry-After");
        if(retryAfter == null || retryAfter.isBlank()) {
            return DEFAULT_RETRY_DELAY;
        }
        
        long delayFromHeader;
        try {
            delayFromHeader = Long.parseLong(retryAfter) * 1000l;
        } catch(NumberFormatException e) {
            return DEFAULT_RETRY_DELAY;
        }
        
        if(delayFromHeader > MAX_RETRY_DELAY) {
            return MAX_RETRY_DELAY;
        }
        
        if(delayFromHeader < MIN_RETRY_DELAY) {
            return MIN_RETRY_DELAY;
        }
        
        return delayFromHeader;
    }
    
    private HttpResponse executeInternal(HttpRequest request) throws IOException {
        RequestBuilder builder = new RequestBuilder(request.getMethod().name());

        String rawUrl = getRawUrl(baseUrl, request.getUri());
        builder.setUrl(rawUrl);

        for (String name : request.getQueryParameterNames()) {
            for (String value : request.getQueryParameters(name)) {
                builder.addQueryParam(name, value);
            }
        }

        // Netty tends to timeout when a GET request has a 'Content-Length' header
        if (request.getMethod().equals(HttpMethod.GET) && request.getHeader("Content-Length") != null) {
            request.removeHeader("Content-Length");
        }

        for (String name : request.getHeaderNames()) {
            for (String value : request.getHeaders(name)) {
                builder.addHeader(name, value);
            }
        }
        if (request.getHeader("User-Agent") == null) {
            builder.addHeader("User-Agent", userAgent);
        }

        if (request.getMethod().equals(HttpMethod.POST)) {
            builder.setBody(request.getContentStream());
        }
        
        try {
            return toSeleniumResponse(
                    client.executeRequest(builder).toCompletableFuture().get()
            );
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    AbstractLoadGenerator.TERMINATION_EXCEPTION_MESSAGE
            );
        } catch(ExecutionException e) {
            if(e.getCause() == null) {
                throw new RuntimeException(
                        "Can't execute selenium request " + request.getMethod() + " => " + rawUrl, 
                        e
                );
            } else if(e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new RuntimeException(
                        "Can't execute selenium request " + request.getMethod() + " => " + rawUrl, 
                        e.getCause()
                );
            }
        }
    }

    private static HttpResponse toSeleniumResponse(org.asynchttpclient.Response response) {
        HttpResponse toReturn = new HttpResponse();

        toReturn.setStatus(response.getStatusCode());
        toReturn.setContent(!response.hasResponseBody()
                ? new ByteArrayInputStream(new byte[0])
                : response.getResponseBodyAsStream());

        for (String name : response.getHeaders().names()) {
            for (String value : response.getHeaders(name)) {
                toReturn.addHeader(name, value);
            }
        }

        return toReturn;
    }

    private static String getRawUrl(URL baseUrl, String uri) {
        String rawUrl;
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            rawUrl = uri;
        } else {
            rawUrl = baseUrl.toString().replaceAll("/$", "") + uri;
        }
        return rawUrl;
    }

}
