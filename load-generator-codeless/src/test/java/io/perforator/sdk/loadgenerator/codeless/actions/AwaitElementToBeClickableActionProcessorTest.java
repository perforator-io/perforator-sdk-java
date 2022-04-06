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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AwaitElementToBeClickableActionProcessorTest extends AbstractActionProcessorTest<AwaitElementToBeClickableActionConfig, AwaitElementToBeClickableActionInstance, AwaitElementToBeClickableActionProcessor> {

    public static final String CHECKED_BTN_CSS_SELECTOR = "#simple-btn";
    public static final String CHECKED_BTN_XPATH_SELECTOR = "//*[@id=\"simple-btn\"]";
    public static final String DISABLE_STATUS_SWITCHER_CSS_SELECTOR = "#disable-switcher";
    public static final String DISABLE_STATUS_SWITCHER_XPATH_SELECTOR = "//*[@id=\"disable-switcher\"]";

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(AwaitElementToBeClickableActionConfig.Fields.timeout, "invalid-timeout"),
                Map.of(AwaitElementToBeClickableActionConfig.Fields.selector, ""),
                Map.of(AwaitElementToBeClickableActionConfig.Fields.cssSelector, ""),
                Map.of(AwaitElementToBeClickableActionConfig.Fields.xpathSelector, "")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.selector, CHECKED_BTN_CSS_SELECTOR,
                        AwaitElementToBeClickableActionConfig.Fields.cssSelector, CHECKED_BTN_CSS_SELECTOR,
                        AwaitElementToBeClickableActionConfig.Fields.xpathSelector, CHECKED_BTN_XPATH_SELECTOR,
                        AwaitElementToBeClickableActionConfig.Fields.timeout, "10.5s"
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
                        AwaitElementToBeClickableActionConfig.Fields.selector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.xpathSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.cssSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.timeout, new TextNode("30s")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.cssSelector, new TextNode(CHECKED_BTN_CSS_SELECTOR),
                        AwaitElementToBeClickableActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.xpathSelector, new TextNode(CHECKED_BTN_XPATH_SELECTOR),
                        AwaitElementToBeClickableActionConfig.Fields.cssSelector, new TextNode(CHECKED_BTN_CSS_SELECTOR)
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.xpathSelector, new TextNode("")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.cssSelector, new TextNode("")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.selector + "}"),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.cssSelector + "}"),
                        AwaitElementToBeClickableActionConfig.Fields.timeout, new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.timeout + "}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.cssSelector + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.css.name())
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.xpathSelector, new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.xpathSelector + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.xpath.name())
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeClickableActionConfig.Fields.xpathSelector, new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.xpathSelector + "}"),
                        AwaitElementToBeClickableActionConfig.Fields.timeout, new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.timeout + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.xpath.name())
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, AwaitElementToBeClickableActionProcessor actionProcessor, AwaitElementToBeClickableActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATIONS_APP_URL);

        WebElement checkedElement = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(CHECKED_BTN_CSS_SELECTOR))
        );

        if (!checkedElement.isEnabled()) {
            WebElement button = new WebDriverWait(
                    driver,
                    actionInstance.getTimeout().toSeconds()
            ).until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector(DISABLE_STATUS_SWITCHER_CSS_SELECTOR))
            );
            button.click();
        }
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, AwaitElementToBeClickableActionProcessor actionProcessor, AwaitElementToBeClickableActionInstance actionInstance) throws Exception {
        WebElement element = driver.findElement(By.cssSelector(CHECKED_BTN_CSS_SELECTOR));
        assertTrue(element != null && element.isEnabled());
    }
}