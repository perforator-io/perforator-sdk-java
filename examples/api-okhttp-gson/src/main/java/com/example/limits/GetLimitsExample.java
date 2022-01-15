package com.example.limits;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.operations.LimitsApi;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetLimitsExample {

    Logger logger = LoggerFactory.getLogger(
            GetLimitsExample.class
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

        Map<String, Integer> limits = limitsApi.getLimits();
        logger.info(
                "There {} limits applied on your account",
                limits.size()
        );

        for (String limitKey : limits.keySet()) {
            logger.info(
                    "Limit {} = {}",
                    limitKey,
                    limits.get(limitKey)
            );
        }
    }

    public static void main(String[] args) throws Exception {
        new GetLimitsExample().run();
    }

}
