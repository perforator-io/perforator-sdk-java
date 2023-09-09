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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InputActionProcessor extends AbstractSelectorActionProcessor<InputActionConfig, InputActionInstance> {

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
                .selector(
                        getOptionalValue(
                                actionValue,
                                null
                        )
                )
                .cssSelector(
                        getOptionalNestedField(
                                InputActionConfig.Fields.cssSelector,
                                actionValue,
                                null
                        )
                )
                .xpathSelector(
                        getOptionalNestedField(
                                InputActionConfig.Fields.xpathSelector,
                                actionValue,
                                null
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                InputActionConfig.Fields.timeout,
                                actionValue,
                                null
                        )
                )
                .enabled(
                        getOptionalNestedField(
                                InputActionConfig.Fields.enabled,
                                actionValue,
                                "true"
                        )
                )
                .build();
    }

    @Override
    public InputActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, InputActionConfig actionConfig) {
        return InputActionInstance.builder()
                .config(
                        actionConfig
                )
                .value(
                        buildStringForActionInstance(
                                InputActionInstance.Fields.value,
                                actionConfig.getValue(),
                                formatter
                        )
                )
                .selectorType(
                        getSelectorType(
                                actionConfig, 
                                suiteConfig.getDefaultSelectorType()
                        )
                )
                .selector(
                        buildRequiredStringSelectorForActionInstance(
                                actionConfig,
                                InputActionInstance.Fields.selector,
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                InputActionInstance.Fields.timeout,
                                actionConfig.getTimeout(),
                                suiteConfig.getWebDriverFluentWaitTimeout(),
                                formatter
                        )
                )
                .enabled(
                        buildEnabledForActionInstance(
                                InputActionInstance.Fields.enabled, 
                                actionConfig.getEnabled(), 
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
                        getActionInstanceLocator(actionInstance)
                )
        );

        element.clear();
        element.sendKeys(actionInstance.getValue());
    }
}