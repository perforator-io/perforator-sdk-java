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

import com.google.common.collect.Lists;
import io.perforator.sdk.api.okhttpgson.model.AnalyticsEvent;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class TransactionEventsAggregatorImpl implements TransactionEventsAggregator {

    private static final int TRANSACTIONS_AGGREGATION_PER_THREAD = 1024;

    private ExecutorService aggregationExecutor;

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        int concurrency = loadGeneratorContext.getSuiteConfigContexts().stream().mapToInt(s -> s.getSuiteConfig().getConcurrency()).sum();
        int aggregationThreads = concurrency * 2 / TRANSACTIONS_AGGREGATION_PER_THREAD + 1;
        aggregationExecutor = Executors.newFixedThreadPool(aggregationThreads);
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        if (aggregationExecutor != null) {
            aggregationExecutor.shutdown();
        }
    }

    @Override
    public void onTransactionStarted(long timestamp, TransactionContextImpl context) {
        if (context.getSuiteContext().getSuiteConfigContext().getSuiteConfig().getWebDriverMode() != WebDriverMode.cloud) {
            return;
        }

        context.getLoadGeneratorContext().getEventsBuffer().add(
                createTransactionEvents(
                        timestamp,
                        context,
                        EventType.transaction_heartbeat,
                        null
                )
        );
    }

    @Override
    public void onTransactionFinished(long timestamp, TransactionContextImpl context, Throwable error) {
        if (context.getSuiteContext().getSuiteConfigContext().getSuiteConfig().getWebDriverMode() != WebDriverMode.cloud) {
            return;
        }

        context.getLoadGeneratorContext().getEventsBuffer().add(
                createTransactionEvents(
                        timestamp,
                        context,
                        EventType.transaction_completed,
                        error
                )
        );
    }

    @Override
    public void onRemoteWebDriverStarted(long timestamp, RemoteWebDriverContextImpl driverContext) {
        SuiteInstanceContextImpl suiteContext = driverContext.getSuiteInstanceContext();
        SuiteConfig suiteConfig = suiteContext.getSuiteConfigContext().getSuiteConfig();

        if (suiteConfig.getWebDriverMode() != WebDriverMode.cloud) {
            return;
        }

        List<AnalyticsEvent> localBuffer = new ArrayList<>();
        suiteContext.getTransactions().forEach(transaction -> {
            AnalyticsEvent eventDto = new AnalyticsEvent();
            eventDto.setTimestamp(timestamp);
            eventDto.setSuiteName(suiteConfig.getName());
            eventDto.setSuiteInstanceId(suiteContext.getSuiteInstanceID());

            eventDto.setSessionId(driverContext.getSessionID());
            eventDto.setBrowserName(driverContext.getBrowserName());
            eventDto.setBrowserVersion(driverContext.getBrowserVersion());

            eventDto.setTransactionId(transaction.getTransactionID());
            eventDto.setTransactionName(transaction.getTransactionName());

            TransactionContextImpl parentTransaction = transaction.getParentTransactionContext();
            if (parentTransaction != null) {
                eventDto.setParentTransactionId(parentTransaction.getTransactionID());
                eventDto.setParentTransactionName(parentTransaction.getTransactionName());
            }

            eventDto.setEventType(EventType.transaction_heartbeat.name());

            localBuffer.add(eventDto);
        });

        if (!localBuffer.isEmpty()) {
            suiteContext.getLoadGeneratorContext().getEventsBuffer().add(localBuffer);
        }
    }

    @Override
    public void onRemoteWebDriverFinished(long timestamp, RemoteWebDriverContextImpl driverContext, Throwable error) {
        SuiteInstanceContextImpl suiteContext = driverContext.getSuiteInstanceContext();
        SuiteConfig suiteConfig = suiteContext.getSuiteConfigContext().getSuiteConfig();

        if (suiteConfig.getWebDriverMode() != WebDriverMode.cloud) {
            return;
        }

        List<AnalyticsEvent> localBuffer = new ArrayList<>();
        suiteContext.getTransactions().forEach(transaction -> {
            AnalyticsEvent eventDto = new AnalyticsEvent();
            eventDto.setTimestamp(timestamp);
            eventDto.setSuiteName(suiteConfig.getName());
            eventDto.setSuiteInstanceId(suiteContext.getSuiteInstanceID());

            eventDto.setSessionId(driverContext.getSessionID());
            eventDto.setBrowserName(driverContext.getBrowserName());
            eventDto.setBrowserVersion(driverContext.getBrowserVersion());

            eventDto.setTransactionId(transaction.getTransactionID());
            eventDto.setTransactionName(transaction.getTransactionName());

            TransactionContextImpl parentTransaction = transaction.getParentTransactionContext();
            if (parentTransaction != null) {
                eventDto.setParentTransactionId(parentTransaction.getTransactionID());
                eventDto.setParentTransactionName(parentTransaction.getTransactionName());
            }

            eventDto.setEventType(EventType.transaction_heartbeat.name());
            localBuffer.add(eventDto);
        });

        if (!localBuffer.isEmpty()) {
            suiteContext.getLoadGeneratorContext().getEventsBuffer().add(localBuffer);
        }
    }

    @Override
    public void onHeartbeat(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        List<TransactionContextImpl> transactions = new ArrayList<>();
        for (SuiteConfigContextImpl suiteConfigContext: loadGeneratorContext.getSuiteConfigContexts()){
            if(suiteConfigContext.getSuiteConfig().getWebDriverMode() != WebDriverMode.cloud){
                continue;
            }
            for(SuiteInstanceContextImpl suiteInstanceContext: suiteConfigContext.getSuiteInstanceContexts()){
                transactions.addAll(suiteInstanceContext.getTransactions());
            }
        }

        List<List<TransactionContextImpl>> partitions = Lists.partition(
                transactions,
                TRANSACTIONS_AGGREGATION_PER_THREAD
        );

        for (List<TransactionContextImpl> partition : partitions) {
            aggregationExecutor.submit(() -> {
                List<AnalyticsEvent> localBuffer = new ArrayList<>();

                for (TransactionContextImpl transaction : partition) {
                    localBuffer.addAll(createTransactionEvents(
                            timestamp,
                            transaction,
                            EventType.transaction_heartbeat,
                            null
                    ));
                }

                if (!localBuffer.isEmpty()) {
                    loadGeneratorContext.getEventsBuffer().add(localBuffer);
                }
            });
        }
    }

    private List<AnalyticsEvent> createTransactionEvents(
            long timestamp,
            TransactionContextImpl transaction,
            EventType eventType,
            Throwable error
    ) {
        SuiteInstanceContextImpl suiteContext = transaction.getSuiteContext();
        SuiteConfig suiteConfig = suiteContext.getSuiteConfigContext().getSuiteConfig();
        Map<String, RemoteWebDriverContextImpl> suiteDriverContexts = suiteContext.getDrivers();

        List<AnalyticsEvent> events = new ArrayList<>();

        if (suiteDriverContexts == null || suiteDriverContexts.isEmpty()) {
            AnalyticsEvent eventDto = new AnalyticsEvent();
            eventDto.setTimestamp(timestamp);
            eventDto.setSuiteName(suiteConfig.getName());
            eventDto.setSuiteInstanceId(suiteContext.getSuiteInstanceID());
            eventDto.setTransactionId(transaction.getTransactionID());
            eventDto.setTransactionName(transaction.getTransactionName());

            TransactionContextImpl parentTransaction = transaction.getParentTransactionContext();
            if (parentTransaction != null) {
                eventDto.setParentTransactionId(parentTransaction.getTransactionID());
                eventDto.setParentTransactionName(parentTransaction.getTransactionName());
            }

            eventDto.setEventType(eventType.name());

            String failureMessage = null;

            if (error != null) {
                StringWriter sw = new StringWriter();
                error.printStackTrace(new PrintWriter(sw));
                failureMessage = sw.toString().trim();
            }

            eventDto.setFailed(error != null);
            eventDto.setFailureMessage(failureMessage);
            events.add(eventDto);
        } else {
            suiteDriverContexts.values().forEach(driverContext -> {
                AnalyticsEvent eventDto = new AnalyticsEvent();
                eventDto.setTimestamp(timestamp);
                eventDto.setSuiteName(suiteConfig.getName());
                eventDto.setSuiteInstanceId(suiteContext.getSuiteInstanceID());

                eventDto.setSessionId(driverContext.getSessionID());
                eventDto.setBrowserName(driverContext.getBrowserName());
                eventDto.setBrowserVersion(driverContext.getBrowserVersion());

                eventDto.setTransactionId(transaction.getTransactionID());
                eventDto.setTransactionName(transaction.getTransactionName());

                TransactionContextImpl parentTransaction = transaction.getParentTransactionContext();
                if (parentTransaction != null) {
                    eventDto.setParentTransactionId(parentTransaction.getTransactionID());
                    eventDto.setParentTransactionName(parentTransaction.getTransactionName());
                }

                eventDto.setEventType(eventType.name());

                String failureMessage = null;

                if (error != null) {
                    StringWriter sw = new StringWriter();
                    error.printStackTrace(new PrintWriter(sw));
                    failureMessage = sw.toString().trim();
                }

                eventDto.setFailed(error != null);
                eventDto.setFailureMessage(failureMessage);
                events.add(eventDto);
            });
        }
        return events;
    }
}
