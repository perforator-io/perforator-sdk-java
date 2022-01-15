package com.example.executions;

import com.example.AbstractExampleTest;
import java.util.Map;

public class CheckIfExecutionExistsExampleTest extends AbstractExampleTest<CheckIfExecutionExistsExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid()
        );
    }
    
}
