package com.example.cloud_runners;

import com.example.AbstractExampleTest;
import java.util.Map;

public class ListCloudRunnersExampleTest extends AbstractExampleTest<ListCloudRunnersExample>{

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid()
        );
    }
    
}
