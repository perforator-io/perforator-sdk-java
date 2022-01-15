package com.example.executions;

import com.example.AbstractExampleTest;
import java.util.Map;

public class CreateExecutionExampleTest extends AbstractExampleTest<CreateExecutionExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey
        );
    }
    
}
