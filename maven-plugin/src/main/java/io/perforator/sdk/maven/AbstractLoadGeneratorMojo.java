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
package io.perforator.sdk.maven;

import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

abstract class AbstractLoadGeneratorMojo<SUITE_PARAMS_TYPE> extends AbstractMojo {

    @Parameter(
            defaultValue = "${project}", 
            readonly = true, 
            required = true
    )
    protected MavenProject project;

    @Parameter(
            defaultValue = "${plugin.artifacts}", 
            readonly = true, 
            required = true
    )
    protected List<Artifact> pluginDependencies;

    /**
     * Additional system properties to expose before running the test.
     */
    @Parameter(
            property = "systemProperties",
            alias = "systemProperties",
            required = false
    )
    protected Map<String, String> systemProperties;

    /**
     * Base URL for API communication.
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_API_BASE_URL,
            alias = LoadGeneratorConfig.Fields.apiBaseUrl,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.apiBaseUrl
    )
    protected String apiBaseUrl;

    /**
     * cliend_id to obtain access token via OAuth 2.0 Client Credentials Grant
     * flow.<br/>
     * It might be a case when sensitive variables should not be stored in source 
     * code according to security requirements.<br/>
     * For such cases, you can propagate this value via system properties:<br/>
     * <b>... -DloadGenerator.apiClientId=...</b><br/>
     * Also, you can propagate this property via environment variable 
     * <b>LOADGENERATOR_APICLIENTID</b>
     */
    @Parameter(
            required = false,
            alias = LoadGeneratorConfig.Fields.apiClientId,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.apiClientId
    )
    protected String apiClientId;

    /**
     * client_secret to obtain access token via OAuth 2.0 Client Credentials
     * Grant flow.<br/>
     * It might be a case when sensitive variables should not be stored in source 
     * code according to security requirements.<br/>
     * For such cases, you can propagate this value via system properties:<br/>
     * <b>... -DloadGenerator.apiClientSecret=...</b><br/>
     * Also, you can propagate this property via environment variable 
     * <b>LOADGENERATOR_APICLIENTSECRET</b>
     */
    @Parameter(
            required = false,
            alias = LoadGeneratorConfig.Fields.apiClientSecret,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.apiClientSecret
    )
    protected String apiClientSecret;

    /**
     * Key of the project where to create a new execution and a browser cloud.<br/>
     * It might be a case when sensitive variables should not be stored in source 
     * code according to security requirements.<br/>
     * For such cases, you can propagate this value via system properties:<br/>
     * <b>... -DloadGenerator.projectKey=...</b><br/>
     * Also, you can propagate this property via environment variable 
     * <b>LOADGENERATOR_PROJECTKEY</b>
     */
    @Parameter(
            required = false,
            alias = LoadGeneratorConfig.Fields.projectKey,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.projectKey
    )
    protected String projectKey;

    /**
     * Key of the execution where to create a new browser cloud.<br/>
     * A new execution is automatically created within the parent project if 
     * an executionKey is not provided.
     */
    @Parameter(
            required = false,
            alias = LoadGeneratorConfig.Fields.executionKey,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.executionKey
    )
    protected String executionKey;

    /**
     * How much time to wait till the browser cloud changes state from QUEUED to PROVISIONING?
     * <br/>
     * <b>Expected format</b>: 'h' symbol represents an hour, 
     * 'm' symbol represents a minute, 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>5m</li>
     * <li>1h</li>
     * <li>1h 20m 15s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_BROWSER_CLOUD_AWAIT_QUEUED_S,
            alias = LoadGeneratorConfig.Fields.browserCloudAwaitQueued,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.browserCloudAwaitQueued
    )
    protected String browserCloudAwaitQueued;

    /**
     * How much time to wait till the browser cloud changes state from PROVISIONING to OPERATIONAL?
     * <br/>
     * <b>Expected format</b>: 'h' symbol represents an hour,
     * 'm' symbol represents a minute, 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>5m</li>
     * <li>1h</li>
     * <li>1h 20m 15s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING_S,
            alias = LoadGeneratorConfig.Fields.browserCloudAwaitProvisioning,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.browserCloudAwaitProvisioning
    )
    protected String browserCloudAwaitProvisioning;
    
    /**
     * Time interval on how often to check browser cloud status.<br/>
     * <b>Expected format</b>: 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>0.5s</li>
     * <li>1s</li>
     * <li>3.75s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_BROWSER_CLOUD_STATUS_POLL_INTERVAL_S,
            alias = LoadGeneratorConfig.Fields.browserCloudStatusPollInterval,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.browserCloudStatusPollInterval
    )
    protected String browserCloudStatusPollInterval;

    /**
     * Should a browser cloud be turned off at the end of the test?
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_BROWSER_CLOUD_TERMINATE_AUTOMATICALY_S,
            alias = LoadGeneratorConfig.Fields.browserCloudTerminateAutomatically,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.browserCloudTerminateAutomatically
    )
    protected String browserCloudTerminateAutomatically;

    /**
     * HTTP connect timeout while establishing connection(s) with remote browsers.<br/>
     * <b>Expected format</b>: 's' symbol after the number represents seconds.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>15s</li>
     * <li>30.500s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_HTTP_CONNECT_TIMEOUT_S,
            alias = LoadGeneratorConfig.Fields.httpConnectTimeout,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.httpConnectTimeout
    )
    protected String httpConnectTimeout;

    /**
     * HTTP read timeout while awaiting response from remote browsers.<br/>
     * <b>Expected format</b>: 's' symbol after the number represents seconds.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>15s</li>
     * <li>30.500s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_HTTP_READ_TIMEOUT_S,
            alias = LoadGeneratorConfig.Fields.httpReadTimeout,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.httpReadTimeout
    )
    protected String httpReadTimeout;

    /**
     * Interval on how often to send transaction events data to API.<br/>
     * <b>Expected format</b>: 's' symbol after the number represents seconds.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>0.25s</li>
     * <li>0.5s</li>
     * <li>1s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_EVENTS_FLUSH_INTERVAL_S,
            alias = LoadGeneratorConfig.Fields.eventsFlushInterval,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.eventsFlushInterval
    )
    protected String eventsFlushInterval;

    /**
     * How many transaction events should be sent to API per one request?<br/>
     * <b>Note</b>: this value might be as high as 2000, everything else on top 
     * will be rejected on API end.
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_EVENTS_FLUSH_THRESHOLD_S,
            alias = LoadGeneratorConfig.Fields.eventsFlushThreshold,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.eventsFlushThreshold
    )
    protected String eventsFlushThreshold;

    /**
     * How often progress statistics should be reported in the log? You can turn
     * off progress reporting by specifying this value as <b>0s<b/>.<br/>
     * <b>Expected format</b>: 's' symbol after the number represents seconds.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>5s</li>
     * <li>10.5s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_REPORTING_INTERVAL_S,
            alias = LoadGeneratorConfig.Fields.reportingInterval,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.reportingInterval
    )
    protected String reportingInterval;

    /**
     * It might be a case when tests start failing too often, either due to the
     * problem with the test(s) logic or due to overloading of the target
     * system.
     * <br>
     * Perforator automatically determines when to introduce a slowdown in case
     * of any abnormalities with tests execution.
     * <br>
     * This flag controls whether automatic slowdown is enabled or not.
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_SLOWDOWN_S,
            alias = LoadGeneratorConfig.Fields.slowdown,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.slowdown
    )
    protected String slowdown;

    /**
     * All the suites are processed concurrently via multiple thread workers.
     * Every thread worker has a dedicated ID.<br/>
     * This flag determines should the worker ID be logged as a part of every log item.
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_LOG_WORKER_ID_S,
            alias = LoadGeneratorConfig.Fields.logWorkerID,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.logWorkerID
    )
    protected String logWorkerID;

    /**
     * A new suite instance ID is generated whenever a thread worker starts
     * processing a test suite.<br/>
     * This flag determines should the suite instance ID be logged for all log 
     * items related to the processing of the suite instance.
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_LOG_SUITE_INSTANCE_ID_S,
            alias = LoadGeneratorConfig.Fields.logSuiteInstanceID,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.logSuiteInstanceID
    )
    protected String logSuiteInstanceID;

    /**
     * Should a selenium session-id be logged while processing a test suite?
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_LOG_REMOTE_WEB_DRIVER_SESSION_ID_S,
            alias = LoadGeneratorConfig.Fields.logRemoteWebDriverSessionID,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.logRemoteWebDriverSessionID
    )
    protected String logRemoteWebDriverSessionID;

    /**
     * Should a transaction id be logged for every transaction in an active
     * state?
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_LOG_TRANSACTION_ID_S,
            alias = LoadGeneratorConfig.Fields.logTransactionID,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.logTransactionID
    )
    protected String logTransactionID;
    
    /**
     * Should a performance test fail at the end of the execution in case of any
     * suite errors?
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_FAIL_ON_SUITE_ERRORS_S,
            alias = LoadGeneratorConfig.Fields.failOnSuiteErrors,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.failOnSuiteErrors
    )
    protected String failOnSuiteErrors;
    
    /**
     * Should a performance test fail at the end of the execution in case of any
     * transaction errors?
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_FAIL_ON_TRANSACTION_ERRORS_S,
            alias = LoadGeneratorConfig.Fields.failOnTransactionErrors,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.failOnTransactionErrors
    )
    protected String failOnTransactionErrors;
    
    /**
     * Should system properties and environment variables override values specified
     * in configuration(s)?
     */
    @Parameter(
            required = false,
            defaultValue = LoadGeneratorConfig.DEFAULT_PRIORITIZE_SYSTEM_PROPERTIES_S,
            alias = LoadGeneratorConfig.Fields.prioritizeSystemProperties,
            property = LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.prioritizeSystemProperties
    )
    protected String prioritizeSystemProperties;
    
    /**
     * The name of the test suite.<br/>
     * Typically this name is used as a top-level transaction covering 
     * the whole suite instance execution.<br/>
     * You can ignore this field, and in such case, suite name will be 
     * auto-generated as ${artifactId}-${version}.
     */
    @Parameter(
            required = false,
            defaultValue = "${project.build.finalName}",
            alias = SuiteConfig.Fields.name,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.name
    )
    protected String name;

    /**
     * Concurrency level of test suites execution, i.e., how many concurrent 
     * threads will process suite instances. Also, this parameter controls how many browsers
     * are allowed to be launched concurrently in the cloud for cloud-based executions.
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_CONCURRENCY_S,
            alias = SuiteConfig.Fields.concurrency,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.concurrency
    )
    protected String concurrency;

    /**
     * Duration of the performance test. Also, this parameter controls how much 
     * time the browser cloud will be accessible once the performance test starts
     * in cloud mode. Duration of the browser cloud is rounded up to the closest 
     * hour value. For example, if duration = 45m, then browser cloud is created 
     * for 1 hour.<br/>
     * <b>Expected format</b>: 'h' symbol represents an hour, 
     * 'm' symbol represents a minute, 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>5m</li>
     * <li>1h</li>
     * <li>1h 20m 15s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_DURATION_S,
            alias = SuiteConfig.Fields.duration,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.duration
    )
    protected String duration;

    /**
     * How much time to wait once performance test starts before executing 
     * actual logic?<br/>
     * <b>Expected format</b>: 'm' symbol represents a minute, 
     * 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>1m</li>
     * <li>2m 30s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_DELAY_S,
            alias = SuiteConfig.Fields.delay,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.delay
    )
    protected String delay;

    /**
     * The time interval for ramping up concurrent processing of suite instances 
     * from 1 up to defined concurrency level. Concurrency is increased evenly 
     * during rampUp period. For example, if you have concurrency = 10 and 
     * rampUp = 10s, then every second additional worker thread will be launched,
     * starting from 1 thread up to 10 threads.<br/>
     * <b>Expected format</b>: 'm' symbol represents a minute, 
     * 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>1m</li>
     * <li>2m 30s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_RAMP_UP_S,
            alias = SuiteConfig.Fields.rampUp,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampUp
    )
    protected String rampUp;

    /**
     * The time interval before the end of the test to stop launching new suite 
     * instances. For example, if you have duration = 10m and rampDown = 2m, 
     * then after the 8th minute of execution no new suite instances will be 
     * launched.<br/>
     * At the same time, if a suite instance started execution before the 8th 
     * minute - such instance will proceed execution till its natural completion.
     * <br/>
     * <b>Expected format</b>: 'm' symbol represents a minute, 
     * 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>1m</li>
     * <li>2m 30s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_RAMP_DOWN_S,
            alias = SuiteConfig.Fields.rampDown,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampDown
    )
    protected String rampDown;

    /**
     * The mode of launching browsers for the test suite.
     * <b>Available modes</b>:
     * <ul>
     * <li>{@link io.perforator.sdk.loadgenerator.core.configs.WebDriverMode#cloud cloud}</li>
     * <li>{@link io.perforator.sdk.loadgenerator.core.configs.WebDriverMode#local local}</li>
     * </ul>
     * <b>Note</b>: transactions reporting is disabled when browsers are launched 
     * locally
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_WEB_DRIVER_MODE_S,
            alias = SuiteConfig.Fields.webDriverMode,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverMode
    )
    protected String webDriverMode;
    
    /**
     * The mode of launching local chrome instances.
     * <b>Available modes</b>:
     * <ul>
     * <li>{@link io.perforator.sdk.loadgenerator.core.configs.ChromeMode#headful headful}</li>
     * <li>{@link io.perforator.sdk.loadgenerator.core.configs.ChromeMode#headless headless}</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_CHROME_MODE_S,
            alias = SuiteConfig.Fields.chromeMode,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.chromeMode
    )
    protected String chromeMode;
    
    /**
     * Should a chrome driver be started in silent mode?
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_CHROME_DRIVER_SILENT_S,
            alias = SuiteConfig.Fields.chromeDriverSilent,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.chromeDriverSilent
    )
    protected String chromeDriverSilent;

    /**
     * The system automatically retries to create a new selenium session, 
     * in case of an error(s), starting from the timestamp of the initial attempt 
     * up until 'webDriverCreateSessionRetryTimeout' is reached.<br/>
     * This parameter is only applicable when webDriverMode = cloud.<br/>
     * <b>Expected format</b>: 'm' symbol represents a minute, 
     * 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>30s</li>
     * <li>1m</li>
     * <li>1m 15s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_WEB_DRIVER_CREATE_SESSION_RETRY_TIMEOUT_S,
            alias = SuiteConfig.Fields.webDriverCreateSessionRetryTimeout,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverCreateSessionRetryTimeout
    )
    protected String webDriverCreateSessionRetryTimeout;

    /**
     * The system automatically retries to delete existing selenium session, 
     * in case of an error(s), starting from the timestamp of the initial attempt 
     * up until 'webDriverDeleteSessionRetryTimeout' is reached.<br/>
     * This parameter is only applicable when webDriverMode = cloud.<br/>
     * <b>Expected format</b>: 'm' symbol represents a minute, 
     * 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>30s</li>
     * <li>1m</li>
     * <li>1m 15s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_WEB_DRIVER_DELETE_SESSION_RETRY_TIMEOUT_S,
            alias = SuiteConfig.Fields.webDriverDeleteSessionRetryTimeout,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverDeleteSessionRetryTimeout
    )
    protected String webDriverDeleteSessionRetryTimeout;

    /**
     * Implicit wait timeout for selenium session.<br/>
     * Documentation: https://www.selenium.dev/documentation/webdriver/waits/#implicit-wait
     * <br/>
     * <b>Expected format</b>: 'm' symbol represents a minute, 
     * 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>0.75s</li>
     * <li>15s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_WEB_DRIVER_SESSION_IMPLICITLY_WAIT_S,
            alias = SuiteConfig.Fields.webDriverSessionImplicitlyWait,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverSessionImplicitlyWait
    )
    protected String webDriverSessionImplicitlyWait;

    /**
     * Selenium timeout to wait for JS execution before throwing an error.<br/>
     * Documentation: https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/WebDriver.Timeouts.html#setScriptTimeout(java.time.Duration)
     * <br/>
     * <b>Expected format</b>: 'm' symbol represents a minute, 
     * 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>0.75s</li>
     * <li>15s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_WEB_DRIVER_SESSION_SCRIPT_TIMEOUT_S,
            alias = SuiteConfig.Fields.webDriverSessionScriptTimeout,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverSessionScriptTimeout
    )
    protected String webDriverSessionScriptTimeout;

    /**
     * Selenium timeout to wait for a page load to complete before throwing an error.<br/>
     * Documentation: https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/WebDriver.Timeouts.html#pageLoadTimeout(java.time.Duration)
     * <br/>
     * <b>Expected format</b>: 'm' symbol represents a minute, 
     * 's' symbol represents a second.<br/>
     * <b>Examples</b>:
     * <ul>
     * <li>0.75s</li>
     * <li>15s</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_WEB_DRIVER_SESSION_PAGE_LOAD_TIMEOUT_S,
            alias = SuiteConfig.Fields.webDriverSessionPageLoadTimeout,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverSessionPageLoadTimeout
    )
    protected String webDriverSessionPageLoadTimeout;

    /**
     * The flag allowing file uploads functionality while working with browsers 
     * in the cloud.<br/>
     * This parameter is only applicable when webDriverMode = cloud.<br/>
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_WEB_DRIVER_USE_LOCAL_FILE_DETECTOR_S,
            alias = SuiteConfig.Fields.webDriverUseLocalFileDetector,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverUseLocalFileDetector
    )
    protected String webDriverUseLocalFileDetector;

    /**
     * Default width of the browser launched via selenium.
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_WEB_DRIVER_WINDOW_WIDTH_S,
            alias = SuiteConfig.Fields.webDriverWindowWidth,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverWindowWidth
    )
    protected String webDriverWindowWidth;

    /**
     * Default height of the browser launched via selenium.
     */
    @Parameter(
            required = false,
            defaultValue = SuiteConfig.DEFAULT_WEB_DRIVER_WINDOW_HEIGHT_S,
            alias = SuiteConfig.Fields.webDriverWindowHeight,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.webDriverWindowHeight
    )
    protected String webDriverWindowHeight;
    
    protected abstract List<SUITE_PARAMS_TYPE> getSuitesParams() throws MojoFailureException;

    protected abstract Class buildLoadGeneratorClass(ClassLoader classLoader) throws MojoFailureException;
    
    protected abstract Class buildSuiteConfigClass(ClassLoader classLoader) throws MojoFailureException;
    
    protected abstract Object buildSuiteConfigInstance(Class suiteConfigClass, SUITE_PARAMS_TYPE suiteParams) throws MojoFailureException;
    
    protected abstract void preprocessAutowiredParameters() throws MojoFailureException;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (systemProperties != null && !systemProperties.isEmpty()) {
            systemProperties.forEach(System::setProperty);
        }
        
        preprocessAutowiredParameters();

        ClassLoader classLoader = buildClassLoader(project, pluginDependencies);
        AtomicReference<Exception> runnerException = new AtomicReference<>();
        Thread runnerThread = new Thread(() -> {
            Runnable loadGenerator;
            try {
                loadGenerator = buildLoadGenerator(classLoader);
            } catch (MojoFailureException e) {
                runnerException.set(e);
                return;
            }

            try {
                loadGenerator.run();
            } catch (RuntimeException e) {
                if (e.getMessage() != null) {
                    runnerException.set(
                            new MojoExecutionException(e.getMessage(), e)
                    );
                } else {
                    runnerException.set(
                            new MojoExecutionException(
                                    "Problem executing " + AbstractLoadGeneratorMojo.this.getClass().getSimpleName(),
                                    e
                            )
                    );
                }
            }
        });

        runnerThread.setName("perforator-mojo-runner");
        runnerThread.setContextClassLoader(classLoader);
        runnerThread.start();

        try {
            runnerThread.join();
        } catch (InterruptedException e) {
            if (getLog().isDebugEnabled()) {
                getLog().error("Runner thread was interrupted", e);
            }
        }

        Exception exception = runnerException.get();
        if (exception != null) {
            MojoExecutionException executionException = getRootCause(MojoExecutionException.class, exception);
            if (executionException != null) {
                throw executionException;
            }

            MojoFailureException failureException = getRootCause(MojoFailureException.class, exception);
            if (failureException != null) {
                throw failureException;
            }

            throw new MojoExecutionException(
                    "Problem running " + getClass().getSimpleName(),
                    exception
            );
        }
    }

    protected Runnable buildLoadGenerator(ClassLoader classLoader) throws MojoFailureException {
        List<SUITE_PARAMS_TYPE> suitesParams = getSuitesParams();
        Class loadGeneratorClass = buildLoadGeneratorClass(classLoader);
        
        Class[] loadGeneratorConstructorArgTypes;
        Object[] loadGeneratorConstructorArgValues;
        
        if(suitesParams != null && !suitesParams.isEmpty()) {
            Class loadGeneratorConfigClass = buildLoadGeneratorConfigClass(classLoader);
            Object loadGeneratorConfigValue = buildLoadGeneratorConfig(loadGeneratorConfigClass);
            
            Class suiteConfigClass = buildSuiteConfigClass(classLoader);
            List suiteConfigs = new ArrayList<>();
            for (SUITE_PARAMS_TYPE suite : suitesParams) {
                suiteConfigs.add(
                        buildSuiteConfigInstance(suiteConfigClass, suite)
                );
            }
            
            loadGeneratorConstructorArgTypes = new Class[]{
                loadGeneratorConfigClass, List.class
            };
            loadGeneratorConstructorArgValues = new Object[]{
                loadGeneratorConfigValue,
                suiteConfigs
            };
        } else {
            Class loadGeneratorConfigClass = buildLoadGeneratorConfigClass(classLoader);
            Object loadGeneratorConfigValue = buildLoadGeneratorConfig(loadGeneratorConfigClass);
            Class suiteConfigClass = buildSuiteConfigClass(classLoader);
            Object suiteConfig = buildSuiteConfigInstance(suiteConfigClass, null);
            
            loadGeneratorConstructorArgTypes = new Class[]{
                loadGeneratorConfigClass, List.class
            };
            loadGeneratorConstructorArgValues = new Object[]{
                loadGeneratorConfigValue,
                List.of(suiteConfig)
            };
        }
        
        return (Runnable) newInstance(
                loadGeneratorClass, 
                loadGeneratorConstructorArgTypes, 
                loadGeneratorConstructorArgValues
        );
    }
    
    protected Class buildLoadGeneratorConfigClass(ClassLoader classLoader) throws MojoFailureException  {
        return loadClass(classLoader, ClassNames.LOAD_GENERATOR_CONFIG);
    }
    
    protected Object buildLoadGeneratorConfig(Class loadGeneratorConfigClass) throws MojoFailureException {
        Function<String,String> localPropsResolvers = this::resolveParameter;
        Function<String,String> sysPropsResolvers = System::getProperty;
        Function<String,String> envPropsResolvers = System::getenv;
        
        return newInstance(
                loadGeneratorConfigClass, 
                new Class[]{
                    Function[].class
                }, 
                new Object[]{
                    new Function[]{
                        sysPropsResolvers,
                        envPropsResolvers,
                        localPropsResolvers
                    }
                }
        );
    }
    
    protected Object buildMapBasedSuiteConfig(Class suiteConfigClass, Map<String, String> suiteParams) throws MojoFailureException {
        List<Function<String,String>> resolvers = new ArrayList<>();
        
        if(suiteParams != null && !suiteParams.isEmpty()) {
            resolvers.add(suiteParams::get);
        }
        
        resolvers.add(System::getProperty);
        resolvers.add(System::getenv);
        resolvers.add(this::resolveParameter);
        
        return newInstance(
                suiteConfigClass,
                new Class[]{
                    Function[].class
                },
                new Object[]{
                    resolvers.toArray(new Function[resolvers.size()])
                }
        );
    }
    
    protected String resolveParameter(String propertyName) {
        try {
            for (Field field : getClass().getDeclaredFields()) {
                if(field.getName().equals(propertyName)) {
                    return (String) field.get(this);
                }
            }
            
            for (Field field : getClass().getSuperclass().getDeclaredFields()) {
                if(field.getName().equals(propertyName)) {
                    return (String) field.get(this);
                }
            }
        } catch(IllegalAccessException | IllegalArgumentException | SecurityException e) {
            throw new RuntimeException(
                    "Can't resolve parameter " + propertyName, 
                    e
            );
        }
        
        return null;
    }
    
    protected Object newInstance(ClassLoader classLoader, String className, Class[] constructorArgTypes, Object[] constructorArgValues) throws MojoFailureException {
        return newInstance(
                loadClass(classLoader, className), 
                constructorArgTypes, 
                constructorArgValues
        );
    }
    
    protected Object newInstance(Class instanceClass, Class[] constructorArgTypes, Object[] constructorArgValues) throws MojoFailureException {
        Constructor instanceConstructor;
        try {
            instanceConstructor = instanceClass.getConstructor(
                    constructorArgTypes
            );
        } catch (SecurityException | NoSuchMethodException e) {
            throw new MojoFailureException(
                    "class " + instanceClass + " has invalid constructor",
                    e
            );
        }

        Object result;
        try {
            result = instanceConstructor.newInstance(
                    constructorArgValues
            );
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
            throw new MojoFailureException(
                    "Can't create new instance of " + instanceClass,
                    e
            );
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw new MojoFailureException(
                        e.getCause().getMessage(),
                        e.getCause()
                );
            } else {
                throw new MojoFailureException(
                        "Can't create new instance of " + instanceClass,
                        e
                );
            }
        }

        return result;
    }
    
    protected Class loadClass(ClassLoader classLoader, String className) throws MojoFailureException {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new MojoFailureException(
                    "Can't load a class " + className,
                    e
            );
        }
    }

    protected <E> E getRootCause(Class<E> clazz, Throwable e) {
        Throwable curr = e;

        while (curr != null) {
            if (clazz.isAssignableFrom(curr.getClass())) {
                return (E) curr;
            } else {
                curr = curr.getCause();
            }
        }

        return null;
    }

    protected ClassLoader buildClassLoader(MavenProject project, List<Artifact> pluginDependencies) throws MojoExecutionException {
        LinkedHashSet<String> paths = new LinkedHashSet<>();

        try {
            paths.addAll(project.getTestClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(
                    "Dependency resolution failed",
                    e
            );
        }

        if (pluginDependencies != null) {
            for (Artifact classPathElement : pluginDependencies) {
                paths.add(classPathElement.getFile().toPath().toString());
            }
        }

        List<URL> urls = new ArrayList<>();
        for (String path : paths) {
            try {
                urls.add(new File(path).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new MojoExecutionException(
                        "Invalid classpath URL " + path,
                        e
                );
            }
        }

        try {
            return new URLClassLoader(
                    urls.toArray(new URL[urls.size()])
            );
        } catch (RuntimeException e) {
            throw new MojoExecutionException(
                    "Unexcpected error happened while building new URLClassLoader from dependencies",
                    e
            );
        }
    }

}
