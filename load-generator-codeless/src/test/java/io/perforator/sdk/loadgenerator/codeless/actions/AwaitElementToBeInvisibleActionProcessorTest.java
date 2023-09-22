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

import static org.junit.jupiter.api.Assertions.assertFalse;

public class AwaitElementToBeInvisibleActionProcessorTest extends AbstractActionProcessorTest<AwaitElementToBeInvisibleActionConfig, AwaitElementToBeInvisibleActionInstance, AwaitElementToBeInvisibleActionProcessor> {

    public static final String CHECKED_BTN_CSS_SELECTOR = "#simple-btn";
    public static final String CHECKED_BTN_XPATH_SELECTOR = "//*[@id=\"simple-btn\"]";
    public static final String HIDE_STATUS_SWITCHER_CSS_SELECTOR = "#hide-switcher";
    public static final String HIDE_STATUS_SWITCHER_XPATH_SELECTOR = "//*[@id=\"hide-switcher\"]";

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(AwaitElementToBeInvisibleActionConfig.Fields.timeout, "invalid-timeout"),
                Map.of(AwaitElementToBeInvisibleActionConfig.Fields.selector, ""),
                Map.of(AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, ""),
                Map.of(AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector, ""),
                Map.of(AwaitElementToBeInvisibleActionConfig.Fields.enabled, "invalid")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.selector, CHECKED_BTN_CSS_SELECTOR,
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, CHECKED_BTN_CSS_SELECTOR,
                        AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector, CHECKED_BTN_XPATH_SELECTOR,
                        AwaitElementToBeInvisibleActionConfig.Fields.timeout, "10.5s",
                        AwaitElementToBeInvisibleActionConfig.Fields.enabled, "true"
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
                        AwaitElementToBeInvisibleActionConfig.Fields.selector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.timeout, new TextNode("30s")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, new TextNode(CHECKED_BTN_CSS_SELECTOR),
                        AwaitElementToBeInvisibleActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector, new TextNode(CHECKED_BTN_XPATH_SELECTOR),
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, new TextNode(CHECKED_BTN_CSS_SELECTOR)
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, new TextNode("")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector, new TextNode("")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, new TextNode("#valid"),
                        AwaitElementToBeInvisibleActionConfig.Fields.enabled, new TextNode("invalid")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.cssSelector + "}"),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeInvisibleActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeInvisibleActionConfig.Fields.cssSelector + "}"),
                        AwaitElementToBeInvisibleActionConfig.Fields.enabled, new TextNode("${" + AwaitElementToBeInvisibleActionConfig.Fields.enabled + "}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeInvisibleActionConfig.Fields.cssSelector + "}"),
                        AwaitElementToBeInvisibleActionConfig.Fields.timeout, new TextNode("${" + AwaitElementToBeInvisibleActionConfig.Fields.timeout + "}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeInvisibleActionConfig.Fields.cssSelector + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.css.name())
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector, new TextNode("${" + AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.xpath.name())
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector, new TextNode("${" + AwaitElementToBeInvisibleActionConfig.Fields.xpathSelector + "}"),
                        AwaitElementToBeInvisibleActionConfig.Fields.timeout, new TextNode("${" + AwaitElementToBeInvisibleActionConfig.Fields.timeout + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.xpath.name())
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, AwaitElementToBeInvisibleActionProcessor actionProcessor, AwaitElementToBeInvisibleActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATIONS_APP_URL);

        WebElement checkedElement = new WebDriverWait(
                driver,
                actionInstance.getTimeout()
        ).until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(CHECKED_BTN_CSS_SELECTOR))
        );
        if (checkedElement.isDisplayed()) {
            WebElement button = new WebDriverWait(
                    driver,
                    actionInstance.getTimeout()
            ).until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector(HIDE_STATUS_SWITCHER_CSS_SELECTOR))
            );
            button.click();
        }
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, AwaitElementToBeInvisibleActionProcessor actionProcessor, AwaitElementToBeInvisibleActionInstance actionInstance) throws Exception {
        assertFalse(
                driver.findElement(By.cssSelector(CHECKED_BTN_CSS_SELECTOR)).isDisplayed()
        );
    }
}