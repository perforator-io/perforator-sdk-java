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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

final class ConcurrencyContextImpl {

    private final SuiteConfig suiteConfig;
    private final boolean slowdownEnabled;

    private final AtomicInteger currentConcurrency;
    private final AtomicInteger desiredConcurrency;
    private final AtomicInteger failedSuitesCounter;
    private final AtomicInteger successfulSuitesCounter;
    private final AtomicLong nextRecalcTimestamp;
    private final int minConcurrency;
    private final int maxConcurrency;

    public ConcurrencyContextImpl(SuiteConfig suiteConfig, boolean slowdownEnabled, long nextRecalcTimestamp) {
        this.suiteConfig = suiteConfig;
        this.slowdownEnabled = slowdownEnabled;
        this.maxConcurrency = suiteConfig.getConcurrency();
        this.minConcurrency = determineMinConcurrency(maxConcurrency);
        this.currentConcurrency = new AtomicInteger(0);
        this.desiredConcurrency = new AtomicInteger(maxConcurrency);
        this.failedSuitesCounter = new AtomicInteger(0);
        this.successfulSuitesCounter = new AtomicInteger(0);
        this.nextRecalcTimestamp = new AtomicLong(nextRecalcTimestamp);
    }
    
    private static int determineMinConcurrency(int maxConcurrency) {
        if(maxConcurrency <= 25) {
            return maxConcurrency;
        } else {
            return Math.max(25, maxConcurrency / 20);
        }
    }

    public int getMinConcurrency() {
        return minConcurrency;
    }

    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    public int updateCurrentConcurrency(int delta) {
        return currentConcurrency.addAndGet(delta);
    }

    public void setCurrentConcurrency(int newValue) {
        currentConcurrency.set(newValue);
    }

    public int getCurrentConcurrency() {
        return currentConcurrency.get();
    }

    public int updateDesiredConcurrency(int delta) {
        return desiredConcurrency.accumulateAndGet(delta, (result, d) -> {
            result = result + d;

            if (result > maxConcurrency) {
                return maxConcurrency;
            } else if (result < minConcurrency) {
                return minConcurrency;
            } else {
                return result;
            }
        });
    }

    public void setDesiredConcurrency(int newValue) {
        if (newValue > maxConcurrency) {
            desiredConcurrency.set(maxConcurrency);
        } else if (newValue < minConcurrency) {
            desiredConcurrency.set(minConcurrency);
        } else {
            desiredConcurrency.set(newValue);
        }
    }

    public int getDesiredConcurrency() {
        return desiredConcurrency.get();
    }

    public SuiteConfig getSuiteConfig() {
        return suiteConfig;
    }

    public boolean isSlowdownEnabled() {
        return slowdownEnabled;
    }

    public void setNextRecalcTimestamp(long nextRecalcTimestamp) {
        this.nextRecalcTimestamp.set(nextRecalcTimestamp);
    }

    public long getNextRecalcTimestamp() {
        return nextRecalcTimestamp.get();
    }

    public int updateFailedSuitesCounter(int delta) {
        return failedSuitesCounter.addAndGet(delta);
    }

    public void setFailedSuitesCounter(int newValue) {
        failedSuitesCounter.set(newValue);
    }

    public int getFailedSuitesCounter() {
        return failedSuitesCounter.get();
    }

    public int updateSuccessfulSuitesCounter(int delta) {
        return successfulSuitesCounter.addAndGet(delta);
    }

    public void setSuccessfulSuitesCounter(int newValue) {
        successfulSuitesCounter.set(newValue);
    }

    public int getSuccessfulSuitesCounter() {
        return successfulSuitesCounter.get();
    }

}
