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

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConcurrencyManagerImpl implements ConcurrencyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrencyManagerImpl.class);
    private static final long RECALC_PERIOD = 30000l;

    private final HashMap<String, ConcurrencyContextImpl> concurrencyContexts = new HashMap<>();

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        concurrencyContexts.clear();
        for (SuiteConfig suiteConfig : loadGeneratorContext.getSuiteConfigs()) {
            ConcurrencyContextImpl concurrencyContext = new ConcurrencyContextImpl(
                    suiteConfig,
                    loadGeneratorContext.getLoadGeneratorConfig().isSlowdown(),
                    timestamp + RECALC_PERIOD
            );
            concurrencyContexts.put(suiteConfig.getId(), concurrencyContext);
        }
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        for (ConcurrencyContextImpl concurrencyContext : concurrencyContexts.values()) {
            concurrencyContext.setNextRecalcTimestamp(Long.MAX_VALUE);
        }
    }

    @Override
    public void onHeartbeat(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        for (ConcurrencyContextImpl concurrencyContext : concurrencyContexts.values()) {
            if (!concurrencyContext.isSlowdownEnabled()) {
                continue;
            }

            if (timestamp < concurrencyContext.getNextRecalcTimestamp()) {
                continue;
            }

            int successfulSuites = concurrencyContext.getSuccessfulSuitesCounter();
            int failedSuites = concurrencyContext.getFailedSuitesCounter();
            int totalSuites = successfulSuites + failedSuites;
            int recalcThreshold = Math.max(25, concurrencyContext.getMaxConcurrency() / 20);
            int scaleAdjustment = Math.max(1, concurrencyContext.getMaxConcurrency() / 20);

            if (totalSuites < recalcThreshold) {
                continue;
            }

            float failedPercentage = failedSuites * 100f / totalSuites;

            concurrencyContext.updateFailedSuitesCounter(-1 * failedSuites);
            concurrencyContext.updateSuccessfulSuitesCounter(-1 * successfulSuites);
            concurrencyContext.setNextRecalcTimestamp(timestamp + RECALC_PERIOD);

            if (failedPercentage < 5) {
                int oldDesiredConcurrency = concurrencyContext.getDesiredConcurrency();
                int newDesiredConcurrency = concurrencyContext.updateDesiredConcurrency(scaleAdjustment);

                if (newDesiredConcurrency > oldDesiredConcurrency) {
                    LOGGER.info(
                            "Increasing desired concurrency from {} to {}",
                            oldDesiredConcurrency,
                            newDesiredConcurrency
                    );
                }
            } else {
                int oldDesiredConcurrency = concurrencyContext.getDesiredConcurrency();
                int newDesiredConcurrency = concurrencyContext.updateDesiredConcurrency(-1 * scaleAdjustment);
                
                if (newDesiredConcurrency < oldDesiredConcurrency) {
                    LOGGER.warn(
                            "Reducing desired concurrency from {} to {} due to {} suite fails out of {} recent",
                            oldDesiredConcurrency,
                            newDesiredConcurrency,
                            failedSuites,
                            totalSuites
                    );
                }
            }
        }
    }

    @Override
    public void onSuiteInstanceStarted(long timestamp, SuiteContextImpl suiteContext) {
        concurrencyContexts.get(
                suiteContext.getSuiteConfig().getId()
        ).updateCurrentConcurrency(1);
    }

    @Override
    public void onSuiteInstanceFinished(long timestamp, SuiteContextImpl suiteContext, Throwable error) {
        ConcurrencyContextImpl concurrencyContext = concurrencyContexts.get(
                suiteContext.getSuiteConfig().getId()
        );

        concurrencyContext.updateCurrentConcurrency(-1);

        if (!concurrencyContext.isSlowdownEnabled()) {
            return;
        }

        if (error == null) {
            concurrencyContext.updateSuccessfulSuitesCounter(1);
        } else {
            concurrencyContext.updateFailedSuitesCounter(1);
        }
    }

    @Override
    public int getMaxConcurrency(SuiteConfig suiteConfig) {
        return concurrencyContexts.get(suiteConfig.getId()).getMaxConcurrency();
    }

    @Override
    public int getMinConcurrency(SuiteConfig suiteConfig) {
        return concurrencyContexts.get(suiteConfig.getId()).getMinConcurrency();
    }

    @Override
    public int getDesiredConcurrency(SuiteConfig suiteConfig) {
        return concurrencyContexts.get(suiteConfig.getId()).getDesiredConcurrency();
    }

    @Override
    public int getCurrentConcurrency(SuiteConfig suiteConfig) {
        return concurrencyContexts.get(suiteConfig.getId()).getCurrentConcurrency();
    }

}
