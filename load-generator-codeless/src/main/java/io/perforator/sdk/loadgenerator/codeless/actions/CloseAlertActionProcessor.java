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
import org.openqa.selenium.Alert;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CloseAlertActionProcessor extends AbstractActionProcessor<CloseAlertActionConfig, CloseAlertActionInstance> {

    public CloseAlertActionProcessor() {
        super(CloseAlertActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public void validateActionConfig(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, CloseAlertActionConfig actionConfig) {
        try {
            if (suiteConfig.getProps().isEmpty()) {
                CloseAlertActionInstance.Action.valueOf(
                        actionConfig.getAction()
                );
            } else {
                suiteConfig.getProps().forEach(formatter -> {
                    CloseAlertActionInstance.Action.valueOf(
                            formatter.format(actionConfig.getAction())
                    );
                });
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("CloseAlertAction with type of '" + actionConfig.getAction() + "' is not supported!");
        }

        super.validateActionConfig(loadGeneratorConfig, suiteConfig, actionConfig);
    }

    @Override
    public CloseAlertActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return CloseAlertActionConfig.builder()
                .action(
                        getRequiredValueOrNestedField(
                                CloseAlertActionConfig.Fields.action,
                                actionValue
                        )
                )
                .text(
                        getOptionalNestedField(
                                CloseAlertActionConfig.Fields.text,
                                actionValue,
                                null
                        )
                )
                .timeout(
                        getOptionalNestedField(
                                CloseAlertActionConfig.Fields.timeout,
                                actionValue,
                                null
                        )
                )
                .build();
    }

    @Override
    public CloseAlertActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, CloseAlertActionConfig actionConfig) {
        return CloseAlertActionInstance.builder()
                .config(
                        actionConfig
                )
                .action(
                        CloseAlertActionInstance.Action.valueOf(
                                buildStringForActionInstance(
                                        CloseAlertActionInstance.Fields.action,
                                        actionConfig.getAction(),
                                        formatter
                                )
                        )
                )
                .text(
                        buildStringForActionInstance(
                                CloseAlertActionInstance.Fields.text,
                                actionConfig.getText(),
                                formatter,
                                false
                        )
                )
                .timeout(
                        buildDurationForActionInstance(
                                CloseAlertActionInstance.Fields.timeout,
                                actionConfig.getTimeout(),
                                suiteConfig.getWebDriverFluentWaitTimeout(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, CloseAlertActionInstance actionInstance) {
        Alert alert = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.alertIsPresent()
        );

        if (actionInstance.getText() != null) {
            alert.sendKeys(actionInstance.getText());
        }

        switch (actionInstance.getAction()) {
            case ok: {
                alert.accept();
                break;
            }
            case cancel: {
                alert.dismiss();
                break;
            }
        }
    }
}