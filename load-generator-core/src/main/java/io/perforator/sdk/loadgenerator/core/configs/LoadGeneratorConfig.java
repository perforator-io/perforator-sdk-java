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
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Configuration with the general properties for load-generator.
 */
@ToString
@FieldNameConstants
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LoadGeneratorConfig implements Configurable {

    /**
     * Default prefix to lookup property values across providers.
     */
    public static final String DEFAULTS_FIELD_PREFIX = "loadGenerator";

    /**
     * Default value for 
     * <b>{@link LoadGeneratorConfig#apiBaseUrl apiBaseUrl}</b>
     * property.
     */
    public static final String DEFAULT_API_BASE_URL = "https://api.perforator.io";

    /**
     * String representation of default value for
     * <b>{@link LoadGeneratorConfig#browserCloudAwaitQueued browserCloudAwaitQueued}</b>
     * property.
     */
    public static final String DEFAULT_BROWSER_CLOUD_AWAIT_QUEUED_S = "1h";

    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_BROWSER_CLOUD_AWAIT_QUEUED_S}</b>)
     * for
     * <b>{@link LoadGeneratorConfig#browserCloudAwaitQueued browserCloudAwaitQueued}</b>
     * property.
     */
    public static final Duration DEFAULT_BROWSER_CLOUD_AWAIT_QUEUED = Configurable.parseDuration(DEFAULT_BROWSER_CLOUD_AWAIT_QUEUED_S);

    /**
     * String representation of default value for
     * <b>{@link LoadGeneratorConfig#browserCloudAwaitQueued browserCloudAwaitQueued}</b>
     * property.
     */
    public static final String DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING_S = "10m";

    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING_S}</b>)
     * for
     * <b>{@link LoadGeneratorConfig#browserCloudAwaitProvisioning browserCloudAwaitProvisioning}</b>
     * property.
     */
    public static final Duration DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING = Configurable.parseDuration(DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#browserCloudStatusPollInterval}</b>
     * property.
     */
    public static final String DEFAULT_BROWSER_CLOUD_STATUS_POLL_INTERVAL_S = "1s";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_BROWSER_CLOUD_STATUS_POLL_INTERVAL_S}</b>) 
     * for 
     * <b>{@link LoadGeneratorConfig#browserCloudStatusPollInterval}</b>
     * property.
     */
    public static final Duration DEFAULT_BROWSER_CLOUD_STATUS_POLL_INTERVAL = Configurable.parseDuration(DEFAULT_BROWSER_CLOUD_STATUS_POLL_INTERVAL_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#browserCloudTerminateAutomatically}</b>
     * property.
     */
    public static final String DEFAULT_BROWSER_CLOUD_TERMINATE_AUTOMATICALY_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_BROWSER_CLOUD_TERMINATE_AUTOMATICALY_S}</b>) 
     * for 
     * <b>{@link LoadGeneratorConfig#browserCloudTerminateAutomatically}</b>
     * property.
     */
    public static final boolean DEFAULT_BROWSER_CLOUD_TERMINATE_AUTOMATICALY = Boolean.parseBoolean(DEFAULT_BROWSER_CLOUD_TERMINATE_AUTOMATICALY_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#httpConnectTimeout}</b>
     * property.
     */
    public static final String DEFAULT_HTTP_CONNECT_TIMEOUT_S = "30s";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_HTTP_CONNECT_TIMEOUT_S}</b>) 
     * for 
     * <b>{@link LoadGeneratorConfig#httpConnectTimeout}</b>
     * property.
     */
    public static final Duration DEFAULT_HTTP_CONNECT_TIMEOUT = Configurable.parseDuration(DEFAULT_HTTP_CONNECT_TIMEOUT_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#httpReadTimeout}</b>
     * property.
     */
    public static final String DEFAULT_HTTP_READ_TIMEOUT_S = "60s";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_HTTP_READ_TIMEOUT_S}</b>) 
     * for 
     * <b>{@link LoadGeneratorConfig#httpReadTimeout}</b>
     * property.
     */
    public static final Duration DEFAULT_HTTP_READ_TIMEOUT = Configurable.parseDuration(DEFAULT_HTTP_READ_TIMEOUT_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#eventsFlushInterval}</b>
     * property.
     */
    public static final String DEFAULT_EVENTS_FLUSH_INTERVAL_S = "0.25s";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_EVENTS_FLUSH_INTERVAL_S}</b>) 
     * for 
     * <b>{@link LoadGeneratorConfig#eventsFlushInterval}</b>
     * property.
     */
    public static final Duration DEFAULT_EVENTS_FLUSH_INTERVAL = Configurable.parseDuration(DEFAULT_EVENTS_FLUSH_INTERVAL_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#eventsFlushThreshold}</b>
     * property.
     */
    public static final String DEFAULT_EVENTS_FLUSH_THRESHOLD_S = "500";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_EVENTS_FLUSH_THRESHOLD_S}</b>) 
     * for 
     * <b>{@link LoadGeneratorConfig#eventsFlushThreshold}</b>
     * property.
     */
    public static final int DEFAULT_EVENTS_FLUSH_THRESHOLD = Integer.parseInt(DEFAULT_EVENTS_FLUSH_THRESHOLD_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#reportingInterval}</b>
     * property.
     */
    public static final String DEFAULT_REPORTING_INTERVAL_S = "5s";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_REPORTING_INTERVAL_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#reportingInterval}</b>
     * property.
     */
    public static final Duration DEFAULT_REPORTING_INTERVAL = Configurable.parseDuration(DEFAULT_REPORTING_INTERVAL_S);
    
    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#concurrencyAutoAdjustment}</b>
     * property.
     */
    public static final String DEFAULT_CONCURRENCY_AUTO_ADJUSTMENT_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_CONCURRENCY_AUTO_ADJUSTMENT_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#concurrencyAutoAdjustment}</b>
     * property.
     */
    public static final boolean DEFAULT_CONCURRENCY_AUTO_ADJUSTMENT = Boolean.parseBoolean(DEFAULT_CONCURRENCY_AUTO_ADJUSTMENT_S);
    
    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#concurrencyRecalcPeriod}</b>
     * property.
     */
    public static final String DEFAULT_CONCURRENCY_RECALC_PERIOD_S = "30s";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_CONCURRENCY_RECALC_PERIOD_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#concurrencyRecalcPeriod}</b>
     * property.
     */
    public static final Duration DEFAULT_CONCURRENCY_RECALC_PERIOD = Configurable.parseDuration(DEFAULT_CONCURRENCY_RECALC_PERIOD_S);
    
    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#concurrencyScaleDownMultiplier}</b>
     * property.
     */
    public static final String DEFAULT_CONCURRENCY_SCALE_DOWN_MULTIPLIER_S = "0.05";
    
    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#concurrencyScaleDownMultiplier}</b>
     * property.
     */
    public static final double DEFAULT_CONCURRENCY_SCALE_DOWN_MULTIPLIER = Double.parseDouble(DEFAULT_CONCURRENCY_SCALE_DOWN_MULTIPLIER_S);
    
    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#concurrencyScaleUpMultiplier}</b>
     * property.
     */
    public static final String DEFAULT_CONCURRENCY_SCALE_UP_MULTIPLIER_S = "0.025";
    
    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#concurrencyScaleUpMultiplier}</b>
     * property.
     */
    public static final double DEFAULT_CONCURRENCY_SCALE_UP_MULTIPLIER = Double.parseDouble(DEFAULT_CONCURRENCY_SCALE_UP_MULTIPLIER_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#logWorkerID}</b>
     * property.
     */
    public static final String DEFAULT_LOG_WORKER_ID_S = "false";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_LOG_WORKER_ID_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#logWorkerID}</b>
     * property.
     */
    public static final boolean DEFAULT_LOG_WORKER_ID = Boolean.parseBoolean(DEFAULT_LOG_WORKER_ID_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#logSuiteInstanceID}</b>
     * property.
     */
    public static final String DEFAULT_LOG_SUITE_INSTANCE_ID_S = "false";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_LOG_SUITE_INSTANCE_ID_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#logSuiteInstanceID}</b>
     * property.
     */
    public static final boolean DEFAULT_LOG_SUITE_INSTANCE_ID = Boolean.parseBoolean(DEFAULT_LOG_SUITE_INSTANCE_ID_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#logRemoteWebDriverSessionID}</b>
     * property.
     */
    public static final String DEFAULT_LOG_REMOTE_WEB_DRIVER_SESSION_ID_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_LOG_REMOTE_WEB_DRIVER_SESSION_ID_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#logRemoteWebDriverSessionID}</b>
     * property.
     */
    public static final boolean DEFAULT_LOG_REMOTE_WEB_DRIVER_SESSION_ID = Boolean.parseBoolean(DEFAULT_LOG_REMOTE_WEB_DRIVER_SESSION_ID_S);

    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#logTransactionID}</b>
     * property.
     */
    public static final String DEFAULT_LOG_TRANSACTION_ID_S = "false";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_LOG_TRANSACTION_ID_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#logTransactionID}</b>
     * property.
     */
    public static final boolean DEFAULT_LOG_TRANSACTION_ID = Boolean.parseBoolean(DEFAULT_LOG_TRANSACTION_ID_S);
    
    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#failOnSuiteErrors}</b>
     * property.
     */
    public static final String DEFAULT_FAIL_ON_SUITE_ERRORS_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_FAIL_ON_SUITE_ERRORS_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#failOnSuiteErrors}</b>
     * property.
     */
    public static final boolean DEFAULT_FAIL_ON_SUITE_ERRORS = Boolean.parseBoolean(DEFAULT_FAIL_ON_SUITE_ERRORS_S);
    
    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#failOnTransactionErrors}</b>
     * property.
     */
    public static final String DEFAULT_FAIL_ON_TRANSACTION_ERRORS_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_FAIL_ON_TRANSACTION_ERRORS_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#failOnTransactionErrors}</b>
     * property.
     */
    public static final boolean DEFAULT_FAIL_ON_TRANSACTION_ERRORS = Boolean.parseBoolean(DEFAULT_FAIL_ON_SUITE_ERRORS_S);
    
    /**
     * String representation of default value for 
     * <b>{@link LoadGeneratorConfig#prioritizeSystemProperties}</b>
     * property.
     */
    public static final String DEFAULT_PRIORITIZE_SYSTEM_PROPERTIES_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_PRIORITIZE_SYSTEM_PROPERTIES_S}</b>)
     * for 
     * <b>{@link LoadGeneratorConfig#prioritizeSystemProperties}</b>
     * property.
     */
    public static final boolean DEFAULT_PRIORITIZE_SYSTEM_PROPERTIES = Boolean.parseBoolean(DEFAULT_PRIORITIZE_SYSTEM_PROPERTIES_S);

    /**
     * String representation of default value for
     * <b>{@link LoadGeneratorConfig#usePreAllocatedIPs}</b>
     * property.
     */
    public static final String DEFAULT_USE_PRE_ALLOCATED_IPS_S = "false";

    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_USE_PRE_ALLOCATED_IPS_S}</b>)
     * for
     * <b>{@link LoadGeneratorConfig#usePreAllocatedIPs}</b>
     * property.
     */
    public static final boolean DEFAULT_USE_PRE_ALLOCATED_IPS = Boolean.parseBoolean(DEFAULT_USE_PRE_ALLOCATED_IPS_S);

    /**
     * Autogenerated ID of the configuration.
     */
    @Getter @FieldNameConstants.Include @EqualsAndHashCode.Include
    protected final String id = UUID.randomUUID().toString();
    
    /**
     * Base URL for API communication
     */
    @Getter @Setter @FieldNameConstants.Include
    protected String apiBaseUrl = DEFAULT_API_BASE_URL;

    /**
     * cliend_id to obtain access token via OAuth 2.0 Client Credentials Grant
     * flow.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected String apiClientId;

    /**
     * client_secret to obtain access token via OAuth 2.0 Client Credentials
     * Grant flow.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected String apiClientSecret;

    /**
     * Key of the project where to create a new execution and a browser cloud.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected String projectKey;

    /**
     * Key of the execution where to create a new browser cloud.
     * <br>
     * A new execution is automatically created within the parent project if 
     * an executionKey is not provided.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected String executionKey;

    /**
     * How much time to wait till the browser cloud changes state from QUEUED to PROVISIONING?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration browserCloudAwaitQueued = DEFAULT_BROWSER_CLOUD_AWAIT_QUEUED;

    /**
     * How much time to wait till the browser cloud changes state from PROVISIONING to OPERATIONAL?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration browserCloudAwaitProvisioning = DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING;

    /**
     * Time interval on how often to check browser cloud status.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration browserCloudStatusPollInterval = DEFAULT_BROWSER_CLOUD_STATUS_POLL_INTERVAL;

    /**
     * Should a browser cloud be turned off at the end of the test?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean browserCloudTerminateAutomatically = DEFAULT_BROWSER_CLOUD_TERMINATE_AUTOMATICALY;

    /**
     * HTTP connect timeout while establishing connection(s) with remote browsers.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT;

    /**
     * HTTP read timeout while awaiting response from remote browsers.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT;

    /**
     * Interval on how often to send transaction events data to API.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration eventsFlushInterval = DEFAULT_EVENTS_FLUSH_INTERVAL;

    /**
     * How many transaction events should be sent to API per one request?
     * <br>
     * <b>Note</b>: this value might be as high as 2000, everything else on top 
     * will be rejected on API end.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected int eventsFlushThreshold = DEFAULT_EVENTS_FLUSH_THRESHOLD;

    /**
     * How often progress statistics should be reported in the log? You can turn
     * off progress reporting by specifying this value as <b>0s</b>.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration reportingInterval = DEFAULT_REPORTING_INTERVAL;
    
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
    @Getter @Setter @FieldNameConstants.Include
    protected boolean concurrencyAutoAdjustment = DEFAULT_CONCURRENCY_AUTO_ADJUSTMENT;
    
    /**
     * How often desired concurrency should be recalculated?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Duration concurrencyRecalcPeriod = DEFAULT_CONCURRENCY_RECALC_PERIOD;
    
    /**
     * Perforator automatically decreases concurrency if there are too many 
     * failing transactions.
     * <br>
     * This property determines concurrency multiplier to use while calculating 
     * scale-down adjustment.
     * <br>
     * For example, suppose the target concurrency is 1000, and the multiplier is 0.05.
     * In that case, the scale-down adjustment for concurrency is 1000 x 0.05 = 50, 
     * so the system should decrease concurrency by 50 threads in case of 
     * too many failing transactions.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected double concurrencyScaleDownMultiplier = DEFAULT_CONCURRENCY_SCALE_DOWN_MULTIPLIER;
    
    /**
     * Perforator automatically increases concurrency if previously it was 
     * slowing down due to failing transactions, and the amount of such failing 
     * transactions decreases.
     * <br>
     * This property determines concurrency multiplier to use while calculating 
     * scale-up adjustment.
     * <br>
     * For example, suppose the target concurrency is 1000, and the multiplier is 0.025.
     * In that case, the scale-up adjustment for concurrency is 1000 x 0.025 = 25, 
     * so the system should increase concurrency by 25 threads in case failing 
     * transactions percent goes down.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected double concurrencyScaleUpMultiplier = DEFAULT_CONCURRENCY_SCALE_UP_MULTIPLIER;

    /**
     * All the suites are processed concurrently via multiple thread workers.
     * Every thread worker has a dedicated ID.
     * <br>
     * This flag determines should the worker ID be logged as a part of every log item.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean logWorkerID = DEFAULT_LOG_WORKER_ID;

    /**
     * A new suite instance ID is generated whenever a thread worker starts
     * processing a test suite.
     * <br>
     * This flag determines should the suite instance ID be logged for all log 
     * items related to the processing of the suite instance.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean logSuiteInstanceID = DEFAULT_LOG_SUITE_INSTANCE_ID;

    /**
     * Should a selenium session-id be logged while processing a test suite?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean logRemoteWebDriverSessionID = DEFAULT_LOG_REMOTE_WEB_DRIVER_SESSION_ID;

    /**
     * Should a transaction id be logged for every transaction in an active
     * state?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean logTransactionID = DEFAULT_LOG_TRANSACTION_ID;
    
    /**
     * Should a performance test fail at the end of the execution in case of any
     * suite errors?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean failOnSuiteErrors = DEFAULT_FAIL_ON_SUITE_ERRORS;
    
    /**
     * Should a performance test fail at the end of the execution in case of any
     * transaction errors?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean failOnTransactionErrors = DEFAULT_FAIL_ON_TRANSACTION_ERRORS;
    
    /**
     * Should system properties and environment variables override values specified
     * in configuration(s)?
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean prioritizeSystemProperties = DEFAULT_PRIORITIZE_SYSTEM_PROPERTIES;

    /**
     * The platform automatically assigns random public IP addresses when creating
     * a browser cloud, and such IPs are not known in advance.
     * Please set usePreAllocatedIPs parameter to true if you would like all browsers
     * to have preallocated IPs, for example, to establish network trust on your firewall side.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean usePreAllocatedIPs = DEFAULT_USE_PRE_ALLOCATED_IPS;

    /**
     * It might be a case when you would like to exclude specific HTTP requests
     * from capturing by browsers running in the cloud and avoid storing such
     * requests in the analytical system.
     *
     * For example, your security team doesn't want to expose test user
     * credentials to external systems, or you know in advance that specific
     * requests are failing all the time, and it is desired to exclude such
     * requests from any analysis.
     *
     * 'dataCapturingExcludes' property allows you to specify a list of URLs to
     * be excluded from capturing by cloud-based browsers.
     *
     * You can specify either absolute URLs to exclude or JS-based patterns to
     * match against the tested HTTP request URL.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected List<String> dataCapturingExcludes;

    /**
     * You can supply an optional field 'httpHeaders', and as a result, all browsers
     * from the cloud will include such headers in every HTTP request. For example,
     * to set the Authorization bearer token.
     */
    @Getter @Setter @FieldNameConstants.Include
    protected Headers browserCloudHttpHeaders;

    /**
     * Default constructor looking up property defaults via the following providers:
     * <ul>
     *   <li>{@link System#getProperty(java.lang.String) }</li>
     *   <li>{@link System#getenv(java.lang.String) }</li>
     * </ul>
     */
    public LoadGeneratorConfig() {
        applyDefaults();
    }

    /**
     * Constructor looking up property defaults in user-supplied property providers.
     * @param defaultsProviders varargs of {@link Function functions} where to lookup up
     * for property defaults.
     */
    public LoadGeneratorConfig(Function<String, String>... defaultsProviders) {
        applyDefaults(defaultsProviders);
    }

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_FIELD_PREFIX;
    }

}