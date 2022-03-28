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
package io.perforator.sdk.loadgenerator.codeless.config;

import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionConfig;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionProcessor;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionProcessorsRegistry;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;

import java.util.List;

public final class CodelessSuiteConfigValidator {

    private CodelessSuiteConfigValidator() {
    }

    public static List<CodelessSuiteConfig> validate(CodelessLoadGeneratorConfig loadGeneratorConfig, List<CodelessSuiteConfig> suiteConfigs) {
        if (suiteConfigs == null || suiteConfigs.isEmpty()) {
            throw new RuntimeException("suites are required");
        }

        for (CodelessSuiteConfig suiteConfig : suiteConfigs) {
            validate(loadGeneratorConfig, suiteConfig);
        }

        return suiteConfigs;
    }

    public static CodelessSuiteConfig validate(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig) {
        String suiteName = suiteConfig.getName();

        if (suiteName == null || suiteName.isBlank()) {
            throw new RuntimeException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.name
                            + " is required"
            );
        }

        validaSuiteSteps(loadGeneratorConfig, suiteConfig);

        return suiteConfig;
    }

    private static void validaSuiteSteps(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig) {
        if (suiteConfig.getSteps() == null || suiteConfig.getSteps().isEmpty()) {
            throw new RuntimeException(
                    "Suite with the name '"
                            + suiteConfig.getName()
                            + "' should have steps defined"
            );
        }

        List<FormattingMap> formatters;
        if (suiteConfig.getProps() == null || suiteConfig.getProps().isEmpty()) {
            formatters = List.of(FormattingMap.EMPTY);
        } else {
            formatters = suiteConfig.getProps();
        }

        for (CodelessStepConfig step : suiteConfig.getSteps()) {
            String stepName = step.getName();

            if (stepName == null || stepName.isBlank()) {
                throw new RuntimeException(
                        "Suite '"
                                + suiteConfig.getName()
                                + "' has a step with an empty name, but it is a required field"
                );
            }

            for (FormattingMap formatter : formatters) {
                String formattedStepName = formatter.format(stepName);
                if (formattedStepName == null || formattedStepName.isBlank()) {
                    throw new RuntimeException(
                            "Suite '"
                                    + suiteConfig.getName()
                                    + "' has a step with the name '"
                                    + stepName
                                    + "' and it is resolved to empty string, but such field is required"
                    );
                }
            }

            validateStepActions(loadGeneratorConfig, suiteConfig, step);
        }
    }

    private static void validateStepActions(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, CodelessStepConfig stepConfig) {
        if (stepConfig.getActions() == null || stepConfig.getActions().isEmpty()) {
            throw new RuntimeException(
                    "Suite '"
                            + suiteConfig.getName()
                            + "' has a step '"
                            + stepConfig.getName()
                            + "' with an empty actions, but actions are required"
            );
        }

        for (ActionConfig action : stepConfig.getActions()) {
            ActionProcessor actionProcessor = ActionProcessorsRegistry.INSTANCE.getActionProcessorByName(
                    action.getActionName()
            );

            if (actionProcessor == null) {
                throw new RuntimeException(
                        "Action '" + action.getActionName() + "' is not supported"
                );
            }

            actionProcessor.validateActionConfig(
                    loadGeneratorConfig,
                    suiteConfig,
                    action
            );
        }
    }

}
