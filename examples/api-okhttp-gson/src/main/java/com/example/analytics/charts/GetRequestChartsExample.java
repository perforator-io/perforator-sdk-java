package com.example.analytics.charts;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRequestChartsExample {

    Logger logger = LoggerFactory.getLogger(
            GetRequestChartsExample.class
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
        
        AnalyticsChartRequest chartRequest = new AnalyticsChartRequest();
        chartRequest.setNamespace(AnalyticsNamespace.REQUESTS.getValue());
        chartRequest.setMetrics(List.of(
                RequestsBasicMetrics.COUNT.getValue(),
                RequestsBasicMetrics.DURATION_P90.getValue(),
                RequestsBasicMetrics.PROBLEMATIC_PERCENTAGE.getValue()
        ));
        chartRequest.setFilters(List.of(
                filter(
                        RequestsField.REQUEST_URL.getValue(), 
                        AnalyticsStringFieldCondition.CONTAINS.getValue(), 
                        "https"
                ),
                filter(
                        RequestsField.DYNAMIC.getValue(), 
                        AnalyticsBooleanFieldCondition.EQUALS.getValue(), 
                        "true"
                ),
                filter(
                        RequestsField.CLOUD_REGION.getValue(), 
                        AnalyticsStringFieldCondition.CONTAINS.getValue(), 
                        "aws-us-"
                )
        ));
        
        AnalyticsChartsRequest chartsRequest = new AnalyticsChartsRequest();
        chartsRequest.setCharts(List.of(chartRequest));
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
        new GetRequestChartsExample().run();
    }

}
