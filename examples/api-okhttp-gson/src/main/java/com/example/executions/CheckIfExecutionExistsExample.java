package com.example.executions;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.operations.ExecutionsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckIfExecutionExistsExample {

    Logger logger = LoggerFactory.getLogger(
            CheckIfExecutionExistsExample.class
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
    
    public void run() throws Exception {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );

        ExecutionsApi executionsApi = builder.getApi(
                ExecutionsApi.class
        );

        try {
            executionsApi.checkIfExecutionExists(projectKey, executionKey);
            logger.info(
                    "Execution {} exists within project {}",
                    executionKey,
                    projectKey
            );
        } catch (ApiException e) {
            logger.info(
                    "Execution {} doesn't exist within project {}",
                    executionKey,
                    projectKey
            );
        }
    }

    public static void main(String[] args) throws Exception {
        new CheckIfExecutionExistsExample().run();
    }

}
