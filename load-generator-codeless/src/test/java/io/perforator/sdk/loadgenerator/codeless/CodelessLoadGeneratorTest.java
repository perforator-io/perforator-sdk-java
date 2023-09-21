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

import io.perforator.sdk.loadgenerator.codeless.actions.AwaitElementToBeVisibleActionConfig;
import io.perforator.sdk.loadgenerator.codeless.actions.OpenActionConfig;
import io.perforator.sdk.loadgenerator.codeless.actions.SleepActionConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessStepConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import io.perforator.sdk.loadgenerator.core.AbstractLoadGeneratorTest;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CodelessLoadGeneratorTest extends AbstractLoadGeneratorTest<CodelessLoadGenerator, CodelessLoadGeneratorConfig, CodelessSuiteConfig> {

    public CodelessLoadGeneratorTest() {
        super(CodelessLoadGenerator.class, CodelessLoadGeneratorConfig.class, CodelessSuiteConfig.class);
    }

    @Override
    protected CodelessSuiteConfig.CodelessSuiteConfigBuilder defaultSuiteConfigBuilder() throws Exception {
        CodelessStepConfig stepConfig = CodelessStepConfig.builder()
                .name("Step One")
                .actions(List.of(
                        OpenActionConfig.builder()
                                .url(verificationsBaseUrl)
                                .build(),
                        AwaitElementToBeVisibleActionConfig.builder()
                                .cssSelector("#async-content")
                                .build(),
                        OpenActionConfig.builder()
                                .url(verificationsBaseUrl + "/satisne")
                                .build(),
                        AwaitElementToBeVisibleActionConfig.builder()
                                .cssSelector("#async-content")
                                .build(),
                        SleepActionConfig.builder()
                                .timeout("1s-2s")
                                .build()
                ))
                .build();
        
        return CodelessSuiteConfig.builder()
                .applyDefaults()
                .name("Testing Suite")
                .step(stepConfig);
    }

    @Test
    public void shouldFailOnInvalidDirectConfig() throws Exception {
        CodelessStepConfig stepConfig = CodelessStepConfig.builder()
                .name("Step One")
                .action(
                        OpenActionConfig.builder()
                                .timeout("invalid_duration")
                                .build()
                )
                .build();
        CodelessLoadGeneratorConfig loadGeneratorConfig = defaultLoadGeneratorConfigBuilder().build();
        CodelessSuiteConfig suiteConfig = defaultSuiteConfigBuilder()
                .step(stepConfig)
                .build();

        assertThrows(
                RuntimeException.class,
                () -> new CodelessLoadGenerator(loadGeneratorConfig, List.of(suiteConfig)),
                "Instantiating new "
                        + CodelessLoadGenerator.class.getName()
                        + " with invalid config should throw exception"
        );
    }
}
