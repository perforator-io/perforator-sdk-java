package com.example.browser_clouds;

import com.example.AbstractExampleTest;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.model.*;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class GetBrowserCloudExampleTest extends AbstractExampleTest<GetBrowserCloudExample>{
    
    private Execution execution;
    private BrowserCloud browserCloud;
    
    @BeforeAll
    public void setup() throws Exception {
        execution = executionsApi.createExecution(
                projectKey, 
                new Execution()
        );
        
        browserCloud = new BrowserCloud();
        browserCloud.setConcurrency(1);
        browserCloud.setDuration(1);
        browserCloud = browserCloudsApi.createBrowserCloud(
                projectKey, 
                execution.getUuid(), 
                browserCloud
        );
    }
    
    @AfterAll
    public void revert() throws Exception {
        try {
            browserCloudsApi.terminateBrowserCloud(
                    projectKey,
                    execution.getUuid(),
                    browserCloud.getUuid()
            );
        } catch(ApiException e) {
            //ignore
        }
    }

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, execution.getUuid(),
                BROWSER_CLOUD_KEY, browserCloud.getUuid()
        );
    }
    
}
