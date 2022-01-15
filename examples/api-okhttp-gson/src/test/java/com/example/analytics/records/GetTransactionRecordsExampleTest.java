package com.example.analytics.records;

import com.example.AbstractExampleTest;
import java.util.Map;

public class GetTransactionRecordsExampleTest extends AbstractExampleTest<GetTransactionRecordsExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid()
        );
    }

}
