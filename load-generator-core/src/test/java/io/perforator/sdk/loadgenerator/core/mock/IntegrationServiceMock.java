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
package io.perforator.sdk.loadgenerator.core.mock;

import io.perforator.sdk.loadgenerator.core.RemoteWebDriverHelper;
import io.perforator.sdk.loadgenerator.core.Threaded;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.service.IntegrationService;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationServiceMock implements IntegrationService<SuiteConfigContextMock, SuiteInstanceContextMock, TransactionContextMock, RemoteWebDriverContextMock> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationServiceMock.class);

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final AtomicInteger suiteInstancesActive = new AtomicInteger(0);
    private final AtomicInteger suiteInstancesSuccessful = new AtomicInteger(0);
    private final AtomicInteger suiteInstancesFailed = new AtomicInteger(0);
    private final AtomicInteger topLevelTransactionsInProgress = new AtomicInteger(0);
    private final AtomicInteger nestedTransactionsInProgress = new AtomicInteger(0);
    private final AtomicInteger sessionsInProgress = new AtomicInteger(0);
    private final ConcurrentHashMap<String, TransactionContextMock> transactionsActive = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TransactionContextMock> transactionsSuccessful = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TransactionContextMock> transactionsFailed = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RemoteWebDriverContextMock> remoteWebDriverContexts = new ConcurrentHashMap<>();

    @Override
    public void onLoadGeneratorStarted() {
        started.set(true);
    }

    @Override
    public SuiteConfigContextMock onSuiteConfigCreated(SuiteConfig suiteConfig) {
        return new SuiteConfigContextMock(suiteConfig);
    }

    @Override
    public SuiteInstanceContextMock onSuiteInstanceStarted(int workerID, SuiteConfigContextMock suiteConfigContext) {
        SuiteConfig suiteConfig = suiteConfigContext.getSuiteConfig();
        long iterationNumber = counters.computeIfAbsent(
                suiteConfig.getId(),
                i -> new AtomicLong(0)
        ).getAndIncrement();
        suiteInstancesActive.incrementAndGet();
        return new SuiteInstanceContextMock(workerID, iterationNumber, new SuiteConfigContextMock(suiteConfig));
    }

    @Override
    public long onSuiteInstanceFinished(SuiteInstanceContextMock suiteContext, Throwable suiteError) {
        suiteInstancesActive.decrementAndGet();
        if (suiteError != null) {
            suiteInstancesFailed.incrementAndGet();

            LOGGER.error(
                    "Suite {}, {} failed with error",
                    suiteContext.getSuiteConfigContext().getSuiteConfig().getName(),
                    suiteContext.getSuiteInstanceID(),
                    suiteError
            );
        } else {
            suiteInstancesSuccessful.incrementAndGet();
        }
        return 0;
    }

    @Override
    public void onLoadGeneratorFinished(Throwable loadGeneratorError) {
        finished.set(true);
    }

    @Override
    public TransactionContextMock startTransaction(SuiteInstanceContextMock suiteContext, String transactionName) {
        TransactionContextMock result = new TransactionContextMock(
                transactionName
        );
        transactionsActive.put(
                result.getTransactionID(),
                result
        );
        topLevelTransactionsInProgress.incrementAndGet();
        return result;
    }

    @Override
    public void finishTransaction(TransactionContextMock transactionContext, Throwable transactionError) {
        topLevelTransactionsInProgress.decrementAndGet();
        transactionsActive.remove(
                transactionContext.getTransactionID()
        );

        if (transactionError != null) {
            transactionsFailed.put(
                    transactionContext.getTransactionID(),
                    transactionContext
            );

            LOGGER.error(
                    "Transaction {}, {} failed with error",
                    transactionContext.getTransactionName(),
                    transactionContext.getTransactionID(),
                    transactionError
            );
        } else {
            transactionsSuccessful.put(
                    transactionContext.getTransactionID(),
                    transactionContext
            );
        }
    }

    @Override
    public RemoteWebDriverContextMock startRemoteWebDriver(SuiteInstanceContextMock suiteContext, ChromeOptions chromeOptions) {
        RemoteWebDriver driver = RemoteWebDriverHelper.createLocalChromeDriver(
                chromeOptions,
                suiteContext.getSuiteConfigContext().getSuiteConfig()
        );
        RemoteWebDriverContextMock context = new RemoteWebDriverContextMock(
                driver
        );
        remoteWebDriverContexts.put(
                driver.getSessionId().toString(),
                context
        );
        sessionsInProgress.incrementAndGet();
        return context;
    }

    public boolean isStarted() {
        return started.get();
    }

    public boolean isFinished() {
        return finished.get();
    }

    @Override
    public long getActiveSuiteInstancesCount() {
        return suiteInstancesActive.get();
    }

    @Override
    public long getSuccessfulSuiteInstancesCount() {
        return suiteInstancesSuccessful.get();
    }

    @Override
    public long getFailedSuiteInstancesCount() {
        return suiteInstancesFailed.get();
    }

    @Override
    public long getActiveTransactionsCount() {
        return transactionsActive.size();
    }

    @Override
    public long getSuccessfulTransactionsCount() {
        return transactionsSuccessful.size();
    }

    @Override
    public long getFailedTransactionsCount() {
        return transactionsFailed.size();
    }

    @Override
    public long getActiveTopLevelTransactionsCount() {
        return topLevelTransactionsInProgress.get();
    }

    @Override
    public long getActiveNestedTransactionsCount() {
        return nestedTransactionsInProgress.get();
    }

    @Override
    public long getActiveSessionsCount() {
        return sessionsInProgress.get();
    }

    public Collection<TransactionContextMock> getActiveTransactions() {
        return transactionsActive.values();
    }

    public Collection<TransactionContextMock> getSuccessfulTransactions() {
        return transactionsSuccessful.values();
    }

    public Collection<TransactionContextMock> getFailedTransactions() {
        return transactionsFailed.values();
    }

    public Collection<RemoteWebDriverContextMock> getAllRemoteWebDriverContexts() {
        return remoteWebDriverContexts.values();
    }

    @Override
    public int getCurrentConcurrency(SuiteConfigContextMock suiteConfigContext) {
        return suiteInstancesActive.get();
    }

    @Override
    public int getDesiredConcurrency(SuiteConfigContextMock suiteConfigContext) {
        return suiteConfigContext.getSuiteConfig().getConcurrency();
    }

    @Override
    public void sleep(SuiteInstanceContextMock context, long duration) {
        Threaded.sleep(duration);
    }

}
