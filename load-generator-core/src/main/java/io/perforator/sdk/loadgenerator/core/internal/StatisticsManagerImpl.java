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

final class StatisticsManagerImpl implements StatisticsManager {

    @Override
    public void onSuiteInstanceStarted(long timestamp, SuiteInstanceContextImpl context) {
        context.getLoadGeneratorContext().getStatisticsContext().incrementSuiteInstancesInProgress();
        context.getSuiteConfigContext().getStatisticsContext().incrementSuiteInstancesInProgress();
    }

    @Override
    public void onSuiteInstanceFinished(long timestamp, SuiteInstanceContextImpl context, Throwable error) {
        LoadGeneratorContextImpl loadGeneratorContext = context.getLoadGeneratorContext();
        SuiteConfigContextImpl suiteConfigContext = context.getSuiteConfigContext();

        loadGeneratorContext.getStatisticsContext().decrementSuiteInstancesInProgress();
        suiteConfigContext.getStatisticsContext().decrementSuiteInstancesInProgress();

        if (error == null) {
            loadGeneratorContext.getStatisticsContext().incrementSuiteInstancesSuccessful();
            suiteConfigContext.getStatisticsContext().incrementSuiteInstancesSuccessful();
        } else {
            loadGeneratorContext.getStatisticsContext().incrementSuiteInstancesFailed();
            suiteConfigContext.getStatisticsContext().incrementSuiteInstancesFailed();
        }
    }

    @Override
    public void onTransactionStarted(long timestamp, TransactionContextImpl context) {
        LoadGeneratorContextImpl loadGeneratorContext = context.getLoadGeneratorContext();
        SuiteConfigContextImpl suiteConfigContext = context.getSuiteContext().getSuiteConfigContext();

        loadGeneratorContext.getStatisticsContext().incrementTransactionsInProgress();
        suiteConfigContext.getStatisticsContext().incrementTransactionsInProgress();
        
        if (context.isNested()) {
            loadGeneratorContext.getStatisticsContext().incrementNestedTransactionsInProgress();
            suiteConfigContext.getStatisticsContext().incrementNestedTransactionsInProgress();
        } else {
            loadGeneratorContext.getStatisticsContext().incrementTopLevelTransactionsInProgress();
            suiteConfigContext.getStatisticsContext().incrementTopLevelTransactionsInProgress();
        }
    }

    @Override
    public void onTransactionFinished(long timestamp, TransactionContextImpl context, Throwable error) {
        LoadGeneratorContextImpl loadGeneratorContext = context.getLoadGeneratorContext();
        SuiteConfigContextImpl suiteConfigContext = context.getSuiteContext().getSuiteConfigContext();

        loadGeneratorContext.getStatisticsContext().decrementTransactionsInProgress();
        suiteConfigContext.getStatisticsContext().decrementTransactionsInProgress();

        if (error == null) {
            loadGeneratorContext.getStatisticsContext().incrementTransactionsSuccessful();
            suiteConfigContext.getStatisticsContext().incrementTransactionsSuccessful();
        } else {
            loadGeneratorContext.getStatisticsContext().incrementTransactionsFailed();
            suiteConfigContext.getStatisticsContext().incrementTransactionsFailed();
        }
        if (context.isNested()) {
            loadGeneratorContext.getStatisticsContext().decrementNestedTransactionsInProgress();
            suiteConfigContext.getStatisticsContext().decrementNestedTransactionsInProgress();
        } else {
            loadGeneratorContext.getStatisticsContext().decrementTopLevelTransactionsInProgress();
            suiteConfigContext.getStatisticsContext().decrementTopLevelTransactionsInProgress();
        }
    }

    @Override
    public void onRemoteWebDriverStarted(long timestamp, RemoteWebDriverContextImpl context) {
        context.getLoadGeneratorContext().getStatisticsContext().incrementSessionsInProgress();
        context.getSuiteInstanceContext().getSuiteConfigContext().getStatisticsContext().incrementSessionsInProgress();
    }

    @Override
    public void onRemoteWebDriverFinished(long timestamp, RemoteWebDriverContextImpl context, Throwable error) {
        context.getLoadGeneratorContext().getStatisticsContext().decrementSessionsInProgress();
        context.getSuiteInstanceContext().getSuiteConfigContext().getStatisticsContext().decrementSessionsInProgress();
    }
}
