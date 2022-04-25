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
        LoadGeneratorContextImpl loadGeneratorContext = context.getLoadGeneratorContext();
        loadGeneratorContext.getStatisticsContext().incrementSuiteInstancesInProgress();
        loadGeneratorContext.getSuiteStatisticsContext(context.getSuiteConfig().getName()).incrementSuiteInstancesInProgress();
    }

    @Override
    public void onSuiteInstanceFinished(long timestamp, SuiteContextImpl context, Throwable error) {
        LoadGeneratorContextImpl loadGeneratorContext = context.getLoadGeneratorContext();
        String suiteName = context.getSuiteConfig().getName();

        loadGeneratorContext.getStatisticsContext().decrementSuiteInstancesInProgress();
        loadGeneratorContext.getSuiteStatisticsContext(suiteName).decrementSuiteInstancesInProgress();

        if (error == null) {
            loadGeneratorContext.getStatisticsContext().incrementSuiteInstancesSuccessful();
            loadGeneratorContext.getSuiteStatisticsContext(suiteName).incrementSuiteInstancesSuccessful();
        } else {
            loadGeneratorContext.getStatisticsContext().incrementSuiteInstancesFailed();
            loadGeneratorContext.getSuiteStatisticsContext(suiteName).incrementSuiteInstancesFailed();
        }
    }

    @Override
    public void onTransactionStarted(long timestamp, TransactionContextImpl context) {
        LoadGeneratorContextImpl loadGeneratorContext = context.getLoadGeneratorContext();
        String suiteName = context.getSuiteContext().getSuiteConfig().getName();

        loadGeneratorContext.getStatisticsContext().incrementTransactionsInProgress();
        loadGeneratorContext.getSuiteStatisticsContext(suiteName).incrementTransactionsInProgress();
        
        if (context.isNested()) {
            loadGeneratorContext.getStatisticsContext().incrementNestedTransactionsInProgress();
            loadGeneratorContext.getSuiteStatisticsContext(suiteName).incrementNestedTransactionsInProgress();
        } else {
            loadGeneratorContext.getStatisticsContext().incrementTopLevelTransactionsInProgress();
            loadGeneratorContext.getSuiteStatisticsContext(suiteName).incrementTopLevelTransactionsInProgress();
        }
    }

    @Override
    public void onTransactionFinished(long timestamp, TransactionContextImpl context, Throwable error) {
        LoadGeneratorContextImpl loadGeneratorContext = context.getLoadGeneratorContext();
        String suiteName = context.getSuiteContext().getSuiteConfig().getName();

        loadGeneratorContext.getStatisticsContext().decrementTransactionsInProgress();
        loadGeneratorContext.getSuiteStatisticsContext(suiteName).decrementTransactionsInProgress();

        if (error == null) {
            loadGeneratorContext.getStatisticsContext().incrementTransactionsSuccessful();
            loadGeneratorContext.getSuiteStatisticsContext(suiteName).incrementTransactionsSuccessful();
        } else {
            loadGeneratorContext.getStatisticsContext().incrementTransactionsFailed();
            loadGeneratorContext.getSuiteStatisticsContext(suiteName).incrementTransactionsFailed();
        }
        if (context.isNested()) {
            loadGeneratorContext.getStatisticsContext().decrementNestedTransactionsInProgress();
            loadGeneratorContext.getSuiteStatisticsContext(suiteName).decrementNestedTransactionsInProgress();
        } else {
            loadGeneratorContext.getStatisticsContext().decrementTopLevelTransactionsInProgress();
            loadGeneratorContext.getSuiteStatisticsContext(suiteName).decrementTopLevelTransactionsInProgress();
        }
    }

    @Override
    public void onRemoteWebDriverStarted(long timestamp, RemoteWebDriverContextImpl context) {
        LoadGeneratorContextImpl loadGeneratorContext = context.getLoadGeneratorContext();
        String suiteName = context.getSuiteContext().getSuiteConfig().getName();

        loadGeneratorContext.getStatisticsContext().incrementSessionsInProgress();
        loadGeneratorContext.getSuiteStatisticsContext(suiteName).incrementSessionsInProgress();
    }

    @Override
    public void onRemoteWebDriverFinished(long timestamp, RemoteWebDriverContextImpl context, Throwable error) {
        LoadGeneratorContextImpl loadGeneratorContext = context.getLoadGeneratorContext();
        String suiteName = context.getSuiteContext().getSuiteConfig().getName();

        loadGeneratorContext.getStatisticsContext().decrementSessionsInProgress();
        loadGeneratorContext.getSuiteStatisticsContext(suiteName).decrementSessionsInProgress();
    }
}
