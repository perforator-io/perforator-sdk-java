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

import io.perforator.sdk.loadgenerator.core.context.SuiteConfigContext;

interface ConcurrencyManager<C extends SuiteConfigContext> extends IntegrationListener {

    int getMaxConcurrency(C suiteConfigContext);

    int getMinConcurrency(C suiteConfigContext);

    int getDesiredConcurrency(C suiteConfigContext);

    int getCurrentConcurrency(C suiteConfigContext);
    
    long getIterationsCounter(C suiteConfigContext);
    
    long getIterationsMax(C suiteConfigContext);

}
