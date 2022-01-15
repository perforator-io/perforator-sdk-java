package com.example.projects;

import java.util.Map;

public class PatchProjectExampleTest extends AbstractProjectExampleTest<PatchProjectExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() {
        return Map.of(PROJECT_KEY_FIELD, projectKey);
    }
    
}
