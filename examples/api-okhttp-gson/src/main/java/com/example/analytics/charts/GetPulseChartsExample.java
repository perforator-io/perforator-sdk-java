package com.example.analytics.charts;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetPulseChartsExample {

    Logger logger = LoggerFactory.getLogger(
            GetPulseChartsExample.class
    );

    String apiBaseUrl = ApiClientBuilder.DEFAULT_API_BASE_URL;

    //Please replace YOUR_CLIENT_ID with you own client id
    String apiClientId = "YOUR_CLIENT_ID";

    //Please replace YOUR_CLIENT_SECRET with you own client secret
    String apiClientSecret = "YOUR_CLIENT_SECRET";

    //Please replace YOUR_PROJECT_KEY with you own project key
    String projectKey = "YOUR_PROJECT_KEY";

    //Please replace YOUR_EXECUTION_KEY with you own execution key
    String executionKey = "YOUR_EXECUTION_KEY";

    public void run() throws Exception {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );

        AnalyticsApi analyticsApi = builder.getApi(
                AnalyticsApi.class
        );
        
        AnalyticsChartRequest pulseChartRequest = new AnalyticsChartRequest();
        pulseChartRequest.setNamespace(AnalyticsNamespace.PULSE.getValue());
        pulseChartRequest.setMetrics(List.of(
                PulseBasicMetrics.TOP_LEVEL_TRANSACTIONS_COUNT.getValue(),
                PulseBasicMetrics.SESSIONS_COUNT.getValue(),
                PulseBasicMetrics.PAGES_COUNT.getValue()
        ));
        
        AnalyticsChartsRequest chartsRequest = new AnalyticsChartsRequest();
        chartsRequest.setCharts(List.of(pulseChartRequest));
        chartsRequest.setGranularity(AnalyticsGranularity.THIRTY_SECONDS.getValue());
        
        List<AnalyticsChartResult> chartResults = analyticsApi.getCharts(
                projectKey, 
                executionKey, 
                chartsRequest
        );
        
        for (AnalyticsChartResult chartResult : chartResults) {
            logger.info(
                    "Chart for {}: timestamps={}, values={}",
                    chartResult.getNamespace(),
                    chartResult.getTimestamps(),
                    chartResult.getValues()
            );
        }
    }
    
    private static AnalyticsFilter filter(String field, String condition, String value) {
        AnalyticsFilter result = new AnalyticsFilter();
        
        result.setField(field);
        result.setCondition(condition);
        result.setValue(value);
        
        return result;
    }

    public static void main(String[] args) throws Exception {
        new GetPulseChartsExample().run();
    }

}
