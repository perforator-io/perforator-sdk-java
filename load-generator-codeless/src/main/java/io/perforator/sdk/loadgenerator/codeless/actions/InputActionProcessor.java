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
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InputActionProcessor extends AbstractActionProcessor<InputActionConfig, InputActionInstance> {

    public InputActionProcessor() {
        super(InputActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public InputActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return InputActionConfig.builder()
                .value(
                        getRequiredNestedField(
                                InputActionConfig.Fields.value,
                                actionValue
                        )
                )
                .cssSelector(
                        getRequiredNestedField(
                                InputActionConfig.Fields.cssSelector,
                                actionValue
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                InputActionConfig.Fields.timeout,
                                actionValue,
                                InputActionConfig.DEFAULT_TIMEOUT
                        )
                )
                .build();
    }

    @Override
    public InputActionInstance buildActionInstance(CodelessSuiteConfig suiteConfig, FormattingMap formatter, InputActionConfig actionConfig) {
        return InputActionInstance.builder()
                .config(
                        actionConfig
                )
                .value(
                        buildStringForActionInstance(
                                InputActionConfig.Fields.value,
                                actionConfig.getValue(),
                                formatter
                        )
                )
                .cssSelector(
                        buildStringForActionInstance(
                                InputActionConfig.Fields.cssSelector,
                                actionConfig.getCssSelector(),
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                InputActionConfig.Fields.timeout,
                                actionConfig.getTimeout(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, InputActionInstance actionInstance) {
        WebElement element = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(actionInstance.getCssSelector())
                )
        );

        element.clear();
        element.sendKeys(actionInstance.getValue());
    }
}