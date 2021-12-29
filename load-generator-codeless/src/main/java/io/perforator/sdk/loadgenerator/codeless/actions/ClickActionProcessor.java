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

public class ClickActionProcessor extends AbstractActionProcessor<ClickActionConfig, ClickActionInstance> {

    public ClickActionProcessor() {
        super(ClickActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public ClickActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return ClickActionConfig.builder()
                .cssSelector(
                        getRequiredValueOrNestedField(
                                ClickActionConfig.Fields.cssSelector,
                                actionValue
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                ClickActionConfig.Fields.timeout,
                                actionValue,
                                ClickActionConfig.DEFAULT_TIMEOUT
                        )
                )
                .build();
    }

    @Override
    public ClickActionInstance buildActionInstance(CodelessSuiteConfig suiteConfig, FormattingMap formatter, ClickActionConfig actionConfig) {
        return ClickActionInstance.builder()
                .config(
                        actionConfig
                )
                .cssSelector(
                        buildStringForActionInstance(
                                ClickActionConfig.Fields.cssSelector,
                                actionConfig.getCssSelector(),
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                ClickActionConfig.Fields.timeout,
                                actionConfig.getTimeout(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, ClickActionInstance actionInstance) {
        WebElement element = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(actionInstance.getCssSelector())
                )
        );

        element.click();
    }
}