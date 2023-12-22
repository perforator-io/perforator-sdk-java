This is an example of the load-generator integrated with the Perforator platform
in a codeless way, i.e., no Java programming skills are required.
You only need to populate configuration in config.yml file and run it via shell 
script(s).

Requirements:
- JDK 11+ is required.
- java executable should be available via shell path.

The prebuilt example comes with a set of handy scripts targeted to execute 
config.yml with the different goals:
- local.sh - executes your config.yml using Chrome browsers started locally. 
  This script is helpful to debug and visually verify that everything works as 
  intended.
- local-headless.sh - it has the same purpose as local.sh, but valuable for 
  CI/CD environments where a graphical environment is unavailable.
- cloud-dry-run.sh - it processes your configuration file using only 10 browsers 
  started in the cloud. The main idea of this script is to avoid extra charges 
  when you need to ensure that actions are executed correctly in cloud-based 
  browsers. For example, it is not optimal to run your full-powered 
  configuration using thousands of browsers and then, in a minute, realize that 
  you have a simple mistake in css/xpath selector.
- cloud-full-run.sh - executes your load test at full speed as defined in 
  config.yml.

There are .cmd equivalents for all .sh scripts, so please use cmd scripts in a 
Windows environment.

Important: Please populate apiClientId, apiClientSecret, and projectKey in
config.yml before you start playing with this example.

We have prepared a dedicated guide on how to use Codeless Load Generator, which 
is available at https://app.perforator.io/guide/load_generator/codeless

While web-based documentation is the primary source of truth, feel free to take 
a look at the below docs describing available options for the config.yml

################################################################################
# Load generator is responsible for orchestrating execution of the load test.  #
# It automatically controls the provisioning of the hardware resources required#
# to launch browser clouds and clean up of such resources.                     #
# It determines when and how to launch new suite instances, reports its        #
# metrics to the analytics system, provides an easy binding/integration        #
# with remote browsers created in the cloud using selenium protocol, etc.      #
#                                                                              #
# So, there are two dedicated sections in this config:                         #
# - *loadGenerator*, general settings applicable for load tests orchestration. #
# - *suites*, the actual configuration of the business logic for load tests.   #
#                                                                              #
# You can override almost all configuration properties via command line args   #
# or environment variables, so you can prepare a config file once and then     #
# utilize it under different conditions, for example:                          #
# - You don't want to store secure information like apiClientId or             #
#   apiClientSecret directly in the source code, but to supply it via          #
#   environment variables.                                                     #
# - You would like to debug your config file using a local browser.            #
# - You would like to verify your config file using limited concurrency, before#
#   executing it full speed.                                                   #
#                                                                              #
# Configuration values are looked up in the following order:                   #
# - System properties, i.e. command-line arguments: java -DloadGenerator.smth=.#
# - Environment variables                                                      #
# - Config value itself                                                        #
################################################################################
#loadGenerator:

  ##############################################################################
  # Base URL for API communication.                                            #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.apiBaseUrl                           #
  # - Environment variable name: LOADGENERATOR_APIBASEURL                      #
  ##############################################################################
  #apiBaseUrl: https://api.perforator.io

  ##############################################################################
  # Api Client ID.                                                             #
  # You can get it at https://app.perforator.io/settings/api                   #
  #                                                                            #
  # This is a required property.                                               #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.apiClientId                          #
  # - Environment variable name: LOADGENERATOR_APICLIENTID                     #
  ##############################################################################
  #apiClientId: YOUR_API_CLIENT_ID

  ##############################################################################
  # Api Client Secret.                                                         #
  # You can get it at https://app.perforator.io/settings/api                   #
  #                                                                            #
  # This is a required property.                                               #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.apiClientSecret                      #
  # - Environment variable name: LOADGENERATOR_APICLIENTSECRET                 #
  ##############################################################################
  #apiClientSecret: YOUR_API_CLIENT_SECRET

  ##############################################################################
  # OAuth 2.0 access token for Perforator API calls.                           #
  # You can generate an access token outside the load generator and bypass such#
  # token without specifying apiClientId and apiClientSecret.                  #
  #                                                                            #
  # Note: Please keep in mind that the access token has a limited validity     #
  # period and usually expires 8 hours after authentication.                   #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.apiToken                             #
  # - Environment variable name: LOADGENERATOR_APITOKEN                        #
  ##############################################################################
  #apiToken: YOUR_PREAUTHENTICATED_TOKEN_VALUE

  ##############################################################################
  # Key of the project where to create a new execution and a browser cloud.    #
  # You can get it at https://app.perforator.io/dashboard                      #
  # Also, you can get it on the specific project page accessible from the      #
  # dashboard or from the left menu.                                           #
  #                                                                            #
  # This is a required property.                                               #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.projectKey                           #
  # - Environment variable name: LOADGENERATOR_PROJECTKEY                      #
  ##############################################################################
  #projectKey: YOUR_PROJECT_KEY

  ##############################################################################
  # Key of the execution where to create a new browser cloud.                  #
  # A new execution is automatically created within the parent project if      #
  # an executionKey is not provided.                                           #
  #                                                                            #
  # This property is not required in majority of the cases.                    #
  # The only case where it might be needed, when you would like to run multiple# 
  # independent load generators in parallel and combine statistics together    #
  # to get 360 view.                                                           #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.executionKey                         #
  # - Environment variable name: LOADGENERATOR_EXECUTIONKEY                    #
  ##############################################################################
  #executionKey: 

  ##############################################################################
  # How much time to wait till the browser cloud changes state                 #
  # from QUEUED to PROVISIONING?                                               #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.browserCloudAwaitQueued              #
  # - Environment variable name: LOADGENERATOR_BROWSERCLOUDAWAITQUEUED         #
  ##############################################################################
  #browserCloudAwaitQueued: 1h

  ##############################################################################
  # How much time to wait till the browser cloud changes state from            #
  # PROVISIONING to OPERATIONAL?                                               #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.browserCloudAwaitProvisioning        #
  # - Environment variable name: LOADGENERATOR_BROWSERCLOUDAWAITPROVISIONING   #
  ##############################################################################
  #browserCloudAwaitProvisioning: 15m

  ##############################################################################
  # Time interval on how often to check browser cloud status.                  #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.browserCloudStatusPollInterval       #
  # - Environment variable name: LOADGENERATOR_BROWSERCLOUDSTATUSPOLLINTERVAL  #
  ##############################################################################
  #browserCloudStatusPollInterval: 1s

  ##############################################################################
  # Should a browser cloud be turned off at the end of the test?               #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.browserCloudTerminateAutomatically   #
  # - Environment variable: LOADGENERATOR_BROWSERCLOUDTERMINATEAUTOMATICALLY   #
  ##############################################################################
  #browserCloudTerminateAutomatically: true

  ##############################################################################
  # HTTP connect timeout while establishing connection(s) with remote browsers.#
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.httpConnectTimeout                   #
  # - Environment variable name: LOADGENERATOR_HTTPCONNECTTIMEOUT              #
  ##############################################################################
  #httpConnectTimeout: 30s

  ##############################################################################
  # HTTP read timeout while awaiting response from remote browsers.            #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.httpReadTimeout                      #
  # - Environment variable name: LOADGENERATOR_HTTPREADTIMEOUT                 #
  ##############################################################################
  #httpReadTimeout: 60s

  ##############################################################################
  # Interval on how often to send transaction events data to API.              #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.eventsFlushInterval                  #
  # - Environment variable name: LOADGENERATOR_EVENTSFLUSHINTERVAL             #
  ##############################################################################
  #eventsFlushInterval: 0.25s

  ##############################################################################
  # How many transaction events should be sent to API per one request?         #
  # Note: this value might be as high as 2000, everything else on top          #
  # will be rejected on API end.                                               #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.eventsFlushThreshold                 #
  # - Environment variable name: LOADGENERATOR_EVENTSFLUSHTHRESHOLD            #
  ##############################################################################
  #eventsFlushThreshold: 500

  ##############################################################################
  # How often progress statistics should be reported in the log?               #
  # You can turn off progress reporting by specifying this value as 0s.        #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.reportingInterval                    #
  # - Environment variable name: LOADGENERATOR_REPORTINGINTERVAL               #
  ##############################################################################
  #reportingInterval: 5s

  ##############################################################################
  # Should a performance test fail at the end of the execution in case of any  #
  # suite errors?                                                              #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.failOnSuiteErrors                    #
  # - Environment variable name: LOADGENERATOR_FAILONSUITEERRORS               #
  ##############################################################################
  #failOnSuiteErrors: true

  ##############################################################################
  # Should a performance test fail at the end of the execution in case of any  #
  # transaction errors?                                                        #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.failOnTransactionErrors              #
  # - Environment variable name: LOADGENERATOR_FAILONTRANSACTIONERRORS         #
  ##############################################################################
  #failOnTransactionErrors: true

  ##############################################################################
  # The platform automatically assigns random public IP addresses when         #
  # creating a browser cloud, and such IPs are not known in advance.           #
  #                                                                            #
  # Please set usePreAllocatedIPs parameter to true if you would like all      #
  # browsers to have preallocated IPs, for example, to establish network trust #
  # on your firewall side.                                                     #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.usePreAllocatedIPs                   #
  # - Environment variable name: LOADGENERATOR_USEPREALLOCATEDIPS              #
  ##############################################################################
  #usePreAllocatedIPs: false

  ##############################################################################
  # It may be a case when you need precise control over capturing HTTP request #
  # headers by browsers running in the cloud and persisting it for analytics   #
  # purposes.                                                                  #
  #                                                                            #
  # For example, your security team doesn't want sensitive information from any#
  # HTTP request headers to be preserved by external platforms.                #
  #                                                                            #
  # The 'dataCapturingIncludeRequestHeaders' property allows you to control    #
  # capturing of any HTTP request headers.                                     #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.dataCapturingIncludeRequestHeaders   #
  # - Env variable name: LOADGENERATOR_DATACAPTURINGINCLUDEREQUESTHEADERS      #
  ##############################################################################
  #dataCapturingIncludeRequestHeaders: true

  ##############################################################################
  # It may be a case when you need precise control over capturing HTTP requests#
  # body by browsers running in the cloud and persisting it for analytics      #
  # purposes.                                                                  #
  #                                                                            #
  # For example, your security team doesn't want sensitive information from any#
  # HTTP request body to be preserved by external platforms.                   #
  #                                                                            #
  # The 'dataCapturingIncludeRequestBody' property allows you to control       #
  # capturing of any HTTP request body.                                        #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.dataCapturingIncludeRequestBody      #
  # - Env variable name: LOADGENERATOR_DATACAPTURINGINCLUDEREQUESTBODY         #
  ##############################################################################
  #dataCapturingIncludeRequestBody: true

  ##############################################################################
  # It may be a case when you need precise control over capturing HTTP response#
  # headers by browsers running in the cloud and persisting it for analytics   #
  # purposes.                                                                  #
  #                                                                            #
  # For example, your security team doesn't want sensitive information from any#
  # HTTP response headers to be preserved by external platforms.               #
  #                                                                            #
  # The 'dataCapturingIncludeResponseHeaders' property allows you to control   #
  # capturing of any HTTP response headers.                                    #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.dataCapturingIncludeResponseHeaders  #
  # - Env variable name: LOADGENERATOR_DATACAPTURINGINCLUDERESPONSEHEADERS     #
  ##############################################################################
  #dataCapturingIncludeResponseHeaders: true

  ##############################################################################
  # It may be a case when you need precise control over capturing HTTP         #
  # responses body by browsers running in the cloud and persisting it for      #
  # analytics purposes.                                                        #
  #                                                                            #
  # For example, your security team doesn't want sensitive information from any#
  # HTTP response body to be preserved by external platforms.                  #
  #                                                                            #
  # The 'dataCapturingIncludeResponseBody' property allows you to control      #
  # capturing of any HTTP response body.                                       #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.dataCapturingIncludeResponseBody     #
  # - Env variable name: LOADGENERATOR_DATACAPTURINGINCLUDERESPONSEBODY        #
  ##############################################################################
  #dataCapturingIncludeResponseBody: true

  ##############################################################################
  # It might be a case when you would like to exclude specific HTTP requests   #
  # from capturing by browsers running in the cloud and avoid storing such     #
  # requests in the analytical system.                                         #
  # For example, your security team doesn't want to expose test user           #
  # credentials to external systems, or you know in advance that specific      #
  # requests are failing all the time, and it is desired to exclude such       #
  # requests from any analysis.                                                # 
  #                                                                            #
  # 'dataCapturingExcludes' property allows you to specify a list of URLs to be#
  # excluded from capturing by cloud-based browsers.                           #
  #                                                                            #
  # You can specify either absolute URLs to exclude or JS-based patterns       #
  # to match against the tested HTTP request URL.                              #
  #                                                                            #
  # This is a optional property.                                               #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.dataCapturingExcludes                #
  # - Environment variable name: LOADGENERATOR_DATACAPTURINGEXCLUDES           #
  ##############################################################################
  #dataCapturingExcludes:
  #  - https://example.com/path/to/exclude
  #  - https://*.example.com/path/tracking.*

  ##############################################################################
  # You can supply a field 'browserCloudHttpHeaders', and as a result, all     #
  # browsers from the cloud will include such headers in every HTTP request.   #
  # For example, to set the Authorization bearer token.                        #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.browserCloudHttpHeaders              #
  # - Environment variable name: LOADGENERATOR_BROWSERCLOUDHTTPHEADERS         #
  ##############################################################################
  #browserCloudHttpHeaders:
  #  Authorization: Basic YOUR_AUTHORIZATION_TOKEN
  #  Your-custom-header: your custom http headers value

  ##############################################################################
  # You can set the 'browserCloudHosts' parameter if you would like            #
  # to propagate additional /etc/hosts to remote browsers.                     #
  # It might be a case where a target website domain name is not resolvable via#
  # public DNS servers. So, to reach such domains from the browsers started in #
  # the cloud, you can supply a map of additional DNS records via              #
  # 'browserCloudHosts' parameter, for example: example.com => 1.2.3.4         #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.browserCloudHosts                    #
  # - Environment variable name: LOADGENERATOR_BROWSERCLOUDHOSTS               #
  ##############################################################################
  #browserCloudHosts:
  #  localhost: 127.0.0.1
  #  example.com: 1.2.3.4

  ##############################################################################
  # You can set the 'constants' key-value map to allow common values reuse     #
  # across different suites and actions.                                       #
  #                                                                            #
  # Suite actions can refer to a value from a constants map using the following#
  # syntax: ${key_name}                                                        #
  #                                                                            #
  # This is an optional property.                                              #
  ##############################################################################
  #constants:
  #  suite.webDriverFluentWaitTimeout: 30s
  #  baseUrl: 'https://example.com'
  #  dashboardUrl: '${baseUrl}/dashboard'
  #  pom.nav: 'nav#main'
  #  pom.nav.home: '${pom.nav} ul li:nth-child(1)'

################################################################################
# Every load test has a set of one or more named suites.                       #
# A suite configuration defines concurrency, and its related parameters, a list#
# of sequential steps describing the flow of the test, and a list of actions   #
# to execute in the browser for every step.                                    #
#                                                                              #
# Load generator creates a suite instance when it starts executing a suite     #
# configuration.                                                               #
# As a general rule, a load generator maintains up to *concurrency* suite      #
# instances in parallel. Whenever a specific suite instance terminates - a load#
# generator spawns a brand new suite instance from the same configuration.     #
#                                                                              #
# The general rule of maintaining concurrent suite instance has two exceptions:#
# - Load generator considers *rampUp* and *delay* properties at the beginning  #
#   of the execution, but target concurrency should be maintained automatically#
#   once a ramp-up phase is completed.                                         #
# - Load generator considers *rampDown* property at the end of the execution,  #
#   so no new suites instances are launched once the ramp down phase is        #
#   activated.                                                                 #
#                                                                              #
# Load generator automatically launches a brand new browser in the cloud for   #
# every suite instance and automatically terminates such browser once suite    #
# instance processing is completed.                                            #
# Launching a new browser instance in the cloud takes around 2-3 seconds, so it#
# is suggested to have a smaller number of suite configurations with a larger  #
# number of steps/actions, compared to a more significant number of suites with#
# a smaller list of steps and actions.                                         #
# Such an approach should minimize the effect of launching brand new browsers  #
# on the overall statistics of the load test.                                  #
################################################################################
#suites:

  ##############################################################################
  # The suite's name - please pick a meaningful name describing the actual     #
  # purpose of the suite.                                                      #
  #                                                                            #
  # The system automatically reports every suite instance execution as a       #
  # top-level transaction with the name of the suite.                          #
  #                                                                            #
  # You can configure multiple suites, and a load generator will process such  #
  # suites in parallel. The only requirement is to have a unique name for      #
  # every configured suite.                                                    #
  ##############################################################################
  #Suite Name A:

    ############################################################################
    # Concurrency level of suite execution, i.e., how many concurrent          #
    # threads will process suite instances.                                    #
    #                                                                          #
    # Also, this parameter controls how many browsers are allowed              #
    # to be launched concurrently in the cloud for cloud-based executions.     #
    # Overrides:                                                               #
    # - System property name: suite.concurrency                                #
    # - Environment variable name: SUITE_CONCURRENCY                           #
    ############################################################################
    #concurrency: 100

    ############################################################################
    # It might be a case when tests start failing too often, either due to the #
    # problem with the test(s) logic or due to overloading of the target       #
    # system.                                                                  #
    #                                                                          #
    # Perforator automatically determines when to introduce a slowdown in case #
    # of any abnormalities with tests execution.                               #
    #                                                                          #
    # This flag controls whether automatic slowdown is enabled or not.         #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.concurrencyAutoAdjustment                  #
    # - Environment variable name: SUITE_CONCURRENCYAUTOADJUSTMENT             #
    ############################################################################
    #concurrencyAutoAdjustment: true

    ############################################################################
    # How often desired concurrency should be recalculated?                    #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.concurrencyRecalcPeriod                    #
    # - Environment variable name: SUITE_CONCURRENCYRECALCPERIOD               #
    ############################################################################
    #concurrencyRecalcPeriod: 30s

    ############################################################################
    # Perforator automatically decreases concurrency if there are too many     #
    # failing transactions.                                                    #
    #                                                                          #
    # This property determines concurrency multiplier to use while calculating #
    # scale-down adjustment.                                                   #
    #                                                                          #
    # For example, suppose the target concurrency is 1000, and the multiplier  #
    # is 0.05. In that case, the scale-down adjustment for concurrency is      #
    # 1000 x 0.05 = 50, so the system should decrease concurrency by 50 threads#
    # in case of too many failing transactions.                                #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.concurrencyScaleDownMultiplier             #
    # - Environment variable name: SUITE_CONCURRENCYSCALEDOWNMULTIPLIER        #
    ############################################################################
    #concurrencyScaleDownMultiplier: 0.05

    ############################################################################
    # Perforator automatically increases concurrency if previously it was      #
    # slowing down due to failing transactions, and the amount of such failing #
    # transactions decreases.                                                  #
    #                                                                          #
    # This property determines concurrency multiplier to use while calculating #
    # scale-up adjustment.                                                     #
    #                                                                          #
    # For example, suppose the target concurrency is 1000, and the multiplier  #
    # is 0.025. In that case, the scale-up adjustment for concurrency is       #
    # 1000 x 0.025 = 25, so the system should increase concurrency by 25       #
    # threads in case failing transactions percent goes down.                  #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.concurrencyScaleUpMultiplier               #
    # - Environment variable name: SUITE_CONCURRENCYSCALEUPMULTIPLIER          #
    ############################################################################
    #concurrencyScaleUpMultiplier: 0.025

    ############################################################################
    # Iterations count to execute this suite.                                  #
    #                                                                          #
    # This is an upper bound of maximum attempts to run the suite.             #
    # The suite should be stopped when the pre-configured duration is elapsed, #
    # or iterations count is reached, whatever comes first.                    #
    #                                                                          #
    # This is an optional property.                                            #
    #                                                                          #
    # Overrides:                                                               #
    # - System property name: suite.iterations                                 #
    # - Environment variable name: SUITE_ITERATIONS                            #
    ############################################################################
    #iterations: 100

    ############################################################################
    # Duration of the performance test for the given suite.                    #
    #                                                                          #
    # Also, this parameter controls how much time the browser cloud            #
    # will be accessible once the performance test starts in cloud mode.       #
    #                                                                          #
    # Duration of the browser cloud is rounded up to the closest               #
    # hour value.                                                              #
    # For example, if duration = 45m, then browser cloud is created for 1 hour.#
    # Overrides:                                                               #
    # - System property name: suite.duration                                   #
    # - Environment variable name: SUITE_DURATION                              #
    ############################################################################
    #duration: 30m
    
    ############################################################################
    # How much time to wait before executing suite logic once performance test #
    # starts?                                                                  #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.delay                                      #
    # - Environment variable name: SUITE_DELAY                                 #
    ############################################################################
    #delay: 0s

    ############################################################################
    # The time interval for ramping up concurrent processing of suite instances#
    # from 1 up to defined concurrency level. Concurrency is increased evenly  #
    # during *rampUp* period. For example, if you have *concurrency* = 10 and  #
    # *rampUp* = 10s, then every second additional worker thread will be       #
    # launched, starting from 1 thread up to 10 threads.                       #
    # Overrides:                                                               #
    # - System property name: suite.rampUp                                     #
    # - Environment variable name: SUITE_RAMPUP                                #
    ############################################################################
    #rampUp: 5m

    ############################################################################
    # The time interval before the end of the test to stop launching new suite #
    # instances. For example, if you have duration = 10m and rampDown = 2m,    #
    # then after the 8th minute of execution no new suite instances will be    #
    # launched.                                                                #
    #                                                                          #
    # At the same time, if a suite instance started execution before the 8th   #
    # minute - such instance will proceed execution till its natural completion#
    # Overrides:                                                               #
    # - System property name: suite.rampDown                                   #
    # - Environment variable name: SUITE_RAMPDOWN                              #
    ############################################################################
    #rampDown: 5m
    
    ############################################################################
    # The mode of launching browsers for the test suite.                       #
    # Available modes:                                                         #
    # - cloud                                                                  #
    # - local                                                                  #
    # Note: transactions reporting is disabled when browsers are launched      #
    # locally.                                                                 #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverMode                              #
    # - Environment variable name: SUITE_WEBDRIVERMODE                         #
    ############################################################################
    #webDriverMode: cloud
    
    ############################################################################
    # The mode of launching chrome instances.                                  #
    # Available modes:                                                         #
    # - headful                                                                #
    # - headless                                                               #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.chromeMode                                 #
    # - Environment variable name: SUITE_CHROMEMODE                            #
    ############################################################################
    #chromeMode: headful
    
    ############################################################################
    # The system automatically retries to create a new selenium session,       #
    # in case of an error(s), starting from the timestamp of the initial       #
    # attempt up until 'webDriverCreateSessionRetryTimeout' is reached.        #
    #                                                                          #
    # This parameter is only applicable when webDriverMode = cloud.            #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverCreateSessionRetryTimeout         #
    # - Environment variable name: SUITE_WEBDRIVERCREATESESSIONRETRYTIMEOUT    #
    ############################################################################
    #webDriverCreateSessionRetryTimeout: 1m
    
    ############################################################################
    # The system automatically retries to delete existing selenium session,    #
    # in case of an error(s), starting from the timestamp of the initial       #
    # attempt up until 'webDriverDeleteSessionRetryTimeout' is reached.        #
    #                                                                          #
    # This parameter is only applicable when webDriverMode = cloud.            #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverDeleteSessionRetryTimeout         #
    # - Environment variable name: SUITE_WEBDRIVERDELETESESSIONRETRYTIMEOUT    #
    ############################################################################
    #webDriverDeleteSessionRetryTimeout: 1m
    
    ############################################################################
    # Implicit wait timeout for selenium session.                              #
    # Specifies the amount of time the driver should wait when searching for   #
    # an element if it is not immediately present.                             #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverSessionImplicitlyWait             #
    # - Environment variable name: SUITE_WEBDRIVERSESSIONIMPLICITLYWAIT        #
    ############################################################################
    #webDriverSessionImplicitlyWait: 0s
    
    ############################################################################
    # Sets the amount of time to wait for an asynchronous JavaScript to finish #
    # execution before throwing an error.                                      #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverSessionScriptTimeout              #
    # - Environment variable name: SUITE_WEBDRIVERSESSIONSCRIPTTIMEOUT         #
    ############################################################################
    #webDriverSessionScriptTimeout: 30s

    ############################################################################
    # Selenium timeout to wait for a page load to complete before throwing     #
    # an error.                                                                #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverSessionPageLoadTimeout            #
    # - Environment variable name: SUITE_WEBDRIVERSESSIONPAGELOADTIMEOUT       #
    ############################################################################
    #webDriverSessionPageLoadTimeout: 30s

    ############################################################################
    # Selenium timeout to poll for action to be completed.                     #
    # This is an equivalent of WebDriverWait / FluentWait.                     #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverFluentWaitTimeout                 #
    # - Environment variable name: SUITE_WEBDRIVERFLUENTWAITTIMEOUT            #
    ############################################################################
    #webDriverFluentWaitTimeout: 30s

    ############################################################################
    # Keep alive remote browser during sleep actions.                          #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.webDriverSessionKeepAlive                  #
    # - Environment variable name: SUITE_WEBDRIVERSESSIONKEEPALIVE             #
    ############################################################################
    #webDriverSessionKeepAlive: true
    
    ############################################################################
    # The flag allowing file uploads functionality while working with browsers #
    # in the cloud.                                                            #
    #                                                                          #
    # This parameter is only applicable when webDriverMode = cloud.            #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverUseLocalFileDetector              #
    # - Environment variable name: SUITE_WEBDRIVERUSELOCALFILEDETECTOR         #
    ############################################################################
    #webDriverUseLocalFileDetector: true
    
    ############################################################################
    # Default window width of the browser.                                     #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverWindowWidth                       #
    # - Environment variable name: SUITE_WEBDRIVERWINDOWWIDTH                  #
    ############################################################################
    #webDriverWindowWidth: 1920
    
    ############################################################################
    # Default window height of the browser.                                    #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverWindowHeight                      #
    # - Environment variable name: SUITE_WEBDRIVERWINDOWHEIGHT                 #
    ############################################################################
    #webDriverWindowHeight: 1080

    ############################################################################
    # Allow browsers connecting to web-sites with insecure HTTPS certificates. #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.webDriverAcceptInsecureCerts               #
    # - Environment variable name: SUITE_WEBDRIVERACCEPTINSECURECERTS          #
    ############################################################################
    #webDriverAcceptInsecureCerts: false

    ############################################################################
    # The mode controlling selector type to use while searching elements on the#
    # page.                                                                    #
    #                                                                          #
    # Available modes:                                                         #
    # - css                                                                    #
    # - xpath                                                                  #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.defaultSelectorType                        #
    # - Environment variable name: SUITE_DEFAULTSELECTORTYPE                   #
    ############################################################################
    #defaultSelectorType: css

    ############################################################################
    # All the suites are processed concurrently via multiple thread workers.   #
    # Every thread worker has a dedicated ID.                                  #
    #                                                                          #
    # This flag determines should the worker ID be logged as a part of every   #
    # log item.                                                                #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.logWorkerID                                #
    # - Environment variable name: SUITE_LOGWORKERID                           #
    ############################################################################
    #logWorkerID: false

    ############################################################################
    # A new suite instance ID is generated whenever a thread worker starts     #
    # processing a test suite.                                                 #
    #                                                                          #
    # This flag determines should the suite instance ID be logged for all log  #
    # items related to the processing of the suite instance.                   #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.logSuiteInstanceID                         #
    # - Environment variable name: SUITE_LOGSUITEINSTANCEID                    #
    ############################################################################
    #logSuiteInstanceID: false

    ############################################################################
    # Should a selenium session-id be logged while processing a test suite?    #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.logRemoteWebDriverSessionID                #
    # - Environment variable name: SUITE_LOGREMOTEWEBDRIVERSESSIONID           #
    ############################################################################
    #logRemoteWebDriverSessionID: true

    ############################################################################
    # Should a transaction id be logged for every transaction in an active     #
    # state?#                                                                  #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.logTransactionID                           #
    # - Environment variable name: SUITE_LOGTRANSACTIONID                      #
    ############################################################################
    #logTransactionID: false

    ############################################################################
    # Should a transaction be logged in case of a failure?                     #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.logFailedTransactions                      #
    # - Environment variable name: SUITE_LOGFAILEDTRANSACTIONS                 #
    ############################################################################
    #logFailedTransactions: false

    ############################################################################
    # Should we log every step when it is executed by the load generator?      #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.logSteps                                   #
    # - Environment variable name: SUITE_LOGSTEPS                              #
    ############################################################################
    #logSteps: false

    ############################################################################
    # Should we log every action when it is executed by the load generator?    #
    #                                                                          #
    # This is an optional property.                                            #
    # Overrides:                                                               #
    # - System property name: suite.logActions                                 #
    # - Environment variable name: SUITE_LOGACTIONS                            #
    ############################################################################
    #logActions: false
    
    ############################################################################
    # Props is an array of key-value pairs which can be referenced in the      #
    # actions. Load generator automatically picks the next item from the array #
    # when it starts executing a new suite instance and preserves key-value    #
    # pairs till the end of the suite instance.                                #
    #                                                                          #
    # Action can refer to a value from a key-values map using the following    #
    # syntax: ${key_name}                                                      #
    #                                                                          #
    # Example:                                                                 #
    #   props:                                                                 #
    #     - user_name: user@example.com                                        #
    #       password: secure_password                                          #
    #     - user_name: user@example.com                                        #
    #       password: secure_password                                          #
    #  steps:                                                                  #
    #    login:                                                                #
    #      - open: https://example.com                                         #
    #      - input:                                                            #
    #          cssSelector: '#user_name'                                       #
    #          value: '${user_name}'                                           #
    #      - input:                                                            #
    #          cssSelector: '#password'                                        #
    #          value: '${password}'                                            #
    #      - click: '#login_button'                                            #
    #                                                                          #
    # This is an optional property.                                            # 
    ############################################################################
    #props: 
    #  - param1_name: param1_value1
    #    param2_name: param2_value1
    #  - param1_name: param1_value2
    #    param2_name: param2_value2

    ############################################################################
    # Props is an array of key-value pairs which can be referenced in the      #
    # actions.                                                                 #
    # Contradictory to 'props', where you specify values directly in the config#
    # ,'propsFile' allows you to supply values stored in an external CSV file. #
    # Load generator automatically picks the next row from CSV file when it    #
    # starts executing a new suite instance and preserves key-value pairs till #
    # the end of the suite instance.                                           #
    #                                                                          #
    # Action can refer to a value from a key-values map using the following    #
    # syntax: ${key_name}                                                      #
    #                                                                          #
    # Example CSV file's data:                                                 #
    # ---------------------------------------------                            #
    # |   user_name           |   password        |                            #
    # |-------------------------------------------|                            #
    # |   user1@example.com   |   user1Password   |                            #
    # |   user2@example.com   |   user2Password   |                            #
    # |   user3@example.com   |   user3Password   |                            #
    # |   user4@example.com   |   user4Password   |                            #
    # ---------------------------------------------                            #
    #                                                                          #
    # Example:                                                                 #
    #   propsFile: path_to_csv_file                                            #
    #  steps:                                                                  #
    #    login:                                                                #
    #      - open: https://example.com                                         #
    #      - input:                                                            #
    #          cssSelector: '#user_name'                                       #
    #          value: '${user_name}'                                           #
    #      - input:                                                            #
    #          cssSelector: '#password'                                        #
    #          value: '${password}'                                            #
    #      - click: '#login_button'                                            #
    #                                                                          #
    # This is an optional property.                                            #
    ############################################################################
    #propsFile: path_to_csv_file
    
    ############################################################################
    # Every suite should have a collection of named steps, and a step          #
    # is a sequence of actions to be executed one by one in the browser.       #
    #                                                                          #
    # Steps are reported to the analytics system as nested transactions        #
    # with the name of the step.                                               #
    #                                                                          #
    # Any failed action leads to step and suite instance failure,              #
    # so both step transaction and suite instance transaction are marked as    #
    # failed.                                                                  #
    #                                                                          #
    # Suite instance stops execution, once a failure occurs, and a new suite   #
    # instance starts execution instead.                                       #
    ############################################################################
    #steps:

      ##########################################################################
      # Name of the step - execution of such step is reported to the analytics #
      # system as nested transaction using the name of the step.               #
      #                                                                        #
      # You can choose whatever meaningful name for the step, but all such     #
      # names should be unique across the suite.                               #
      ##########################################################################
      #Open landing page and await components to be loaded:

        ########################################################################
        # Action to open specified URL in the current browser window.          #
        #                                                                      #
        # Default timeout to open the page is inherited from                   #
        # suite.webDriverSessionPageLoadTimeout, which is 30s by default.      #
        #                                                                      #
        # An action fails if such a timeout is reached, but a page is still    #
        # loading.                                                             #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - open:                                                              #
        #     url: https://...                                                 #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- open: https://verifications.perforator.io/?delay=250ms

        ########################################################################
        # Action to automatically crawl the website and visit all links/pages  #
        # uncovered during automatic navigation.                               #
        #                                                                      #
        # You can adjust the following settings to control crawler behavior:   #
        #                                                                      #
        # - url - entry point for the bot to start automatic crawling.         #
        # You can omit this configuration, and the bot should start navigating #
        # from the currently open page.                                        #
        #                                                                      #
        # - domains - list of the domains to allow crawling. The crawler skips #
        # visiting link if its domain isn't whitelisted.                       #
        # You can omit this field, and the default domain will be determined   #
        # based on the URL field or the URL of the currently opened page       #
        # (in case the URL field is omitted as well).                          #
        #                                                                      #
        # - randomize - flag controlling the order of links to visit.          #
        # Randomization is enabled by default, meaning that when the crawler   #
        # decides to visit the next link, it will pick a random one from       #
        # the queue of uncovered links. The crawler will visit uncovered links #
        # in natural order if you disable this flag.                           #
        #                                                                      #
        # - delay - delay to wait before visiting the next link available      #
        # in the crawling queue. The default value is 5s                       #
        #                                                                      #
        # - maxVisitsPerUrl - setting controlling how many times the crawler   #
        # should visit uncovered link/url. The default value is 1              #
        #                                                                      #
        # - maxVisitsOverall - setting controlling how many links should be    #
        # visited overall. The default value is 1024                           #
        #                                                                      #
        # - maxQueueSize - setting controlling the upper bound of how many     #
        # links can be available in the crawling queue at any given moment.    #
        # The default value is 4096                                            #
        #                                                                      #
        # - maxDuration - the maximum duration of the crawling action.         #
        # The default value is 5m                                              #
        #                                                                      #
        # - pageLoadTimeout - timeout to await for any page to be loaded during#
        # crawling action. The default value is 30s                            #
        #                                                                      #
        # - scriptTimeout - timeout to await for JS script execution extracting#
        # links from the currently opened page. The default value is 30s       #
        #                                                                      #
        # - linksExtractorScript - JS script to extract links/URLs available   #
        # for crawling from the currently opened page. This script is executed #
        # on every visited page to add new items into the crawling queue.      #
        # The default script is below:                                         #
        #                                                                      #
        # const result = [];                                                   #
        # const links = document.querySelectorAll("a[href]:not([href^='javascript']):not([href^='void']):not([href='#'])");
        # for(var i=0; i < links.length; i++){                                 #
        #   result.push(links[i].href);                                        #
        # }                                                                    #
        # return result;                                                       #
        #                                                                      #
        # Examples:                                                            #
        #                                                                      #
        # - crawler: https://verifications.perforator.io/                      #
        # - crawler:                                                           #
        #     url: https://verifications.perforator.io/                        #
        # - crawler:                                                           #
        #     url: https://verifications.perforator.io/                        #
        #     delay: 30s                                                       #
        #     maxVisitsOverall: 15                                             #
        #     maxDuration: 15m                                                 #
        ########################################################################
        #- crawler: https://verifications.perforator.io/?delay=250ms
        
        ########################################################################
        # Action to await page load event in the current browser window.       #
        #                                                                      #
        # Parameter of this action specifies how much time to wait till load   #
        # event occurs, before giving up and throwing an exception             #
        ########################################################################
        #- awaitPageLoad: 5s
        
        ########################################################################
        # Action to await element to be visible on the page of the current     #
        # browser window.                                                      #
        #                                                                      #
        # Parameter of this action specifies selector for the element to find  #
        # in the DOM tree of the page. By default, load-generator uses 'css'   #
        # selectors, but you can switch default and global behavior to use     #
        # 'xpath' based selectors via changing 'defaultSelectorType' property  #
        # of the suite.                                                        #
        #                                                                      #
        # Additionally, if you want to have more granular control over selector#
        # type, you can specify selector as a child property, for example:     #
        # - awaitElementToBeVisible:                                           #
        #     cssSelector: '#async-container'                                  #
        #                                                                      #
        # - awaitElementToBeVisible:                                           #
        #     xpathSelector: '//*[@id="async-container"]'                      #
        #                                                                      #
        # Element is visible when it is present in the DOM tree of the page and#
        # it is visible. Visibility means that the element is not only         #
        # displayed but also has a height and width that is greater than 0.    #
        #                                                                      #
        # Default timeout to await element to be visible is inherited from     #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but specified element  #
        # is not visible yet.                                                  #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - awaitElementToBeVisible:                                           #
        #     cssSelector: '#async-container'                                  #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- awaitElementToBeVisible: '#async-container'

        ########################################################################
        # Action to await element to be clickable on the page of the current   #
        # browser window.                                                      #
        #                                                                      #
        # Parameter of this action specifies selector for the element to find  #
        # in the DOM tree of the page. By default, load-generator uses 'css'   #
        # selectors, but you can switch default and global behavior to use     #
        # 'xpath' based selectors via changing 'defaultSelectorType' property  #
        # of the suite.                                                        #
        #                                                                      #
        # Additionally, if you want to have more granular control over selector#
        # type, you can specify selector as a child property, for example:     #
        # - awaitElementToBeClickable:                                         #
        #     cssSelector: '#form-submit-button'                               #
        #                                                                      #
        # - awaitElementToBeClickable:                                         #
        #     xpathSelector: '//*[@id="form-submit-button"]'                   #
        #                                                                      #
        # Element is clickable when it is visible on the page, it is enabled,  #
        # and you can click on it.                                             #
        #                                                                      #
        # Default timeout to await element to be clickable is inherited from   #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but specified element  #
        # is not clickable yet.                                                #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - awaitElementToBeClickable:                                         #
        #     cssSelector: '#form-submit-button'                               #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- awaitElementToBeClickable: '#form-submit-button'
        
        ########################################################################
        # Action to await element to be disabled on the page of the current    #
        # browser window.                                                      #
        #                                                                      #
        # Parameter of this action specifies selector for the element to find  #
        # in the DOM tree of the page. By default, load-generator uses 'css'   #
        # selectors, but you can switch default and global behavior to use     #
        # 'xpath' based selectors via changing 'defaultSelectorType' property  #
        # of the suite.                                                        #
        #                                                                      #
        # Additionally, if you want to have more granular control over selector#
        # type, you can specify selector as a child property, for example:     #
        # - awaitElementToBeDisabled:                                          #
        #     cssSelector: '#disabled-element'                                 #
        #                                                                      #
        # - awaitElementToBeDisabled:                                          #
        #     xpathSelector: '//*[@id="disabled-element"]'                     #
        #                                                                      #
        # Element is disabled when it is present in the DOM tree,              #
        # it is visible, and it has a *disabled* attribute turned on.          #
        # Typically this is html input element.                                #
        #                                                                      #
        # Default timeout to await element to be disabled is inherited from    #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but specified element  #
        # is not disabled yet.                                                 #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - awaitElementToBeDisabled:                                          #
        #     cssSelector: '#disabled-element'                                 #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- awaitElementToBeDisabled: '#disabled-element'

        ########################################################################
        # Action to await element to be enabled on the page of the current     #
        # browser window.                                                      #
        #                                                                      #
        # Parameter of this action specifies selector for the element to find  #
        # in the DOM tree of the page. By default, load-generator uses 'css'   #
        # selectors, but you can switch default and global behavior to use     #
        # 'xpath' based selectors via changing 'defaultSelectorType' property  #
        # of the suite.                                                        #
        #                                                                      #
        # Additionally, if you want to have more granular control over selector#
        # type, you can specify selector as a child property, for example:     #
        # - awaitElementToBeEnabled:                                           #
        #     cssSelector: '#enabled-element'                                  #
        #                                                                      #
        # - awaitElementToBeEnabled:                                           #
        #     xpathSelector: '//*[@id="enabled-element"]'                      #
        #                                                                      #
        # Element is enabled when it is present in the DOM tree,               #
        # it is visible, and it has no *disabled* attribute.                   #
        #                                                                      #
        # Default timeout to await element to be enabled is inherited from     #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but specified element  #
        # is not enabled yet.                                                  #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - awaitElementToBeEnabled:                                           #
        #     cssSelector: '#enabled-element'                                  #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- awaitElementToBeEnabled: '#enabled-element'

        ########################################################################
        # Action to await element to be invisible on the page of the current   #
        # browser window.                                                      #
        #                                                                      #
        # Parameter of this action specifies selector for the element to find  #
        # in the DOM tree of the page. By default, load-generator uses 'css'   #
        # selectors, but you can switch default and global behavior to use     #
        # 'xpath' based selectors via changing 'defaultSelectorType' property  #
        # of the suite.                                                        #
        #                                                                      #
        # Additionally, if you want to have more granular control over selector#
        # type, you can specify selector as a child property, for example:     #
        # - awaitElementToBeInvisible:                                         #
        #     cssSelector: '#invisible-element'                                #
        #                                                                      #
        # - awaitElementToBeInvisible:                                         #
        #     xpathSelector: '//*[@id="invisible-element"]'                    #
        #                                                                      #
        # Element is invisible when it is either invisible or not present      #
        # in the DOM tree.                                                     #
        #                                                                      #
        # Default timeout to await element to be invisible is inherited from   #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but specified element  #
        # is not invisible yet.                                                #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - awaitElementToBeInvisible:                                         #
        #     cssSelector: '#invisible-element'                                #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- awaitElementToBeInvisible: '#invisible-element'

        ########################################################################
        # Action to await alert to be present in the current browser window.   #
        #                                                                      #
        # Parameter of this action specifies how much time to wait till alert  #
        # is shown, before giving up and throwing an exception                 #
        ########################################################################
        #- awaitAlertToBePresent: 30s

        ########################################################################
        # Action to click on the element in the current browser window.        #
        #                                                                      #
        # Parameter of this action specifies selector for the element to find  #
        # in the DOM tree of the page. By default, load-generator uses 'css'   #
        # selectors, but you can switch default and global behavior to use     #
        # 'xpath' based selectors via changing 'defaultSelectorType' property  #
        # of the suite.                                                        #
        #                                                                      #
        # Additionally, if you want to have more granular control over selector#
        # type, you can specify selector as a child property, for example:     #
        # - click:                                                             #
        #     cssSelector: '#clickable-element'                                #
        #                                                                      #
        # - click:                                                             #
        #     xpathSelector: '//*[@id="clickable-element"]'                    #
        #                                                                      #
        # Click action can be performed only on clickable elements, so as a    #
        # prerequisite, an action waits till element is present in the DOM     #
        # tree, it is visible and enabled.                                     #
        #                                                                      #
        # Default timeout to await element to be clickable is inherited from   #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but specified element  #
        # is not clickable yet.                                                #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - click:                                                             #
        #     cssSelector: '#clickable-element'                                #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- click: '#clickable-element'

        ########################################################################
        # Action to close an alert shown in the current browser window.        #
        #                                                                      #
        # Parameter of this action specifies alert button to click. It can be  #
        # either 'ok' or 'cancel'                                              #
        #                                                                      #
        # Close alert action can be performed only when an alert is shown, so  #
        # as a prerequisite, an action waits till alert is present.            #
        #                                                                      #
        # Default timeout to await alert to be shown is inherited from         #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but an alert is not    #
        # present yet.                                                         #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - closeAlert:                                                        #
        #     action: ok                                                       #
        #     timeout: 15.5s                                                   #
        #                                                                      #
        # There are some cases, when an alert has an input field, so a user    #
        # should enter a text. You can use the following construct to send a   #
        # text alongside closing the alert:                                    #
        # - closeAlert:                                                        #
        #     action: ok                                                       #
        #     text: some text                                                  #
        #     timeout: 30s                                                     #
        ########################################################################
        #- closeAlert: ok

        ########################################################################
        # Action to focus on the element in the current browser window.        #
        #                                                                      #
        # Parameter of this action specifies selector for the element to find  #
        # in the DOM tree of the page. By default, load-generator uses 'css'   #
        # selectors, but you can switch default and global behavior to use     #
        # 'xpath' based selectors via changing 'defaultSelectorType' property  #
        # of the suite.                                                        #
        #                                                                      #
        # Additionally, if you want to have more granular control over selector#
        # type, you can specify selector as a child property, for example:     #
        # - focus:                                                             #
        #     cssSelector: '#element'                                          #
        #                                                                      #
        # - focus:                                                             #
        #     xpathSelector: '//*[@id="element"]'                              #
        #                                                                      #
        # Focus action can be performed only on visible elements, so as a      #
        # prerequisite, an action waits till element is present in the DOM     #
        # tree and it is visible.                                              #
        #                                                                      #
        # Default timeout to await element to be visible is inherited from     #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but specified element  #
        # is not visible yet.                                                  #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - focus:                                                             #
        #     cssSelector: '#element'                                          #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- focus: '#element'

        ########################################################################
        # Action to enter text into input element of the current browser window#
        #                                                                      #
        # This action has two required parameters:                             #
        # - either cssSelector or xpathSelector, specifying target element     #
        # where to enter a text.                                               #
        # - value, actual text to enter into the input element.                #
        #                                                                      #
        # Such action can also be used to upload files when target element is  #
        # '<input type="file">'. To do so, please specify path to the file you #
        # would like to upload in the 'value' property, for example:           #
        #   value: './path/to/file/for/upload.json'                            #
        #                                                                      #
        # Input action can be performed only on clickable elements, so as a    #
        # prerequisite, an action waits till element is present in the DOM     #
        # tree ,it is visible and enabled.                                     #
        #                                                                      #
        # Default timeout to await element to be clickable is inherited from   #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but specified element  #
        # is not clickable yet.                                                #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - input:                                                             #
        #     cssSelector: '#element'                                          #
        #     value: 'text to enter or file path to upload'                    #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- input:
        #    cssSelector: '#element'
        #    value: 'text to enter or file path to upload'

        ########################################################################
        # Action to scroll till the element in the current browser window.     #
        #                                                                      #
        # Parameter of this action specifies selector for the element to find  #
        # in the DOM tree of the page. By default, load-generator uses 'css'   #
        # selectors, but you can switch default and global behavior to use     #
        # 'xpath' based selectors via changing 'defaultSelectorType' property  #
        # of the suite.                                                        #
        #                                                                      #
        # Additionally, if you want to have more granular control over selector#
        # type, you can specify selector as a child property, for example:     #
        # - scroll:                                                            #
        #     cssSelector: '#element'                                          #
        #                                                                      #
        # - scroll:                                                            #
        #     xpathSelector: '//*[@id="element"]'                              #
        #                                                                      #
        # Scroll action can be performed only on visible elements, so as a     #
        # prerequisite, an action waits till element is present in the DOM     #
        # tree and it is visible.                                              #
        #                                                                      #
        # Default timeout to await element to be visible is inherited from     #
        # suite.webDriverFluentWaitTimeout, which is 30s by default.           #
        #                                                                      #
        # An action fails if such a timeout is reached, but specified element  #
        # is not visible yet.                                                  #
        #                                                                      #
        # You can use the following construct, if you would like to override   #
        # default timeout:                                                     #
        # - scroll:                                                            #
        #     cssSelector: '#element'                                          #
        #     timeout: 15.5s                                                   #
        ########################################################################
        #- scroll: '#element'

        ########################################################################
        # Action to postpone any activities according to specified duration.   #
        #                                                                      #
        # Parameter of sleep action specifies duration to sleep.               #
        #                                                                      #
        # Sleep action supports randomization, so you can use the following    #
        # construct to introduce random sleeping within specific range:        #
        # - sleep: 5s-10s                                                      #
        # Actual value would be randomly picked at the time of the action      #
        # processing.                                                          #
        ########################################################################
        #- sleep: 3.5s

        ########################################################################
        # Action to ignore remaining actions of the step.                      #
        #                                                                      #
        # Parameter of this action disables(or enables) processing of          #
        # remaining actions within a current step only.                        #
        # Actions from all remaining steps will be processed as usual.         #
        #                                                                      #
        # Examples:                                                            #
        # - ignoreRemainingActions: true                                       #
        # - ignoreRemainingActions: false                                      #
        # - ignoreRemainingActions: '${flagFromProps}'                         #
        # - ignoreRemainingActions: '${flagFromConstants}'                     #
        ########################################################################
        #- ignoreRemainingActions: true

        ########################################################################
        # Action to ignore all remaining actions and steps of the suite.       #
        # Effectively it leads to successful completion of the suite instance. #
        #                                                                      #
        # Examples:                                                            #
        # - ignoreRemainingSteps: true                                         #
        # - ignoreRemainingSteps: false                                        #
        # - ignoreRemainingSteps: '${flagFromProps}'                           #
        # - ignoreRemainingSteps: '${flagFromConstants}'                       #
        ########################################################################
        #- ignoreRemainingSteps: true