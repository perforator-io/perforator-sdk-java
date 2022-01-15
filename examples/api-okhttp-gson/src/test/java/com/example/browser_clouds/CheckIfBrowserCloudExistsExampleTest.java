package com.example.browser_clouds;

import com.example.AbstractExampleTest;
import java.util.Map;
import java.util.UUID;

public class CheckIfBrowserCloudExistsExampleTest extends AbstractExampleTest<CheckIfBrowserCloudExistsExample>{

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid(),
                BROWSER_CLOUD_KEY, UUID.randomUUID().toString()
        );
    }
    
}
