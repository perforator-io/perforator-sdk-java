package com.example.browser_clouds;

import com.example.AbstractExampleTest;
import java.util.Map;

public class ListBrowserCloudsExampleTest extends AbstractExampleTest<ListBrowserCloudsExample>{

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid()
        );
    }
    
}
