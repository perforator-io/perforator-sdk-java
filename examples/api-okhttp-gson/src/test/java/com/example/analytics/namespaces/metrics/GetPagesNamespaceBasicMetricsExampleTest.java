package com.example.analytics.namespaces.metrics;

import com.example.AbstractExampleTest;
import java.util.Map;

public class GetPagesNamespaceBasicMetricsExampleTest extends AbstractExampleTest<GetPagesNamespaceBasicMetricsExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid()
        );
    }

}
