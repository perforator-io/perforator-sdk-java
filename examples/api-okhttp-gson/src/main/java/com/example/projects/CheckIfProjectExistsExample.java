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
package com.example.projects;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.operations.ProjectsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckIfProjectExistsExample {

    Logger logger = LoggerFactory.getLogger(
            CheckIfProjectExistsExample.class
    );
    
    String apiBaseUrl = ApiClientBuilder.DEFAULT_API_BASE_URL;
    
    //Please replace YOUR_CLIENT_ID with you own client id
    String apiClientId = "YOUR_CLIENT_ID";
    
    //Please replace YOUR_CLIENT_SECRET with you own client secret
    String apiClientSecret = "YOUR_CLIENT_SECRET";
    
    //Please replace YOUR_PROJECT_KEY with you own project key
    String projectKey = "YOUR_PROJECT_KEY";
    
    public void run() throws Exception {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );
        
        ProjectsApi projectsApi = builder.getApi(
                ProjectsApi.class
        );
        
        try {
            projectsApi.checkIfProjectExists(projectKey);
            logger.info(
                    "Project with the key {} exists.",
                    projectKey
            );
        } catch(ApiException e) {
            logger.info(
                    "Project with the key {} doesn't exist",
                    projectKey
            );
        }
    }

    public static void main(String[] args) throws Exception {
        new CheckIfProjectExistsExample().run();
    }

}
