package com.example.analytics.namespaces;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListAnalyticsNamespacesExample {
    
    Logger logger = LoggerFactory.getLogger(
            ListAnalyticsNamespacesExample.class
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
        
        List<String> namespaces = analyticsApi.getNamespaces(
                projectKey, 
                executionKey
        );
        
        logger.info(
                "There are {} namespaces: {}",
                namespaces.size(),
                namespaces
        );
    }
    
    public static void main(String[] args) throws Exception {
        new ListAnalyticsNamespacesExample().run();
    }
    
}
