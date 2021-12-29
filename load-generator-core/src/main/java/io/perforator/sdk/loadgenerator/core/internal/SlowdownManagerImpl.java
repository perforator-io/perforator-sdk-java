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
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

final class SlowdownManagerImpl implements SlowdownManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlowdownManagerImpl.class);

    private final int statusesSize;
    private final ReentrantLock statusesLock;
    private final BitSet statuses;
    private int statusesSequence;

    public SlowdownManagerImpl(LoadGeneratorConfig loadGeneratorConfig, List<SuiteConfig> suiteConfigs) {
        this.statusesSize = calculateSlowdownThreshold(loadGeneratorConfig, suiteConfigs);
        this.statuses = new BitSet(statusesSize);
        this.statusesSequence = 0;
        this.statusesLock = new ReentrantLock();

        for (int i = 0; i < statusesSize; i++) {
            statuses.set(i);
        }
    }

    private static int calculateSlowdownThreshold(LoadGeneratorConfig loadGeneratorConfig, List<SuiteConfig> suiteConfigs) {
        int result = loadGeneratorConfig.getSlowdownTransactionsThreshold();

        if (result < 10) {
            result = 10;
            LOGGER.warn(
                    "{}.{} has been defaulted to {}",
                    LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX,
                    LoadGeneratorConfig.Fields.slowdownTransactionsThreshold,
                    result
            );
        }

        int overallConcurrency = suiteConfigs.stream()
                .filter(suiteConfig -> suiteConfig.getWebDriverMode().equals(WebDriverMode.cloud))
                .mapToInt(SuiteConfig::getConcurrency)
                .sum();

        int maxSlowDownTransactionsThreshold = 1000 + overallConcurrency;

        if (result > maxSlowDownTransactionsThreshold) {
            result = maxSlowDownTransactionsThreshold;
            LOGGER.warn(
                    "{}.{} has been defaulted to {}",
                    LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX,
                    LoadGeneratorConfig.Fields.slowdownTransactionsThreshold,
                    result
            );
        }

        return result;
    }

    @Override
    public void onTransactionFinished(long timestamp, TransactionContextImpl context, Throwable error) {
        if (context.isNested()) {
            statusesLock.lock();
            try {
                if (statusesSequence == Integer.MAX_VALUE) {
                    statusesSequence = 0;
                    statuses.set(0, error == null);
                } else {
                    statuses.set(++statusesSequence % statusesSize, error == null);
                }
            } finally {
                statusesLock.unlock();
            }
        }
    }

    @Override
    public long getSlowdownTimeout() {
        float failedPercent = 100 - (float) (statuses.cardinality() * 100) / statusesSize;

        if (failedPercent < 25) {
            return 0;
        } else if (failedPercent < 50) {
            return (long) (10 * failedPercent);
        } else if (failedPercent < 75) {
            return (long) (15 * failedPercent);
        } else {
            return (long) (20 * failedPercent);
        }
    }

}
