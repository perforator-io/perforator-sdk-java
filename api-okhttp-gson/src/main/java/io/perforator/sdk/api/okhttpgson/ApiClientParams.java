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

import java.time.Duration;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@Builder
@FieldNameConstants
public class ApiClientParams {
    
    @NonNull
    @Builder.Default
    private final String apiBaseUrl = ApiClientBuilder.DEFAULT_API_BASE_URL;
    
    private final String apiClientID;
    
    private final String apiClientSecret;
    
    private final String apiClientToken;
    
    @NonNull
    @Builder.Default
    private final Duration connectTimeout = ApiClientBuilder.DEFAULT_HTTP_CONNECT_TIMEOUT; 
    
    @NonNull
    @Builder.Default
    private final Duration readTimeout = ApiClientBuilder.DEFAULT_HTTP_READ_TIMEOUT; 
    
    @NonNull
    @Builder.Default
    private final String userAgent = new UserAgentProvider().getUserAgent();
    
}
