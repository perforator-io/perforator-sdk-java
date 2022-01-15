package com.example.projects;

import java.util.Map;

public class UpdateProjectExampleTest extends AbstractProjectExampleTest<UpdateProjectExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() {
        return Map.of(PROJECT_KEY_FIELD, projectKey);
    }
    
}
