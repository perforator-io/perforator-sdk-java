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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AwaitElementToBeVisibleActionProcessorTest extends AbstractActionProcessorTest<AwaitElementToBeVisibleActionConfig, AwaitElementToBeVisibleActionInstance, AwaitElementToBeVisibleActionProcessor> {

    public static final String CHECKED_BTN_CSS_SELECTOR = "#simple-btn";
    public static final String HIDE_STATUS_SWITCHER_CSS_SELECTOR = "#hide-switcher";

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(AwaitElementToBeVisibleActionConfig.Fields.timeout, "invalid-timeout"),
                Map.of(AwaitElementToBeVisibleActionConfig.Fields.cssSelector, "")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        AwaitElementToBeVisibleActionConfig.Fields.cssSelector, CHECKED_BTN_CSS_SELECTOR,
                        AwaitElementToBeVisibleActionConfig.Fields.timeout, "10.5s"
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
                        AwaitElementToBeVisibleActionConfig.Fields.cssSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeVisibleActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeVisibleActionConfig.Fields.timeout, new TextNode("30s")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeVisibleActionConfig.Fields.cssSelector, new TextNode(CHECKED_BTN_CSS_SELECTOR),
                        AwaitElementToBeVisibleActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + AwaitElementToBeClickableActionConfig.Fields.cssSelector + "}"),
                newObjectNode(Map.of(
                        AwaitElementToBeVisibleActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeVisibleActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        AwaitElementToBeVisibleActionConfig.Fields.cssSelector, new TextNode("${" + AwaitElementToBeVisibleActionConfig.Fields.cssSelector + "}"),
                        AwaitElementToBeVisibleActionConfig.Fields.timeout, new TextNode("${" + AwaitElementToBeVisibleActionConfig.Fields.timeout + "}")
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, AwaitElementToBeVisibleActionProcessor actionProcessor, AwaitElementToBeVisibleActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATIONS_APP_URL);

        WebElement checkedElement = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(CHECKED_BTN_CSS_SELECTOR))
        );

        if (!checkedElement.isDisplayed()) {
            WebElement button = new WebDriverWait(
                    driver,
                    actionInstance.getTimeout().toSeconds()
            ).until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector(HIDE_STATUS_SWITCHER_CSS_SELECTOR))
            );
            button.click();
        }
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, AwaitElementToBeVisibleActionProcessor actionProcessor, AwaitElementToBeVisibleActionInstance actionInstance) throws Exception {
        assertTrue(
                driver.findElement(By.cssSelector(CHECKED_BTN_CSS_SELECTOR)).isDisplayed()
        );
    }
}