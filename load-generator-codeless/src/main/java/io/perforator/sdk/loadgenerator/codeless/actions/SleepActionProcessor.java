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
import io.perforator.sdk.loadgenerator.codeless.RandomDuration;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import io.perforator.sdk.loadgenerator.core.Threaded;
import java.util.List;
import org.openqa.selenium.remote.RemoteWebDriver;

public class SleepActionProcessor extends AbstractActionProcessor<SleepActionConfig, SleepActionInstance> {

    public SleepActionProcessor() {
        super(SleepActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public SleepActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return SleepActionConfig.builder()
                .timeout(
                        getRequiredValueOrNestedField(
                                SleepActionConfig.Fields.timeout,
                                actionValue
                        )
                )
                .build();
    }

    @Override
    public SleepActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, SleepActionConfig actionConfig) {
        return SleepActionInstance.builder()
                .config(
                        actionConfig
                )
                .timeout(
                        buildRandomDurationForActionInstance(
                                SleepActionConfig.Fields.timeout,
                                actionConfig.getTimeout(),
                                formatter
                        ).random()
                )
                .build();
    }

    @Override
    public void validateActionConfig(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, SleepActionConfig actionConfig) {
        super.validateActionConfig(loadGeneratorConfig, suiteConfig, actionConfig);

        if (actionConfig.getTimeout() == null || actionConfig.getTimeout().isEmpty()) {
            throw new RuntimeException(
                    "Action '" + actionConfig.getActionName() + "' should have a duration specified"
            );
        }

        List<FormattingMap> formatters;
        if (suiteConfig.getProps() == null || suiteConfig.getProps().isEmpty()) {
            formatters = List.of(FormattingMap.EMPTY);
        } else {
            formatters = suiteConfig.getProps();
        }

        for (FormattingMap formatter : formatters) {
            RandomDuration randomDuration = buildRandomDurationForActionInstance(
                    SleepActionConfig.Fields.timeout,
                    actionConfig.getTimeout(),
                    formatter
            );

            if (randomDuration.getFrom() == null && randomDuration.getTo() == null) {
                continue;
            }

            if (randomDuration.getFrom() != null && randomDuration.getFrom().toMillis() < 0) {
                throw new RuntimeException(
                        "Action '" + actionConfig.getActionName() + "' should have a positive duration"
                );
            }

            if (randomDuration.getFrom() != null && randomDuration.getFrom().toMillis() >= 60000) {
                throw new RuntimeException(
                        "Action '" + actionConfig.getActionName() + "' should have a duration < 60s"
                );
            }

            if (randomDuration.getTo() != null && randomDuration.getTo().toMillis() < 0) {
                throw new RuntimeException(
                        "Action '" + actionConfig.getActionName() + "' should have a positive duration"
                );
            }

            if (randomDuration.getTo() != null && randomDuration.getTo().toMillis() >= 60000) {
                throw new RuntimeException(
                        "Action '" + actionConfig.getActionName() + "' should have a duration < 60s"
                );
            }
        }
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, SleepActionInstance actionInstance) {
        Threaded.sleep(actionInstance.getTimeout().toMillis());
    }
}
