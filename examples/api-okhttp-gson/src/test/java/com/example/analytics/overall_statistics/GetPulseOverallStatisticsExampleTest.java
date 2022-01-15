package com.example.analytics.overall_statistics;

import com.example.AbstractExampleTest;
import java.util.Map;

public class GetPulseOverallStatisticsExampleTest extends AbstractExampleTest<GetPulseOverallStatisticsExample> {

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, getExecution().getUuid()
        );
    }

}
