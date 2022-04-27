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

import io.perforator.sdk.api.okhttpgson.model.AnalyticsEvent;

import java.util.ArrayList;
import java.util.List;

final class ConcurrencyEventsAggregatorImpl implements ConcurrencyEventsAggregator {

    @Override
    public void onHeartbeat(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        if(loadGeneratorContext.isFinished() || loadGeneratorContext.isLocalOnly()){
            return;
        }
        
        List<AnalyticsEvent> events = new ArrayList<>();

        for (SuiteConfigContextImpl suiteConfigContext : loadGeneratorContext.getSuiteConfigContexts()) {
            String suiteName = suiteConfigContext.getSuiteConfig().getName();
            StatisticsContextImpl statisticsContext = suiteConfigContext.getStatisticsContext();

            events.add(
                    createConcurrencyEvent(
                            timestamp,
                            ConcurrencyType.concurrent_top_level_transactions,
                            statisticsContext.getTopLevelTransactionsInProgress(),
                            suiteName
                    )
            );
            events.add(
                    createConcurrencyEvent(
                            timestamp,
                            ConcurrencyType.concurrent_nested_transactions,
                            statisticsContext.getNestedTransactionsInProgress(),
                            suiteName
                    )
            );
            events.add(
                    createConcurrencyEvent(
                            timestamp,
                            ConcurrencyType.concurrent_transactions,
                            statisticsContext.getTransactionsInProgress(),
                            suiteName
                    )
            );
            events.add(
                    createConcurrencyEvent(
                            timestamp,
                            ConcurrencyType.concurrent_sessions,
                            statisticsContext.getSessionsInProgress(),
                            suiteName
                    )
            );
        }

        StatisticsContextImpl loadGeneratorStatisticsContext = loadGeneratorContext.getStatisticsContext();
        
        events.add(
                createConcurrencyEvent(
                        timestamp,
                        ConcurrencyType.concurrent_top_level_transactions,
                        loadGeneratorStatisticsContext.getTopLevelTransactionsInProgress(),
                        null
                )
        );
        events.add(
                createConcurrencyEvent(
                        timestamp,
                        ConcurrencyType.concurrent_nested_transactions,
                        loadGeneratorStatisticsContext.getNestedTransactionsInProgress(),
                        null
                )
        );
        events.add(
                createConcurrencyEvent(
                        timestamp,
                        ConcurrencyType.concurrent_transactions,
                        loadGeneratorStatisticsContext.getTransactionsInProgress(),
                        null
                )
        );
        events.add(
                createConcurrencyEvent(
                        timestamp,
                        ConcurrencyType.concurrent_sessions,
                        loadGeneratorStatisticsContext.getSessionsInProgress(),
                        null
                )
        );

        if (!events.isEmpty()) {
            loadGeneratorContext.getEventsBuffer().add(events);
        }
    }

    private AnalyticsEvent createConcurrencyEvent(
            long timestamp,
            ConcurrencyType concurrencyType,
            long concurrencyValue,
            String suiteName
    ) {
        AnalyticsEvent eventDto = new AnalyticsEvent();
        eventDto.setTimestamp(timestamp);
        eventDto.setConcurrencyType(concurrencyType.name());
        eventDto.setConcurrencyValue((int) concurrencyValue);
        eventDto.setEventType(EventType.concurrency.name());
        eventDto.setSuiteName(suiteName);
        return eventDto;
    }

    enum ConcurrencyType {
        concurrent_top_level_transactions,
        concurrent_nested_transactions,
        concurrent_transactions,
        concurrent_sessions
    }

}
