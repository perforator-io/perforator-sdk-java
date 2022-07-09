package com.example.cloud_runners;

import com.example.AbstractExampleTest;
import java.util.Map;
import java.util.UUID;

public class CheckIfCloudRunnerExistsExampleTest extends AbstractExampleTest<CheckIfCloudRunnerExistsExample>{

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid(),
                CLOUD_RUNNER_KEY, UUID.randomUUID().toString()
        );
    }
    
}
