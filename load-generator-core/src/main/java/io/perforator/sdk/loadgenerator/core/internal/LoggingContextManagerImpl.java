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

import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicBoolean;

final class LoggingContextManagerImpl implements LoggingContextManager {

    private static final String MDC_PERFORATOR_CONTEXT = "X-PERFORATOR-CONTEXT";

    @Override
    public final void onSuiteInstanceStarted(long timestamp, SuiteInstanceContextImpl context) {
        if (context.isRebuildLoggingContext()) {
            MDC.put(MDC_PERFORATOR_CONTEXT, rebuildLoggingContext(context));
        }
    }

    @Override
    public final void onSuiteInstanceFinished(long timestamp, SuiteInstanceContextImpl context, Throwable error) {
        MDC.clear();
    }

    @Override
    public final void onTransactionStarted(long timestamp, TransactionContextImpl context) {
        if (context.getSuiteContext().isRebuildLoggingContext()) {
            MDC.put(MDC_PERFORATOR_CONTEXT, rebuildLoggingContext(context.getSuiteContext()));
        }
    }

    @Override
    public final void onTransactionFinished(long timestamp, TransactionContextImpl context, Throwable error) {
        if (context.getSuiteContext().isRebuildLoggingContext()) {
            MDC.put(MDC_PERFORATOR_CONTEXT, rebuildLoggingContext(context.getSuiteContext()));
        }
    }

    @Override
    public final void onRemoteWebDriverStarted(long timestamp, RemoteWebDriverContextImpl context) {
        if (context.getSuiteInstanceContext().isRebuildLoggingContext()) {
            MDC.put(MDC_PERFORATOR_CONTEXT, rebuildLoggingContext(context.getSuiteInstanceContext()));
        }
    }

    @Override
    public final void onRemoteWebDriverFinished(long timestamp, RemoteWebDriverContextImpl context, Throwable error) {
        if (context.getSuiteInstanceContext().isRebuildLoggingContext()) {
            MDC.put(MDC_PERFORATOR_CONTEXT, rebuildLoggingContext(context.getSuiteInstanceContext()));
        }
    }

    private String rebuildLoggingContext(SuiteInstanceContextImpl context) {
        StringBuilder result = new StringBuilder();

        if (context.isLogWorkerID()) {
            result.append("worker:").append(context.getWorkerID());
        }

        if (context.isLogSuiteInstanceID()) {
            if (result.length() > 0) {
                result.append("; ");
            }
            result.append("suite:").append(context.getSuiteInstanceID());
        }

        if (context.isLogTransactionID()) {
            if (result.length() > 0) {
                result.append("; ");
            }

            if (!context.getTransactions().isEmpty()) {
                result.append("transaction:");

                AtomicBoolean appended = new AtomicBoolean(false);
                context.getTransactions().descendingIterator().forEachRemaining(transaction -> {
                    if (appended.get()) {
                        result.append(", ").append(transaction.getTransactionID());
                    } else {
                        result.append(transaction.getTransactionID());
                        appended.set(true);
                    }
                });
            }
        }

        if (context.isLogRemoteWebDriverSessionID()) {
            if (result.length() > 0) {
                result.append("; ");
            }

            if (!context.getDrivers().isEmpty()) {
                result.append("session:");

                AtomicBoolean appended = new AtomicBoolean(false);
                context.getDrivers().forEach((sessionID, remoteWebDriver) -> {
                    if (appended.get()) {
                        result.append(", ").append(sessionID);
                    } else {
                        result.append(sessionID);
                        appended.set(true);
                    }
                });
            }
        }

        return result.toString();
    }

}
