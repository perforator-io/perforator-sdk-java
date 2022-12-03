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

import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.context.SuiteConfigContext;
import io.perforator.sdk.loadgenerator.core.context.SuiteInstanceContext;
import io.perforator.sdk.loadgenerator.core.context.TransactionContext;
import io.perforator.sdk.loadgenerator.core.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//TODO: add javadoc
public abstract class AbstractLoadGenerator implements Runnable, StatisticsService {

    public static final String TERMINATION_EXCEPTION_MESSAGE = "Process was terminated manually";
    public static final String TERMINATION_BANNER = "\n"
            + " -------------------------------------------------------------------------------------------------------------------------------\n"
            + "|                                                                                                                               |\n"
            + "|    |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||    |\n"
            + "|                                                                                                                               |\n"
            + "|                                          Process termination(^C) has been requested.                                          |\n"
            + "|                                                                                                                               |\n"
            + "|                            Please wait a bit till all resources are cleaned up and closed properly.                           |\n"
            + "|                                                                                                                               |\n"
            + "|    |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||    |\n"
            + "|                                                                                                                               |\n"
            + " -------------------------------------------------------------------------------------------------------------------------------\n";

    static {
        System.setProperty("log4j.shutdownHookEnabled", "false");
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final AtomicInteger workerSequence = new AtomicInteger(0);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final LoadGeneratorConfig loadGeneratorConfig;
    private final List<SuiteConfig> suiteConfigs;
    private final IntegrationService mediator;
    private final ExecutorService executor;

    public AbstractLoadGenerator(LoadGeneratorConfig loadGeneratorConfig, List<SuiteConfig> suiteConfigs) {
        this(null, loadGeneratorConfig, suiteConfigs);
    }

    protected AbstractLoadGenerator(IntegrationService mediator, LoadGeneratorConfig loadGeneratorConfig, List<SuiteConfig> suiteConfigs) {
        if(loadGeneratorConfig == null) {
            throw new RuntimeException("loadGeneratorConfig is required");
        }

        LoadGeneratorConfigValidator.validate(loadGeneratorConfig);

        if(suiteConfigs == null || suiteConfigs.isEmpty()) {
            throw new RuntimeException("suiteConfigs is required");
        }

        if(loadGeneratorConfig.isPrioritizeSystemProperties()) {
            loadGeneratorConfig.applyDefaults();
            
            for (SuiteConfig suiteConfig : suiteConfigs) {
                suiteConfig.applyDefaults();
            }
        }
        
        if (mediator == null) {
            try {
                Class<?> clazz = getClass().getClassLoader().loadClass(
                        "io.perforator.sdk.loadgenerator.core.internal.MediatingIntegrationServiceImpl"
                );
                Constructor<?> constructor = clazz.getDeclaredConstructor(
                        LoadGeneratorConfig.class,
                        List.class,
                        Runnable.class
                );
                constructor.setAccessible(true);
                this.mediator = (IntegrationService) constructor.newInstance(
                        loadGeneratorConfig,
                        suiteConfigs,
                        (Runnable) this::onShutdown
                );
                constructor.setAccessible(false);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can't instantiate default mediator", e);
            }
        } else {
            this.mediator = mediator;
        }
        this.loadGeneratorConfig = loadGeneratorConfig;
        this.suiteConfigs = copy(suiteConfigs);
        this.executor = Executors.newCachedThreadPool(this::buildWorkerThread);
    }

    protected static final <T> List<T> copy(List<?> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();
        for (Object item : items) {
            result.add((T) item);
        }

        return result;
    }

    protected abstract void runSuite(SuiteInstanceContext suiteInstanceContext);

    @Override
    public final void run() {
        if (started.getAndSet(true)) {
            throw new RuntimeException(
                    "Can't run the same runner multiple times"
            );
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::cancel));

        try {
            mediator.onLoadGeneratorStarted();
        } catch (RuntimeException e) {
            this.onShutdown();
            throw e;
        }

        List<Future> futures = new ArrayList<>();
        for (SuiteConfig suiteConfig : suiteConfigs) {
            AtomicInteger preStartCounter = new AtomicInteger();
            SuiteConfigContext suiteConfigContext = mediator.onSuiteConfigCreated(suiteConfig);
            for (int i = 0; i < suiteConfig.getConcurrency(); i++) {
                futures.add(
                        executor.submit(
                                new SuiteRunner(preStartCounter, suiteConfigContext, i)
                        )
                );
            }
        }

        try {
            for (Future future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            if (logger.isDebugEnabled()) {
                logger.error("Process is interrupted", e);
            }
            cancel();
            throw new RuntimeException(TERMINATION_EXCEPTION_MESSAGE, e);
        }

        onShutdown();
        
        long failedSuites = getFailedSuiteInstancesCount();
        if(failedSuites > 0 && loadGeneratorConfig.isFailOnSuiteErrors()) {
            throw new RuntimeException(
                    "There are " + failedSuites + " failed suites"
            );
        }
        
        long failedTransactions = getFailedTransactionsCount();
        if(failedTransactions > 0 && loadGeneratorConfig.isFailOnTransactionErrors()) {
            throw new RuntimeException(
                    "There are " + failedTransactions + " failed transactions"
            );
        }
    }

    @Override
    public final long getSuccessfulSuiteInstancesCount() {
        return mediator.getSuccessfulSuiteInstancesCount();
    }

    @Override
    public final long getFailedSuiteInstancesCount() {
        return mediator.getFailedSuiteInstancesCount();
    }

    @Override
    public final long getActiveSuiteInstancesCount() {
        return mediator.getActiveSuiteInstancesCount();
    }

    @Override
    public final long getSuccessfulTransactionsCount() {
        return mediator.getSuccessfulTransactionsCount();
    }

    @Override
    public final long getFailedTransactionsCount() {
        return mediator.getFailedTransactionsCount();
    }

    @Override
    public final long getActiveTransactionsCount() {
        return mediator.getActiveTransactionsCount();
    }

    @Override
    public long getActiveTopLevelTransactionsCount() {
        return mediator.getActiveTopLevelTransactionsCount();
    }

    @Override
    public long getActiveNestedTransactionsCount() {
        return mediator.getActiveNestedTransactionsCount();
    }

    @Override
    public long getActiveSessionsCount() {
        return mediator.getActiveSessionsCount();
    }

    protected final boolean shouldBeFinished() {
        return finished.get() || cancelled.get() || Threaded.isInterrupted();
    }

    protected final boolean isStarted() {
        return started.get();
    }

    protected final boolean isCancelled() {
        return cancelled.get();
    }

    protected final boolean isFinished() {
        return finished.get();
    }

    protected final RemoteWebDriverService getRemoteWebDriverService() {
        return mediator;
    }

    protected final TransactionsService getTransactionsService() {
        return mediator;
    }

    protected final StatisticsService getStatisticsService() {
        return mediator;
    }
    
    protected final TransactionContext startTransaction(SuiteInstanceContext suiteInstanceContext, String transactionName) {
        return mediator.startTransaction(suiteInstanceContext, transactionName);
    }
    
    protected final void finishTransaction(TransactionContext transactionContext, Throwable transactionError) {
        if(shouldBeFinished()) {
            mediator.finishTransaction(
                    transactionContext, 
                    new RuntimeException(TERMINATION_EXCEPTION_MESSAGE)
            );
        } else {
            mediator.finishTransaction(
                    transactionContext, 
                    transactionError
            );
        }
    }

    protected LoadGeneratorConfig getLoadGeneratorConfig(){
        return loadGeneratorConfig;
    }

    private void propagateConsumerContext(SuiteInstanceContext suiteInstanceContext) {
        Perforator.SUITE_INSTANCE_CONTEXT.set(suiteInstanceContext);
        Perforator.REMOTE_WEBDRIVER_SERVICE.set(mediator);
        Perforator.SLEEP_SERVICE.set(mediator);
        Perforator.TRANSACTIONS_SERVICE.set(mediator);
        Perforator.TRANSACTIONS.set(new HashMap<>());
    }

    private void cleanupConsumerContext() {
        Perforator.SUITE_INSTANCE_CONTEXT.remove();
        Perforator.REMOTE_WEBDRIVER_SERVICE.remove();
        Perforator.SLEEP_SERVICE.remove();
        Perforator.TRANSACTIONS_SERVICE.remove();
        Perforator.TRANSACTIONS.remove();
    }

    private synchronized void cancel() {
        if (!finished.get()) {
            cancelled.set(true);
        }
        
        try {
            this.onShutdown();
        } finally {
            shutDownLogger();
        }
    }

    private synchronized void onShutdown() {
        if (finished.getAndSet(true)) {
            return;
        }

        if (cancelled.get()) {
            logger.info(TERMINATION_BANNER);
        }

        executor.shutdownNow();

        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "Problem awaiting for executor to be finished",
                        e
                );
            }
        }

        if (cancelled.get()) {
            mediator.onLoadGeneratorFinished(
                    new RuntimeException(TERMINATION_EXCEPTION_MESSAGE)
            );
        } else {
            mediator.onLoadGeneratorFinished(
                    null
            );
        }
    }

    private void shutDownLogger() {
        try {
            getClass().getClassLoader().loadClass(
                    "org.apache.logging.log4j.LogManager"
            ).getDeclaredMethod(
                    "shutdown"
            ).invoke(
                    null
            );
        } catch (ReflectiveOperationException e) {
            //ignore
        }
    }

    private Thread buildWorkerThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName("perforator-worker-" + workerSequence.getAndIncrement());
        thread.setUncaughtExceptionHandler((t, e) -> {
            if (logger.isDebugEnabled()) {
                logger.error("Unexpected issue happened", e);
            }
        });

        return thread;
    }

    private class SuiteRunner implements Runnable {

        private final AtomicInteger preStartCounter;
        private final int workerNumber;
        private final SuiteConfigContext suiteConfigContext;

        public SuiteRunner(AtomicInteger preStartCounter, SuiteConfigContext suiteConfigContext, int workerNumber) {
            this.preStartCounter = preStartCounter;
            this.suiteConfigContext = suiteConfigContext;
            this.workerNumber = workerNumber;
        }

        @Override
        public void run() {
            SuiteConfig suiteConfig = suiteConfigContext.getSuiteConfig();
            long startTime = System.currentTimeMillis();
            long endTime = startTime + suiteConfig.getDuration().toMillis() - suiteConfig.getRampDown().toMillis();
            long delay = suiteConfig.getDelay().toMillis() + (suiteConfig.getRampUp().toMillis() / suiteConfig.getConcurrency()) * workerNumber;

            if (delay > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Sleeping {}ms for the worker {} processing suite {}",
                            delay,
                            workerNumber,
                            suiteConfig.getName()
                    );
                }
                Threaded.sleep(delay);
            }

            long slowdown = 0;
            while (!shouldBeFinished()) {
                long remainingTime = endTime - System.currentTimeMillis();
                if(remainingTime <= 0) {
                    return;
                }
                
                if (slowdown > 0) {
                    slowdown = Math.min(slowdown, remainingTime);
                    Threaded.sleep(slowdown);
                    slowdown = 0;
                }
                
                SuiteInstanceContext suiteInstanceContext = startSuiteContextIfAllowed(
                        this.suiteConfigContext
                );
                if (suiteInstanceContext == null) {
                    if (shouldBeFinished()) {
                        return;
                    } else {
                        Threaded.sleep(
                                calculateDelayForDesiredConcurrency(
                                        this.suiteConfigContext,
                                        1,
                                        250
                                )
                        );
                        continue;
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Worker {} has started processing suite {} using suite instance id {}",
                            workerNumber,
                            suiteConfig.getName(),
                            suiteInstanceContext.getSuiteInstanceID()
                    );
                }

                try {
                    propagateConsumerContext(suiteInstanceContext);
                    runSuite(suiteInstanceContext);
                    slowdown = mediator.onSuiteInstanceFinished(
                            suiteInstanceContext,
                            isCancelled() ? new RuntimeException(TERMINATION_EXCEPTION_MESSAGE) : null
                    );

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Worker {} has successfully completed processing suite {} using suite instance id {}",
                                workerNumber,
                                suiteConfig.getName(),
                                suiteInstanceContext.getSuiteInstanceID()
                        );
                    }

                } catch (RuntimeException e) {
                    if (!isCancelled() && logger.isDebugEnabled()) {
                        logger.error(
                                "Worker {} has failed processing suite {} using suite instance id {}",
                                workerNumber,
                                suiteConfig.getName(),
                                suiteInstanceContext.getSuiteInstanceID(),
                                e
                        );
                    }

                    if (shouldBeFinished()) {
                        mediator.onSuiteInstanceFinished(
                                suiteInstanceContext,
                                new RuntimeException(TERMINATION_EXCEPTION_MESSAGE)
                        );
                        return;
                    } else {
                        slowdown = mediator.onSuiteInstanceFinished(
                                suiteInstanceContext,
                                e
                        );
                    }
                } finally {
                    cleanupConsumerContext();
                }
            }
        }
        
        private long calculateDelayForDesiredConcurrency(SuiteConfigContext suiteConfigContext, long minDelay, long maxDelay) {
            double maxConcurrency = suiteConfigContext.getSuiteConfig().getConcurrency();
            double desiredConcurrency = mediator.getDesiredConcurrency(suiteConfigContext);
            double delta = maxConcurrency - desiredConcurrency;
            double multiplier = delta / maxConcurrency;
            
            return Math.max(
                    minDelay, 
                    (long)(multiplier * maxDelay)
            );
        }
        
        private SuiteInstanceContext startSuiteContextIfAllowed(SuiteConfigContext suiteConfigContext) {
            try {
                int suitesToBeStarted = preStartCounter.incrementAndGet();
                int currentConcurrency = mediator.getCurrentConcurrency(suiteConfigContext);
                int desiredConcurrency = mediator.getDesiredConcurrency(suiteConfigContext);

                if (currentConcurrency + suitesToBeStarted <= desiredConcurrency && !shouldBeFinished()) {
                    return mediator.onSuiteInstanceStarted(
                            workerNumber,
                            suiteConfigContext
                    );
                } else {
                    return null;
                }
            } finally {
                preStartCounter.decrementAndGet();
            }
        }

    }

}
