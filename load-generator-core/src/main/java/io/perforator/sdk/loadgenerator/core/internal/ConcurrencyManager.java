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

interface ConcurrencyManager extends IntegrationListener {

    int getMaxConcurrency(SuiteConfig suiteConfig);

    int getMinConcurrency(SuiteConfig suiteConfig);

    int getDesiredConcurrency(SuiteConfig suiteConfig);

    int getCurrentConcurrency(SuiteConfig suiteConfig);

}
