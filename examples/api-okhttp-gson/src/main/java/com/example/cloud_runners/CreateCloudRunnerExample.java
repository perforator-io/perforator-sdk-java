package com.example.cloud_runners;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.CloudRunnersApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateCloudRunnerExample {
    
    Logger logger = LoggerFactory.getLogger(
            CreateCloudRunnerExample.class
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

        CloudRunner cloudRunner = new CloudRunner();
        cloudRunner.setSshPublicKey("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAILhKzVzL8RYBFDZblagiF1fPKLiI9JRYw8CrU2lrCwVN user@example.com");
        cloudRunner.setHardwareType(CloudRunnerHardwareType.T3_SMALL.toString());

        cloudRunner = cloudRunnersApi.startCloudRunner(
                projectKey,
                executionKey,
                cloudRunner
        );

        logger.info(
                "Cloud Runner: "
                + "key={}, "
                + "hardwareType={}, "
                + "hardwareCpu={}, "
                + "hardwareMemory={}, "
                + "status={}, "
                + "createdAt={}, "
                + "updatedAt={}, ",
                cloudRunner.getUuid(),
                cloudRunner.getHardwareType(),
                cloudRunner.getHardwareCpu(),
                cloudRunner.getHardwareMemory(),
                cloudRunner.getStatus(),
                cloudRunner.getCreatedAt(),
                cloudRunner.getUpdatedAt()
        );
    }

    public static void main(String[] args) throws Exception {
        new CreateCloudRunnerExample().run();
    }

}
