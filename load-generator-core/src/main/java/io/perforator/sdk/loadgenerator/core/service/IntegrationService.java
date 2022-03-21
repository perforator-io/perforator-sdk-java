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
package io.perforator.sdk.loadgenerator.core.service;

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.context.RemoteWebDriverContext;
import io.perforator.sdk.loadgenerator.core.context.SuiteContext;
import io.perforator.sdk.loadgenerator.core.context.TransactionContext;

//TODO: add javadoc
public interface IntegrationService
        <S extends SuiteContext, T extends TransactionContext, R extends RemoteWebDriverContext>
        extends TransactionsService<S, T>, RemoteWebDriverService<S, R>, StatisticsService {

    void onLoadGeneratorStarted();

    void onLoadGeneratorFinished(Throwable loadGeneratorError);

    S onSuiteInstanceStarted(int workerID, SuiteConfig suiteConfig);

    long onSuiteInstanceFinished(S suiteContext, Throwable suiteError);
    
    int getCurrentConcurrency(SuiteConfig suiteConfig);
    
    int getDesiredConcurrency(SuiteConfig suiteConfig);

}
