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
import okhttp3.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

final class HttpClientsManagerImpl implements HttpClientsManager {

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        loadGeneratorContext.setOkHttpClient(
                buildHttpClient(
                        loadGeneratorContext.getLoadGeneratorConfig().getHttpConnectTimeout(),
                        loadGeneratorContext.getLoadGeneratorConfig().getHttpReadTimeout(),
                        loadGeneratorContext.getLoadGeneratorConfig().isHttpCacheDns(),
                        loadGeneratorContext.getSuiteConfigs().stream().mapToInt(SuiteConfig::getConcurrency).sum()
                )
        );
    }

    private OkHttpClient buildHttpClient(Duration connectTimeout, Duration readTimeout, boolean cacheDNS, int poolSize) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.protocols(List.of(Protocol.HTTP_1_1));
        builder.connectTimeout(connectTimeout);
        builder.readTimeout(readTimeout);

        if (cacheDNS) {
            builder.dns(CachedDns.INSTANCE);
        }

        builder.connectionPool(
                new ConnectionPool(
                        poolSize,
                        5,
                        TimeUnit.SECONDS
                )
        );

        builder.pingInterval(Duration.ofSeconds(5));
        builder.retryOnConnectionFailure(true);
        builder.followRedirects(true);
        builder.followSslRedirects(true);

        builder.addNetworkInterceptor(chain -> {
            Request request = chain.request();
            Response response = chain.proceed(request);
            return response.code() == 408
                    ? response.newBuilder().code(500).message("Server-Side Timeout").build()
                    : response;
        });

        return builder.build();
    }

    private static class CachedDns implements Dns {

        static final CachedDns INSTANCE = new CachedDns();

        private final ConcurrentHashMap<String, List<InetAddress>> cache = new ConcurrentHashMap<>();

        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            try {
                List<InetAddress> items = cache.computeIfAbsent(hostname, h -> {
                    try {
                        return Dns.SYSTEM.lookup(h);
                    } catch (UnknownHostException unknownHostException) {
                        throw new RuntimeException(unknownHostException);
                    }
                });
                
                return List.of(items.get((int)(Math.random() * items.size())));
            } catch (RuntimeException e) {
                if (e.getCause() != null && e.getCause() instanceof UnknownHostException) {
                    throw (UnknownHostException) e.getCause();
                } else {
                    throw new UnknownHostException("Can't determine IP address of " + hostname);
                }
            }
        }

    }
}
