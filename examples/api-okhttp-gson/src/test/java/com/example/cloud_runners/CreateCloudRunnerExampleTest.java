package com.example.cloud_runners;

import com.example.AbstractExampleTest;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.model.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class CreateCloudRunnerExampleTest extends AbstractExampleTest<CreateCloudRunnerExample>{
    
    private Execution execution;
    
    @BeforeAll
    public void setup() throws Exception {
        execution = executionsApi.createExecution(
                projectKey, 
                new Execution()
        );
    }
    
    @AfterAll
    public void revert() throws Exception {
        List<CloudRunner> cloudRunners = cloudRunnersApi.listCloudRunners(
                projectKey, 
                execution.getUuid()
        );
        
        for (CloudRunner cloudRunner : cloudRunners) {
            try {
                cloudRunnersApi.terminateCloudRunner(
                        projectKey,
                        execution.getUuid(),
                        cloudRunner.getUuid()
                );
            } catch (ApiException e) {
                //ignore
            }
        }
    }

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, execution.getUuid()
        );
    }
    
}
