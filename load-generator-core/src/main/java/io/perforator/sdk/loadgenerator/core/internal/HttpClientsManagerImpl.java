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

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.ResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.time.Duration;

final class HttpClientsManagerImpl implements HttpClientsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientsManagerImpl.class);

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        loadGeneratorContext.setAsyncHttpClient(
                buildAsyncHttpClient(
                        loadGeneratorContext.getLoadGeneratorConfig().getHttpConnectTimeout(),
                        loadGeneratorContext.getLoadGeneratorConfig().getHttpReadTimeout(),
                        loadGeneratorContext.getLoadGeneratorConfig().isHttpCacheDns(),
                        loadGeneratorContext.getSuiteConfigs().stream().mapToInt(SuiteConfig::getConcurrency).sum()
                )
        );
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        AsyncHttpClient client = loadGeneratorContext.getAsyncHttpClient();
        if (client != null && !client.isClosed()) {
            try {
                client.close();
            } catch (IOException e) {
                LOGGER.error("Can't close http client", e);
            }
        }
    }

    private AsyncHttpClient buildAsyncHttpClient(Duration connectTimeout, Duration readTimeout, boolean cacheDNS, int poolSize) {
        return Dsl.asyncHttpClient(
                Dsl.config()
                        .setConnectTimeout((int) connectTimeout.toMillis())
                        .setHandshakeTimeout((int) connectTimeout.toMillis())
                        .setReadTimeout((int) readTimeout.toMillis())
                        .setPooledConnectionIdleTimeout(15000)
                        .setMaxConnections(poolSize)
                        .setMaxConnectionsPerHost(poolSize)
                        .setMaxRequestRetry(2)
                        .addResponseFilter(new ResponseFilter() {
                            @Override
                            public <T> FilterContext<T> filter(FilterContext<T> ctx) {
                                int statusCode = ctx.getResponseStatus().getStatusCode();
                                if (statusCode == 408) {
                                    return new FilterContext.FilterContextBuilder<>(ctx)
                                            .responseStatus(new CustomHttpResponseStatus(
                                                    ctx.getResponseStatus(),
                                                    500,
                                                    "Server-Side Timeout"
                                            ))
                                            .build();
                                }
                                return ctx;
                            }
                        })
                        .build()
        );
    }

    private static class CustomHttpResponseStatus extends HttpResponseStatus {

        private final HttpResponseStatus status;
        private final int code;
        private final String statusText;

        public CustomHttpResponseStatus(HttpResponseStatus status, int code, String statusText) {
            super(status.getUri());
            this.status = status;
            this.code = code;
            this.statusText = statusText;
        }

        @Override
        public int getStatusCode() {
            return this.code;
        }

        @Override
        public String getStatusText() {
            return this.statusText;
        }

        @Override
        public String getProtocolName() {
            return this.status.getProtocolName();
        }

        @Override
        public int getProtocolMajorVersion() {
            return this.status.getProtocolMajorVersion();
        }

        @Override
        public int getProtocolMinorVersion() {
            return this.status.getProtocolMinorVersion();
        }

        @Override
        public String getProtocolText() {
            return this.status.getProtocolText();
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return this.status.getRemoteAddress();
        }

        @Override
        public SocketAddress getLocalAddress() {
            return this.status.getLocalAddress();
        }
    }
}
