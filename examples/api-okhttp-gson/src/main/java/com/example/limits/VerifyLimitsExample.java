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
package com.example.limits;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.operations.LimitsApi;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerifyLimitsExample {

    Logger logger = LoggerFactory.getLogger(
            VerifyLimitsExample.class
    );
    
    String apiBaseUrl = ApiClientBuilder.DEFAULT_API_BASE_URL;
    
    //Please replace YOUR_CLIENT_ID with you own client id
    String apiClientId = "YOUR_CLIENT_ID";
    
    //Please replace YOUR_CLIENT_SECRET with you own client secret
    String apiClientSecret = "YOUR_CLIENT_SECRET";
    
    public void run() throws Exception {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );
        
        LimitsApi limitsApi = builder.getApi(
                LimitsApi.class
        );

        int desiredBrowsersConcurrency = 30; // 30 concurrent browsers
        int desiredBrowserCloudDuration = 1; // 1 hour duration

        try {
            limitsApi.verifyLimits(Map.of(
                    "CONCURRENT_BROWSER_CLOUDS", 1,
                    "CONCURRENT_BROWSERS", desiredBrowsersConcurrency,
                    "BROWSER_CLOUD_DURATION_HOURS", desiredBrowserCloudDuration
            ));
            logger.info(
                    "It is allowed to launch a new browser cloud with "
                    + "{} browsers concurrency and "
                    + "{} hours duration.",
                    desiredBrowsersConcurrency,
                    desiredBrowserCloudDuration
            );
        } catch (ApiException e) {
            logger.error(
                    "It is not allowed to launch a new browser cloud with "
                    + "{} browsers concurrency and "
                    + "{} hours duration",
                    desiredBrowsersConcurrency,
                    desiredBrowserCloudDuration,
                    e
            );
        }
    }

    public static void main(String[] args) throws Exception {
        new VerifyLimitsExample().run();
    }

}
