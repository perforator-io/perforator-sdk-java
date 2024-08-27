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
package io.perforator.sdk.api.okhttpgson;

import io.perforator.sdk.api.okhttpgson.invoker.ApiClient;
import io.perforator.sdk.api.okhttpgson.operations.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assumptions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractApiTest {
    
    protected static final String API_BASE_URL_PROPERTY = "LOADGENERATOR_APIBASEURL";
    protected static final String API_CLIENT_ID_PROPERTY = "LOADGENERATOR_APICLIENTID";
    protected static final String API_CLIENT_SECRET_PROPERTY = "LOADGENERATOR_APICLIENTSECRET";
    protected static final String PROJECT_KEY_PROPERTY = "LOADGENERATOR_PROJECTKEY";
    protected static final String EXECUTION_KEY_PROPERTY = "LOADGENERATOR_EXECUTIONKEY";
    
    private static final boolean TRACE_HTTP_CALLS = false;
    
    protected ApiClientBuilder builder;
    protected ApiClient apiClient;
    
    protected LimitsApi limitsApi;
    protected ProjectsApi projectsApi;
    protected ExecutionsApi executionsApi;
    protected AnalyticsApi analyticsApi;
    protected BrowserCloudsApi browserCloudsApi;
    
    protected String projectKey;
    protected String executionKey;
    
    @BeforeAll
    public void setup() throws Exception {
        assumeTrue(hasRequiredProperty(API_CLIENT_ID_PROPERTY));
        assumeTrue(hasRequiredProperty(API_CLIENT_SECRET_PROPERTY));
        assumeTrue(hasRequiredProperty(API_BASE_URL_PROPERTY));
        
        builder = new ApiClientBuilder(
                getRequiredProperty(API_CLIENT_ID_PROPERTY), 
                getRequiredProperty(API_CLIENT_SECRET_PROPERTY), 
                getRequiredProperty(API_BASE_URL_PROPERTY)
        );
        
        if(TRACE_HTTP_CALLS) {
            builder.getApiClient().setDebugging(true);
        }
        
        apiClient = builder.getApiClient();
        limitsApi = builder.getApi(LimitsApi.class);
        projectsApi = builder.getApi(ProjectsApi.class);
        executionsApi = builder.getApi(ExecutionsApi.class);
        analyticsApi = builder.getApi(AnalyticsApi.class);
        browserCloudsApi = builder.getApi(BrowserCloudsApi.class);
        
        if(hasRequiredProperty(PROJECT_KEY_PROPERTY)) {
            projectKey = getRequiredProperty(PROJECT_KEY_PROPERTY);
        } else {
            projectKey = projectsApi.listProjects().get(0).getUuid();
        }
        
        if(hasRequiredProperty(EXECUTION_KEY_PROPERTY)) {
            executionKey = getRequiredProperty(EXECUTION_KEY_PROPERTY);
        } else {
            executionKey = executionsApi.listExecutions(projectKey).get(0).getUuid();
        }
    }
    
    protected boolean hasRequiredProperty(String key) {
        String value = System.getProperty(key);
        if(value != null && !value.isBlank()) {
            return true;
        }
        
        value = System.getenv(key);
        return value != null && !value.isBlank();
    }
    
    protected String getRequiredProperty(String key) {
        String result = System.getProperty(key);
        if(result != null && !result.isBlank()) {
            return result.trim();
        }
        
        result = System.getenv(key);
        if(result != null && !result.isBlank()) {
            return result.trim();
        }

        throw new IllegalArgumentException("Required system/env property " + key + " is not defined");
    }
    
}
