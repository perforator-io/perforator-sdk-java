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
import io.perforator.sdk.api.okhttpgson.ApiClientParams;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.operations.*;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;

final class ApiClientManagerImpl implements ApiClientManager {

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        LoadGeneratorConfig loadGeneratorConfig = loadGeneratorContext.getLoadGeneratorConfig();

        validate(loadGeneratorConfig);

        String apiClientId = loadGeneratorConfig.getApiClientId();
        String apiClientSecret = loadGeneratorConfig.getApiClientSecret();
        String apiToken = loadGeneratorConfig.getApiToken();
        String apiBaseUrl = loadGeneratorConfig.getApiBaseUrl();
        Duration httpConnectTimeout = loadGeneratorConfig.getHttpConnectTimeout();
        Duration httpReadTimeout = loadGeneratorConfig.getHttpReadTimeout();

        ApiClientBuilder apiClientBuilder = new ApiClientBuilder(
                ApiClientParams.builder()
                        .apiBaseUrl(apiBaseUrl)
                        .apiClientID(apiClientId)
                        .apiClientSecret(apiClientSecret)
                        .apiClientToken(apiToken)
                        .connectTimeout(httpConnectTimeout)
                        .readTimeout(httpReadTimeout)
                        .build()
        );
        
        loadGeneratorContext.setLimitsApi(
                apiClientBuilder.getApi(LimitsApi.class)
        );
        loadGeneratorContext.setProjectsApi(
                apiClientBuilder.getApi(ProjectsApi.class)
        );
        loadGeneratorContext.setExecutionsApi(
                apiClientBuilder.getApi(ExecutionsApi.class)
        );
        loadGeneratorContext.setBrowserCloudsApi(
                apiClientBuilder.getApi(BrowserCloudsApi.class)
        );
        loadGeneratorContext.setApiClientParams(
                apiClientBuilder.getApiClientParams()
        );

        try {
            loadGeneratorContext.getLimitsApi().getLimits();
        } catch (ApiException e) {
            OAuthProblemException oAuthProblemException = getOAuthProblemException(e);

            if(oAuthProblemException != null) {
                throw new RuntimeException(
                        generateAuthenticationErrorMessage(
                                loadGeneratorConfig,
                                oAuthProblemException
                        )
                );
            } else {
                throw new RuntimeException(
                        "Problem authenticating API client",
                        e
                );
            }
        }
    }

    private String generateAuthenticationErrorMessage(LoadGeneratorConfig loadGeneratorConfig, OAuthProblemException oAuthProblemException) {
        StringBuilder result = new StringBuilder();

        result.append("Unable to authenticate API client at ");
        result.append(loadGeneratorConfig.getApiBaseUrl());
        result.append("/oauth/token");
        result.append(" ; Please verify ");
        result.append(LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX).append(".").append(LoadGeneratorConfig.Fields.apiClientId);
        result.append(" and ");
        result.append(LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX).append(".").append(LoadGeneratorConfig.Fields.apiClientSecret);

        if(oAuthProblemException != null) {
            if(oAuthProblemException.getResponseStatus() > 0) {
                result.append(
                        " ; Response code = "
                ).append(
                        oAuthProblemException.getResponseStatus()
                );
            }

            if(oAuthProblemException.getError() != null && !oAuthProblemException.getError().isBlank()) {
                result.append(
                        " ; Error = "
                ).append(
                        oAuthProblemException.getError()
                );
            }

            if(oAuthProblemException.getDescription()!= null && !oAuthProblemException.getDescription().isBlank()) {
                result.append(
                        " ; Description = "
                ).append(
                        oAuthProblemException.getDescription()
                );
            }
        }

        return result.toString();
    }

    private OAuthProblemException getOAuthProblemException(Throwable exception) {
        if(exception == null) {
            return null;
        }

        if(exception instanceof OAuthProblemException) {
            return (OAuthProblemException)exception;
        }

        return getOAuthProblemException(exception.getCause());
    }

    private void validate(LoadGeneratorConfig loadGeneratorConfig) {
        if (loadGeneratorConfig == null) {
            throw new IllegalArgumentException("loadGeneratorConfig is required");
        }

        if (loadGeneratorConfig.getApiBaseUrl() == null || loadGeneratorConfig.getApiBaseUrl().isBlank()) {
            throw new IllegalArgumentException(
                    LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + LoadGeneratorConfig.Fields.apiBaseUrl
                            + " should not be blank"
            );
        } else {
            URL url;
            try {
                url = URI.create(loadGeneratorConfig.getApiBaseUrl()).toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(
                        LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX
                                + "."
                                + LoadGeneratorConfig.Fields.apiBaseUrl
                                + "("
                                + loadGeneratorConfig.getApiBaseUrl()
                                + ") has incorrect format",
                        e
                );
            }

            if (url.getPath() != null && url.getPath().length() > 0) {
                throw new IllegalArgumentException(
                        LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX
                                + "."
                                + LoadGeneratorConfig.Fields.apiBaseUrl
                                + "("
                                + url
                                + ") should not have path fragments. Expected format: "
                                + LoadGeneratorConfig.DEFAULT_API_BASE_URL
                );
            }
        }

        if (loadGeneratorConfig.getApiToken() != null) {
            if(loadGeneratorConfig.getApiToken().isBlank()) {
                throw new IllegalArgumentException(
                        LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX
                                + "."
                                + LoadGeneratorConfig.Fields.apiToken
                                + " should not be blank"
                );
            }
        } else {
            if (loadGeneratorConfig.getApiClientId() == null || loadGeneratorConfig.getApiClientId().isBlank()) {
                throw new IllegalArgumentException(
                        LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX
                                + "."
                                + LoadGeneratorConfig.Fields.apiClientId
                                + " should not be blank"
                );
            }

            if (loadGeneratorConfig.getApiClientSecret() == null || loadGeneratorConfig.getApiClientSecret().isBlank()) {
                throw new IllegalArgumentException(
                        LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX
                                + "."
                                + LoadGeneratorConfig.Fields.apiClientSecret
                                + " should not be blank"
                );
            }
        }
    }

}
