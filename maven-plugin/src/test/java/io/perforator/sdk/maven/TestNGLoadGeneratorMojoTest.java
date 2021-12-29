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
package io.perforator.sdk.maven;

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.testng.TestNGSuiteConfig;
import org.junit.jupiter.api.Test;

public class TestNGLoadGeneratorMojoTest extends AbstractMojoTest<TestNGLoadGeneratorMojo> {

    private static final TestNGSuiteConfig DEFAULT_TESTNG_SUITE_CONFIG = new TestNGSuiteConfig(p -> null);

    public TestNGLoadGeneratorMojoTest() {
        super(TestNGLoadGeneratorMojo.class, TestNGLoadGeneratorMojo.MOJO_NAME);
    }

    @Test
    public void verifyTestNgSuiteConfigPropertoes() throws Exception {
        verifyDefaults(
                DEFAULT_TESTNG_SUITE_CONFIG,
                SuiteConfig.DEFAULTS_FIELD_PREFIX
        );

    }
}
