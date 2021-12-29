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
package com.example.analytics.namespaces.metrics;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetPulseNamespaceMetricsExample {

    Logger logger = LoggerFactory.getLogger(
            GetPulseNamespaceMetricsExample.class
    );

    String apiBaseUrl = ApiClientBuilder.DEFAULT_API_BASE_URL;

    //Please replace YOUR_CLIENT_ID with you own client id
    String apiClientId = "YOUR_CLIENT_ID";

    //Please replace YOUR_CLIENT_SECRET with you own client secret
    String apiClientSecret = "YOUR_CLIENT_SECRET";

    //Please replace YOUR_PROJECT_KEY with you own project key
    String projectKey = "YOUR_PROJECT_KEY";

    //Please replace YOUR_EXECUTION_KEY with you own execution key
    String executionKey = "YOUR_EXECUTION_KEY";
    
    int maxMetricsToLog = 256;

    public void run() throws Exception {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );

        AnalyticsApi analyticsApi = builder.getApi(
                AnalyticsApi.class
        );

        List<AnalyticsMetricInfo> metrics = analyticsApi.getNamespaceMetrics(
                projectKey,
                executionKey,
                AnalyticsNamespace.PULSE.getValue(),
                false
        );
        
        logger.info(
                "There are {} metrics in pulse namespace => {}, ... , displaying only the first {} metrics", 
                metrics.size(),
                metrics.stream()
                        .limit(maxMetricsToLog)
                        .map(m -> m.getName() + ":" + m.getType())
                        .collect(Collectors.joining(" , ")),
                maxMetricsToLog
        );
    }

    public static void main(String[] args) throws Exception {
        new GetPulseNamespaceMetricsExample().run();
    }

}
