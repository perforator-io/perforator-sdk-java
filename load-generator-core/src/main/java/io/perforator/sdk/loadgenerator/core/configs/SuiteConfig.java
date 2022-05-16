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
package io.perforator.sdk.loadgenerator.core.configs;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;

/**
 * Configuration for performance testing suite.
 */
@ToString
@FieldNameConstants
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SuiteConfig implements Configurable {
    
    /**
     * Default prefix to lookup property values across providers.
     */
    public static final String DEFAULTS_FIELD_PREFIX = "suite";

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#concurrency}</b>
     * property.
     */
    public static final String DEFAULT_CONCURRENCY_S = "30";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_CONCURRENCY_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#concurrency}</b>
     * property.
     */
    public static final int DEFAULT_CONCURRENCY = Integer.parseInt(DEFAULT_CONCURRENCY_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#duration}</b>
     * property.
     */
    public static final String DEFAULT_DURATION_S = "15m";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_DURATION_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#duration}</b>
     * property.
     */
    public static final Duration DEFAULT_DURATION = Configurable.parseDuration(DEFAULT_DURATION_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#delay}</b>
     * property.
     */
    public static final String DEFAULT_DELAY_S = "0s";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_DELAY_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#delay}</b>
     * property.
     */
    public static final Duration DEFAULT_DELAY = Configurable.parseDuration(DEFAULT_DELAY_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#rampUp}</b>
     * property.
     */
    public static final String DEFAULT_RAMP_UP_S = "0s";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_RAMP_UP_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#rampUp}</b>
     * property.
     */
    public static final Duration DEFAULT_RAMP_UP = Configurable.parseDuration(DEFAULT_RAMP_UP_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#rampDown}</b>
     * property.
     */
    public static final String DEFAULT_RAMP_DOWN_S = "2m";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_RAMP_DOWN_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#rampDown}</b>
     * property.
     */
    public static final Duration DEFAULT_RAMP_DOWN = Configurable.parseDuration(DEFAULT_RAMP_DOWN_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#webDriverMode}</b>
     * property.
     */
    public static final String DEFAULT_WEB_DRIVER_MODE_S = "cloud";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_WEB_DRIVER_MODE_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#webDriverMode}</b>
     * property.
     */
    public static final WebDriverMode DEFAULT_WEB_DRIVER_MODE = WebDriverMode.valueOf(DEFAULT_WEB_DRIVER_MODE_S);
    
    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#chromeMode}</b>
     * property.
     */
    public static final String DEFAULT_CHROME_MODE_S = "headful";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_CHROME_MODE_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#chromeMode}</b>
     * property.
     */
    public static final ChromeMode DEFAULT_CHROME_MODE = ChromeMode.valueOf(DEFAULT_CHROME_MODE_S);
    
    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#chromeDriverSilent}</b>
     * property.
     */
    public static final String DEFAULT_CHROME_DRIVER_SILENT_S = "true";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_CHROME_DRIVER_SILENT_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#chromeDriverSilent}</b>
     * property.
     */
    public static final boolean DEFAULT_CHROME_DRIVER_SILENT = Boolean.parseBoolean(DEFAULT_CHROME_DRIVER_SILENT_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#webDriverCreateSessionRetryTimeout}</b>
     * property.
     */
    public static final String DEFAULT_WEB_DRIVER_CREATE_SESSION_RETRY_TIMEOUT_S = "1m";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_WEB_DRIVER_CREATE_SESSION_RETRY_TIMEOUT_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#webDriverCreateSessionRetryTimeout}</b>
     * property.
     */
    public static final Duration DEFAULT_WEB_DRIVER_CREATE_SESSION_RETRY_TIMEOUT = Configurable.parseDuration(DEFAULT_WEB_DRIVER_CREATE_SESSION_RETRY_TIMEOUT_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#webDriverDeleteSessionRetryTimeout}</b>
     * property.
     */
    public static final String DEFAULT_WEB_DRIVER_DELETE_SESSION_RETRY_TIMEOUT_S = "1m";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_WEB_DRIVER_DELETE_SESSION_RETRY_TIMEOUT_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#webDriverDeleteSessionRetryTimeout}</b>
     * property.
     */
    public static final Duration DEFAULT_WEB_DRIVER_DELETE_SESSION_RETRY_TIMEOUT = Configurable.parseDuration(DEFAULT_WEB_DRIVER_DELETE_SESSION_RETRY_TIMEOUT_S);
    
    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#webDriverSessionImplicitlyWait}</b>
     * property.
     */
    public static final String DEFAULT_WEB_DRIVER_SESSION_IMPLICITLY_WAIT_S = "0s";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_WEB_DRIVER_SESSION_IMPLICITLY_WAIT_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#webDriverSessionImplicitlyWait}</b>
     * property.
     */
    public static final Duration DEFAULT_WEB_DRIVER_SESSION_IMPLICITLY_WAIT = Configurable.parseDuration(DEFAULT_WEB_DRIVER_SESSION_IMPLICITLY_WAIT_S);
    
    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#webDriverSessionScriptTimeout}</b>
     * property.
     */
    public static final String DEFAULT_WEB_DRIVER_SESSION_SCRIPT_TIMEOUT_S = "30s";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_WEB_DRIVER_SESSION_SCRIPT_TIMEOUT_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#webDriverSessionScriptTimeout}</b>
     * property.
     */
    public static final Duration DEFAULT_WEB_DRIVER_SESSION_SCRIPT_TIMEOUT = Configurable.parseDuration(DEFAULT_WEB_DRIVER_SESSION_SCRIPT_TIMEOUT_S);
    
    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#webDriverSessionPageLoadTimeout}</b>
     * property.
     */
    public static final String DEFAULT_WEB_DRIVER_SESSION_PAGE_LOAD_TIMEOUT_S = "30s";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_WEB_DRIVER_SESSION_PAGE_LOAD_TIMEOUT_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#webDriverSessionPageLoadTimeout}</b>
     * property.
     */
    public static final Duration DEFAULT_WEB_DRIVER_SESSION_PAGE_LOAD_TIMEOUT = Configurable.parseDuration(DEFAULT_WEB_DRIVER_SESSION_PAGE_LOAD_TIMEOUT_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#webDriverUseLocalFileDetector}</b>
     * property.
     */
    public static final String DEFAULT_WEB_DRIVER_USE_LOCAL_FILE_DETECTOR_S = "true";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_WEB_DRIVER_USE_LOCAL_FILE_DETECTOR_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#webDriverUseLocalFileDetector}</b>
     * property.
     */
    public static final boolean DEFAULT_WEB_DRIVER_USE_LOCAL_FILE_DETECTOR = Boolean.parseBoolean(DEFAULT_WEB_DRIVER_USE_LOCAL_FILE_DETECTOR_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#webDriverWindowWidth}</b>
     * property.
     */
    public static final String DEFAULT_WEB_DRIVER_WINDOW_WIDTH_S = "1920";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_WEB_DRIVER_WINDOW_WIDTH_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#webDriverWindowWidth}</b>
     * property.
     */
    public static final int DEFAULT_WEB_DRIVER_WINDOW_WIDTH = Integer.parseInt(DEFAULT_WEB_DRIVER_WINDOW_WIDTH_S);

    /**
     * String representation of default value for 
     * <b>{@link SuiteConfig#webDriverWindowHeight}</b>
     * property.
     */
    public static final String DEFAULT_WEB_DRIVER_WINDOW_HEIGHT_S = "1080";
    
    /**
     * Default value(<b>{@value SuiteConfig#DEFAULT_WEB_DRIVER_WINDOW_HEIGHT_S}</b>) 
     * for 
     * <b>{@link SuiteConfig#webDriverWindowHeight}</b>
     * property.
     */
    public static final int DEFAULT_WEB_DRIVER_WINDOW_HEIGHT = Integer.parseInt(DEFAULT_WEB_DRIVER_WINDOW_HEIGHT_S);

    /**
     * Autogenerated ID of the configuration.
     */
    @Getter @FieldNameConstants.Include @EqualsAndHashCode.Include
    protected final String id = UUID.randomUUID().toString();

    /**
     * The name of the test suite.
     * <br>
     * Typically this name is used as a top-level transaction covering 
     * the whole suite instance execution.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected String name;

    /**
     * Concurrency level of test suites execution, i.e., how many concurrent 
     * threads will process suite instances. Also, this parameter controls how many browsers
     * are allowed to be launched concurrently in the cloud for cloud-based executions.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected int concurrency = DEFAULT_CONCURRENCY;

    /**
     * Duration of the performance test. Also, this parameter controls how much 
     * time the browser cloud will be accessible once the performance test starts
     * in cloud mode. Duration of the browser cloud is rounded up to the closest 
     * hour value. For example, if duration = 45m, then browser cloud is created 
     * for 1 hour.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration duration = DEFAULT_DURATION;

    /**
     * How much time to wait once performance test starts before executing 
     * actual logic?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration delay = DEFAULT_DELAY;

    /**
     * The time interval for ramping up concurrent processing of suite instances 
     * from 1 up to defined concurrency level. Concurrency is increased evenly 
     * during rampUp period. For example, if you have concurrency = 10 and 
     * rampUp = 10s, then every second additional worker thread will be launched,
     * starting from 1 thread up to 10 threads.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration rampUp = DEFAULT_RAMP_UP;

    /**
     * The time interval before the end of the test to stop launching new suite 
     * instances. For example, if you have duration = 10m and rampDown = 2m, 
     * then after the 8th minute of execution no new suite instances will be 
     * launched.
     * <br>
     * At the same time, if a suite instance started execution before the 8th 
     * minute - such instance will proceed execution till its natural completion.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration rampDown = DEFAULT_RAMP_DOWN;

    /**
     * The mode of launching browsers for the test suite.
     * <b>Available modes</b>:
     * <ul>
     * <li>{@link WebDriverMode#cloud cloud}</li>
     * <li>{@link WebDriverMode#local local}</li>
     * </ul>
     * <b>Note</b>: transactions reporting is disabled when browsers are launched 
     * locally
     */
    @Getter @Setter @FieldNameConstants.Include
    protected WebDriverMode webDriverMode = DEFAULT_WEB_DRIVER_MODE;
    
    /**
     * The mode of launching chrome instances.
     * <b>Available modes</b>:
     * <ul>
     * <li>{@link ChromeMode#headless headless}</li>
     * <li>{@link ChromeMode#headful headful}</li>
     * </ul>
     */
    @Getter @Setter @FieldNameConstants.Include
    protected ChromeMode chromeMode = DEFAULT_CHROME_MODE;
    
    /**
     * Should a chrome driver be started in silent mode?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean chromeDriverSilent = DEFAULT_CHROME_DRIVER_SILENT;

    /**
     * The system automatically retries to create a new selenium session, 
     * in case of an error(s), starting from the timestamp of the initial attempt 
     * up until 'webDriverCreateSessionRetryTimeout' is reached.
     * <br>
     * This parameter is only applicable when webDriverMode = cloud.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration webDriverCreateSessionRetryTimeout = DEFAULT_WEB_DRIVER_CREATE_SESSION_RETRY_TIMEOUT;

    /**
     * The system automatically retries to delete existing selenium session, 
     * in case of an error(s), starting from the timestamp of the initial attempt 
     * up until 'webDriverDeleteSessionRetryTimeout' is reached.
     * <br>
     * This parameter is only applicable when webDriverMode = cloud.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration webDriverDeleteSessionRetryTimeout = DEFAULT_WEB_DRIVER_DELETE_SESSION_RETRY_TIMEOUT;

    /**
     * Implicit wait timeout for selenium session.
     * <br>
     * Please see {@link org.openqa.selenium.WebDriver.Timeouts#implicitlyWait(long, java.util.concurrent.TimeUnit) Timeouts#implicitlyWait} documentation.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration webDriverSessionImplicitlyWait = DEFAULT_WEB_DRIVER_SESSION_IMPLICITLY_WAIT;

    /**
     * Selenium timeout to wait for JS execution before throwing an error.
     * <br>
     * Please see {@link org.openqa.selenium.WebDriver.Timeouts#setScriptTimeout(long, java.util.concurrent.TimeUnit) Timeouts#setScriptTimeout} documentation.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration webDriverSessionScriptTimeout = DEFAULT_WEB_DRIVER_SESSION_SCRIPT_TIMEOUT;

    /**
     * Selenium timeout to wait for a page load to complete before throwing an error.
     * <br>
     * Please see {@link org.openqa.selenium.WebDriver.Timeouts#pageLoadTimeout(long, java.util.concurrent.TimeUnit) Timeouts#pageLoadTimeout} documentation.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration webDriverSessionPageLoadTimeout = DEFAULT_WEB_DRIVER_SESSION_PAGE_LOAD_TIMEOUT;

    /**
     * The flag allowing file uploads functionality while working with browsers 
     * in the cloud.
     * <br>
     * This parameter is only applicable when webDriverMode = cloud.
     * <br>
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean webDriverUseLocalFileDetector = DEFAULT_WEB_DRIVER_USE_LOCAL_FILE_DETECTOR;

    /**
     * Default width of the browser launched via selenium.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected int webDriverWindowWidth = DEFAULT_WEB_DRIVER_WINDOW_WIDTH;

    /**
     * Default height of the browser launched via selenium.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected int webDriverWindowHeight = DEFAULT_WEB_DRIVER_WINDOW_HEIGHT;

    /**
     * Default constructor looking up property defaults via the following providers:
     * <ul>
     *   <li>{@link System#getProperty(java.lang.String) }</li>
     *   <li>{@link System#getenv(java.lang.String) }</li>
     * </ul>
     */
    public SuiteConfig() {
        applyDefaults();
    }

    /**
     * Constructor looking up property defaults in user-supplied property providers.
     * @param defaultsProviders varargs of {@link Function functions} where to lookup up
     * for property defaults.
     */
    public SuiteConfig(Function<String, String>... defaultsProviders) {
        applyDefaults(defaultsProviders);
    }

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_FIELD_PREFIX;
    }

}
