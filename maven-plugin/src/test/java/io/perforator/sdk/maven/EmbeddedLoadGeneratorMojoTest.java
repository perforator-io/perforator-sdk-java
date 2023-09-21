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
import io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig;
import org.junit.jupiter.api.Test;

public class EmbeddedLoadGeneratorMojoTest extends AbstractMojoTest<EmbeddedLoadGeneratorMojo> {

    private static final EmbeddedSuiteConfig DEFAULT_EMBEDDED_SUITE_CONFIG = EmbeddedSuiteConfig.builder().build();

    public EmbeddedLoadGeneratorMojoTest() {
        super(EmbeddedLoadGeneratorMojo.class, EmbeddedLoadGeneratorMojo.MOJO_NAME);
    }

    @Test
    public void verifyEmbeddedSuiteConfigPropertoes() throws Exception {
        verifyDefaults(
                DEFAULT_EMBEDDED_SUITE_CONFIG,
                SuiteConfig.DEFAULTS_FIELD_PREFIX
        );
    }

}
