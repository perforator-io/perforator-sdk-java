package com.example.browser_clouds;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.BrowserCloudsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetBrowserCloudDetailsExample {
    
    Logger logger = LoggerFactory.getLogger(
            GetBrowserCloudDetailsExample.class
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
    
    //Please replace YOUR_BROWSER_CLOUD_KEY with you own browser cloud key
    String browserCloudKey = "YOUR_BROWSER_CLOUD_KEY";
    
    public void run() throws Exception {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );

        BrowserCloudsApi browserCloudsApi = builder.getApi(
                BrowserCloudsApi.class
        );

        BrowserCloudDetails details = browserCloudsApi.getBrowserCloudDetails(
                projectKey,
                executionKey,
                browserCloudKey
        );

        logger.info(
                "Browser cloud: "
                + "key={}, "
                + "status={}, "
                + "seleniumHubURL={}, "
                + "browsersRequestedCount={}, "
                + "browsersStartingCount={}, "
                + "browsersReadyCount={}, "
                + "readyAt={}",
                details.getUuid(),
                details.getStatus(),
                details.getSeleniumHubURL(),
                details.getBrowsersRequestedCount(),
                details.getBrowsersStartingCount(),
                details.getBrowsersReadyCount(),
                details.getReadyAt()
        );
    }

    public static void main(String[] args) throws Exception {
        new GetBrowserCloudDetailsExample().run();
    }

}
