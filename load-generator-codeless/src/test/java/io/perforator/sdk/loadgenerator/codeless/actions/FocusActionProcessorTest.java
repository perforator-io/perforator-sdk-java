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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class FocusActionProcessorTest extends AbstractActionProcessorTest<FocusActionConfig, FocusActionInstance, FocusActionProcessor> {

    public static final String VERIFICATION_CSS_SELECTOR = "#navbarCollapse > ul > li:nth-child(1) > a";

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(FocusActionConfig.Fields.cssSelector, ""),
                Map.of(FocusActionConfig.Fields.timeout, "invalid-timeout")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        FocusActionConfig.Fields.cssSelector, VERIFICATION_CSS_SELECTOR,
                        FocusActionConfig.Fields.timeout, "10.5s"
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
                        FocusActionConfig.Fields.cssSelector, new TextNode("")
                )),
                newObjectNode(Map.of(
                        FocusActionConfig.Fields.cssSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        FocusActionConfig.Fields.cssSelector, new TextNode(VERIFICATION_CSS_SELECTOR),
                        FocusActionConfig.Fields.timeout, new TextNode("invalid-timeout")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + FocusActionConfig.Fields.cssSelector + "}"),
                newObjectNode(Map.of(
                        FocusActionConfig.Fields.cssSelector, new TextNode("${" + FocusActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        FocusActionConfig.Fields.cssSelector, new TextNode("${" + FocusActionConfig.Fields.cssSelector + "}"),
                        FocusActionConfig.Fields.timeout, new TextNode("${" + FocusActionConfig.Fields.timeout + "}")
                )),
                newObjectNode(Map.of(
                        FocusActionConfig.Fields.cssSelector, new TextNode("${" + FocusActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        FocusActionConfig.Fields.cssSelector, new TextNode("${" + FocusActionConfig.Fields.cssSelector + "}"),
                        FocusActionConfig.Fields.timeout, new TextNode("${" + FocusActionConfig.Fields.timeout + "}")
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, FocusActionProcessor actionProcessor, FocusActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATIONS_APP_URL);
        WebElement checkedElement = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(VERIFICATION_CSS_SELECTOR))
        );
        assertNotEquals(checkedElement, driver.switchTo().activeElement());
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, FocusActionProcessor actionProcessor, FocusActionInstance actionInstance) throws Exception {
        WebElement webElement = driver.findElement(By.cssSelector(VERIFICATION_CSS_SELECTOR));
        assertEquals(webElement, driver.switchTo().activeElement());
    }
}