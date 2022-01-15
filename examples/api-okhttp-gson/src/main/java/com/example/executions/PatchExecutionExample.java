package com.example.executions;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.ExecutionsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatchExecutionExample {

    Logger logger = LoggerFactory.getLogger(
            PatchExecutionExample.class
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

        Execution execution = new Execution();
        execution.setNotes(
                "Optional, but important content related to execution details."
                + " Execution name should be preserved,"
                + " since it is omited during patch operation call."
        );
        execution = executionsApi.patchExecution(
                projectKey,
                executionKey,
                execution
        );

        logger.info(
                "Execution: "
                + "key={}, "
                + "name={}, "
                + "notes={}, "
                + "createdAt={}, "
                + "updatedAt={}, ",
                execution.getUuid(),
                execution.getName(),
                execution.getNotes(),
                execution.getCreatedAt(),
                execution.getUpdatedAt()
        );
    }

    public static void main(String[] args) throws Exception {
        new PatchExecutionExample().run();
    }

}
