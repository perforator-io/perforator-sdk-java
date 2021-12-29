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
import com.fasterxml.jackson.databind.node.TextNode;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AwaitPageLoadActionProcessorTest extends AbstractActionProcessorTest<AwaitPageLoadActionConfig, AwaitPageLoadActionInstance, AwaitPageLoadActionProcessor> {

    public static final String CHECKED_BTN_CSS_SELECTOR = "#simple-btn";
    protected static final String VERIFICATION_APP_URL_WITH_DELAY = VERIFICATIONS_APP_URL + "/?delay=2000ms";

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(AwaitPageLoadActionConfig.Fields.timeout, "invalid-timeout")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        AwaitPageLoadActionConfig.Fields.timeout, "10.5s"
                )
        );
    }

    @Override
    protected List<JsonNode> buildInvalidActionConfigs() throws Exception {
        return List.of(
                new TextNode(""),
                new TextNode("${invalid-placeholder}"),
                newObjectNode(),
                newObjectNode(Map.of(
                        AwaitPageLoadActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + AwaitPageLoadActionConfig.Fields.timeout + "}"),
                newObjectNode(Map.of(
                        AwaitPageLoadActionConfig.Fields.timeout, new TextNode("${" + AwaitPageLoadActionConfig.Fields.timeout + "}")
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, AwaitPageLoadActionProcessor actionProcessor, AwaitPageLoadActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATION_APP_URL_WITH_DELAY);
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, AwaitPageLoadActionProcessor actionProcessor, AwaitPageLoadActionInstance actionInstance) throws Exception {
        assertTrue(
                driver.findElement(By.cssSelector(CHECKED_BTN_CSS_SELECTOR)).isDisplayed()
        );
    }
}