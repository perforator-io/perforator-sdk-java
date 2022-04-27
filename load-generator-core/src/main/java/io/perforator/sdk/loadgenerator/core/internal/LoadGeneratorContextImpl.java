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

import io.perforator.sdk.api.okhttpgson.model.AnalyticsEvent;
import io.perforator.sdk.api.okhttpgson.operations.*;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import org.asynchttpclient.AsyncHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

final class LoadGeneratorContextImpl {

    private final long startedAt;
    private final LoadGeneratorConfig loadGeneratorConfig;
    private final List<SuiteConfigContextImpl> suiteConfigContexts;
    private final Queue<List<AnalyticsEvent>> eventsBuffer;
    private final StatisticsContextImpl statisticsContext;
    private final AtomicBoolean isLocalOnly = new AtomicBoolean(true);

    private AsyncHttpClient httpClient;
    private CreditsApi creditsApi;
    private LimitsApi limitsApi;
    private ProjectsApi projectsApi;
    private ExecutionsApi executionsApi;
    private BrowserCloudsApi browserCloudsApi;
    private BrowserCloudContextImpl browserCloudContext;
    private AtomicBoolean isFinished = new AtomicBoolean(false);

    LoadGeneratorContextImpl(long startedAt, LoadGeneratorConfig loadGeneratorConfig, List<SuiteConfig> suiteConfigs) {
        this.startedAt = startedAt;
        this.loadGeneratorConfig = loadGeneratorConfig;
        this.eventsBuffer = new ConcurrentLinkedQueue<>();
        this.statisticsContext = new StatisticsContextImpl();
        this.suiteConfigContexts = new ArrayList<>();
        for (SuiteConfig suiteConfig: suiteConfigs){
            this.suiteConfigContexts.add(
                    new SuiteConfigContextImpl(this, suiteConfig)
            );
            if(suiteConfig.getWebDriverMode() == WebDriverMode.cloud){
                isLocalOnly.set(false);
            }
        }
    }

    public SuiteConfigContextImpl getSuiteConfigContext(SuiteConfig suiteConfig) {
        for(SuiteConfigContextImpl context : this.suiteConfigContexts){
            if(context.getSuiteConfig().equals(suiteConfig)){
                return context;
            }
        }
        throw new RuntimeException("Can't find SuiteConfigContext by SuiteConfig: " + suiteConfig.getId());
    }

    public long getStartedAt() {
        return startedAt;
    }

    public LoadGeneratorConfig getLoadGeneratorConfig() {
        return loadGeneratorConfig;
    }

    public List<SuiteConfigContextImpl> getSuiteConfigContexts() {
        return suiteConfigContexts;
    }

    public Queue<List<AnalyticsEvent>> getEventsBuffer() {
        return eventsBuffer;
    }

    public StatisticsContextImpl getStatisticsContext() {
        return statisticsContext;
    }

    public AsyncHttpClient getAsyncHttpClient() {
        return httpClient;
    }

    public void setAsyncHttpClient(AsyncHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CreditsApi getCreditsApi() {
        return creditsApi;
    }

    public void setCreditsApi(CreditsApi creditsApi) {
        this.creditsApi = creditsApi;
    }

    public LimitsApi getLimitsApi() {
        return limitsApi;
    }

    public void setLimitsApi(LimitsApi limitsApi) {
        this.limitsApi = limitsApi;
    }

    public ProjectsApi getProjectsApi() {
        return projectsApi;
    }

    public void setProjectsApi(ProjectsApi projectsApi) {
        this.projectsApi = projectsApi;
    }

    public ExecutionsApi getExecutionsApi() {
        return executionsApi;
    }

    public void setExecutionsApi(ExecutionsApi executionsApi) {
        this.executionsApi = executionsApi;
    }

    public BrowserCloudsApi getBrowserCloudsApi() {
        return browserCloudsApi;
    }

    public void setBrowserCloudsApi(BrowserCloudsApi browserCloudsApi) {
        this.browserCloudsApi = browserCloudsApi;
    }

    public BrowserCloudContextImpl getBrowserCloudContext() {
        return browserCloudContext;
    }

    public void setBrowserCloudContext(BrowserCloudContextImpl browserCloudContext) {
        this.browserCloudContext = browserCloudContext;
    }

    public boolean isLocalOnly() {
        return isLocalOnly.get();
    }

    public boolean isFinished() {
        return isFinished.get();
    }

    public void setFinished() {
        this.isFinished.set(true);
    }
}
