package com.example.limits;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.model.PlatformLimit;
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
                    PlatformLimit.CONCURRENT_BROWSER_CLOUDS.getValue(), 1,
                    PlatformLimit.CONCURRENT_BROWSERS.getValue(), desiredBrowsersConcurrency,
                    PlatformLimit.BROWSER_CLOUD_DURATION_HOURS.getValue(), desiredBrowserCloudDuration
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
