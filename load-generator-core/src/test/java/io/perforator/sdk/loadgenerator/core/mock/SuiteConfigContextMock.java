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
package io.perforator.sdk.loadgenerator.core.mock;

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.context.SuiteConfigContext;

public class SuiteConfigContextMock implements SuiteConfigContext {

    private SuiteConfig suiteConfig;

    public SuiteConfigContextMock(SuiteConfig suiteConfig) {
        this.suiteConfig = suiteConfig;
    }

    @Override
    public SuiteConfig getSuiteConfig() {
        return suiteConfig;
    }
}