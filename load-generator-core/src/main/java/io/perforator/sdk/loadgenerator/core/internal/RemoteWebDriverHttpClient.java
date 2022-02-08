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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import lombok.SneakyThrows;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import static io.perforator.sdk.loadgenerator.core.Threaded.sleep;

final class RemoteWebDriverHttpClient implements HttpClient {

    private final SuiteContextImpl suiteContext;
    private final AsyncHttpClient client;
    private final URL baseUrl;
    private final String userAgent;

    public RemoteWebDriverHttpClient(SuiteContextImpl suiteContext) {
        this.suiteContext = suiteContext;
        this.client = suiteContext.getLoadGeneratorContext().getAsyncHttpClient();
        this.baseUrl = suiteContext.getLoadGeneratorContext().getBrowserCloudContext().getSeleniumHubURL();
        this.userAgent = ApiClientBuilder.DEFAULT_USER_AGENT;
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
        HttpResponse response;

        int networkExceptionCount = 0;
        int toManyRequestsCounter = 0;
        long maxRetryTime = System.currentTimeMillis() + suiteContext.getSuiteConfig().getWebDriverCreateSessionRetryTimeout().toMillis();
        while (true) {
            try {
                response = executeInternal(request);

                if (response.getStatus() == 200) {
                    return response;
                }

                if (response.getStatus() == 429) {
                    String retryAfter = response.getHeader("Retry-After");
                    if (retryAfter == null || retryAfter.isEmpty()) {
                        retryAfter = "1";
                    }

                    sleep(Integer.parseInt(retryAfter) * 1000L);
                    toManyRequestsCounter++;
                }

                if (maxRetryTime < System.currentTimeMillis()
                        && toManyRequestsCounter >= suiteContext.getSuiteConfig().getWebDriverCreateSessionRetryMinAttempts()) {
                    return response;
                }
            } catch (IOException e) {
                networkExceptionCount++;

                if (networkExceptionCount > suiteContext.getSuiteConfig().getWebDriverCreateSessionRetryMinAttempts()) {
                    throw e;
                } else {
                    sleep(1000L * networkExceptionCount);
                }
            }
        }
    }

    private HttpResponse processTerminateSessionRequest(HttpRequest request) throws IOException {
        long maxRetryTime = System.currentTimeMillis() + suiteContext.getSuiteConfig().getWebDriverDeleteSessionRetryTimeout().toMillis();
        int networkExceptionCount = 0;
        while (true) {
            try {
                HttpResponse response = executeInternal(request);
                if (response.getStatus() == 200) {
                    return response;
                }

                if (response.getStatus() == 404) {
                    response.setStatus(200);
                    return response;
                }

                if (maxRetryTime < System.currentTimeMillis()) {
                    return response;
                }
                sleep(1000);
            } catch (IOException e) {
                networkExceptionCount++;
                if (networkExceptionCount > suiteContext.getSuiteConfig().getWebDriverDeleteSessionRetryMinAttempts()) {
                    throw e;
                } else {
                    sleep(1000L * networkExceptionCount);
                }
            }
        }
    }

    @SneakyThrows
    private HttpResponse executeInternal(HttpRequest request) throws IOException {
        RequestBuilder builder = new RequestBuilder(request.getMethod().name());

        String rawUrl = getRawUrl(baseUrl.toURI(), request.getUri());
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

        return toSeleniumResponse(
                client.executeRequest(builder).toCompletableFuture().join()
        );
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

    private static String getRawUrl(URI baseUrl, String uri) {
        String rawUrl;
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            rawUrl = uri;
        } else {
            rawUrl = baseUrl.toString().replaceAll("/$", "") + uri;
        }
        return rawUrl;
    }

}
