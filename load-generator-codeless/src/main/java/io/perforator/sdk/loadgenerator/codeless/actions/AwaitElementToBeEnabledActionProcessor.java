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
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AwaitElementToBeEnabledActionProcessor extends AbstractActionProcessor<AwaitElementToBeEnabledActionConfig, AwaitElementToBeEnabledActionInstance> {

    public AwaitElementToBeEnabledActionProcessor() {
        super(AwaitElementToBeEnabledActionConfig.DEFAULT_ACTION_NAME);
    }

    private static ExpectedCondition<WebElement> elementToBeEnabled(final By locator) {
        return new ExpectedCondition<>() {
            @Override
            public WebElement apply(WebDriver driver) {
                WebElement element = ExpectedConditions.visibilityOfElementLocated(locator).apply(driver);
                try {
                    if (element != null && element.isEnabled()) {
                        return element;
                    }
                    return null;
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "element to be enabled: " + locator;
            }
        };
    }

    @Override
    public AwaitElementToBeEnabledActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return AwaitElementToBeEnabledActionConfig.builder()
                .cssSelector(
                        getRequiredValueOrNestedField(
                                AwaitElementToBeEnabledActionConfig.Fields.cssSelector,
                                actionValue
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                AwaitElementToBeEnabledActionConfig.Fields.timeout,
                                actionValue,
                                AwaitElementToBeEnabledActionConfig.DEFAULT_TIMEOUT
                        )
                )
                .build();
    }

    @Override
    public AwaitElementToBeEnabledActionInstance buildActionInstance(CodelessSuiteConfig suiteConfig, FormattingMap formatter, AwaitElementToBeEnabledActionConfig actionConfig) {
        return AwaitElementToBeEnabledActionInstance.builder()
                .config(
                        actionConfig
                )
                .cssSelector(
                        buildStringForActionInstance(
                                AwaitElementToBeEnabledActionInstance.Fields.cssSelector,
                                actionConfig.getCssSelector(),
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                AwaitElementToBeEnabledActionInstance.Fields.timeout,
                                actionConfig.getTimeout(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, AwaitElementToBeEnabledActionInstance actionInstance) {
        new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                elementToBeEnabled(By.cssSelector(actionInstance.getCssSelector()))
        );
    }
}