package com.example.browser_clouds;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.BrowserCloudsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListBrowserCloudsExample {

    Logger logger = LoggerFactory.getLogger(
            ListBrowserCloudsExample.class
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

        BrowserCloudsApi browserCloudsApi = builder.getApi(
                BrowserCloudsApi.class
        );

        List<BrowserCloud> browserClouds = browserCloudsApi.listBrowserClouds(
                projectKey,
                executionKey
        );
        logger.info(
                "There are {} browser clouds for the execution {}",
                browserClouds.size(),
                executionKey
        );

        for (BrowserCloud browserCloud : browserClouds) {
            logger.info(
                    "Browser cloud: "
                    + "key={}, "
                    + "concurrency={}, "
                    + "status={}, "
                    + "createdAt={}, "
                    + "updatedAt={}, ",
                    browserCloud.getUuid(),
                    browserCloud.getConcurrency(),
                    browserCloud.getStatus(),
                    browserCloud.getCreatedAt(),
                    browserCloud.getUpdatedAt()
            );
        }
    }

    public static void main(String[] args) throws Exception {
        new ListBrowserCloudsExample().run();
    }

}
