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
import io.perforator.sdk.loadgenerator.core.Threaded;
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
    public SleepActionInstance buildActionInstance(CodelessSuiteConfig suiteConfig, FormattingMap formatter, SleepActionConfig actionConfig) {
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
    public void processActionInstance(RemoteWebDriver driver, SleepActionInstance actionInstance) {
        Threaded.sleep(actionInstance.getTimeout().toMillis());
    }
}