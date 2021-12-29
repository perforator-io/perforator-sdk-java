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

class StatisticsManagerImpl implements StatisticsManager {
    
    @Override
    public void onSuiteInstanceStarted(long timestamp, SuiteContextImpl context) {
        context.getLoadGeneratorContext().getStatisticsContext().incrementSuiteInstancesInProgress();
    }

    @Override
    public void onSuiteInstanceFinished(long timestamp, SuiteContextImpl context, Throwable error) {
        context.getLoadGeneratorContext().getStatisticsContext().decrementSuiteInstancesInProgress();

        if (error == null) {
            context.getLoadGeneratorContext().getStatisticsContext().incrementSuiteInstancesSuccessful();
        } else {
            context.getLoadGeneratorContext().getStatisticsContext().incrementSuiteInstancesFailed();
        }
    }

    @Override
    public void onTransactionStarted(long timestamp, TransactionContextImpl context) {
        context.getLoadGeneratorContext().getStatisticsContext().incrementTransactionsInProgress();
    }

    @Override
    public void onTransactionFinished(long timestamp, TransactionContextImpl context, Throwable error) {
        context.getLoadGeneratorContext().getStatisticsContext().decrementTransactionsInProgress();

        if (error == null) {
            context.getLoadGeneratorContext().getStatisticsContext().incrementTransactionsSuccessful();
        } else {
            context.getLoadGeneratorContext().getStatisticsContext().incrementTransactionsFailed();
        }
    }
    
}
