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

public class FocusActionProcessor extends AbstractActionProcessor<FocusActionConfig, FocusActionInstance> {

    public FocusActionProcessor() {
        super(FocusActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public FocusActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return FocusActionConfig.builder()
                .cssSelector(
                        getRequiredValueOrNestedField(
                                FocusActionConfig.Fields.cssSelector,
                                actionValue
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                FocusActionConfig.Fields.timeout,
                                actionValue,
                                FocusActionConfig.DEFAULT_TIMEOUT
                        )
                )
                .build();
    }

    @Override
    public FocusActionInstance buildActionInstance(CodelessSuiteConfig suiteConfig, FormattingMap formatter, FocusActionConfig actionConfig) {
        return FocusActionInstance.builder()
                .config(
                        actionConfig
                )
                .cssSelector(
                        buildStringForActionInstance(
                                FocusActionConfig.Fields.cssSelector,
                                actionConfig.getCssSelector(),
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                FocusActionConfig.Fields.timeout,
                                actionConfig.getTimeout(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, FocusActionInstance actionInstance) {
        WebElement element = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(actionInstance.getCssSelector())
                )
        );

        driver.executeScript("arguments[0].focus();", element);

    }
}