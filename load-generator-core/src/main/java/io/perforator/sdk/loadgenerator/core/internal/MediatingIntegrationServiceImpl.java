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

import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.service.IntegrationService;

import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.chrome.ChromeOptions;

final class MediatingIntegrationServiceImpl implements IntegrationService<SuiteConfigContextImpl, SuiteInstanceContextImpl, TransactionContextImpl, RemoteWebDriverContextImpl> {

    private final EventsRouterImpl eventsRouter;
    private final TimeProvider timeProvider;
    private final SuiteManager suiteManager;
    private final ApiClientManager apiClientManager;
    private final HttpClientsManager httpClientsManager;
    private final BrowserCloudManager browserCloudManager;
    private final RemoteWebDriverManager remoteWebDriverManager;
    private final HeartbeatManager heartbeatManager;
    private final SleepManager sleepManager;
    private final TransactionsManager transactionsManager;
    private final TransactionEventsAggregator transactionEventsAggregator;
    private final ConcurrencyEventsAggregatorImpl concurrencyEventsAggregator;
    private final AnalyticsEventsFlusher analyticsEventsFlusher;
    private final SlowdownManager slowdownManager;
    private final ConcurrencyManager concurrencyManager;
    private final LoggingContextManager loggingContextManager;
    private final SeleniumLoggingManager seleniumLoggingManager;
    private final ReportingManager reportingManager;
    private final StatisticsManagerImpl statisticsManager;
    private final InfoMessagesManager infoMessagesManager;
    private final LoadGeneratorContextManagerImpl loadGeneratorContextManager;

    private final LoadGeneratorContextImpl loadGeneratorContext;

    MediatingIntegrationServiceImpl(LoadGeneratorConfig loadGeneratorConfig, List<SuiteConfig> suiteConfigs, Runnable shutdownHook) {

        this.eventsRouter = new EventsRouterImpl();
        this.timeProvider = new TimeProviderImpl();
        this.suiteManager = new SuiteManagerImpl(timeProvider, eventsRouter);
        this.apiClientManager = new ApiClientManagerImpl();
        this.httpClientsManager = new HttpClientsManagerImpl();
        this.browserCloudManager = new BrowserCloudManagerImpl(timeProvider, shutdownHook);
        this.remoteWebDriverManager = new RemoteWebDriverManagerImpl(timeProvider, eventsRouter);
        this.heartbeatManager = new HeartbeatManagerImpl(timeProvider, eventsRouter);
        this.sleepManager = new SleepManagerImpl(timeProvider, eventsRouter);
        this.transactionsManager = new TransactionsManagerImpl(timeProvider, eventsRouter);
        this.transactionEventsAggregator = new TransactionEventsAggregatorImpl();
        this.analyticsEventsFlusher = new AnalyticsEventsFlusherImpl();
        this.concurrencyEventsAggregator = new ConcurrencyEventsAggregatorImpl();
        this.slowdownManager = new SlowdownManagerImpl(timeProvider, loadGeneratorConfig);
        this.concurrencyManager = new ConcurrencyManagerImpl();
        this.loggingContextManager = new LoggingContextManagerImpl(loadGeneratorConfig);
        this.seleniumLoggingManager = new SeleniumLoggingManagerImpl();
        this.reportingManager = new ReportingManagerImpl();
        this.statisticsManager = new StatisticsManagerImpl();
        this.infoMessagesManager = new InfoMessagesManagerImpl();
        this.loadGeneratorContextManager = new LoadGeneratorContextManagerImpl();

        this.loadGeneratorContext = new LoadGeneratorContextImpl(
                this.timeProvider.getCurrentTime(),
                loadGeneratorConfig,
                suiteConfigs
        );

        eventsRouter.setLoadGeneratorStartedListeners(Arrays.asList(
                apiClientManager,
                suiteManager,
                httpClientsManager,
                browserCloudManager,
                transactionEventsAggregator,
                heartbeatManager,
                analyticsEventsFlusher,
                reportingManager,
                seleniumLoggingManager,
                infoMessagesManager
        ));

        eventsRouter.setLoadGeneratorFinishedListeners(Arrays.asList(
                loadGeneratorContextManager,
                concurrencyManager,
                suiteManager,
                httpClientsManager,
                browserCloudManager,
                reportingManager,
                analyticsEventsFlusher,
                heartbeatManager,
                transactionEventsAggregator,
                infoMessagesManager
        ));

        eventsRouter.setSuiteInstanceStartedListeners(Arrays.asList(
                concurrencyManager,
                statisticsManager,
                loggingContextManager,
                transactionsManager
        ));

        eventsRouter.setSuiteInstanceFinishedListeners(Arrays.asList(
                concurrencyManager,
                statisticsManager,
                remoteWebDriverManager,
                transactionsManager,
                loggingContextManager
        ));
        
        eventsRouter.setSuiteInstanceKeepAliveListeners(Arrays.asList(
                remoteWebDriverManager
        ));

        eventsRouter.setTransactionStartedListeners(Arrays.asList(
                statisticsManager,
                loggingContextManager,
                transactionEventsAggregator
        ));

        eventsRouter.setTransactionFinishedListeners(Arrays.asList(
                statisticsManager,
                transactionEventsAggregator,
                loggingContextManager
        ));

        eventsRouter.setRemoteWebDriverStartedListeners(Arrays.asList(
                statisticsManager,
                loggingContextManager,
                transactionEventsAggregator
        ));

        eventsRouter.setRemoteWebDriverFinishedListeners(Arrays.asList(
                statisticsManager,
                transactionEventsAggregator,
                loggingContextManager
        ));

        eventsRouter.setHeartbeatListeners(Arrays.asList(
                concurrencyManager,
                transactionEventsAggregator,
                concurrencyEventsAggregator,
                analyticsEventsFlusher,
                reportingManager
        ));
    }

    @Override
    public void onLoadGeneratorStarted() {
        eventsRouter.onLoadGeneratorStarted(
                getCurrentTime(),
                loadGeneratorContext
        );
    }

    @Override
    public void onLoadGeneratorFinished(Throwable loadGeneratorError) {
        eventsRouter.onLoadGeneratorFinished(
                getCurrentTime(),
                loadGeneratorContext,
                loadGeneratorError
        );
    }

    @Override
    public SuiteConfigContextImpl onSuiteConfigCreated(SuiteConfig suiteConfig) {
        return loadGeneratorContext.getSuiteConfigContext(suiteConfig);
    }

    @Override
    public SuiteInstanceContextImpl onSuiteInstanceStarted(int workerID, SuiteConfigContextImpl suiteConfigContext) {
        return suiteManager.startSuiteInstance(workerID, loadGeneratorContext, suiteConfigContext);
    }

    @Override
    public long onSuiteInstanceFinished(SuiteInstanceContextImpl suiteContext, Throwable suiteError) {
        long slowdownTimeout = slowdownManager.getSlowdownTimeout(
                suiteContext,
                suiteError
        );

        suiteManager.stopSuiteInstance(
                suiteContext,
                suiteError
        );

        return slowdownTimeout;
    }

    @Override
    public RemoteWebDriverContextImpl startRemoteWebDriver(SuiteInstanceContextImpl suiteContext, ChromeOptions chromeOptions) {
        return remoteWebDriverManager.startRemoteWebDriver(suiteContext, chromeOptions);
    }

    @Override
    public TransactionContextImpl startTransaction(SuiteInstanceContextImpl suiteContext, String transactionName) {
        return transactionsManager.startTransaction(suiteContext, transactionName);
    }

    @Override
    public void finishTransaction(TransactionContextImpl transactionContext, Throwable transactionError) {
        transactionsManager.finishTransaction(transactionContext, transactionError);
    }

    @Override
    public long getActiveSuiteInstancesCount() {
        return loadGeneratorContext.getStatisticsContext().getSuiteInstancesInProgress();
    }

    @Override
    public long getSuccessfulSuiteInstancesCount() {
        return loadGeneratorContext.getStatisticsContext().getSuiteInstancesSuccessful();
    }

    @Override
    public long getFailedSuiteInstancesCount() {
        return loadGeneratorContext.getStatisticsContext().getSuiteInstancesFailed();
    }

    @Override
    public long getActiveTransactionsCount() {
        return loadGeneratorContext.getStatisticsContext().getTransactionsInProgress();
    }

    @Override
    public long getSuccessfulTransactionsCount() {
        return loadGeneratorContext.getStatisticsContext().getTransactionsSuccessful();
    }

    @Override
    public long getFailedTransactionsCount() {
        return loadGeneratorContext.getStatisticsContext().getTransactionsFailed();
    }

    @Override
    public long getActiveTopLevelTransactionsCount() {
        return loadGeneratorContext.getStatisticsContext().getTopLevelTransactionsInProgress();
    }

    @Override
    public long getActiveNestedTransactionsCount() {
        return loadGeneratorContext.getStatisticsContext().getNestedTransactionsInProgress();
    }

    @Override
    public long getActiveSessionsCount() {
        return loadGeneratorContext.getStatisticsContext().getSessionsInProgress();
    }

    @Override
    public int getCurrentConcurrency(SuiteConfigContextImpl suiteConfigContext) {
        return concurrencyManager.getCurrentConcurrency(suiteConfigContext);
    }

    @Override
    public int getDesiredConcurrency(SuiteConfigContextImpl suiteConfigContext) {
        return concurrencyManager.getDesiredConcurrency(suiteConfigContext);
    }
    
    @Override
    public void sleep(SuiteInstanceContextImpl context, long duration) {
        sleepManager.sleep(context, duration);
    }

    private long getCurrentTime() {
        return timeProvider.getCurrentTime();
    }

}
