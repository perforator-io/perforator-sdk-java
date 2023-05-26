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

public class AwaitElementToBeClickableActionProcessor extends AbstractSelectorActionProcessor<AwaitElementToBeClickableActionConfig, AwaitElementToBeClickableActionInstance> {
    public AwaitElementToBeClickableActionProcessor() {
        super(AwaitElementToBeClickableActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public AwaitElementToBeClickableActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return AwaitElementToBeClickableActionConfig.builder()
                .selector(
                        getOptionalValue(
                                actionValue,
                                null
                        )
                )
                .cssSelector(
                        getOptionalNestedField(
                                AwaitElementToBeClickableActionConfig.Fields.cssSelector,
                                actionValue,
                                null
                        )
                )
                .xpathSelector(
                        getOptionalNestedField(
                                AwaitElementToBeClickableActionConfig.Fields.xpathSelector,
                                actionValue,
                                null
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                AwaitElementToBeClickableActionConfig.Fields.timeout,
                                actionValue,
                                null
                        )
                )
                .enabled(
                        getOptionalNestedField(
                                AwaitElementToBeClickableActionConfig.Fields.enabled,
                                actionValue,
                                "true"
                        )
                )
                .build();
    }

    @Override
    public AwaitElementToBeClickableActionInstance buildActionInstance(
            CodelessLoadGeneratorConfig loadGeneratorConfig,
            CodelessSuiteConfig suiteConfig,
            FormattingMap formatter,
            AwaitElementToBeClickableActionConfig actionConfig
    ) {
        return AwaitElementToBeClickableActionInstance.builder()
                .config(
                        actionConfig
                )
                .selectorType(
                        getSelectorType(actionConfig, loadGeneratorConfig.getDefaultSelectorType())
                )
                .selector(
                        buildRequiredStringSelectorForActionInstance(
                                actionConfig,
                                AwaitElementToBeClickableActionInstance.Fields.selector,
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                AwaitElementToBeClickableActionInstance.Fields.timeout,
                                actionConfig.getTimeout(),
                                suiteConfig.getWebDriverFluentWaitTimeout(),
                                formatter
                        )
                )
                .enabled(
                        buildEnabledForActionInstance(
                                AwaitElementToBeClickableActionInstance.Fields.enabled, 
                                actionConfig.getEnabled(), 
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, AwaitElementToBeClickableActionInstance actionInstance) {
        new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.elementToBeClickable(
                        getActionInstanceLocator(actionInstance)
                )
        );
    }
}