package com.example.projects;

import java.util.Map;

public class GetProjectExampleTest extends AbstractProjectExampleTest<GetProjectExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() {
        return Map.of(PROJECT_KEY_FIELD, projectKey);
    }
    
}
