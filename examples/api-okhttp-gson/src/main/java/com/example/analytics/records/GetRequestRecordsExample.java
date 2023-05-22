package com.example.analytics.records;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRequestRecordsExample {

    Logger logger = LoggerFactory.getLogger(
            GetRequestRecordsExample.class
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
        request.setNamespace(AnalyticsNamespace.REQUESTS.getValue());
        request.setFields(List.of(
                RequestsField.REQUEST_URL.getValue(),
                RequestsField.REQUEST_TYPE.getValue(),
                RequestsField.REQUEST_METHOD.getValue(),
                RequestsField.PAGE_URL.getValue(),
                RequestsField.TIMESTAMP.getValue(),
                RequestsField.DURATION.getValue(),
                RequestsField.RESPONSE_CODE.getValue(),
                RequestsField.RESPONSE_SIZE.getValue(),
                RequestsField.FAILED.getValue(),
                RequestsField.FAILURE_MESSAGE.getValue()
        ));
        request.setFilters(List.of(
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
        
        AnalyticsRecordsResult response = analyticsApi.getNamespaceRecords(
                projectKey, 
                executionKey, 
                request
        );
        
        logger.info(
                "There are {} request records found: {}", 
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
        new GetRequestRecordsExample().run();
    }

}
