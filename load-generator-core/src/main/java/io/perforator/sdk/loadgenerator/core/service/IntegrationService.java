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
import io.perforator.sdk.loadgenerator.core.context.SuiteConfigContext;
import io.perforator.sdk.loadgenerator.core.context.SuiteInstanceContext;
import io.perforator.sdk.loadgenerator.core.context.TransactionContext;

//TODO: add javadoc
public interface IntegrationService
        <C extends SuiteConfigContext,S extends SuiteInstanceContext, T extends TransactionContext, R extends RemoteWebDriverContext>
        extends TransactionsService<S, T>, RemoteWebDriverService<S, R>, StatisticsService {

    void onLoadGeneratorStarted();

    void onLoadGeneratorFinished(Throwable loadGeneratorError);

    S onSuiteInstanceStarted(int workerID, C suiteConfigContext);

    long onSuiteInstanceFinished(S suiteContext, Throwable suiteError);
    
    int getCurrentConcurrency(C suiteConfigContext);
    
    int getDesiredConcurrency(C suiteConfigContext);

    C onSuiteConfigCreated(SuiteConfig suiteConfig);
}
