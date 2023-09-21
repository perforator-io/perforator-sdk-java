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
package io.perforator.sdk.loadgenerator.core;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.AnalyticsApi;
import io.perforator.sdk.api.okhttpgson.operations.BrowserCloudsApi;
import io.perforator.sdk.api.okhttpgson.operations.ExecutionsApi;
import io.perforator.sdk.api.okhttpgson.operations.ProjectsApi;
import io.perforator.sdk.loadgenerator.core.configs.ChromeMode;
import io.perforator.sdk.loadgenerator.core.configs.Config;
import io.perforator.sdk.loadgenerator.core.configs.ConfigBuilder;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import io.perforator.sdk.loadgenerator.core.mock.IntegrationServiceMock;
import io.perforator.sdk.loadgenerator.core.mock.RemoteWebDriverContextMock;
import io.perforator.sdk.loadgenerator.core.service.IntegrationService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractLoadGeneratorTest<L extends AbstractLoadGenerator, C extends Config, S extends Config> {
    
    private static final String SUITE_MIN_DURATION_PROPERTY = "io.perforator.sdk.loadgenerator.core.internal.SuiteManagerImpl.minDuration";
    
    protected static final boolean CHROME_BROWSER_AVAILABLE = isChromeBrowserAvailable();
    protected static final String API_BASE_URL_PROPERTY = (LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "_" + LoadGeneratorConfig.Fields.apiBaseUrl).toUpperCase();
    protected static final String API_CLIENT_ID_PROPERTY = (LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "_" + LoadGeneratorConfig.Fields.apiClientId).toUpperCase();
    protected static final String API_CLIENT_SECRET_PROPERTY = (LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "_" + LoadGeneratorConfig.Fields.apiClientSecret).toUpperCase();
    protected static final String PROJECT_KEY_PROPERTY = (LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "_" + LoadGeneratorConfig.Fields.projectKey).toUpperCase();

    protected final Class<L> loadGeneratorClass;
    protected final Class<C> loadGeneratorConfigClass;
    protected final Class<S> suiteConfigClass;

    protected String apiBaseUrl;
    protected String apiClientId;
    protected String apiClientSecret;
    protected String projectKey;
    protected String verificationsBaseUrl;

    protected ApiClientBuilder apiClientBuilder;
    protected ProjectsApi projectsApi;
    protected ExecutionsApi executionsApi;
    protected BrowserCloudsApi browserCloudsApi;
    protected AnalyticsApi analyticsApi;

    public AbstractLoadGeneratorTest(Class<L> loadGeneratorClass, Class<C> loadGeneratorConfigClass, Class<S> suiteConfigClass) {
        this.loadGeneratorClass = loadGeneratorClass;
        this.loadGeneratorConfigClass = loadGeneratorConfigClass;
        this.suiteConfigClass = suiteConfigClass;
        
        assertTrue(LoadGeneratorConfig.class.isAssignableFrom(loadGeneratorConfigClass));
        assertTrue(SuiteConfig.class.isAssignableFrom(suiteConfigClass));
    }

    //TODO: implement test verifying javadocs
    @BeforeAll
    public void setup() throws Exception {
        assumeTrue(hasRequiredProperty(API_BASE_URL_PROPERTY));
        assumeTrue(hasRequiredProperty(API_CLIENT_ID_PROPERTY));
        assumeTrue(hasRequiredProperty(API_CLIENT_SECRET_PROPERTY));
        assumeTrue(hasRequiredProperty(PROJECT_KEY_PROPERTY));

        apiBaseUrl = getRequiredProperty(API_BASE_URL_PROPERTY);
        apiClientId = getRequiredProperty(API_CLIENT_ID_PROPERTY);
        apiClientSecret = getRequiredProperty(API_CLIENT_SECRET_PROPERTY);
        projectKey = getRequiredProperty(PROJECT_KEY_PROPERTY);
        verificationsBaseUrl = apiBaseUrl.replace("api", "verifications");

        apiClientBuilder = new ApiClientBuilder(apiClientId, apiClientSecret, apiBaseUrl);
        projectsApi = apiClientBuilder.getApi(ProjectsApi.class);
        executionsApi = apiClientBuilder.getApi(ExecutionsApi.class);
        browserCloudsApi = apiClientBuilder.getApi(BrowserCloudsApi.class);
        analyticsApi = apiClientBuilder.getApi(AnalyticsApi.class);
        
        System.setProperty(SUITE_MIN_DURATION_PROPERTY, Duration.ofMillis(1).toString());
    }
    
    @AfterAll
    public void tearDown() throws Exception {
        System.clearProperty(SUITE_MIN_DURATION_PROPERTY);
    }

    @Test
    public void verifyInstantiationRequirements() throws Exception {
        assertNotNull(loadGeneratorClass);
        assertNotNull(loadGeneratorConfigClass);
        assertNotNull(suiteConfigClass);

        assertTrue(AbstractLoadGenerator.class.isAssignableFrom(loadGeneratorClass));
        assertTrue(LoadGeneratorConfig.class.isAssignableFrom(loadGeneratorConfigClass));
        assertTrue(SuiteConfig.class.isAssignableFrom(suiteConfigClass));

        Constructor<L> defaultConstructor = getDefaultConstructor();
        assertNotNull(defaultConstructor);
        assertTrue(Modifier.isPublic(defaultConstructor.getModifiers()));
        assertNotNull(getDefaultInstance());

        Constructor<L> customConstructor = getCustomConstructor();
        assertNotNull(customConstructor);
        assertTrue(Modifier.isPrivate(customConstructor.getModifiers()));
        assertNotNull(getCustomInstance(new IntegrationServiceMock()));
    }

    @Test
    public void verifyLocalLoadGeneratorWithMockedServices() throws Exception {
        assumeTrue(CHROME_BROWSER_AVAILABLE);
        int concurrency = getDefaultConcurrency();

        Map<String, String> suiteParams = Map.of(
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.duration, "0.1s",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.concurrency, concurrency + "",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.iterations, concurrency + "",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampUp, "0s",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampDown, "0s",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverMode, WebDriverMode.local.name(),
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.chromeMode, ChromeMode.headless.name()
        );

        IntegrationServiceMock mediationService = new IntegrationServiceMock();
        C loadGeneratorConfig = defaultLoadGeneratorConfigBuilder().buildWithDefaults();
        S suiteConfig = defaultSuiteConfigBuilder().buildWithDefaults(suiteParams::get);

        L loadGenerator = getCustomInstance(
                mediationService,
                loadGeneratorConfig,
                suiteConfig
        );

        List<String> executionsBeforeRun = getExecutionList(projectKey);

        loadGenerator.run();

        assertTrue(mediationService.isStarted());
        assertTrue(mediationService.isFinished());
        assertEquals(0, mediationService.getActiveSuiteInstancesCount());
        assertEquals(0, loadGenerator.getActiveSuiteInstancesCount());
        assertEquals(0, mediationService.getFailedSuiteInstancesCount());
        assertEquals(0, loadGenerator.getFailedSuiteInstancesCount());
        assertEquals(concurrency, mediationService.getSuccessfulSuiteInstancesCount());
        assertEquals(concurrency, loadGenerator.getSuccessfulSuiteInstancesCount());

        assertTrue(mediationService.getActiveTransactions().isEmpty());
        assertEquals(0, loadGenerator.getActiveTransactionsCount());
        assertTrue(mediationService.getFailedTransactions().isEmpty());
        assertEquals(0, loadGenerator.getFailedTransactionsCount());
        assertTrue(mediationService.getSuccessfulTransactions().size() > 0);
        assertTrue(loadGenerator.getSuccessfulTransactionsCount() > 0);
        assertEquals(mediationService.getSuccessfulTransactions().size(), loadGenerator.getSuccessfulTransactionsCount());

        assertEquals(concurrency, mediationService.getAllRemoteWebDriverContexts().size());
        for (RemoteWebDriverContextMock context : mediationService.getAllRemoteWebDriverContexts()) {
            assertNotNull(context);
            assertNotNull(context.getRemoteWebDriver());
            assertNull(context.getRemoteWebDriver().getSessionId());
        }

        List<String> executionsAfterRun = getExecutionList(projectKey);
        assertTrue(executionsBeforeRun.containsAll(executionsAfterRun));
    }

    @Test
    public void verifyLocalLoadGeneratorWithDefaultMediator() throws Exception {
        assumeTrue(CHROME_BROWSER_AVAILABLE);
        int concurrency = getDefaultConcurrency();

        Map<String, String> suiteParams = Map.of(
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.name, "suite-name-" + UUID.randomUUID(),
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.duration, "0.1s",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.concurrency, concurrency + "",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.iterations, concurrency + "",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampUp, "0s",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampDown, "0s",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverMode, WebDriverMode.local.name(),
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.chromeMode, ChromeMode.headless.name()
        );

        C loadGeneratorConfig = defaultLoadGeneratorConfigBuilder().buildWithDefaults();
        S suiteConfig = defaultSuiteConfigBuilder().buildWithDefaults(suiteParams::get);
        L loadGenerator = getDefaultInstance(
                loadGeneratorConfig,
                suiteConfig
        );

        List<String> executionsBeforeRun = getExecutionList(projectKey);

        loadGenerator.run();

        assertEquals(0, loadGenerator.getActiveSuiteInstancesCount());
        assertEquals(0, loadGenerator.getFailedSuiteInstancesCount());
        assertEquals(concurrency, loadGenerator.getSuccessfulSuiteInstancesCount());
        assertEquals(0, loadGenerator.getActiveTransactionsCount());
        assertEquals(0, loadGenerator.getFailedTransactionsCount());
        assertTrue(loadGenerator.getSuccessfulTransactionsCount() > 0);

        List<String> executionsAfterRun = getExecutionList(projectKey);
        assertTrue(executionsBeforeRun.containsAll(executionsAfterRun));
    }

    @Test
    public void verifyLoadGeneratorWithBrowsersInTheCloud() throws Exception {
        LoadGeneratorRunContext resultWithoutExcludes = runLoadGeneratorWithRemoteBrowsersAndAwaitMetrics();
        
        assertNotNull(resultWithoutExcludes);
        assertNotNull(resultWithoutExcludes.getLoadGenerator());
        assertNotNull(resultWithoutExcludes.getLoadGeneratorConfig());
        assertNotNull(resultWithoutExcludes.getSuiteConfig());
        assertNotNull(resultWithoutExcludes.getProjectKey());
        assertNotNull(resultWithoutExcludes.getExecutionKey());
        assertNotNull(resultWithoutExcludes.getBrowserCloudKey());
        assertNotNull(resultWithoutExcludes.getRequestsStatistics());
        assertFalse(resultWithoutExcludes.getRequestsStatistics().isEmpty());
        assertNotNull(resultWithoutExcludes.getTransactionsStatistics());
        assertFalse(resultWithoutExcludes.getTransactionsStatistics().isEmpty());
        
        LoadGeneratorRunContext resultWithExcludes = runLoadGeneratorWithRemoteBrowsersAndAwaitMetrics("*/assets/*");
        
        assertNotNull(resultWithExcludes);
        assertNotNull(resultWithExcludes.getLoadGenerator());
        assertNotNull(resultWithExcludes.getLoadGeneratorConfig());
        assertNotNull(resultWithExcludes.getSuiteConfig());
        assertNotNull(resultWithExcludes.getProjectKey());
        assertNotNull(resultWithExcludes.getExecutionKey());
        assertNotNull(resultWithExcludes.getBrowserCloudKey());
        assertNotNull(resultWithExcludes.getRequestsStatistics());
        assertFalse(resultWithExcludes.getRequestsStatistics().isEmpty());
        assertNotNull(resultWithExcludes.getTransactionsStatistics());
        assertFalse(resultWithExcludes.getTransactionsStatistics().isEmpty());
        
        boolean assetsFounds = false;
        for (String requestURL : resultWithoutExcludes.getRequestsStatistics().keySet()) {
            if(requestURL.contains("/assets/")) {
                assetsFounds = true;
                assertFalse(
                        resultWithExcludes.getRequestsStatistics().containsKey(requestURL),
                        "Requests " + requestURL + " should not be captured"
                );
            } else {
                assertEquals(
                        resultWithoutExcludes.getRequestsStatistics().get(requestURL), 
                        resultWithExcludes.getRequestsStatistics().get(requestURL),
                        "Requests count for " + requestURL + " should be equal"
                );
            }
        }
        
        assertTrue(
                assetsFounds, 
                "Requests statistics should contain requests with '/assets/' path"
        );
    }
    
    protected LoadGeneratorRunContext runLoadGeneratorWithRemoteBrowsersAndAwaitMetrics(String... dataCapturingExcludes) throws Exception {
        int concurrency = 4;

        Map<String, String> suiteParams = Map.of(
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.name, "suite-name-" + UUID.randomUUID(),
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.duration, "2s",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.concurrency, concurrency + "",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.iterations, concurrency + "",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampUp, "0s",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampDown, "0s",
                SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverMode, WebDriverMode.cloud.name()
        );

        C loadGeneratorConfig;
        if(dataCapturingExcludes != null && dataCapturingExcludes.length > 0) {
            Map<String, String> exludeParams = Map.of(
                    LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.dataCapturingExcludes,
                    Arrays.stream(dataCapturingExcludes).collect(Collectors.joining(","))
            );
            loadGeneratorConfig = defaultLoadGeneratorConfigBuilder().buildWithDefaults(exludeParams::get);
        } else {
            loadGeneratorConfig = defaultLoadGeneratorConfigBuilder().buildWithDefaults();
        }
        
        S suiteConfig = defaultSuiteConfigBuilder().buildWithDefaults(suiteParams::get);
        
        L loadGenerator = getDefaultInstance(
                loadGeneratorConfig,
                suiteConfig
        );
        List<String> executionsBeforeRun = getExecutionList(projectKey);
        loadGenerator.run();
        List<String> executionsAfterRun = getExecutionList(projectKey);

        assertEquals(executionsBeforeRun.size() + 1, executionsAfterRun.size());
        String executionKey = null;
        String browserCloudKey = null;
        for(String e: executionsAfterRun){
            if(!executionsBeforeRun.contains(e)){
                executionKey = e;
                browserCloudKey = getBrowserCloudList(projectKey, e).stream().findFirst().orElse(null);
                break;
            }
        }

        assertNotNull(browserCloudKey);

        String status = browserCloudsApi.getBrowserCloudDetails(projectKey, executionKey, browserCloudKey).getStatus();
        assertTrue(status.equalsIgnoreCase("terminating") || status.equalsIgnoreCase("terminated"));

        assertEquals(0, loadGenerator.getActiveSuiteInstancesCount());
        assertEquals(0, loadGenerator.getFailedSuiteInstancesCount());
        assertTrue(loadGenerator.getSuccessfulSuiteInstancesCount() > 0);
        assertEquals(0, loadGenerator.getActiveTransactionsCount());
        assertEquals(0, loadGenerator.getFailedTransactionsCount());
        assertTrue(loadGenerator.getSuccessfulTransactionsCount() > 0);

        long allTransactionsCount = loadGenerator.getSuccessfulTransactionsCount() 
                + loadGenerator.getFailedTransactionsCount() 
                + loadGenerator.getActiveTransactionsCount();
        long endTime = System.currentTimeMillis() + 90000;
        Map<String, Long> transactionStats = null;
        while (System.currentTimeMillis() < endTime) {
            transactionStats = getTransactionsStatistics(executionKey);

            if (transactionStats.get(TransactionsBasicMetrics.STATUS_IN_PROGRESS_COUNT.getValue()) == 0 
                    && transactionStats.get(TransactionsBasicMetrics.COUNT.getValue()) == allTransactionsCount) {
                break;
            } else {
                Threaded.sleep(1000);
            }
        }
        
        assertEquals(
                allTransactionsCount,
                transactionStats.get(TransactionsBasicMetrics.COUNT.getValue()),
                "Inconsistency in all transactions count"
        );

        assertEquals(
                loadGenerator.getFailedTransactionsCount(),
                transactionStats.get(TransactionsBasicMetrics.STATUS_FAILED_COUNT.getValue()),
                "Inconsistency in failed transactions count"
        );
        assertEquals(
                loadGenerator.getSuccessfulTransactionsCount(),
                transactionStats.get(TransactionsBasicMetrics.STATUS_SUCCESSFUL_COUNT.getValue()),
                "Inconsistency in successful transactions count"
        );
        assertEquals(
                loadGenerator.getActiveTransactionsCount(),
                transactionStats.get(TransactionsBasicMetrics.STATUS_IN_PROGRESS_COUNT.getValue()),
                "Inconsistency in active transactions count"
        );
        
        Map<String, Long> requestStats = getRequestsStatistics(executionKey);
        assertNotNull(requestStats);
        assertFalse(requestStats.isEmpty());
        
        LoadGeneratorRunContext result = new LoadGeneratorRunContext();
        
        result.setLoadGenerator(loadGenerator);
        result.setLoadGeneratorConfig(loadGeneratorConfig);
        result.setSuiteConfig(suiteConfig);
        result.setProjectKey(projectKey);
        result.setExecutionKey(executionKey);
        result.setBrowserCloudKey(browserCloudKey);
        result.setTransactionsStatistics(transactionStats);
        result.setRequestsStatistics(requestStats);
        
        return result;
    }
    
    protected Map<String, Long> getRequestsStatistics(String executionKey) throws Exception {
        AnalyticsGroupedStatisticsRequest statsRequest = new AnalyticsGroupedStatisticsRequest();
        statsRequest.setNamespace(
                AnalyticsNamespace.REQUESTS.getValue()
        );
        statsRequest.setMetrics(List.of(
                RequestsBasicMetrics.COUNT.getValue()
        ));
        statsRequest.setGroupBy(List.of(
                RequestsField.REQUEST_URL.getValue()
        ));
        statsRequest.setGroupingFlags(Map.of(
                RequestsField.REQUEST_URL.getValue(), 
                Map.of(RequestsGroupingFlag.IGNORE_URL_PARAMS.getValue(), true)
        ));
        
        AnalyticsGroupedStatisticsResult statsResponse = analyticsApi.getGroupedStatistics(
                projectKey, 
                executionKey, 
                statsRequest
        );
        assertNotNull(statsResponse);
        assertNotNull(statsResponse.getResults());
        
        Map<String, Long> result = new HashMap<>();
        
        for (Map<String, Object> fields : statsResponse.getResults()) {
            assertNotNull(fields);
            assertEquals(2, fields.size());
            
            String requestURL = (String)fields.get(RequestsField.REQUEST_URL.getValue());
            assertNotNull(requestURL);
            
            Object requestsCountUncasted = fields.get(RequestsBasicMetrics.COUNT.getValue());
            assertNotNull(requestsCountUncasted);
            
            if(Number.class.isAssignableFrom(requestsCountUncasted.getClass())) {
                result.put(requestURL, ((Number)requestsCountUncasted).longValue());
            } else {
                fail(
                        RequestsBasicMetrics.COUNT.getValue()
                        + " metric should be a number, but it is "
                        + requestsCountUncasted.getClass()
                );
            }
        }
        
        return result;
    }

    protected Map<String, Long> getTransactionsStatistics(String executionKey) throws Exception {
        AnalyticsOverallStatisticsRequest request = new AnalyticsOverallStatisticsRequest();
        request.setNamespace(AnalyticsNamespace.TRANSACTIONS.getValue());
        request.setMetrics(Arrays.asList(
                TransactionsBasicMetrics.COUNT.getValue(),
                TransactionsBasicMetrics.STATUS_SUCCESSFUL_COUNT.getValue(),
                TransactionsBasicMetrics.STATUS_FAILED_COUNT.getValue(),
                TransactionsBasicMetrics.STATUS_IN_PROGRESS_COUNT.getValue()
        ));

        List<AnalyticsOverallStatisticsResult> payloads = analyticsApi.getOverallStatistics(
                projectKey, executionKey, List.of(request)
        );

        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : payloads.get(0).getResults().entrySet()) {
            if (entry.getValue() == null) {
                fail("Transaction metrics " + entry.getKey() + " should not be null");
            } else {
                result.put(entry.getKey(), entry.getValue().longValue());
            }
        }

        return result;
    }

    protected int getDefaultConcurrency() {
        return 4;
    }

    protected Constructor<L> getDefaultConstructor() throws Exception {
        return loadGeneratorClass.getDeclaredConstructor(
                loadGeneratorConfigClass,
                List.class
        );
    }

    protected L getDefaultInstance() throws Exception {
        return getDefaultInstance(
                defaultLoadGeneratorConfigBuilder().buildWithDefaults(),
                defaultSuiteConfigBuilder().buildWithDefaults()
        );
    }

    protected L getDefaultInstance(C loadGeneratorConfig, S suiteConfig) throws Exception {
        return getDefaultConstructor().newInstance(
                loadGeneratorConfig,
                List.of(suiteConfig)
        );
    }

    protected Constructor<L> getCustomConstructor() throws Exception {
        return loadGeneratorClass.getDeclaredConstructor(IntegrationService.class,
                loadGeneratorConfigClass,
                List.class
        );
    }

    protected L getCustomInstance(
            IntegrationService mediationService
    ) throws Exception {
        return getCustomInstance(
                mediationService,
                defaultLoadGeneratorConfigBuilder().buildWithDefaults(),
                defaultSuiteConfigBuilder().buildWithDefaults()
        );
    }

    protected L getCustomInstance(
            IntegrationService mediationService,
            C loadGeneratorConfig,
            S suiteConfig
    ) throws Exception {
        Constructor<L> customConstructor = getCustomConstructor();
        customConstructor.setAccessible(true);

        try {
            return customConstructor.newInstance(
                    mediationService,
                    loadGeneratorConfig,
                    List.of(suiteConfig)
            );
        } finally {
            customConstructor.setAccessible(false);
        }
    }

    protected ConfigBuilder<C,?> defaultLoadGeneratorConfigBuilder() throws Exception {
        ConfigBuilder<C,?> result = (ConfigBuilder<C,?>) getBuilderMethod(loadGeneratorConfigClass).invoke(null);
        return result.applyDefaults();
    }

    protected ConfigBuilder<S,?> defaultSuiteConfigBuilder() throws Exception {
        ConfigBuilder<S,?> result = (ConfigBuilder<S,?>) getBuilderMethod(suiteConfigClass).invoke(null);
        return result.applyDefaults();
    }
    
    protected static Method getBuilderMethod(Class configClass) {
        for (Method method : configClass.getDeclaredMethods()) {
            if(!method.getName().equals("builder")) {
                continue;
            }
            
            if(!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            
            if(method.getParameterCount() > 0) {
                continue;
            }
            
            return method;
        }
        
        throw new IllegalArgumentException(
                "Config " + configClass + " doesn't have static builder method"
        );
    }

    protected boolean hasRequiredProperty(String key) {
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return true;
        }

        value = System.getenv(key);
        return value != null && !value.isBlank();
    }

    protected String getRequiredProperty(String key) {
        String result = System.getProperty(key);
        if (result != null && !result.isBlank()) {
            return result.trim();
        }

        result = System.getenv(key);
        if (result != null && !result.isBlank()) {
            return result.trim();
        }

        throw new IllegalArgumentException("Required system/env property " + key + " is not defined");
    }

    private List<String> getExecutionList(String projectKey) throws ApiException {
        return executionsApi.listExecutions(projectKey)
                .stream()
                .map(Execution::getUuid)
                .collect(Collectors.toList());
    }

    private List<String> getBrowserCloudList(String projectKey, String executionKey) throws ApiException {
        return browserCloudsApi.listBrowserClouds(projectKey, executionKey)
                .stream()
                .map(BrowserCloud::getUuid)
                .collect(Collectors.toList());
    }

    private static boolean isChromeBrowserAvailable() {
        RemoteWebDriver driver = null;
        try {
            driver = RemoteWebDriverHelper.createLocalChromeDriver(
                    null, 
                    SuiteConfig.builder()
                            .webDriverMode(WebDriverMode.local)
                            .chromeMode(ChromeMode.headless)
                            .build()
            );
            return true;
        } catch(Exception e) {
            return false;
        } finally {
            if(driver != null) {
                driver.quit();
            }
        }
    }
    
    protected class LoadGeneratorRunContext {
        
        private L loadGenerator;
        private C loadGeneratorConfig;
        private S suiteConfig;
        private String projectKey;
        private String executionKey;
        private String browserCloudKey;
        private Map<String, Long> requestsStatistics;
        private Map<String, Long> transactionsStatistics;

        public L getLoadGenerator() {
            return loadGenerator;
        }

        public void setLoadGenerator(L loadGenerator) {
            this.loadGenerator = loadGenerator;
        }

        public C getLoadGeneratorConfig() {
            return loadGeneratorConfig;
        }

        public void setLoadGeneratorConfig(C loadGeneratorConfig) {
            this.loadGeneratorConfig = loadGeneratorConfig;
        }

        public S getSuiteConfig() {
            return suiteConfig;
        }

        public void setSuiteConfig(S suiteConfig) {
            this.suiteConfig = suiteConfig;
        }

        public String getProjectKey() {
            return projectKey;
        }

        public void setProjectKey(String projectKey) {
            this.projectKey = projectKey;
        }

        public String getExecutionKey() {
            return executionKey;
        }

        public void setExecutionKey(String executionKey) {
            this.executionKey = executionKey;
        }

        public String getBrowserCloudKey() {
            return browserCloudKey;
        }

        public void setBrowserCloudKey(String browserCloudKey) {
            this.browserCloudKey = browserCloudKey;
        }

        public Map<String, Long> getRequestsStatistics() {
            return requestsStatistics;
        }

        public void setRequestsStatistics(Map<String, Long> requestsStatistics) {
            this.requestsStatistics = requestsStatistics;
        }

        public Map<String, Long> getTransactionsStatistics() {
            return transactionsStatistics;
        }

        public void setTransactionsStatistics(Map<String, Long> transactionsStatistics) {
            this.transactionsStatistics = transactionsStatistics;
        }
        
    }

}
