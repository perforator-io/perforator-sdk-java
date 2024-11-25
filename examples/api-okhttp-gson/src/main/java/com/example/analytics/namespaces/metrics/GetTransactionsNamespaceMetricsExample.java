package com.example.analytics.namespaces.metrics;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTransactionsNamespaceMetricsExample {

    Logger logger = LoggerFactory.getLogger(
            GetTransactionsNamespaceMetricsExample.class
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
    
    int maxMetricsToLog = 256;

    public void run() throws Exception {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );

        AnalyticsApi analyticsApi = builder.getApi(
                AnalyticsApi.class
        );

        List<AnalyticsMetricInfo> metrics = analyticsApi.getNamespaceMetrics(
                projectKey,
                executionKey,
                AnalyticsNamespace.TRANSACTIONS.getValue()
        );
        
        logger.info(
                "There are {} metrics in transactions namespace => {}, ... , displaying only the first {} metrics", 
                metrics.size(),
                metrics.stream()
                        .limit(maxMetricsToLog)
                        .map(m -> m.getName() + ":" + m.getType())
                        .collect(Collectors.joining(" , ")),
                maxMetricsToLog
        );
    }

    public static void main(String[] args) throws Exception {
        new GetTransactionsNamespaceMetricsExample().run();
    }

}
