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
import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AwaitAlertToBePresentActionProcessor extends AbstractActionProcessor<AwaitAlertToBePresentActionConfig, AwaitAlertToBePresentActionInstance> {

    public AwaitAlertToBePresentActionProcessor() {
        super(AwaitAlertToBePresentActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public AwaitAlertToBePresentActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return AwaitAlertToBePresentActionConfig.builder()
                .timeout(
                        getRequiredValueOrNestedField(
                                AwaitAlertToBePresentActionConfig.Fields.timeout,
                                actionValue
                        )
                )
                .enabled(
                        getOptionalNestedField(
                                AwaitAlertToBePresentActionConfig.Fields.enabled,
                                actionValue,
                                "true"
                        )
                )
                .build();
    }

    @Override
    public AwaitAlertToBePresentActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, AwaitAlertToBePresentActionConfig actionConfig) {
        return AwaitAlertToBePresentActionInstance.builder()
                .config(
                        actionConfig
                )
                .timeout(
                        buildDurationForActionInstance(
                                AwaitAlertToBePresentActionInstance.Fields.timeout,
                                actionConfig.getTimeout(),
                                suiteConfig.getWebDriverFluentWaitTimeout(),
                                formatter
                        )
                )
                .enabled(
                        buildEnabledForActionInstance(
                                AwaitAlertToBePresentActionInstance.Fields.enabled, 
                                actionConfig.getEnabled(), 
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, AwaitAlertToBePresentActionInstance actionInstance) {
        new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.alertIsPresent()
        );
    }
}