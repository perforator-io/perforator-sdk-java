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
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AwaitElementToBeDisabledActionProcessor extends AbstractSelectorActionProcessor<AwaitElementToBeDisabledActionConfig, AwaitElementToBeDisabledActionInstance> {

    public AwaitElementToBeDisabledActionProcessor() {
        super(AwaitElementToBeDisabledActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public AwaitElementToBeDisabledActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return AwaitElementToBeDisabledActionConfig.builder()
                .selector(getOptionalValue(
                        actionValue,
                        null)
                )
                .cssSelector(
                        getOptionalNestedField(
                                AwaitElementToBeDisabledActionConfig.Fields.cssSelector,
                                actionValue,
                                null
                        )
                )
                .xpathSelector(
                        getOptionalNestedField(
                                AwaitElementToBeDisabledActionConfig.Fields.xpathSelector,
                                actionValue,
                                null
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                AwaitElementToBeDisabledActionConfig.Fields.timeout,
                                actionValue,
                                AwaitElementToBeDisabledActionConfig.DEFAULT_TIMEOUT
                        )
                )
                .build();
    }

    @Override
    public AwaitElementToBeDisabledActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, AwaitElementToBeDisabledActionConfig actionConfig) {
        return AwaitElementToBeDisabledActionInstance.builder()
                .config(
                        actionConfig
                )
                .selectorType(
                        getSelectorType(actionConfig, loadGeneratorConfig.getDefaultSelectorType())
                )
                .selector(
                        buildRequiredStringSelectorForActionInstance(
                                actionConfig,
                                AwaitElementToBeDisabledActionInstance.Fields.selector,
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                AwaitElementToBeDisabledActionInstance.Fields.timeout,
                                actionConfig.getTimeout(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, AwaitElementToBeDisabledActionInstance actionInstance) {
        new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                elementToBeDisabled(
                        getActionInstanceLocator(actionInstance)
                )
        );
    }

    private ExpectedCondition<WebElement> elementToBeDisabled(final By locator) {
        return new ExpectedCondition<>() {
            @Override
            public WebElement apply(WebDriver driver) {
                WebElement element = ExpectedConditions.visibilityOfElementLocated(locator).apply(driver);
                try {
                    if (element != null && !element.isEnabled()) {
                        return element;
                    }
                    return null;
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "element to be disabled: " + locator;
            }
        };
    }
}