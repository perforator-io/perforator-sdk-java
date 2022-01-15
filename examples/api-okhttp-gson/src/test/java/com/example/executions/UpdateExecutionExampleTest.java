package com.example.executions;

import com.example.AbstractExampleTest;
import io.perforator.sdk.api.okhttpgson.model.*;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class UpdateExecutionExampleTest extends AbstractExampleTest<UpdateExecutionExample> {
    
    private Execution initialExecution;
    
    @BeforeAll
    public void setup() throws Exception {
        initialExecution = getExecution();
    }
    
    @AfterAll
    public void revert() throws Exception {
        executionsApi.updateExecution(
                projectKey, 
                initialExecution.getUuid(), 
                initialExecution
        );
    }

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, initialExecution.getUuid()
        );
    }
    
}
