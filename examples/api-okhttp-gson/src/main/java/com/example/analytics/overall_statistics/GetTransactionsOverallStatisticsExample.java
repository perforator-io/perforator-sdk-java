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
package com.example.analytics.overall_statistics;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTransactionsOverallStatisticsExample {

    Logger logger = LoggerFactory.getLogger(
            GetTransactionsOverallStatisticsExample.class
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
        
        AnalyticsOverallStatisticsRequest statsRequest = new AnalyticsOverallStatisticsRequest();
        statsRequest.setNamespace(
                AnalyticsNamespace.TRANSACTIONS.getValue()
        );
        statsRequest.setMetrics(List.of(
                TransactionsBasicMetrics.TOP_LEVEL_COUNT.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_DURATION_P90.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_IN_PROGRESS_COUNT.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_IN_PROGRESS_PERCENTAGE.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_IN_PROGRESS_DURATION_P90.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_SUCCESSFUL_COUNT.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_SUCCESSFUL_PERCENTAGE.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_SUCCESSFUL_DURATION_P90.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_FAILED_COUNT.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_FAILED_PERCENTAGE.getValue(),
                TransactionsBasicMetrics.TOP_LEVEL_FAILED_DURATION_P90.getValue(),
                TransactionsBasicMetrics.NESTED_COUNT.getValue(),
                TransactionsBasicMetrics.NESTED_DURATION_P90.getValue(),
                TransactionsBasicMetrics.NESTED_IN_PROGRESS_COUNT.getValue(),
                TransactionsBasicMetrics.NESTED_IN_PROGRESS_PERCENTAGE.getValue(),
                TransactionsBasicMetrics.NESTED_IN_PROGRESS_DURATION_P90.getValue(),
                TransactionsBasicMetrics.NESTED_SUCCESSFUL_COUNT.getValue(),
                TransactionsBasicMetrics.NESTED_SUCCESSFUL_PERCENTAGE.getValue(),
                TransactionsBasicMetrics.NESTED_SUCCESSFUL_DURATION_P90.getValue(),
                TransactionsBasicMetrics.NESTED_FAILED_COUNT.getValue(),
                TransactionsBasicMetrics.NESTED_FAILED_PERCENTAGE.getValue(),
                TransactionsBasicMetrics.NESTED_FAILED_DURATION_P90.getValue()
        ));
        statsRequest.setFilters(List.of(
                filter(
                        TransactionsField.TRANSACTION_NAME.getValue(), 
                        AnalyticsStringFieldCondition.NOT_EQUALS.getValue(), 
                        "Name to exclude"
                )
        ));
        
        List<AnalyticsOverallStatisticsResult> results = analyticsApi.getOverallStatistics(
                projectKey, 
                executionKey, 
                List.of(statsRequest)
        );
        
        for (AnalyticsOverallStatisticsResult result : results) {
            logger.info("OverallStatistics for {}: {}",
                    result.getNamespace(),
                    result.getResults()
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
        new GetTransactionsOverallStatisticsExample().run();
    }

}
