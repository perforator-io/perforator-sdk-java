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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ConcurrencyManagerImpl implements ConcurrencyManager<SuiteConfigContextImpl> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrencyManagerImpl.class);
    private static final long RECALC_PERIOD = 30000L;

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        for (SuiteConfigContextImpl suiteConfigContext : loadGeneratorContext.getSuiteConfigContexts()) {
            suiteConfigContext.getConcurrencyContext().setNextRecalcTimestamp(Long.MAX_VALUE);
        }
    }

    @Override
    public void onHeartbeat(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        for (SuiteConfigContextImpl suiteConfigContext : loadGeneratorContext.getSuiteConfigContexts()) {
            ConcurrencyContextImpl concurrencyContext = suiteConfigContext.getConcurrencyContext();
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
    public void onSuiteInstanceStarted(long timestamp, SuiteInstanceContextImpl suiteContext) {
        suiteContext.getSuiteConfigContext().getConcurrencyContext().updateCurrentConcurrency(1);
    }

    @Override
    public void onSuiteInstanceFinished(long timestamp, SuiteInstanceContextImpl suiteContext, Throwable error) {
        ConcurrencyContextImpl concurrencyContext = suiteContext.getSuiteConfigContext().getConcurrencyContext();

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
    public int getMaxConcurrency(SuiteConfigContextImpl suiteConfigContext) {
        return suiteConfigContext.getConcurrencyContext().getMaxConcurrency();
    }

    @Override
    public int getMinConcurrency(SuiteConfigContextImpl suiteConfigContext) {
        return suiteConfigContext.getConcurrencyContext().getMinConcurrency();
    }

    @Override
    public int getDesiredConcurrency(SuiteConfigContextImpl suiteConfigContext) {
        return suiteConfigContext.getConcurrencyContext().getDesiredConcurrency();
    }

    @Override
    public int getCurrentConcurrency(SuiteConfigContextImpl suiteConfigContext) {
        return suiteConfigContext.getConcurrencyContext().getCurrentConcurrency();
    }

}
