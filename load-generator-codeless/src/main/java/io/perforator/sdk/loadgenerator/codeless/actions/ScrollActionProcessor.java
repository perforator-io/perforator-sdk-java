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

public class ScrollActionProcessor extends AbstractActionProcessor<ScrollActionConfig, ScrollActionInstance> {

    public ScrollActionProcessor() {
        super(ScrollActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public ScrollActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return ScrollActionConfig.builder()
                .cssSelector(
                        getRequiredValueOrNestedField(
                                ScrollActionConfig.Fields.cssSelector,
                                actionValue
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                ScrollActionConfig.Fields.timeout,
                                actionValue,
                                ScrollActionConfig.DEFAULT_TIMEOUT
                        )
                )
                .build();
    }

    @Override
    public ScrollActionInstance buildActionInstance(CodelessSuiteConfig suiteConfig, FormattingMap formatter, ScrollActionConfig actionConfig) {
        return ScrollActionInstance.builder()
                .config(
                        actionConfig
                )
                .cssSelector(
                        buildStringForActionInstance(
                                ScrollActionConfig.Fields.cssSelector,
                                actionConfig.getCssSelector(),
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                ScrollActionConfig.Fields.timeout,
                                actionConfig.getTimeout(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, ScrollActionInstance actionInstance) {
        WebElement element = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(actionInstance.getCssSelector())
                )
        );

        driver.executeScript("arguments[0].scrollIntoView();", element);
    }
}