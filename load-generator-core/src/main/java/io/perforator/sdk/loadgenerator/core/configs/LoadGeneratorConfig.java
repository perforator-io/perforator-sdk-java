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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Configuration with the general properties for load-generator.
 */
@Getter
@ToString
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@FieldNameConstants
@Jacksonized
public class LoadGeneratorConfig implements Config {

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
    public static final Duration DEFAULT_BROWSER_CLOUD_AWAIT_QUEUED = StringConverter.toDuration(DEFAULT_BROWSER_CLOUD_AWAIT_QUEUED_S);

    /**
     * String representation of default value for
     * <b>{@link LoadGeneratorConfig#browserCloudAwaitProvisioning browserCloudAwaitProvisioning}</b>
     * property.
     */
    public static final String DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING_S = "15m";

    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING_S}</b>)
     * for
     * <b>{@link LoadGeneratorConfig#browserCloudAwaitProvisioning browserCloudAwaitProvisioning}</b>
     * property.
     */
    public static final Duration DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING = StringConverter.toDuration(DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING_S);

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
    public static final Duration DEFAULT_BROWSER_CLOUD_STATUS_POLL_INTERVAL = StringConverter.toDuration(DEFAULT_BROWSER_CLOUD_STATUS_POLL_INTERVAL_S);

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
    public static final boolean DEFAULT_BROWSER_CLOUD_TERMINATE_AUTOMATICALY = StringConverter.toBoolean(DEFAULT_BROWSER_CLOUD_TERMINATE_AUTOMATICALY_S);

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
    public static final Duration DEFAULT_HTTP_CONNECT_TIMEOUT = StringConverter.toDuration(DEFAULT_HTTP_CONNECT_TIMEOUT_S);

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
    public static final Duration DEFAULT_HTTP_READ_TIMEOUT = StringConverter.toDuration(DEFAULT_HTTP_READ_TIMEOUT_S);

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
    public static final Duration DEFAULT_EVENTS_FLUSH_INTERVAL = StringConverter.toDuration(DEFAULT_EVENTS_FLUSH_INTERVAL_S);

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
    public static final int DEFAULT_EVENTS_FLUSH_THRESHOLD = StringConverter.toInt(DEFAULT_EVENTS_FLUSH_THRESHOLD_S);

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
    public static final Duration DEFAULT_REPORTING_INTERVAL = StringConverter.toDuration(DEFAULT_REPORTING_INTERVAL_S);
    
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
    public static final boolean DEFAULT_FAIL_ON_SUITE_ERRORS = StringConverter.toBoolean(DEFAULT_FAIL_ON_SUITE_ERRORS_S);
    
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
    public static final boolean DEFAULT_FAIL_ON_TRANSACTION_ERRORS = StringConverter.toBoolean(DEFAULT_FAIL_ON_SUITE_ERRORS_S);

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
    public static final boolean DEFAULT_USE_PRE_ALLOCATED_IPS = StringConverter.toBoolean(DEFAULT_USE_PRE_ALLOCATED_IPS_S);
    
    /**
     * String representation of default value for
     * <b>{@link LoadGeneratorConfig#dataCapturingIncludeRequestHeaders}</b>
     * property.
     */
    public static final String DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_HEADERS_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_HEADERS_S}</b>)
     * for
     * <b>{@link LoadGeneratorConfig#dataCapturingIncludeRequestHeaders}</b>
     * property.
     */
    public static final boolean DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_HEADERS = StringConverter.toBoolean(DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_HEADERS_S);
    
    /**
     * String representation of default value for
     * <b>{@link LoadGeneratorConfig#dataCapturingIncludeRequestBody}</b>
     * property.
     */
    public static final String DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_BODY_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_BODY_S}</b>)
     * for
     * <b>{@link LoadGeneratorConfig#dataCapturingIncludeRequestBody}</b>
     * property.
     */
    public static final boolean DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_BODY = StringConverter.toBoolean(DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_BODY_S);
    
    /**
     * String representation of default value for
     * <b>{@link LoadGeneratorConfig#dataCapturingIncludeResponseHeaders}</b>
     * property.
     */
    public static final String DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_HEADERS_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_HEADERS_S}</b>)
     * for
     * <b>{@link LoadGeneratorConfig#dataCapturingIncludeResponseHeaders}</b>
     * property.
     */
    public static final boolean DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_HEADERS = StringConverter.toBoolean(DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_HEADERS_S);
    
    /**
     * String representation of default value for
     * <b>{@link LoadGeneratorConfig#dataCapturingIncludeResponseBody}</b>
     * property.
     */
    public static final String DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_BODY_S = "true";
    
    /**
     * Default value(<b>{@value LoadGeneratorConfig#DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_BODY_S}</b>)
     * for
     * <b>{@link LoadGeneratorConfig#dataCapturingIncludeResponseBody}</b>
     * property.
     */
    public static final boolean DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_BODY = StringConverter.toBoolean(DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_BODY_S);

    /**
     * Autogenerated ID of the configuration.
     */
    @Default
    String id = UUID.randomUUID().toString();
    
    /**
     * Base URL for API communication
     */
    @Default
    String apiBaseUrl = DEFAULT_API_BASE_URL;

    /**
     * cliend_id to obtain access token via OAuth 2.0 Client Credentials Grant
     * flow.
     */
    String apiClientId;

    /**
     * client_secret to obtain access token via OAuth 2.0 Client Credentials
     * Grant flow.
     */
    String apiClientSecret;

    /**
     * OAuth 2.0 access token for Perforator API calls.
     * You can generate an access token outside the load generator and bypass 
     * such token without specifying 
     * {@link LoadGeneratorConfig#apiClientId} and 
     * {@link LoadGeneratorConfig#apiClientSecret}.
     * <b>Note:</b> Please keep in mind that the access token has a limited 
     * validity period and usually expires 8 hours after authentication.
     */
    String apiToken;

    /**
     * Key of the project where to create a new execution and a browser cloud.
     */
    String projectKey;

    /**
     * Key of the execution where to create a new browser cloud.
     * <br>
     * A new execution is automatically created within the parent project if 
     * an executionKey is not provided.
     */
    String executionKey;

    /**
     * How much time to wait till the browser cloud changes state from QUEUED to PROVISIONING?
     */
    @Default
    Duration browserCloudAwaitQueued = DEFAULT_BROWSER_CLOUD_AWAIT_QUEUED;

    /**
     * How much time to wait till the browser cloud changes state from PROVISIONING to OPERATIONAL?
     */
    @Default
    Duration browserCloudAwaitProvisioning = DEFAULT_BROWSER_CLOUD_AWAIT_PROVISIONING;

    /**
     * Time interval on how often to check browser cloud status.
     */
    @Default
    Duration browserCloudStatusPollInterval = DEFAULT_BROWSER_CLOUD_STATUS_POLL_INTERVAL;

    /**
     * Should a browser cloud be turned off at the end of the test?
     */
    @Default
    boolean browserCloudTerminateAutomatically = DEFAULT_BROWSER_CLOUD_TERMINATE_AUTOMATICALY;

    /**
     * HTTP connect timeout while establishing connection(s) with remote browsers.
     */
    @Default
    Duration httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT;

    /**
     * HTTP read timeout while awaiting response from remote browsers.
     */
    @Default
    Duration httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT;

    /**
     * Interval on how often to send transaction events data to API.
     */
    @Default
    Duration eventsFlushInterval = DEFAULT_EVENTS_FLUSH_INTERVAL;

    /**
     * How many transaction events should be sent to API per one request?
     * <br>
     * <b>Note</b>: this value might be as high as 2000, everything else on top 
     * will be rejected on API end.
     */
    @Default
    int eventsFlushThreshold = DEFAULT_EVENTS_FLUSH_THRESHOLD;

    /**
     * How often progress statistics should be reported in the log? You can turn
     * off progress reporting by specifying this value as <b>0s</b>.
     */
    @Default
    Duration reportingInterval = DEFAULT_REPORTING_INTERVAL;
    
    /**
     * Should a performance test fail at the end of the execution in case of any
     * suite errors?
     */
    @Default
    boolean failOnSuiteErrors = DEFAULT_FAIL_ON_SUITE_ERRORS;
    
    /**
     * Should a performance test fail at the end of the execution in case of any
     * transaction errors?
     */
    @Default
    boolean failOnTransactionErrors = DEFAULT_FAIL_ON_TRANSACTION_ERRORS;

    /**
     * The platform automatically assigns random public IP addresses when creating
     * a browser cloud, and such IPs are not known in advance.
     * Please set usePreAllocatedIPs parameter to true if you would like all browsers
     * to have preallocated IPs, for example, to establish network trust on your firewall side.
     */
    @Default
    boolean usePreAllocatedIPs = DEFAULT_USE_PRE_ALLOCATED_IPS;
    
    /**
     * It may be a case when you need precise control over capturing HTTP request 
     * headers by browsers running in the cloud and persisting it for analytics 
     * purposes. 
     * 
     * For example, your security team doesn't want sensitive information from 
     * any HTTP request headers to be preserved by external platforms.
     * 
     * The dataCapturingIncludeRequestHeaders property allows you to control 
     * capturing of any HTTP request headers.
     */
    @Default
    boolean dataCapturingIncludeRequestHeaders = DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_HEADERS;
    
    /**
     * It may be a case when you need precise control over capturing HTTP requests 
     * body by browsers running in the cloud and persisting it for analytics 
     * purposes.
     * 
     * For example, your security team doesn't want sensitive information from 
     * any HTTP request body to be preserved by external platforms.
     * 
     * The dataCapturingIncludeRequestBody property allows you to control capturing 
     * of any HTTP request body.
     */
    @Default
    boolean dataCapturingIncludeRequestBody = DEFAULT_DATA_CAPTURING_INCLUDE_REQUEST_BODY;
    
    /**
     * It may be a case when you need precise control over capturing HTTP response 
     * headers by browsers running in the cloud and persisting it for analytics 
     * purposes.
     * 
     * For example, your security team doesn't want sensitive information from any 
     * HTTP response headers to be preserved by external platforms.
     * 
     * The dataCapturingIncludeResponseHeaders property allows you to control capturing 
     * of any HTTP response headers.
     */
    @Default
    boolean dataCapturingIncludeResponseHeaders = DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_HEADERS;
    
    /**
     * It may be a case when you need precise control over capturing HTTP responses 
     * body by browsers running in the cloud and persisting it for analytics purposes. 
     * 
     * For example, your security team doesn't want sensitive information from 
     * any HTTP response body to be preserved by external platforms.
     * 
     * The 'dataCapturingIncludeResponseBody' property allows you to control capturing 
     * of any HTTP response body.
     */
    @Default
    boolean dataCapturingIncludeResponseBody = DEFAULT_DATA_CAPTURING_INCLUDE_RESPONSE_BODY;

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
    @Singular
    List<String> dataCapturingExcludes;

    /**
     * You can supply an optional field 'browserCloudHttpHeaders', and as a result, 
     * all browsers from the cloud will include such headers in every HTTP request. 
     * For example, to set the Authorization bearer token.
     */
    @Singular
    Map<String, String> browserCloudHttpHeaders;

    /*
     * Please set the ‘browserCloudHosts’ parameter if you would like
     * to propagate additional /etc/hosts to remote browsers.
     * It might be a case where a target website domain name is not resolvable via
     * public DNS servers. So, to reach such domains from the browsers started in the cloud,
     * you can supply a map of additional DNS records via 'browserCloudHosts' parameter,
     * for example: example.com = 1.2.3.4
     */
    @Singular
    Map<String, String> browserCloudHosts;
    
    public static abstract class LoadGeneratorConfigBuilder<C extends LoadGeneratorConfig, B extends LoadGeneratorConfigBuilder<C, B>> implements ConfigBuilder<C, B> {

        @Override
        public String getDefaultsPrefix() {
            return DEFAULTS_FIELD_PREFIX;
        }
        
    }

}