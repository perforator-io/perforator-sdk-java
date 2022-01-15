package com.example.projects;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "CreateProjectExampleTest.enabled", matches = "true")
public class CreateProjectExampleTest extends AbstractProjectExampleTest<CreateProjectExample>{

    @Override
    protected Map<String, Object> getAdditionalFields() {
        return Collections.EMPTY_MAP;
    }
    
}
