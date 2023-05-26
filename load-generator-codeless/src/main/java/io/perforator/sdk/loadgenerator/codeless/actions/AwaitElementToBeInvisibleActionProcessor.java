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

public class AwaitElementToBeInvisibleActionProcessor extends AbstractSelectorActionProcessor<AwaitElementToBeInvisibleActionConfig, AwaitElementToBeInvisibleActionInstance> {

    public AwaitElementToBeInvisibleActionProcessor() {
        super(AwaitElementToBeInvisibleActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public AwaitElementToBeInvisibleActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return AwaitElementToBeInvisibleActionConfig.builder()
                .selector(
                        getOptionalValue(
                                actionValue,
                                null
                        )
                )
                .cssSelector(
                        getOptionalNestedField(
                                AwaitElementToBeInvisibleActionConfig.Fields.cssSelector,
                                actionValue,
                                null
                        )
                )
                .xpathSelector(
                        getOptionalNestedField(
                                AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector,
                                actionValue,
                                null
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                AwaitElementToBeInvisibleActionConfig.Fields.timeout,
                                actionValue,
                                null
                        )
                )
                .enabled(
                        getOptionalNestedField(
                                AwaitElementToBeInvisibleActionConfig.Fields.enabled,
                                actionValue,
                                "true"
                        )
                )
                .build();
    }

    @Override
    public AwaitElementToBeInvisibleActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, AwaitElementToBeInvisibleActionConfig actionConfig) {
        return AwaitElementToBeInvisibleActionInstance.builder()
                .config(
                        actionConfig
                )
                .selectorType(
                        getSelectorType(actionConfig, loadGeneratorConfig.getDefaultSelectorType())
                )
                .selector(
                        buildRequiredStringSelectorForActionInstance(
                                actionConfig,
                                AwaitElementToBeInvisibleActionInstance.Fields.selector,
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                AwaitElementToBeInvisibleActionInstance.Fields.timeout,
                                actionConfig.getTimeout(),
                                suiteConfig.getWebDriverFluentWaitTimeout(),
                                formatter
                        )
                )
                .enabled(
                        buildEnabledForActionInstance(
                                AwaitElementToBeInvisibleActionInstance.Fields.enabled, 
                                actionConfig.getEnabled(), 
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, AwaitElementToBeInvisibleActionInstance actionInstance) {
        new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.invisibilityOfElementLocated(
                        getActionInstanceLocator(actionInstance)
                )
        );
    }
}