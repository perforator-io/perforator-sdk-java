package com.example.analytics.namespaces.fields;

import com.example.AbstractExampleTest;
import java.util.Map;

public class GetRequestsNamespaceFieldsExampleTest extends AbstractExampleTest<GetRequestsNamespaceFieldsExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid()
        );
    }

}
