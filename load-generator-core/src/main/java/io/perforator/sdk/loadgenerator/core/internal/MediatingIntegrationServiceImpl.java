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
import org.openqa.selenium.Capabilities;

import java.util.Arrays;
import java.util.List;

final class MediatingIntegrationServiceImpl implements IntegrationService<SuiteContextImpl, TransactionContextImpl, RemoteWebDriverContextImpl> {

    private final EventsRouterImpl eventsRouter;
    private final TimeProvider timeProvider;
    private final SuiteManager suiteManager;
    private final ApiClientManager apiClientManager;
    private final HttpClientsManager httpClientsManager;
    private final BrowserCloudManager browserCloudManager;
    private final RemoteWebDriverManager remoteWebDriverManager;
    private final HeartbeatManager heartbeatManager;
    private final TransactionsManager transactionsManager;
    private final TransactionEventsAggregator transactionEventsAggregator;
    private final TransactionEventsFlusher transactionEventsFlusher;
    private final SlowdownManager slowdownManager;
    private final LoggingContextManager loggingContextManager;
    private final SeleniumLoggingManager seleniumLoggingManager;
    private final ReportingManager reportingManager;
    private final StatisticsManagerImpl statisticsManager;
    private final InfoMessagesManager infoMessagesManager;

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
        this.transactionsManager = new TransactionsManagerImpl(timeProvider, eventsRouter);
        this.transactionEventsAggregator = new TransactionEventsAggregatorImpl();
        this.transactionEventsFlusher = new TransactionEventsFlusherImpl();
        this.slowdownManager = new SlowdownManagerImpl(timeProvider, loadGeneratorConfig);
        this.loggingContextManager = new LoggingContextManagerImpl(loadGeneratorConfig);
        this.seleniumLoggingManager = new SeleniumLoggingManagerImpl();
        this.reportingManager = new ReportingManagerImpl();
        this.statisticsManager = new StatisticsManagerImpl();
        this.infoMessagesManager = new InfoMessagesManagerImpl();

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
                transactionEventsFlusher,
                reportingManager,
                seleniumLoggingManager,
                infoMessagesManager
        ));

        eventsRouter.setLoadGeneratorFinishedListeners(Arrays.asList(
                suiteManager,
                httpClientsManager,
                browserCloudManager,
                reportingManager,
                transactionEventsFlusher,
                heartbeatManager,
                transactionEventsAggregator,
                infoMessagesManager
        ));

        eventsRouter.setSuiteInstanceStartedListeners(Arrays.asList(
                statisticsManager,
                loggingContextManager,
                transactionsManager
        ));

        eventsRouter.setSuiteInstanceFinishedListeners(Arrays.asList(
                statisticsManager,
                remoteWebDriverManager,
                transactionsManager,
                loggingContextManager
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
                loggingContextManager,
                transactionEventsAggregator
        ));

        eventsRouter.setRemoteWebDriverFinishedListeners(Arrays.asList(
                transactionEventsAggregator,
                loggingContextManager
        ));

        eventsRouter.setHeartbeatListeners(Arrays.asList(
                transactionEventsAggregator,
                transactionEventsFlusher,
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
    public SuiteContextImpl onSuiteInstanceStarted(int workerID, SuiteConfig suiteConfig) {
        return suiteManager.startSuiteInstance(workerID, loadGeneratorContext, suiteConfig);
    }

    @Override
    public long onSuiteInstanceFinished(SuiteContextImpl suiteContext, Throwable suiteError) {
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
    public RemoteWebDriverContextImpl startRemoteWebDriver(SuiteContextImpl suiteContext, Capabilities capabilities) {
        return remoteWebDriverManager.startRemoteWebDriver(suiteContext, capabilities);
    }

    @Override
    public TransactionContextImpl startTransaction(SuiteContextImpl suiteContext, String transactionName) {
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

    private long getCurrentTime() {
        return timeProvider.getCurrentTime();
    }

}
