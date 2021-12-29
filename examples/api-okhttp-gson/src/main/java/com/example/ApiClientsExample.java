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
package com.example;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import io.perforator.sdk.api.okhttpgson.operations.BrowserCloudsApi;
import io.perforator.sdk.api.okhttpgson.operations.CreditsApi;
import io.perforator.sdk.api.okhttpgson.operations.ExecutionsApi;
import io.perforator.sdk.api.okhttpgson.operations.LimitsApi;
import io.perforator.sdk.api.okhttpgson.operations.ProjectsApi;

public class ApiClientsExample {
    
    String apiBaseUrl = ApiClientBuilder.DEFAULT_API_BASE_URL;
    
    //Please replace YOUR_CLIENT_ID with you own client id
    String apiClientId = "YOUR_CLIENT_ID";
    
    //Please replace YOUR_CLIENT_SECRET with you own client secret
    String apiClientSecret = "YOUR_CLIENT_SECRET";
    
    public void run() {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );

        ProjectsApi projectsApi = builder.getApi(
                ProjectsApi.class
        );

        ExecutionsApi executionsApi = builder.getApi(
                ExecutionsApi.class
        );

        BrowserCloudsApi browserCloudsApi = builder.getApi(
                BrowserCloudsApi.class
        );

        AnalyticsApi analyticsApi = builder.getApi(
                AnalyticsApi.class
        );

        CreditsApi creditsApi = builder.getApi(
                CreditsApi.class
        );

        LimitsApi limitsApi = builder.getApi(
                LimitsApi.class
        );
    }
    
    public static void main(String[] args) throws Exception {
        new ApiClientsExample().run();
    }

}
