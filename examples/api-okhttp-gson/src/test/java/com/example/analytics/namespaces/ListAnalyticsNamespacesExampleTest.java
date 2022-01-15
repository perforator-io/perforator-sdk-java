package com.example.analytics.namespaces;

import com.example.AbstractExampleTest;
import java.util.Map;

public class ListAnalyticsNamespacesExampleTest extends AbstractExampleTest<ListAnalyticsNamespacesExample>{

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid()
        );
    }
    
}
