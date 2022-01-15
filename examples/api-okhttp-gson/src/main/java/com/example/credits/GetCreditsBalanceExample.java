package com.example.credits;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.CreditsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetCreditsBalanceExample {

    Logger logger = LoggerFactory.getLogger(
            GetCreditsBalanceExample.class
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

        CreditsApi creditsApi = builder.getApi(
                CreditsApi.class
        );

        CreditsBalance creditsBalance = creditsApi.getCreditsBalance();
        logger.info(
                "Credits balance: "
                + "available {} browser/hours, "
                + "utilized {} browser/hours during the past 30 days, "
                + "utilized {} browser/hours during the past 90 days, "
                + "utilized {} browser/hours overall.",
                creditsBalance.getAvailableCredits(),
                creditsBalance.getThirtyDaysUtilization(),
                creditsBalance.getNinetyDaysUtilization(),
                creditsBalance.getOverallUtilization()
        );
    }

    public static void main(String[] args) throws Exception {
        new GetCreditsBalanceExample().run();
    }

}
