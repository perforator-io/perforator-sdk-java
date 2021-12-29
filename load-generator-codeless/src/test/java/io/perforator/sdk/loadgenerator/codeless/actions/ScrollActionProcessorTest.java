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
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScrollActionProcessorTest extends AbstractActionProcessorTest<ScrollActionConfig, ScrollActionInstance, ScrollActionProcessor> {

    public static final String VERIFICATION_CSS_SELECTOR = "#disabled-range";

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(ScrollActionConfig.Fields.cssSelector, ""),
                Map.of(ScrollActionConfig.Fields.timeout, "invalid-timeout")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        ScrollActionConfig.Fields.cssSelector, VERIFICATION_CSS_SELECTOR,
                        ScrollActionConfig.Fields.timeout, "10.5s"
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
                        ScrollActionConfig.Fields.cssSelector, new TextNode("")
                )),
                newObjectNode(Map.of(
                        ScrollActionConfig.Fields.cssSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        ScrollActionConfig.Fields.cssSelector, new TextNode(VERIFICATION_CSS_SELECTOR),
                        ScrollActionConfig.Fields.timeout, new TextNode("invalid-timeout")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + ScrollActionConfig.Fields.cssSelector + "}"),
                newObjectNode(Map.of(
                        ScrollActionConfig.Fields.cssSelector, new TextNode("${" + ScrollActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        ScrollActionConfig.Fields.cssSelector, new TextNode("${" + ScrollActionConfig.Fields.cssSelector + "}"),
                        ScrollActionConfig.Fields.timeout, new TextNode("${" + ScrollActionConfig.Fields.timeout + "}")
                )),
                newObjectNode(Map.of(
                        ScrollActionConfig.Fields.cssSelector, new TextNode("${" + ScrollActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        ScrollActionConfig.Fields.cssSelector, new TextNode("${" + ScrollActionConfig.Fields.cssSelector + "}"),
                        ScrollActionConfig.Fields.timeout, new TextNode("${" + ScrollActionConfig.Fields.timeout + "}")
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, ScrollActionProcessor actionProcessor, ScrollActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATIONS_APP_URL);
        scrollToTop(driver);
        Long scrollPosition = (Long) driver.executeScript("return window.pageYOffset;");
        assertEquals(0, scrollPosition);
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, ScrollActionProcessor actionProcessor, ScrollActionInstance actionInstance) throws Exception {
        Long scrollPosition = (Long) driver.executeScript("return window.pageYOffset;");
        assertTrue(scrollPosition > 0);
    }

    private void scrollToTop(RemoteWebDriver driver) {
        driver.executeScript("window.scrollTo(0, -document.body.scrollHeight)");
    }
}