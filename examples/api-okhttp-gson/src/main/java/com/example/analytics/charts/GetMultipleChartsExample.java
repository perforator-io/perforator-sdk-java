package com.example.analytics.charts;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetMultipleChartsExample {

    Logger logger = LoggerFactory.getLogger(
            GetMultipleChartsExample.class
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
        
        AnalyticsChartRequest pagesChartRequest = new AnalyticsChartRequest();
        pagesChartRequest.setNamespace(
                AnalyticsNamespace.PAGES.getValue()
        );
        pagesChartRequest.setMetrics(List.of(
                PagesBasicMetrics.COUNT.getValue(),
                PagesBasicMetrics.DOM_CONTENT_LOAD_P90.getValue(),
                PagesBasicMetrics.PAGE_LOAD_P90.getValue()
        ));
        pagesChartRequest.setFilters(List.of(
                filter(
                        PagesField.PAGE_URL.getValue(), 
                        AnalyticsStringFieldCondition.CONTAINS.getValue(), 
                        "https"
                ),
                filter(
                        PagesField.PAGE_LOAD.getValue(), 
                        AnalyticsLongFieldCondition.GREATER_THAN_OR_EQUALS.getValue(), 
                        "1000"
                )
        ));
        
        AnalyticsChartRequest transactionsChartRequest = new AnalyticsChartRequest();
        transactionsChartRequest.setNamespace(
                AnalyticsNamespace.TRANSACTIONS.getValue()
        );
        transactionsChartRequest.setMetrics(List.of(
                TransactionsBasicMetrics.COUNT.getValue(),
                TransactionsBasicMetrics.DURATION_P90.getValue(),
                TransactionsBasicMetrics.STATUS_FAILED_PERCENTAGE.getValue()
        ));
        transactionsChartRequest.setFilters(List.of(
                filter(
                        TransactionsField.TYPE.getValue(), 
                        AnalyticsStringFieldCondition.EQUALS.getValue(), 
                        AnalyticsTransactionType.TOP_LEVEL.getValue()
                )
        ));
        
        AnalyticsChartsRequest chartsRequest = new AnalyticsChartsRequest();
        chartsRequest.setCharts(List.of(pagesChartRequest,
                transactionsChartRequest
        ));
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
        new GetMultipleChartsExample().run();
    }

}
