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
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AwaitElementToBeVisibleActionProcessor extends AbstractActionProcessor<AwaitElementToBeVisibleActionConfig, AwaitElementToBeVisibleActionInstance> {

    public AwaitElementToBeVisibleActionProcessor() {
        super(AwaitElementToBeVisibleActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public AwaitElementToBeVisibleActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return AwaitElementToBeVisibleActionConfig.builder()
                .cssSelector(
                        getRequiredValueOrNestedField(
                                AwaitElementToBeVisibleActionConfig.Fields.cssSelector,
                                actionValue
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                AwaitElementToBeVisibleActionConfig.Fields.timeout,
                                actionValue,
                                AwaitElementToBeVisibleActionConfig.DEFAULT_TIMEOUT
                        )
                )
                .build();
    }

    @Override
    public AwaitElementToBeVisibleActionInstance buildActionInstance(CodelessSuiteConfig suiteConfig, FormattingMap formatter, AwaitElementToBeVisibleActionConfig actionConfig) {
        return AwaitElementToBeVisibleActionInstance.builder()
                .config(
                        actionConfig
                )
                .cssSelector(
                        buildStringForActionInstance(
                                AwaitElementToBeVisibleActionInstance.Fields.cssSelector,
                                actionConfig.getCssSelector(),
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                AwaitElementToBeVisibleActionInstance.Fields.timeout,
                                actionConfig.getTimeout(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, AwaitElementToBeVisibleActionInstance actionInstance) {
        new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(actionInstance.getCssSelector()))
        );
    }
}