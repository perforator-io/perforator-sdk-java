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

public class AwaitElementToBeDisabledActionProcessorTest extends AbstractActionProcessorTest<AwaitElementToBeDisabledActionConfig, AwaitElementToBeDisabledActionInstance, AwaitElementToBeDisabledActionProcessor> {

    public static final String CHECKED_BTN_CSS_SELECTOR = "#simple-btn";
    public static final String CHECKED_BTN_XPATH_SELECTOR = "//*[@id=\"simple-btn\"]";
    public static final String DISABLE_STATUS_SWITCHER_CSS_SELECTOR = "#disable-switcher";
    public static final String DISABLE_STATUS_SWITCHER_XPATH_SELECTOR = "//*[@id=\"disable-switcher\"]";

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(AwaitElementToBeDisabledActionConfig.Fields.timeout, "invalid-timeout"),
                Map.of(AwaitElementToBeDisabledActionConfig.Fields.selector, ""),
                Map.of(AwaitElementToBeDisabledActionConfig.Fields.cssSelector, ""),
                Map.of(AwaitElementToBeDisabledActionConfig.Fields.xpathSelector, "")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.selector, CHECKED_BTN_CSS_SELECTOR,
                        AwaitElementToBeDisabledActionConfig.Fields.cssSelector, CHECKED_BTN_CSS_SELECTOR,
                        AwaitElementToBeDisabledActionConfig.Fields.xpathSelector, CHECKED_BTN_XPATH_SELECTOR,
                        AwaitElementToBeDisabledActionConfig.Fields.timeout, "10.5s"
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
                        AwaitElementToBeDisabledActionConfig.Fields.selector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.xpathSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.cssSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.timeout, new TextNode("30s")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.cssSelector, new TextNode(CHECKED_BTN_CSS_SELECTOR),
                        AwaitElementToBeDisabledActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.xpathSelector, new TextNode(CHECKED_BTN_XPATH_SELECTOR),
                        AwaitElementToBeDisabledActionConfig.Fields.cssSelector, new TextNode(CHECKED_BTN_CSS_SELECTOR)
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.cssSelector, new TextNode("")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.xpathSelector, new TextNode("")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.cssSelector + "}"),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeDisabledActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeDisabledActionConfig.Fields.cssSelector + "}"),
                        AwaitElementToBeDisabledActionConfig.Fields.timeout, new TextNode("${" + AwaitElementToBeDisabledActionConfig.Fields.timeout + "}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeDisabledActionConfig.Fields.cssSelector + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.css.name())
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.xpathSelector, new TextNode("${" + AwaitElementToBeDisabledActionConfig.Fields.xpathSelector + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.xpath.name())
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeDisabledActionConfig.Fields.xpathSelector, new TextNode("${" + AwaitElementToBeDisabledActionConfig.Fields.xpathSelector + "}"),
                        AwaitElementToBeDisabledActionConfig.Fields.timeout, new TextNode("${" + AwaitElementToBeDisabledActionConfig.Fields.timeout + "}"),
                        SELECTOR_TYPE_KEY, new TextNode(SelectorType.xpath.name())
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, AwaitElementToBeDisabledActionProcessor actionProcessor, AwaitElementToBeDisabledActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATIONS_APP_URL);

        WebElement checkedElement = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(CHECKED_BTN_CSS_SELECTOR))
        );

        if (checkedElement.isEnabled()) {
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
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, AwaitElementToBeDisabledActionProcessor actionProcessor, AwaitElementToBeDisabledActionInstance actionInstance) throws Exception {
        assertFalse(
                driver.findElement(By.cssSelector(CHECKED_BTN_CSS_SELECTOR)).isEnabled()
        );
    }
}