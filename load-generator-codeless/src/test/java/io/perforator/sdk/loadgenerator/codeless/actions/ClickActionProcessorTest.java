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
import io.perforator.sdk.loadgenerator.codeless.config.SelectorType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClickActionProcessorTest extends AbstractActionProcessorTest<ClickActionConfig, ClickActionInstance, ClickActionProcessor> {

    public static final String VERIFICATION_CSS_SELECTOR = "#navbarCollapse > ul > li:nth-child(1) > a";
    public static final String VERIFICATION_XPATH_SELECTOR = "//*[@id=\"navbarCollapse\"]/ul/li[1]/a";

    public static final String CSS_SELECTOR_TYPE_KEY = "cssSelectorType";
    public static final String XPATH_SELECTOR_TYPE_KEY = "xpathSelectorType";


    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(ClickActionConfig.Fields.timeout, "invalid-timeout"),
                Map.of(ClickActionConfig.Fields.selector, ""),
                Map.of(ClickActionConfig.Fields.cssSelector, ""),
                Map.of(ClickActionConfig.Fields.xpathSelector, "")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        ClickActionConfig.Fields.selector, VERIFICATION_CSS_SELECTOR,
                        ClickActionConfig.Fields.cssSelector, VERIFICATION_CSS_SELECTOR,
                        ClickActionConfig.Fields.xpathSelector, VERIFICATION_XPATH_SELECTOR,
                        ClickActionConfig.Fields.timeout, "10.5s"
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
                        ClickActionConfig.Fields.selector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.xpathSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.cssSelector, new TextNode("")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.cssSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.cssSelector, new TextNode(VERIFICATION_CSS_SELECTOR),
                        ClickActionConfig.Fields.timeout, new TextNode("invalid-timeout")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.cssSelector, new TextNode(VERIFICATION_CSS_SELECTOR),
                        ClickActionConfig.Fields.xpathSelector, new TextNode(VERIFICATION_XPATH_SELECTOR)
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.cssSelector, new TextNode("")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.xpathSelector, new TextNode("")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + ClickActionConfig.Fields.cssSelector + "}"),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.cssSelector, new TextNode("${" + ClickActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.cssSelector, new TextNode("${" + ClickActionConfig.Fields.cssSelector + "}"),
                        ClickActionConfig.Fields.timeout, new TextNode("${" + ClickActionConfig.Fields.timeout + "}")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.cssSelector, new TextNode("${" + ClickActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.cssSelector, new TextNode("${" + ClickActionConfig.Fields.cssSelector + "}"),
                        ClickActionConfig.Fields.timeout, new TextNode("${" + ClickActionConfig.Fields.timeout + "}")
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.xpathSelector, new TextNode("${" + ClickActionConfig.Fields.xpathSelector + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.xpath.name())
                )),
                newObjectNode(Map.of(
                        ClickActionConfig.Fields.xpathSelector, new TextNode("${" + ClickActionConfig.Fields.xpathSelector + "}"),
                        ClickActionConfig.Fields.timeout, new TextNode("${" + ClickActionConfig.Fields.timeout + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.xpath.name())
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, ClickActionProcessor actionProcessor, ClickActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATIONS_APP_URL);
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, ClickActionProcessor actionProcessor, ClickActionInstance actionInstance) throws Exception {
        assertEquals(driver.getCurrentUrl(), VERIFICATIONS_APP_URL + "/satisne");
    }
}