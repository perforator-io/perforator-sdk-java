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
import org.openqa.selenium.remote.RemoteWebDriver;

public interface ActionProcessor<T extends ActionConfig, V extends ActionInstance<T>> {

    String getSupportedActionName();

    boolean isActionSupported(String actionName, JsonNode actionValue);

    T buildActionConfig(String actionName, JsonNode actionValue);

    void validateActionConfig(CodelessSuiteConfig suiteConfig, T actionConfig);

    V buildActionInstance(CodelessSuiteConfig suiteConfig, FormattingMap formatter, T actionConfig);

    void processActionInstance(RemoteWebDriver driver, V actionInstance);

}
