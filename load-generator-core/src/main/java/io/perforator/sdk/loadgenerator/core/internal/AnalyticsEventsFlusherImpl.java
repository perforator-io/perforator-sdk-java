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

import com.google.gson.Gson;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.model.AnalyticsEvent;
import io.perforator.sdk.api.okhttpgson.model.AnalyticsEventsSubmissionResult;
import io.perforator.sdk.loadgenerator.core.Threaded;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

final class AnalyticsEventsFlusherImpl implements AnalyticsEventsFlusher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsEventsFlusherImpl.class);
    private static final Gson GSON = new Gson();

    private final AtomicInteger inflightEvents = new AtomicInteger(0);
    private ExecutorService executor;

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl context) {
        int concurrency = context.getSuiteConfigs().stream().mapToInt(SuiteConfig::getConcurrency).sum();
        int flusherThreads = concurrency * 4 / context.getLoadGeneratorConfig().getEventsFlushThreshold() + 1;

        executor = Executors.newFixedThreadPool(flusherThreads);
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl context, Throwable error) {
        if (executor == null) {
            return;
        }

        long lastReporting = 0;
        while (!context.getEventsBuffer().isEmpty() || inflightEvents.get() > 0) {
            if (lastReporting + 1000 <= System.currentTimeMillis()) {
                LOGGER.info("Please wait - there are still {} analytical events to be flushed",
                        context.getEventsBuffer().stream().mapToInt(List::size).sum() + inflightEvents.get()
                );
                lastReporting = System.currentTimeMillis();
            }

            Threaded.sleep(
                    context.getLoadGeneratorConfig().getEventsFlushInterval().toMillis()
            );
        }

        executor.shutdown();
    }

    @Override
    public void onHeartbeat(long timestamp, LoadGeneratorContextImpl context) {
        int threshold = context.getLoadGeneratorConfig().getEventsFlushThreshold();
        List<AnalyticsEvent> batch = new ArrayList<>(threshold);
        List<AnalyticsEvent> events = null;

        while ((events = context.getEventsBuffer().poll()) != null) {
            for (AnalyticsEvent event : events) {
                inflightEvents.addAndGet(1);
                batch.add(event);
                if (batch.size() >= threshold) {
                    submitEventsToExecutor(context, batch);
                    batch = new ArrayList<>(threshold);
                }
            }
        }

        if (!batch.isEmpty()) {
            submitEventsToExecutor(context, batch);
        }
    }

    private void submitEventsToExecutor(LoadGeneratorContextImpl context, List<AnalyticsEvent> batch) {
        CompletableFuture.supplyAsync(
                () -> sendAnalyticalEvents(context, batch),
                executor
        ).whenComplete((retryableEvents, error) -> {
            if (error != null) {
                context.getEventsBuffer().add(batch);
            } else if (retryableEvents != null && !retryableEvents.isEmpty()) {
                context.getEventsBuffer().add(retryableEvents);
            }

            inflightEvents.addAndGet(-1 * batch.size());
        });
    }

    private List<AnalyticsEvent> sendAnalyticalEvents(LoadGeneratorContextImpl context, List<AnalyticsEvent> events) {
        List<AnalyticsEvent> retryableEvents = new ArrayList<>();

        try {
            LOGGER.debug("Sending {} events for processing", events.size());
            AnalyticsEventsSubmissionResult result = context.getBrowserCloudsApi().sendAnalyticsEvents(
                    context.getBrowserCloudContext().getProjectKey(),
                    context.getBrowserCloudContext().getExecutionKey(),
                    context.getBrowserCloudContext().getBrowserCloudKey(),
                    events
            );

            LOGGER.debug(
                    "{} events were submitted for processing => {} rejected and {} retryable",
                    events.size(),
                    result.getRejectedEvents() == null ? 0 : result.getRejectedEvents().size(),
                    result.getRetryableEvents() == null ? 0 : result.getRetryableEvents().size()
            );

            if (result.getRejectedEvents() != null && !result.getRejectedEvents().isEmpty()) {
                LOGGER.warn(
                        "{} events were rejected => {}",
                        result.getRejectedEvents().size(),
                        GSON.toJson(result.getRejectedEvents())
                );
            }

            if (result.getRetryableEvents() != null && !result.getRetryableEvents().isEmpty()) {
                LOGGER.warn(
                        "{} events have to be resubmitted later",
                        result.getRetryableEvents().size()
                );
                retryableEvents.addAll(result.getRetryableEvents());
            }
        } catch (ApiException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Can't send events", e);
            }
            retryableEvents.addAll(events);
        }

        return retryableEvents;
    }

}
