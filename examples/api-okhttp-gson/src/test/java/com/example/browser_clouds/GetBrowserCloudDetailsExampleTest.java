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
package com.example.browser_clouds;

import com.example.AbstractExampleTest;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.model.*;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class GetBrowserCloudDetailsExampleTest extends AbstractExampleTest<GetBrowserCloudDetailsExample>{
    
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
