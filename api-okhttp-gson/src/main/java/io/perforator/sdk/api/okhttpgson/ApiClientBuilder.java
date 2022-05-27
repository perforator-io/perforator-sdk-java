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
package io.perforator.sdk.api.okhttpgson;

import io.perforator.sdk.api.okhttpgson.invoker.ApiClient;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApiClientBuilder {
    
    public static String DEFAULT_API_BASE_URL = "https://api.perforator.io";
    public static Duration DEFAULT_HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    public static Duration DEFAULT_HTTP_READ_TIMEOUT = Duration.ofSeconds(30);
    
    private final ApiClientParams apiClientParams;
    private final ApiClient apiClient;
    private final Map<Class, Object> instances = new HashMap<>();

    public ApiClientBuilder(String clientId, String clientSecret) {
        this(clientId, clientSecret, DEFAULT_API_BASE_URL);
    }
    
    public ApiClientBuilder(String clientId, String clientSecret, String apiBaseUrl) {
        this(clientId, clientSecret, apiBaseUrl, DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
    }
    
    public ApiClientBuilder(String clientId, String clientSecret, String apiBaseUrl, Duration connectTimeout, Duration readTimeout) {
        this(
                ApiClientParams.builder()
                        .apiBaseUrl(apiBaseUrl)
                        .apiClientID(clientId)
                        .apiClientSecret(clientSecret)
                        .connectTimeout(connectTimeout)
                        .readTimeout(readTimeout)
                        .build()
        );
    }
    
    public ApiClientBuilder(ApiClientParams apiClientParams) {
        this.apiClientParams = apiClientParams;
        this.apiClient = new ApiClient(
                apiClientParams.getApiBaseUrl(),
                apiClientParams.getApiClientID(),
                apiClientParams.getApiClientSecret(),
                Collections.EMPTY_MAP
        );
        
        if(apiClientParams.getApiClientToken() != null && !apiClientParams.getApiClientToken().isBlank()) {
            this.apiClient.setAccessToken(apiClientParams.getApiClientToken());
        }
        
        this.apiClient.setConnectTimeout((int)apiClientParams.getConnectTimeout().toMillis());
        this.apiClient.setReadTimeout((int)apiClientParams.getReadTimeout().toMillis());
        this.apiClient.setUserAgent(apiClientParams.getUserAgent());
    }
    
    public <T> T getApi(Class<T> apiClass) {
        if (!apiClass.getName().startsWith("io.perforator.sdk.api.okhttpgson.operations")) {
            throw new IllegalArgumentException(
                    "apiClass is incorrect - it should be from io.perforator.sdk.api.okhttpgson.operations package"
            );
        }

        return (T) instances.computeIfAbsent(
                apiClass,
                c -> {
                    try {
                        return apiClass.getConstructor(ApiClient.class).newInstance(apiClient);
                    } catch (Exception e) {
                        throw new IllegalStateException(
                                "Can't instantiate new api instance => " + apiClass
                        );
                    }
                }
        );
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public ApiClientParams getApiClientParams() {
        return apiClientParams;
    }
    
}
