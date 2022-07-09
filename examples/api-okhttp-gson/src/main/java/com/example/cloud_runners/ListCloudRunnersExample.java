package com.example.cloud_runners;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.CloudRunnersApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListCloudRunnersExample {

    Logger logger = LoggerFactory.getLogger(
            ListCloudRunnersExample.class
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

        CloudRunnersApi cloudRunnersApi = builder.getApi(
                CloudRunnersApi.class
        );

        List<CloudRunner> cloudRunners = cloudRunnersApi.listCloudRunners(
                projectKey,
                executionKey
        );
        
        if(cloudRunners == null || cloudRunners.isEmpty()) {
            logger.info(
                    "There are no cloud runners for the execution {}",
                    executionKey
            );
            return;
        }
        
        logger.info(
                "There are {} cloud runners for the execution {}",
                cloudRunners.size(),
                executionKey
        );

        for (CloudRunner cloudRunner : cloudRunners) {
            logger.info(
                    "Cloud Runner: "
                    + "key={}, "
                    + "hardwareType={}, "
                    + "hardwareCpu={}, "
                    + "hardwareMemory={}, "
                    + "status={}, "
                    + "createdAt={}, "
                    + "updatedAt={}, ",
                    cloudRunner.getHardwareType(),
                    cloudRunner.getHardwareCpu(),
                    cloudRunner.getHardwareMemory(),
                    cloudRunner.getStatus(),
                    cloudRunner.getCreatedAt(),
                    cloudRunner.getUpdatedAt()
            );
        }
    }

    public static void main(String[] args) throws Exception {
        new ListCloudRunnersExample().run();
    }

}
