package com.example.cloud_runners;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.operations.CloudRunnersApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckIfCloudRunnerExistsExample {

    Logger logger = LoggerFactory.getLogger(
            CheckIfCloudRunnerExistsExample.class
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
    
    //Please replace YOUR_CLOUD_RUNNER_KEY with you own cloud runner key
    String cloudRunnerKey = "YOUR_CLOUD_RUNNER_KEY";
    
    public void run() {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );

        CloudRunnersApi cloudRunnersApi = builder.getApi(
                CloudRunnersApi.class
        );

        try {
            cloudRunnersApi.checkIfCloudRunnerExists(
                    projectKey,
                    executionKey,
                    cloudRunnerKey
            );
            logger.info(
                    "Cloud runner with the key {} exists.",
                    cloudRunnerKey
            );
        } catch (ApiException e) {
            logger.info(
                    "Cloud runner with the key {} doesn't exist",
                    cloudRunnerKey
            );
        }
    }

    public static void main(String[] args) throws Exception {
        new CheckIfCloudRunnerExistsExample().run();
    }

}
