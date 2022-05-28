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

import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.invoker.auth.OAuth;
import io.perforator.sdk.api.okhttpgson.model.CreditsBalance;
import io.perforator.sdk.api.okhttpgson.operations.CreditsApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiClientBuilderTest {
    protected static final String API_BASE_URL_PROPERTY = "LOADGENERATOR_APIBASEURL";
    protected static final String API_CLIENT_ID_PROPERTY = "LOADGENERATOR_APICLIENTID";
    protected static final String API_CLIENT_SECRET_PROPERTY = "LOADGENERATOR_APICLIENTSECRET";

    @Test
    public void verifyWithClientIdAndClientSecretParams() {
        createAndVerifyApiClientBuilder(ApiClientParams.builder()
                .apiClientID(getRequiredProperty(API_CLIENT_ID_PROPERTY))
                .apiClientSecret(getRequiredProperty(API_CLIENT_SECRET_PROPERTY))
                .apiBaseUrl(getRequiredProperty(API_BASE_URL_PROPERTY))
                .build()
        );
    }

    @Test
    public void verifyWithApiTokenParams() throws Exception {
        String apiToken = generateApiToken();
        createAndVerifyApiClientBuilder(ApiClientParams.builder()
                .apiClientToken(apiToken)
                .apiBaseUrl(getRequiredProperty(API_BASE_URL_PROPERTY))
                .build()
        );
    }

    private void createAndVerifyApiClientBuilder(ApiClientParams params) {
        ApiClientBuilder apiClientBuilder = new ApiClientBuilder(params);
        CreditsBalance balance = assertDoesNotThrow(
                () -> apiClientBuilder.getApi(CreditsApi.class).getCreditsBalance()
        );

        assertNotNull(balance);
    }

    private String generateApiToken() throws ApiException {
        ApiClientBuilder apiClientBuilder = new ApiClientBuilder(
                ApiClientParams.builder()
                        .apiClientID(getRequiredProperty(API_CLIENT_ID_PROPERTY))
                        .apiClientSecret(getRequiredProperty(API_CLIENT_SECRET_PROPERTY))
                        .apiBaseUrl(getRequiredProperty(API_BASE_URL_PROPERTY))
                        .build()
        );

        apiClientBuilder.getApi(CreditsApi.class).getCreditsBalance();
        return ((OAuth) apiClientBuilder.getApiClient().getAuthentication("OAuth")).getAccessToken();
    }

    protected String getRequiredProperty(String key) {
        String result = System.getProperty(key);
        if (result != null && !result.isBlank()) {
            return result.trim();
        }

        result = System.getenv(key);
        if (result != null && !result.isBlank()) {
            return result.trim();
        }

        throw new IllegalArgumentException("Required system/env property " + key + " is not defined");
    }
}