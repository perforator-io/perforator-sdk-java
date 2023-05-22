package com.example.analytics.records;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetSessionRecordsExample {

    Logger logger = LoggerFactory.getLogger(
            GetSessionRecordsExample.class
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
        
        AnalyticsRecordsRequest request = new AnalyticsRecordsRequest();
        request.setNamespace(AnalyticsNamespace.SESSIONS.getValue());
        request.setFields(List.of(
                SessionsField.SESSION_ID.getValue(),
                SessionsField.START_TIME.getValue(),
                SessionsField.FINISH_TIME.getValue(),
                SessionsField.PAGES_COUNT.getValue(),
                SessionsField.REQUESTS_COUNT.getValue(),
                SessionsField.PROBLEMATIC_REQUESTS_COUNT.getValue()
        ));
        request.setFilters(List.of(
                filter(
                        SessionsField.PROBLEMATIC_REQUESTS_COUNT.getValue(), 
                        AnalyticsLongFieldCondition.GREATER_THAN_OR_EQUALS.getValue(), 
                        "1"
                ),
                filter(
                        SessionsField.CLOUD_REGION.getValue(), 
                        AnalyticsStringFieldCondition.CONTAINS.getValue(), 
                        "aws-us-"
                )
        ));
        
        AnalyticsRecordsResult response = analyticsApi.getNamespaceRecords(
                projectKey, 
                executionKey, 
                request
        );
        
        logger.info(
                "There are {} session records found: {}", 
                response.getRecords().size(),
                response.getRecords()
        );
    }
    
    private static AnalyticsFilter filter(String field, String condition, String value) {
        AnalyticsFilter result = new AnalyticsFilter();
        
        result.setField(field);
        result.setCondition(condition);
        result.setValue(value);
        
        return result;
    }

    public static void main(String[] args) throws Exception {
        new GetSessionRecordsExample().run();
    }

}
