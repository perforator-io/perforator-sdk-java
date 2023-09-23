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
package io.perforator.sdk.loadgenerator.codeless.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.service.AutoService;
import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import org.openqa.selenium.remote.RemoteWebDriver;

@SuppressWarnings("rawtypes")
@AutoService(ActionProcessor.class)
public class IgnoreRemainingStepsActionProcessor extends AbstractActionProcessor<IgnoreRemainingStepsActionConfig, IgnoreRemainingStepsActionInstance> {

    public IgnoreRemainingStepsActionProcessor() {
        super(IgnoreRemainingStepsActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public IgnoreRemainingStepsActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return IgnoreRemainingStepsActionConfig.builder()
                .enabled(
                        getRequiredValueOrNestedField(
                                IgnoreRemainingStepsActionConfig.Fields.enabled,
                                actionValue
                        )
                )
                .build();
    }

    @Override
    public IgnoreRemainingStepsActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, IgnoreRemainingStepsActionConfig actionConfig) {
        return IgnoreRemainingStepsActionInstance.builder()
                .config(
                        actionConfig
                )
                .enabled(
                        buildEnabledForActionInstance(
                                IgnoreRemainingStepsActionConfig.Fields.enabled,
                                actionConfig.getEnabled(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, IgnoreRemainingStepsActionInstance actionInstance) {
        //DO nothing
    }
}
