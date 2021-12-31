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
import io.perforator.sdk.api.okhttpgson.model.TransactionEvent;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.Map;

final class TransactionEventsAggregatorImpl implements TransactionEventsAggregator {
    
    private static final int TRANSACTIONS_AGGREGATION_PER_THREAD = 1024;
    
    private ExecutorService aggregationExecutor;

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        int concurrency = loadGeneratorContext.getSuiteConfigs().stream().mapToInt(SuiteConfig::getConcurrency).sum();
        int aggregationThreads = concurrency * 2 / TRANSACTIONS_AGGREGATION_PER_THREAD + 1;
        aggregationExecutor = Executors.newFixedThreadPool(aggregationThreads);
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        if(aggregationExecutor != null) {
            aggregationExecutor.shutdown();
        }
    }

    @Override
    public void onTransactionStarted(long timestamp, TransactionContextImpl context) {
        if (context.getSuiteContext().getSuiteConfig().getWebDriverMode() != WebDriverMode.cloud) {
            return;
        }

        context.getLoadGeneratorContext().getEventsBuffer().add(
                createAnalyticalEvents(
                        timestamp,
                        context,
                        EventType.transaction_heartbeat,
                        null
                )
        );
    }

    @Override
    public void onTransactionFinished(long timestamp, TransactionContextImpl context, Throwable error) {
        if (context.getSuiteContext().getSuiteConfig().getWebDriverMode() != WebDriverMode.cloud) {
            return;
        }

        context.getLoadGeneratorContext().getEventsBuffer().add(
                createAnalyticalEvents(
                        timestamp,
                        context,
                        EventType.transaction_completed,
                        error
                )
        );
    }

    @Override
    public void onRemoteWebDriverStarted(long timestamp, RemoteWebDriverContextImpl driverContext) {
        SuiteContextImpl suiteContext = driverContext.getSuiteContext();
        SuiteConfig suiteConfig = suiteContext.getSuiteConfig();

        if (suiteConfig.getWebDriverMode() != WebDriverMode.cloud) {
            return;
        }

        List<TransactionEvent> localBuffer = new ArrayList<>();
        suiteContext.getTransactions().forEach(transaction -> {
            TransactionEvent eventDto = new TransactionEvent();
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
        SuiteContextImpl suiteContext = driverContext.getSuiteContext();
        SuiteConfig suiteConfig = suiteContext.getSuiteConfig();

        if (suiteConfig.getWebDriverMode() != WebDriverMode.cloud) {
            return;
        }

        List<TransactionEvent> localBuffer = new ArrayList<>();
        suiteContext.getTransactions().forEach(transaction -> {
            TransactionEvent eventDto = new TransactionEvent();
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
        loadGeneratorContext.getSuiteContexts().stream().filter(
                s -> s.getSuiteConfig().getWebDriverMode() == WebDriverMode.cloud
        ).forEach(suiteContext -> {
            transactions.addAll(suiteContext.getTransactions());
        });
        
        List<List<TransactionContextImpl>> partitions = Lists.partition(
                transactions, 
                TRANSACTIONS_AGGREGATION_PER_THREAD
        );
        
        for (List<TransactionContextImpl> partition : partitions) {
            aggregationExecutor.submit(() -> {
                List<TransactionEvent> localBuffer = new ArrayList<>();

                for (TransactionContextImpl transaction : partition) {
                    localBuffer.addAll(createAnalyticalEvents(
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

    private List<TransactionEvent> createAnalyticalEvents(
            long timestamp,
            TransactionContextImpl transaction,
            EventType eventType,
            Throwable error
    ) {
        SuiteContextImpl suiteContext = transaction.getSuiteContext();
        SuiteConfig suiteConfig = suiteContext.getSuiteConfig();
        Map<String, RemoteWebDriverContextImpl> suiteDriverContexts = suiteContext.getDrivers();

        List<TransactionEvent> events = new ArrayList<>();

        if (suiteDriverContexts == null || suiteDriverContexts.isEmpty()) {
            TransactionEvent eventDto = new TransactionEvent();
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
                TransactionEvent eventDto = new TransactionEvent();
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

    public enum EventType {
        transaction_heartbeat,
        transaction_completed
    }

}
