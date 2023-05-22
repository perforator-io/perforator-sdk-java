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

import java.util.concurrent.TimeUnit;

public class OpenActionProcessor extends AbstractActionProcessor<OpenActionConfig, OpenActionInstance> {

    public OpenActionProcessor() {
        super(OpenActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public OpenActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return OpenActionConfig.builder()
                .url(
                        getRequiredValueOrNestedField(
                                OpenActionConfig.Fields.url,
                                actionValue
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                OpenActionConfig.Fields.timeout,
                                actionValue,
                                null
                        )
                )
                .build();
    }

    @Override
    public OpenActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, OpenActionConfig actionConfig) {
        return OpenActionInstance.builder()
                .config(
                        actionConfig
                )
                .url(
                        buildUrlForActionInstance(
                                OpenActionConfig.Fields.url,
                                actionConfig.getUrl(),
                                formatter
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                OpenActionConfig.Fields.timeout,
                                actionConfig.getTimeout(),
                                suiteConfig.getWebDriverSessionPageLoadTimeout(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, OpenActionInstance actionInstance) {
        driver.manage().timeouts().pageLoadTimeout(
                actionInstance.getTimeout().toMillis(),
                TimeUnit.MILLISECONDS
        );

        driver.navigate().to(actionInstance.getUrl());
    }

}
