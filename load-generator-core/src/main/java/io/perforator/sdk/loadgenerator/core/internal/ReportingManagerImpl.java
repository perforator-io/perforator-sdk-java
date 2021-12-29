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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

final class ReportingManagerImpl implements ReportingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingManagerImpl.class);

    private boolean reportingEnabled = false;
    private long reportingInterval;
    private long lastReportingTimestamp;

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        lastReportingTimestamp = timestamp;

        Duration reportingDuration = loadGeneratorContext.getLoadGeneratorConfig().getReportingInterval();
        if (reportingDuration == null || reportingDuration.toMillis() <= 0) {
            reportingEnabled = false;
        } else {
            reportingEnabled = true;

            if (reportingDuration.toMillis() < 1000) {
                LOGGER.warn(
                        "{}.{} is too small - defaulting it to 1s",
                        LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX,
                        LoadGeneratorConfig.Fields.reportingInterval
                );
                reportingInterval = 1000;
            } else {
                reportingInterval = reportingDuration.toMillis();
            }
        }
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        reportingEnabled = false;
        logStatistics(loadGeneratorContext.getStatisticsContext());
    }

    @Override
    public void onHeartbeat(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        if (!reportingEnabled) {
            return;
        }

        if (lastReportingTimestamp + reportingInterval > timestamp) {
            return;
        }

        lastReportingTimestamp = timestamp;

        logStatistics(loadGeneratorContext.getStatisticsContext());
    }

    private static void logStatistics(StatisticsContextImpl statisticsContext) {
        LOGGER.info(
                "suites: active = {}, failed = {}, successful = {}; transactions: active = {}, failed = {}, successful = {}",
                statisticsContext.getSuiteInstancesInProgress(),
                statisticsContext.getSuiteInstancesFailed(),
                statisticsContext.getSuiteInstancesSuccessful(),
                statisticsContext.getTransactionsInProgress(),
                statisticsContext.getTransactionsFailed(),
                statisticsContext.getTransactionsSuccessful()
        );
    }

}
