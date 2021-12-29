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
package io.perforator.sdk.loadgenerator.codeless;

import io.perforator.sdk.loadgenerator.codeless.actions.OpenActionConfig;
import io.perforator.sdk.loadgenerator.codeless.actions.SleepActionConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessStepConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import io.perforator.sdk.loadgenerator.core.AbstractLoadGeneratorTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CodelessLoadGeneratorTest extends AbstractLoadGeneratorTest<CodelessLoadGenerator, CodelessLoadGeneratorConfig, CodelessSuiteConfig> {

    public CodelessLoadGeneratorTest() {
        super(CodelessLoadGenerator.class, CodelessLoadGeneratorConfig.class, CodelessSuiteConfig.class);
    }

    @Override
    protected CodelessSuiteConfig buildDefaultSuiteConfig() throws Exception {
        CodelessSuiteConfig result = super.buildDefaultSuiteConfig();
        result.setName("Testing Suite");
        
        CodelessStepConfig stepConfig = new CodelessStepConfig();
        stepConfig.setName("Step One");
        stepConfig.setActions(List.of(
                OpenActionConfig.builder()
                        .timeout(OpenActionConfig.DEFAULT_TIMEOUT)
                        .url(verificationsBaseUrl)
                        .build(),
                OpenActionConfig.builder()
                        .timeout(OpenActionConfig.DEFAULT_TIMEOUT)
                        .url(verificationsBaseUrl + "/satisne")
                        .build(),
                SleepActionConfig.builder()
                        .timeout("1s-2s")
                        .build()
        ));

        result.setSteps(List.of(stepConfig));

        return result;
    }

    @Test
    public void shouldFailOnInvalidDirectConfig() throws Exception {
        CodelessLoadGeneratorConfig loadGeneratorConfig = buildDefaultLoadGeneratorConfig();
        CodelessSuiteConfig suiteConfig = buildDefaultSuiteConfig();

        CodelessStepConfig stepConfig = new CodelessStepConfig();
        stepConfig.setName("Step One");
        stepConfig.setActions(List.of(
                OpenActionConfig.builder()
                        //.duration field is required
                        .timeout(verificationsBaseUrl)
                        .build()
        ));

        suiteConfig.setSteps(List.of(stepConfig));

        assertThrows(
                RuntimeException.class,
                () -> new CodelessLoadGenerator(loadGeneratorConfig, List.of(suiteConfig)),
                "Instantiating new "
                        + CodelessLoadGenerator.class.getName()
                        + " with invalid config should throw exception"
        );
    }
}
