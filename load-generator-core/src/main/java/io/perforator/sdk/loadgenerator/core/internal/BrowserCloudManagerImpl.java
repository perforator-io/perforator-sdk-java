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
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
        if (isLocalOnly(loadGeneratorContext)) {
            return;
        }

        List<SuiteConfig> suiteConfigs = loadGeneratorContext.getSuiteConfigs();
        LoadGeneratorConfig loadGeneratorConfig = loadGeneratorContext.getLoadGeneratorConfig();

        int concurrency = suiteConfigs.stream()
                .filter(suite -> suite.getWebDriverMode() == WebDriverMode.cloud)
                .mapToInt(SuiteConfig::getConcurrency)
                .sum();

        int requiredDuration = suiteConfigs.stream()
                .filter(suite -> suite.getWebDriverMode() == WebDriverMode.cloud)
                .mapToInt(e -> (int) e.getDuration().toMillis() / 1000)
                .max()
                .orElse(0);

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
        String browserCloudKey;

        verifyProjectExists(loadGeneratorContext, projectKey);

        if (executionKey != null && !executionKey.isBlank()) {
            verifyExecutionExists(loadGeneratorContext, projectKey, executionKey);
        } else {
            try {
                Execution execution = createExecution(
                        loadGeneratorContext,
                        projectKey,
                        generateExecutionNotes(loadGeneratorContext, suiteConfigs)
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
                    dataCapturingExcludes
            );

            LOGGER.info("Created browser cloud {}", browserCloud.getUuid());

            if(dataCapturingExcludes != null && !dataCapturingExcludes.isEmpty()){
                LOGGER.warn(
                        "Browser cloud will avoid capturing the following HTTP requests: {}",
                        dataCapturingExcludes
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

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        if (isLocalOnly(loadGeneratorContext)) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        cleanup(loadGeneratorContext);
    }

    private boolean isLocalOnly(LoadGeneratorContextImpl loadGeneratorContext) {
        return loadGeneratorContext.getSuiteConfigs()
                .stream()
                .noneMatch(suite -> suite.getWebDriverMode() == WebDriverMode.cloud);
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

        if (limits.containsKey(PlatformLimits.CONCURRENT_BROWSER_CLOUDS.name())) {
            verify(PlatformLimits.CONCURRENT_BROWSER_CLOUDS + " limit", () -> {
                loadGeneratorContext.getLimitsApi().verifyLimits(
                        Map.of(PlatformLimits.CONCURRENT_BROWSER_CLOUDS.name(), 1)
                );
            });
        }

        if (limits.containsKey(PlatformLimits.CONCURRENT_BROWSERS.name())) {
            verify(PlatformLimits.CONCURRENT_BROWSERS + " limit", () -> {
                loadGeneratorContext.getLimitsApi().verifyLimits(
                        Map.of(PlatformLimits.CONCURRENT_BROWSERS.name(), concurrency)
                );
            });
        }

        if (limits.containsKey(PlatformLimits.BROWSER_CLOUD_DURATION_HOURS.name())) {
            verify(PlatformLimits.BROWSER_CLOUD_DURATION_HOURS + " limit", () -> {
                loadGeneratorContext.getLimitsApi().verifyLimits(
                        Map.of(PlatformLimits.BROWSER_CLOUD_DURATION_HOURS.name(), duration)
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
                    PlatformLimits.CONCURRENT_BROWSER_CLOUDS.name(), 1,
                    PlatformLimits.CONCURRENT_BROWSERS.name(), concurrency,
                    PlatformLimits.BROWSER_CLOUD_DURATION_HOURS.name(), duration
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

    private BrowserCloud createBrowserCloud(LoadGeneratorContextImpl loadGeneratorContext, String projectKey, String executionKey, int concurrency, int duration, boolean usePreAllocatedIPs, List<String> dataCapturingExcludes) throws ApiException {
        BrowserCloud payload = new BrowserCloud();
        payload.setConcurrency(concurrency);
        payload.duration(duration);
        payload.setUsePreAllocatedIPs(usePreAllocatedIPs);
        payload.setDataCapturingExcludes(dataCapturingExcludes);
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

        StringBuilder stb = new StringBuilder();
        int estimatedConcurrency = 0;
        long estimatedDuration = 0;

        StringBuilder suitesInfoStb = new StringBuilder();
        for (SuiteConfig suiteConfig: suiteConfigs){
            if(suiteConfig.getWebDriverMode() != WebDriverMode.cloud){
                continue;
            }
            estimatedConcurrency += suiteConfig.getConcurrency();
            estimatedDuration = Math.max(estimatedDuration, suiteConfig.getDuration().toMillis());

            suitesInfoStb.append("<p><strong>Suite</strong>: ").append(suiteConfig.getName()).append("</p>")
                    .append("<ul>")
                    .append("<li>concurrency: ").append(suiteConfig.getConcurrency()).append("</p>")
                    .append("<li>duration: ").append(suiteConfig.getDuration().toString().toLowerCase().replace("pt", "")).append("</li>")
                    .append("<li>delay: ").append(suiteConfig.getDelay().toString().toLowerCase().replace("pt", "")).append("</li>")
                    .append("<li>rampUp: ").append(suiteConfig.getRampUp().toString().toLowerCase().replace("pt", "")).append("</li>")
                    .append("<li>rampDown: ").append(suiteConfig.getRampDown().toString().toLowerCase().replace("pt", "")).append("</li>")
                    .append("</ul>");
        }

        stb.append("<p><strong>Estimated concurrency</strong>: ").append(estimatedConcurrency).append("</p>")
                .append("<p><strong>Estimated duration</strong>: ").append(Duration.ofMillis(estimatedDuration).toString().toLowerCase().replace("pt", "")).append("</p>")
                .append("<p><strong>Suites</strong>: ").append(suiteConfigs.size()).append("</p></br>")
                .append(suitesInfoStb);


        return stb.toString();
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
