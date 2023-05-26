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
import org.openqa.selenium.support.ui.WebDriverWait;

public class AwaitPageLoadActionProcessor extends AbstractActionProcessor<AwaitPageLoadActionConfig, AwaitPageLoadActionInstance> {

    public AwaitPageLoadActionProcessor() {
        super(AwaitPageLoadActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public AwaitPageLoadActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return AwaitPageLoadActionConfig.builder()
                .timeout(
                        getRequiredValueOrNestedField(
                                AwaitPageLoadActionConfig.Fields.timeout,
                                actionValue
                        )
                )
                .enabled(
                        getOptionalNestedField(
                                AwaitPageLoadActionInstance.Fields.enabled,
                                actionValue,
                                "true"
                        )
                )
                .build();
    }

    @Override
    public AwaitPageLoadActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, AwaitPageLoadActionConfig actionConfig) {
        return AwaitPageLoadActionInstance.builder()
                .config(
                        actionConfig
                )
                .timeout(
                        buildDurationForActionInstance(
                                AwaitPageLoadActionInstance.Fields.timeout,
                                actionConfig.getTimeout(),
                                suiteConfig.getWebDriverFluentWaitTimeout(),
                                formatter
                        )
                )
                .enabled(
                        buildEnabledForActionInstance(
                                AwaitPageLoadActionInstance.Fields.enabled, 
                                actionConfig.getEnabled(), 
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, AwaitPageLoadActionInstance actionInstance) {
        new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                webDriver -> driver.executeScript("return document.readyState").equals("complete")
        );
    }
}