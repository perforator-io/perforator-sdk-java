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
  # It might be a case when tests start failing too often, either due to the   #
  # problem with the test(s) logic or due to overloading of the target         #
  # system.                                                                    #
  #                                                                            #
  # Perforator automatically determines when to introduce a slowdown in case   #
  # of any abnormalities with tests execution.                                 #
  #                                                                            #
  # This flag controls whether automatic slowdown is enabled or not.           #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.concurrencyAutoAdjustment            #
  # - Environment variable name: LOADGENERATOR_CONCURRENCYAUTOADJUSTMENT       #
  ##############################################################################
  #concurrencyAutoAdjustment: true

  ##############################################################################
  # How often desired concurrency should be recalculated?                      #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.concurrencyRecalcPeriod              #
  # - Environment variable name: LOADGENERATOR_CONCURRENCYRECALCPERIOD         #
  ##############################################################################
  #concurrencyRecalcPeriod: 30s

  ##############################################################################
  # Perforator automatically decreases concurrency if there are too many       #
  # failing transactions.                                                      #
  #                                                                            #
  # This property determines concurrency multiplier to use while calculating   #
  # scale-down adjustment.                                                     #
  #                                                                            #
  # For example, suppose the target concurrency is 1000, and the multiplier is #
  # 0.05. In that case, the scale-down adjustment for concurrency is           #
  # 1000 x 0.05 = 50, so the system should decrease concurrency by 50 threads  #
  # in case of too many failing transactions.                                  #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.concurrencyScaleDownMultiplier       #
  # - Environment variable name: LOADGENERATOR_CONCURRENCYSCALEDOWNMULTIPLIER  #
  ##############################################################################
  #concurrencyScaleDownMultiplier: 0.05

  ##############################################################################
  # Perforator automatically increases concurrency if previously it was        #
  # slowing down due to failing transactions, and the amount of such failing   #
  # transactions decreases.                                                    #
  #                                                                            #
  # This property determines concurrency multiplier to use while calculating   #
  # scale-up adjustment.                                                       #
  #                                                                            #
  # For example, suppose the target concurrency is 1000, and the multiplier is #
  # 0.025. In that case, the scale-up adjustment for concurrency is            #
  # 1000 x 0.025 = 25, so the system should increase concurrency by 25 threads #
  # in case failing transactions percent goes down.                            #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.concurrencyScaleUpMultiplier         #
  # - Environment variable name: LOADGENERATOR_CONCURRENCYSCALEUPMULTIPLIER    #
  ##############################################################################
  #concurrencyScaleUpMultiplier: 0.025

  ##############################################################################
  # All the suites are processed concurrently via multiple thread workers.     #
  # Every thread worker has a dedicated ID.                                    #
  #                                                                            #
  # This flag determines should the worker ID be logged as a part of every log #
  # item.                                                                      #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.logWorkerID                          #
  # - Environment variable name: LOADGENERATOR_LOGWORKERID                     #
  ##############################################################################
  #logWorkerID: false

  ##############################################################################
  # A new suite instance ID is generated whenever a thread worker starts       #
  # processing a test suite.                                                   #
  #                                                                            #
  # This flag determines should the suite instance ID be logged for all log    #
  # items related to the processing of the suite instance.                     #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.logSuiteInstanceID                   #
  # - Environment variable name: LOADGENERATOR_LOGSUITEINSTANCEID              #
  ##############################################################################
  #logSuiteInstanceID: false

  ##############################################################################
  # Should a selenium session-id be logged while processing a test suite?      #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.logRemoteWebDriverSessionID          #
  # - Environment variable name: LOADGENERATOR_LOGREMOTEWEBDRIVERSESSIONID     #
  ##############################################################################
  #logRemoteWebDriverSessionID: true

  ##############################################################################
  # Should a transaction id be logged for every transaction in an active state?#
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.logTransactionID                     #
  # - Environment variable name: LOADGENERATOR_LOGTRANSACTIONID                #
  ##############################################################################
  #logTransactionID: false
  
  ##############################################################################
  # Should we log every step when it is executed by the load generator?        #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.logSteps                             #
  # - Environment variable name: LOADGENERATOR_LOGSTEPS                        #
  ##############################################################################
  #logSteps: false
  
  ##############################################################################
  # Should we log every action when it is executed by the load generator?      #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.logActions                           #
  # - Environment variable name: LOADGENERATOR_LOGACTIONS                      #
  ##############################################################################
  #logActions: false

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
  # The mode controlling selector type to use while searching elements on the  #
  # page.                                                                      #
  #                                                                            #
  # Available modes:                                                           #
  # - css                                                                      #
  # - xpath                                                                    #
  #                                                                            #
  # This is an optional property.                                              #
  # Overrides:                                                                 #
  # - System property name: loadGenerator.defaultSelectorType                  #
  # - Environment variable name: LOADGENERATOR_DEFAULTSELECTORTYPE             #
  ##############################################################################
  #defaultSelectorType: css

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
  # You can set the ‘browserCloudHosts’ parameter if you would like            #
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
  # suites in parallel. The only requirement is to have a unique name for     #
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
    # Should a chrome driver be started in silent mode?                        #
    # Chrome driver service sends all output by default to System::out stream, #
    # and its output is not necessary in a majority of the cases.              #
    # Please turn off this flag if you would like to see output from Chrome    #
    # driver service.                                                          #
    #                                                                          #
    # This is an optional property.                                            # 
    # Overrides:                                                               #
    # - System property name: suite.chromeDriverSilent                         #
    # - Environment variable name: SUITE_CHROMEDRIVERSILENT                    #
    ############################################################################
    #chromeDriverSilent: true
    
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
        # Default timeout to open the page is 30s.                             #
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
        # of the load-generator.                                               #
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
        # Default timeout to await element to be visible is 30s.               #
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
        # of the load-generator.                                               #
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
        # Default timeout to await element to be clickable is 30s.             #
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
        # of the load-generator.                                               #
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
        # Default timeout to await element to be disabled is 30s.              #
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
        # of the load-generator.                                               #
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
        # Default timeout to await element to be enabled is 30s.               #
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
        # of the load-generator.                                               #
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
        # Default timeout to await element to be invisible is 30s.             #
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
        # of the load-generator.                                               #
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
        # Default timeout to await element to be clickable is 30s.             #
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
        # Default timeout to await alert to be shown is 30s.                   #
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
        # of the load-generator.                                               #
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
        # Default timeout to await element to be visible is 30s.               #
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
        # Default timeout to await element to be clickable is 30s.             #
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
        # of the load-generator.                                               #
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
        # Default timeout to await element to be visible is 30s.               #
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