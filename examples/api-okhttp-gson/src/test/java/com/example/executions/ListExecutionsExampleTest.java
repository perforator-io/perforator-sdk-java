package com.example.executions;

import com.example.AbstractExampleTest;
import java.util.Map;

public class ListExecutionsExampleTest extends AbstractExampleTest<ListExecutionsExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() {
        return Map.of(PROJECT_KEY_FIELD, projectKey);
    }
    
}
