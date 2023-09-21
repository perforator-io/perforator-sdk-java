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
package io.perforator.sdk.loadgenerator.embedded;

import io.perforator.sdk.loadgenerator.core.AbstractLoadGeneratorTest;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;

public class EmbeddedLoadGeneratorTest extends AbstractLoadGeneratorTest<EmbeddedLoadGenerator, LoadGeneratorConfig, EmbeddedSuiteConfig> {

    public EmbeddedLoadGeneratorTest() {
        super(EmbeddedLoadGenerator.class, LoadGeneratorConfig.class, EmbeddedSuiteConfig.class);
    }

    @Override
    protected EmbeddedSuiteConfig.EmbeddedSuiteConfigBuilder defaultSuiteConfigBuilder() throws Exception {
        return EmbeddedSuiteConfig.builder()
                .processorClass(EmbeddedSuiteProcessorMock.class.getName())
                .processorSingleton(true)
                .applyDefaults();
    }
    
}
