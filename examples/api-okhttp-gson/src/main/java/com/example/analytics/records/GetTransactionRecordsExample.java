package com.example.analytics.records;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTransactionRecordsExample {

    Logger logger = LoggerFactory.getLogger(
            GetTransactionRecordsExample.class
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
        request.setNamespace(AnalyticsNamespace.TRANSACTIONS.getValue());
        request.setFields(List.of(
                TransactionsField.TRANSACTION_NAME.getValue(),
                TransactionsField.TYPE.getValue(),
                TransactionsField.TRANSACTION_ID.getValue(),
                TransactionsField.PARENT_TRANSACTION_ID.getValue(),
                TransactionsField.PARENT_TRANSACTION_NAME.getValue(),
                TransactionsField.SUITE_NAME.getValue(),
                TransactionsField.START_TIME.getValue(),
                TransactionsField.FINISH_TIME.getValue(),
                TransactionsField.DURATION.getValue(),
                TransactionsField.STATUS.getValue(),
                TransactionsField.FAILURE_MESSAGE.getValue()
        ));
        request.setFilters(List.of(
                filter(
                        TransactionsField.STATUS.getValue(), 
                        AnalyticsStringFieldCondition.EQUALS.getValue(), 
                        AnalyticsTransactionStatus.FAILED.getValue()
                )
        ));
        
        AnalyticsRecordsResult response = analyticsApi.getNamespaceRecords(
                projectKey, 
                executionKey, 
                request
        );
        
        logger.info(
                "There are {} transaction records found: {}", 
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
        new GetTransactionRecordsExample().run();
    }

}
