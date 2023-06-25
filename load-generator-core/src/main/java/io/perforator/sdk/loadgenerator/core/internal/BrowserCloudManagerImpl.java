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

import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.model.BrowserCloud;
import io.perforator.sdk.api.okhttpgson.model.BrowserCloudDetails;
import io.perforator.sdk.api.okhttpgson.model.CreditsBalance;
import io.perforator.sdk.api.okhttpgson.model.Execution;
import io.perforator.sdk.api.okhttpgson.model.PlatformLimit;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.perforator.sdk.loadgenerator.core.Threaded.sleep;

final class BrowserCloudManagerImpl implements BrowserCloudManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserCloudManagerImpl.class);

    private final TimeProvider timeProvider;
    private final Runnable shutdownHook;
    private Timer timer;

    public BrowserCloudManagerImpl(TimeProvider timeProvider, Runnable shutdownHook) {
        this.timeProvider = timeProvider;
        this.shutdownHook = shutdownHook;
    }

    private static void verify(String verificationSubject, ApiRunnable runnable) {
        LOGGER.info("Starting verification: {}, please wait...", verificationSubject);
        try {
            runnable.run();
        } catch (ApiException e) {
            throw new RuntimeException("Failed verification: " + verificationSubject, e);
        }
        LOGGER.info("Successful verification: {}", verificationSubject);
    }

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        if (loadGeneratorContext.isLocalOnly()) {
            return;
        }

        LoadGeneratorConfig loadGeneratorConfig = loadGeneratorContext.getLoadGeneratorConfig();

        List<SuiteConfig> suiteConfigs = new ArrayList<>();
        int concurrency = 0;
        int requiredDuration = 0;

        for (SuiteConfigContextImpl configContext: loadGeneratorContext.getSuiteConfigContexts()){
            if(configContext.getSuiteConfig().getWebDriverMode() != WebDriverMode.cloud){
                continue;
            }
            SuiteConfig suiteConfig = configContext.getSuiteConfig();
            
            if(suiteConfig.getWebDriverConcurrency() != null) {
                concurrency += Math.max(1, suiteConfig.getWebDriverConcurrency());
            } else {
                concurrency += Math.max(1, suiteConfig.getConcurrency());
            }
            
            requiredDuration = Math.max(requiredDuration, (int) suiteConfig.getDuration().toMillis() / 1000);

            suiteConfigs.add(suiteConfig);
        }

        if (concurrency <= 0) {
            throw new RuntimeException(
                    "Browser cloud should have concurrency greater than 0"
            );
        }

        if (requiredDuration <= 0) {
            throw new RuntimeException(
                    "Browser cloud should have duration greater than 0"
            );
        }

        int durationHours = (requiredDuration - 1) / 3600 + 1;

        verifyOrganizationLimitsAndBalance(loadGeneratorContext, durationHours, concurrency);

        String projectKey = loadGeneratorConfig.getProjectKey();
        String executionKey = loadGeneratorConfig.getExecutionKey();
        boolean usePreAllocatedIPs = loadGeneratorConfig.isUsePreAllocatedIPs();
        List<String> dataCapturingExcludes = loadGeneratorConfig.getDataCapturingExcludes();
        Map<String, String> browserCloudHttpHeaders = loadGeneratorConfig.getBrowserCloudHttpHeaders();
        Map<String, String> browserCloudHosts = loadGeneratorConfig.getBrowserCloudHosts();
        String browserCloudKey;
        verifyProjectExists(loadGeneratorContext, projectKey);

        String generatedExecutionNotes = generateExecutionNotes(loadGeneratorContext, suiteConfigs);

        if (executionKey != null && !executionKey.isBlank()) {
            verifyExecutionExists(loadGeneratorContext, projectKey, executionKey);
            String executionNotes = getExecutionNotes(loadGeneratorContext, projectKey, executionKey);
            String mergedNotes = mergeExecutionNotes(executionNotes, generatedExecutionNotes);
            if(!mergedNotes.equals(executionNotes)){
                updateExecutionNotes(loadGeneratorContext, projectKey, executionKey, mergedNotes);
            }
        } else {
            try {
                Execution execution = createExecution(
                        loadGeneratorContext,
                        projectKey,
                        generatedExecutionNotes
                );
                executionKey = execution.getUuid();
            } catch (ApiException e) {
                throw new RuntimeException("Can't create new execution", e);
            }
        }

        loadGeneratorConfig.setExecutionKey(executionKey);
        loadGeneratorContext.setBrowserCloudContext(
                new BrowserCloudContextImpl(projectKey, executionKey)
        );

        try {
            LOGGER.info("Creating browser cloud ...");
            BrowserCloud browserCloud = createBrowserCloud(
                    loadGeneratorContext,
                    projectKey,
                    executionKey,
                    concurrency,
                    durationHours,
                    usePreAllocatedIPs,
                    dataCapturingExcludes,
                    browserCloudHttpHeaders,
                    browserCloudHosts
            );

            LOGGER.info("Created browser cloud {}", browserCloud.getUuid());

            if(dataCapturingExcludes != null && !dataCapturingExcludes.isEmpty()){
                LOGGER.warn(
                        "Browser cloud will avoid capturing the following HTTP requests: {}",
                        dataCapturingExcludes
                );
            }
            
            if(!loadGeneratorConfig.isDataCapturingIncludeRequestBody()) {
                LOGGER.warn(
                        "Browser cloud will avoid capturing HTTP requests body"
                );
            }
            
            if(!loadGeneratorConfig.isDataCapturingIncludeRequestHeaders()) {
                LOGGER.warn(
                        "Browser cloud will avoid capturing HTTP requests headers"
                );
            }
            
            if(!loadGeneratorConfig.isDataCapturingIncludeResponseBody()) {
                LOGGER.warn(
                        "Browser cloud will avoid capturing HTTP responses body"
                );
            }
            
            if(!loadGeneratorConfig.isDataCapturingIncludeResponseHeaders()) {
                LOGGER.warn(
                        "Browser cloud will avoid capturing HTTP responses headers"
                );
            }
            
            if(browserCloudHttpHeaders != null && !browserCloudHttpHeaders.isEmpty()) {
                LOGGER.info(
                        "Browser cloud will expose additional headers for all outgoing HTTP requests: {}",
                        browserCloudHttpHeaders
                );
            }

            if(browserCloudHosts != null && !browserCloudHosts.isEmpty()) {
                LOGGER.info(
                        "Browser cloud will expose additional /etc/hosts to remote browsers: {}",
                        browserCloudHosts
                );
            }

            browserCloudKey = browserCloud.getUuid();
        } catch (ApiException e) {
            throw new RuntimeException(
                    "Can't create new browser cloud: "
                            + "response code = " + e.getCode() + ", "
                            + "response body = " + e.getResponseBody() + ", "
                            + "response headers = " + e.getResponseHeaders(),
                    e
            );
        }

        loadGeneratorContext.setBrowserCloudContext(
                new BrowserCloudContextImpl(projectKey, executionKey, browserCloudKey)
        );

        Duration statusCheckInterval = loadGeneratorContext.getLoadGeneratorConfig().getBrowserCloudStatusPollInterval();
        BrowserCloudDetails browserCloudDetails = awaitBrowserCloud(
                loadGeneratorContext,
                projectKey,
                executionKey,
                browserCloudKey,
                loadGeneratorConfig.getBrowserCloudAwaitQueued(),
                loadGeneratorConfig.getBrowserCloudAwaitProvisioning(),
                statusCheckInterval
        );

        loadGeneratorContext.setBrowserCloudContext(
                new BrowserCloudContextImpl(projectKey, executionKey, browserCloudDetails)
        );

        timer = new Timer();
        timer.schedule(
                new StatusChecker(loadGeneratorContext),
                0,
                statusCheckInterval.toMillis()
        );

    }

    private String getExecutionNotes(LoadGeneratorContextImpl context, String projectKey, String executionKey) {
        try {
            return context.getExecutionsApi().getExecution(projectKey, executionKey).getNotes();
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get execution notes", e);
        }
    }

    private void updateExecutionNotes(LoadGeneratorContextImpl context, String projectKey, String executionKey, String notes) {
        try {
            Execution execution = new Execution();
            execution.setNotes(notes);
            context.getExecutionsApi().patchExecution(projectKey, executionKey, execution);
        } catch (ApiException e) {
            throw new RuntimeException("Failed to update execution notes", e);
        }
    }

    private String mergeExecutionNotes(String ... notes) {
        return Stream.of(notes).collect(Collectors.joining("<br/>"));
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        if (loadGeneratorContext.isLocalOnly()) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        cleanup(loadGeneratorContext);
    }

    private BrowserCloudDetails awaitBrowserCloud(LoadGeneratorContextImpl loadGeneratorContext, String projectKey, String executionKey, String browserCloudUuid, Duration awaitQueuedDuration, Duration awaitProvisioningDuration, Duration statusCheckInterval) {
        long start = System.currentTimeMillis();
        long waitMillis = awaitQueuedDuration.toMillis();
        String browserCloudLastStatus = "queued";

        while (start + waitMillis > System.currentTimeMillis()) {
            try {
                BrowserCloudDetails details = getBrowserCloudDetails(
                        loadGeneratorContext,
                        projectKey,
                        executionKey,
                        browserCloudUuid
                );
                if (details.getStatus().equalsIgnoreCase("operational")) {
                    LOGGER.info(
                            "Browser cloud is ready: "
                                    + "duration = {} hour(s), "
                                    + "concurrent browsers = {}, "
                                    + "browser name = {}, "
                                    + "browser version = {}, "
                                    + "selenium hub = {}",
                            details.getDuration(),
                            details.getBrowsersReadyCount(),
                            details.getBrowserName(),
                            details.getBrowserVersion(),
                            details.getSeleniumHubURL()
                    );
                    return details;
                } else if (details.getStatus().equalsIgnoreCase("terminating")) {
                    throw new RuntimeException(
                            "Browser cloud "
                                    + browserCloudUuid
                                    + " is already terminating"
                    );
                } else if (details.getStatus().equalsIgnoreCase("terminated")) {
                    throw new RuntimeException(
                            "Browser cloud "
                                    + browserCloudUuid
                                    + " is already terminated"
                    );
                } else {
                    if (details.getStatus().equalsIgnoreCase("provisioning") &&
                            browserCloudLastStatus.equalsIgnoreCase("queued")
                    ) {
                        start = System.currentTimeMillis();
                        waitMillis = awaitProvisioningDuration.toMillis();
                    }
                    LOGGER.info(
                            "Browser cloud is not ready yet. "
                                    + "Status = {}, "
                                    + "RequestedBrowsers = {}, "
                                    + "StartingBrowsers = {}, "
                                    + "ReadyBrowsers = {}",
                            details.getStatus(),
                            details.getBrowsersRequestedCount(),
                            details.getBrowsersStartingCount(),
                            details.getBrowsersReadyCount()
                    );
                }
                browserCloudLastStatus = details.getStatus();
            } catch (ApiException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(
                            "Can't get browser cloud details, {}",
                            browserCloudUuid,
                            e
                    );
                }
            }

            sleep(statusCheckInterval.toMillis());
        }

        try {
            cleanup(loadGeneratorContext);
        } finally {
            throw new RuntimeException(
                    "Browser Cloud "
                            + browserCloudUuid
                            + " was not initialized successfully from "
                            + browserCloudLastStatus.toUpperCase()
                            + " status within "
                            + (browserCloudLastStatus.equalsIgnoreCase("queued")
                            ? awaitQueuedDuration.toMillis()
                            : awaitProvisioningDuration.toMillis())
                            + " millis"
            );
        }
    }

    private void cleanup(LoadGeneratorContextImpl loadGeneratorContext) {
        if (loadGeneratorContext.getBrowserCloudContext() == null) {
            return;
        }

        String projectKey = loadGeneratorContext.getBrowserCloudContext().getProjectKey();
        String executionKey = loadGeneratorContext.getBrowserCloudContext().getExecutionKey();
        String browserCloudKey = loadGeneratorContext.getBrowserCloudContext().getBrowserCloudKey();
        try {
            if (projectKey != null && executionKey != null && browserCloudKey != null) {
                terminateBrowserCloud(loadGeneratorContext, projectKey, executionKey, browserCloudKey);
            } else if (projectKey != null && executionKey != null) {
                List<BrowserCloud> browserClouds = listBrowserClouds(loadGeneratorContext, projectKey, executionKey);

                if (browserClouds != null && !browserClouds.isEmpty()) {
                    for (BrowserCloud browserCloud : browserClouds) {
                        BrowserCloudDetails browserCloudDetails = getBrowserCloudDetails(
                                loadGeneratorContext,
                                projectKey,
                                executionKey,
                                browserCloud.getUuid()
                        );

                        String status = browserCloudDetails.getStatus();
                        if (!"terminating".equalsIgnoreCase(status) && !"terminated".equalsIgnoreCase(status)) {
                            terminateBrowserCloud(loadGeneratorContext, projectKey, executionKey, browserCloud.getUuid());
                        }
                    }
                }
            }
        } catch (ApiException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Problem terminating browser cloud for execution {}", executionKey, e);
            }
        }
    }

    private void verifyProjectExists(LoadGeneratorContextImpl loadGeneratorContext, String projectKey) {
        verify("project " + projectKey, () -> {
            loadGeneratorContext.getProjectsApi().checkIfProjectExists(projectKey);
        });
    }

    private void verifyExecutionExists(LoadGeneratorContextImpl loadGeneratorContext, String projectKey, String executionKey) {
        verify("project " + projectKey + ", execution " + executionKey, () -> {
            loadGeneratorContext.getExecutionsApi().checkIfExecutionExists(projectKey, executionKey);
        });
    }

    private void verifyOrganizationLimitsAndBalance(LoadGeneratorContextImpl loadGeneratorContext, int duration, int concurrency) {
        Map<String, Integer> limits;
        try {
            limits = loadGeneratorContext.getLimitsApi().getLimits();
        } catch (ApiException e) {
            throw new RuntimeException("Can't get current limits", e);
        }

        if (limits.containsKey(PlatformLimit.CONCURRENT_BROWSER_CLOUDS.getValue())) {
            verify(PlatformLimit.CONCURRENT_BROWSER_CLOUDS + " limit", () -> {
                loadGeneratorContext.getLimitsApi().verifyLimits(
                        Map.of(PlatformLimit.CONCURRENT_BROWSER_CLOUDS.getValue(), 1)
                );
            });
        }

        if (limits.containsKey(PlatformLimit.CONCURRENT_BROWSERS.getValue())) {
            verify(PlatformLimit.CONCURRENT_BROWSERS + " limit", () -> {
                loadGeneratorContext.getLimitsApi().verifyLimits(
                        Map.of(PlatformLimit.CONCURRENT_BROWSERS.getValue(), concurrency)
                );
            });
        }

        if (limits.containsKey(PlatformLimit.BROWSER_CLOUD_DURATION_HOURS.getValue())) {
            verify(PlatformLimit.BROWSER_CLOUD_DURATION_HOURS + " limit", () -> {
                loadGeneratorContext.getLimitsApi().verifyLimits(
                        Map.of(PlatformLimit.BROWSER_CLOUD_DURATION_HOURS.getValue(), duration)
                );
            });
        }

        verify("credits balance", () -> {
            CreditsBalance balance = loadGeneratorContext.getCreditsApi().getCreditsBalance();
            int availableCredits = balance.getAvailableCredits();
            int requiredCredits = duration * concurrency;

            if (availableCredits < requiredCredits) {
                throw new RuntimeException(
                        "Insufficient credits balance: required credits = " + requiredCredits + ", available credits = " + availableCredits
                );
            }
        });

        verify("overall limits and balance all together", () -> {
            loadGeneratorContext.getLimitsApi().verifyLimits(Map.of(
                    PlatformLimit.CONCURRENT_BROWSER_CLOUDS.getValue(), 1,
                    PlatformLimit.CONCURRENT_BROWSERS.getValue(), concurrency,
                    PlatformLimit.BROWSER_CLOUD_DURATION_HOURS.getValue(), duration
            ));
        });
    }

    private Execution createExecution(
            LoadGeneratorContextImpl loadGeneratorContext,
            String projectUuid,
            String executionNotes
    ) throws ApiException {
        Execution execution = new Execution();
        execution.setNotes(executionNotes);

        return loadGeneratorContext.getExecutionsApi().createExecution(
                projectUuid,
                execution
        );
    }

    private List<BrowserCloud> listBrowserClouds(LoadGeneratorContextImpl loadGeneratorContext, String projectKey, String executionKey) throws ApiException {
        return loadGeneratorContext.getBrowserCloudsApi().listBrowserClouds(
                projectKey,
                executionKey
        );
    }

    private BrowserCloudDetails getBrowserCloudDetails(LoadGeneratorContextImpl loadGeneratorContext, String projectKey, String executionKey, String browserCloudUuid) throws ApiException {
        return loadGeneratorContext.getBrowserCloudsApi().getBrowserCloudDetails(
                projectKey,
                executionKey,
                browserCloudUuid
        );
    }

    private BrowserCloud createBrowserCloud(LoadGeneratorContextImpl loadGeneratorContext, String projectKey, String executionKey, int concurrency, int duration, boolean usePreAllocatedIPs, List<String> dataCapturingExcludes, Map<String, String> browserCloudHttpHeaders, Map<String, String> browserCloudHosts) throws ApiException {
        BrowserCloud payload = new BrowserCloud();
        payload.setConcurrency(concurrency);
        payload.duration(duration);
        payload.setUsePreAllocatedIPs(usePreAllocatedIPs);
        payload.setDataCapturingExcludes(dataCapturingExcludes);
        payload.setHttpHeaders(browserCloudHttpHeaders);
        payload.setHosts(browserCloudHosts);
        
        payload.setDataCapturingIncludeRequestHeaders(
                loadGeneratorContext.getLoadGeneratorConfig().isDataCapturingIncludeRequestHeaders()
        );
        payload.setDataCapturingIncludeRequestBody(
                loadGeneratorContext.getLoadGeneratorConfig().isDataCapturingIncludeRequestBody()
        );
        payload.setDataCapturingIncludeResponseHeaders(
                loadGeneratorContext.getLoadGeneratorConfig().isDataCapturingIncludeResponseHeaders()
        );
        payload.setDataCapturingIncludeResponseBody(
                loadGeneratorContext.getLoadGeneratorConfig().isDataCapturingIncludeResponseBody()
        );

        return loadGeneratorContext.getBrowserCloudsApi().createBrowserCloud(
                projectKey,
                executionKey,
                payload
        );
    }

    private void terminateBrowserCloud(LoadGeneratorContextImpl loadGeneratorContext, String projectKey, String executionKey, String browserCloudUuid) throws ApiException {
        LOGGER.info("Terminating browser cloud -> {}", browserCloudUuid);
        loadGeneratorContext.getBrowserCloudsApi().terminateBrowserCloud(
                projectKey,
                executionKey,
                browserCloudUuid
        );
        LOGGER.info("Browser cloud has been successfully terminated -> {}", browserCloudUuid);
    }

    //TODO: create configurable property LoadGenerator.executionNotesGeneratorClass
    private String generateExecutionNotes(LoadGeneratorContextImpl loadGeneratorContext, List<SuiteConfig> suiteConfigs) {
        List<String> details = new ArrayList<>();
        details.add("<p><span class=\"text-gray-700\">Load Generator Details:</span></p>");
        
        for (SuiteConfig suiteConfig: suiteConfigs){
            long suiteConfigIterations = suiteConfig.getIterations();
            String iterationsInfo = "";
            if (suiteConfigIterations != SuiteConfig.DEFAULT_ITERATIONS) {
                iterationsInfo
                        = "<p><span class=\"text-gray-700\">iterations</span>: "
                        + suiteConfigIterations
                        + "</p>";
            }
            
            String webDriverConcurrencyInfo = "";
            if (suiteConfig.getWebDriverConcurrency() != null) {
                webDriverConcurrencyInfo
                        = "<p><span class=\"text-gray-700\">webDriverConcurrency</span>: "
                        + suiteConfig.getWebDriverConcurrency()
                        + "</p>";
            }
            
            details.add(
                    new StringBuilder()
                            .append("<p><span class=\"text-gray-700\">suite</span>: ")
                            .append(suiteConfig.getName())
                            .append("</p>")
                            .append("<p><span class=\"text-gray-700\">webDriverMode</span>: ")
                            .append(suiteConfig.getWebDriverMode())
                            .append("</p>")
                            .append("<p><span class=\"text-gray-700\">concurrency</span>: ")
                            .append(suiteConfig.getConcurrency())
                            .append("</p>")
                            .append(webDriverConcurrencyInfo)
                            .append(iterationsInfo)
                            .append("<p><span class=\"text-gray-700\">duration</span>: ")
                            .append(suiteConfig.getDuration().toString().toLowerCase().replace("pt", ""))
                            .append("</p>")
                            .append("<p><span class=\"text-gray-700\">delay</span>: ")
                            .append(suiteConfig.getDelay().toString().toLowerCase().replace("pt", ""))
                            .append("</p>")
                            .append("<p><span class=\"text-gray-700\">rampUp</span>: ")
                            .append(suiteConfig.getRampUp().toString().toLowerCase().replace("pt", ""))
                            .append("</p>")
                            .append("<p><span class=\"text-gray-700\">rampDown</span>: ")
                            .append(suiteConfig.getRampDown().toString().toLowerCase().replace("pt", ""))
                            .append("</p>")
                            .toString()
            );
        }

        return details.stream().collect(Collectors.joining("<p><span class=\"text-gray-700\">---</span></p>"));
    }

    @FunctionalInterface
    private interface ApiRunnable {
        void run() throws ApiException;
    }

    private class StatusChecker extends TimerTask {

        private final LoadGeneratorContextImpl loadGeneratorContext;

        public StatusChecker(LoadGeneratorContextImpl loadGeneratorContext) {
            this.loadGeneratorContext = loadGeneratorContext;
        }

        @Override
        public void run() {
            BrowserCloudDetails details;

            try {
                details = getBrowserCloudDetails(
                        loadGeneratorContext,
                        loadGeneratorContext.getBrowserCloudContext().getProjectKey(),
                        loadGeneratorContext.getBrowserCloudContext().getExecutionKey(),
                        loadGeneratorContext.getBrowserCloudContext().getBrowserCloudKey()
                );
            } catch (ApiException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(
                            "Can't get browser cloud status, {}",
                            loadGeneratorContext.getBrowserCloudContext().getBrowserCloudKey(),
                            e
                    );
                }
                return;
            }

            loadGeneratorContext.setBrowserCloudContext(
                    new BrowserCloudContextImpl(
                            loadGeneratorContext.getBrowserCloudContext().getProjectKey(),
                            loadGeneratorContext.getBrowserCloudContext().getExecutionKey(),
                            details
                    )
            );

            if ((details.getStatus().equalsIgnoreCase("terminating") ||
                    details.getStatus().equalsIgnoreCase("terminated"))) {
                shutdownHook.run();
            }
        }
    }
}
