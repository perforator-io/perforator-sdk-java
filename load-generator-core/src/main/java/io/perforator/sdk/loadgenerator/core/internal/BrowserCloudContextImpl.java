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
package io.perforator.sdk.loadgenerator.core.internal;

import io.perforator.sdk.api.okhttpgson.model.BrowserCloudDetails;
import io.perforator.sdk.loadgenerator.core.context.BrowserCloudContext;
import java.net.MalformedURLException;

import java.net.URL;

final class BrowserCloudContextImpl implements BrowserCloudContext {

    private final String projectKey;
    private final String executionKey;
    private final String browserCloudKey;
    private final URL seleniumHubURL;
    private final String browserCloudStatus;

    public BrowserCloudContextImpl(String projectKey, String executionKey) {
        this(projectKey, executionKey, null, null, null);
    }
    
    public BrowserCloudContextImpl(String projectKey, String executionKey, String browserCloudKey) {
        this(projectKey, executionKey, browserCloudKey, null, null);
    }
    
    public BrowserCloudContextImpl(String projectKey, String executionKey, BrowserCloudDetails details) {
        this(
                projectKey, 
                executionKey, 
                details != null ? details.getUuid() : null, 
                details != null ? toUrl(details.getSeleniumHubURL()) : null, 
                details != null ? details.getStatus() : null
        );
    }

    public BrowserCloudContextImpl(String projectKey, String executionKey, String browserCloudKey, URL seleniumHubURL, String browserCloudStatus) {
        this.projectKey = projectKey;
        this.executionKey = executionKey;
        this.browserCloudKey = browserCloudKey;
        this.seleniumHubURL = seleniumHubURL;
        this.browserCloudStatus = browserCloudStatus;
    }
    
    private static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL " + url + " is incorrect", e);
        }
    }

    @Override
    public String getProjectKey() {
        return projectKey;
    }

    @Override
    public String getExecutionKey() {
        return executionKey;
    }

    @Override
    public String getBrowserCloudKey() {
        return browserCloudKey;
    }
    
    @Override
    public String getBrowserCloudStatus() {
        return browserCloudStatus;
    }

    public URL getSeleniumHubURL() {
        return seleniumHubURL;
    }
    
}