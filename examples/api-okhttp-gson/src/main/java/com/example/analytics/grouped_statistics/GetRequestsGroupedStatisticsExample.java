package com.example.analytics.grouped_statistics;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRequestsGroupedStatisticsExample {

    Logger logger = LoggerFactory.getLogger(
            GetRequestsGroupedStatisticsExample.class
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
        
        AnalyticsGroupedStatisticsRequest statsRequest = new AnalyticsGroupedStatisticsRequest();
        statsRequest.setNamespace(
                AnalyticsNamespace.REQUESTS.getValue()
        );
        statsRequest.setMetrics(List.of(
                RequestsBasicMetrics.COUNT.getValue(),
                RequestsBasicMetrics.PROBLEMATIC_PERCENTAGE.getValue(),
                RequestsBasicMetrics.DURATION_P90.getValue()
        ));
        statsRequest.setFilters(List.of(
                filter(
                        RequestsField.REQUEST_URL.getValue(), 
                        AnalyticsStringFieldCondition.CONTAINS.getValue(), 
                        "https"
                ),
                filter(
                        RequestsField.CLOUD_REGION.getValue(), 
                        AnalyticsStringFieldCondition.CONTAINS.getValue(), 
                        "aws-us-"
                )
        ));
        statsRequest.setGroupBy(List.of(
                RequestsField.REQUEST_URL.getValue()
        ));
        statsRequest.setGroupingFlags(Map.of(
                RequestsField.REQUEST_URL.getValue(), 
                Map.of(RequestsGroupingFlag.IGNORE_URL_PARAMS.getValue(), true)
        ));
        statsRequest.setOrderBy(List.of(
                RequestsBasicMetrics.DURATION_P90.getValue()
        ));
        statsRequest.setOrderDirection(
                AnalyticsOrderDirection.DESC.getValue()
        );
        
        AnalyticsGroupedStatisticsResult result = analyticsApi.getGroupedStatistics(
                projectKey, 
                executionKey, 
                statsRequest
        );
        
        logger.info(
                "There are {} grouped records found for {}",
                result.getResults().size(),
                result.getNamespace()
        );
        for (Map<String, Object> groupedRecord : result.getResults()) {
            logger.info("Grouped record: {}", groupedRecord);
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
        new GetRequestsGroupedStatisticsExample().run();
    }

}
