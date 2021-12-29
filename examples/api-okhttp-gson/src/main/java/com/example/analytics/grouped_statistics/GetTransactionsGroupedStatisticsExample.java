/*
 * Copyright Perforator, Inc. and contributors. All rights reserved.
 *
 * Use of this software is governed by the Business Source License
 * included in the LICENSE file.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0.
 */
package com.example.analytics.grouped_statistics;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTransactionsGroupedStatisticsExample {

    Logger logger = LoggerFactory.getLogger(
            GetTransactionsGroupedStatisticsExample.class
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
                AnalyticsNamespace.TRANSACTIONS.getValue()
        );
        statsRequest.setMetrics(List.of(
                TransactionsBasicMetrics.COUNT.getValue(),
                TransactionsBasicMetrics.STATUS_IN_PROGRESS_COUNT.getValue(),
                TransactionsBasicMetrics.STATUS_SUCCESSFUL_COUNT.getValue(),
                TransactionsBasicMetrics.STATUS_FAILED_COUNT.getValue(),
                TransactionsBasicMetrics.DURATION_P90.getValue()
        ));
        statsRequest.setFilters(List.of(
                filter(
                        TransactionsField.TRANSACTION_NAME.getValue(), 
                        AnalyticsStringFieldCondition.NOT_EQUALS.getValue(), 
                        "Name to exclude"
                )
        ));
        statsRequest.setGroupBy(List.of(
                TransactionsField.TRANSACTION_NAME.getValue()
        ));
        statsRequest.setOrderBy(List.of(
                TransactionsBasicMetrics.COUNT.getValue()
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
        new GetTransactionsGroupedStatisticsExample().run();
    }

}
