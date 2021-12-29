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
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CloseAlertActionProcessorTest extends AbstractActionProcessorTest<CloseAlertActionConfig, CloseAlertActionInstance, CloseAlertActionProcessor> {

    public static final String SHOW_ALERT_BTN_CSS_SELECTOR = "#show-prompt-btn";

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(CloseAlertActionConfig.Fields.action, "invalid-action"),
                Map.of(CloseAlertActionConfig.Fields.timeout, "invalid-timeout")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        CloseAlertActionConfig.Fields.action, CloseAlertActionInstance.Action.ok.name(),
                        CloseAlertActionConfig.Fields.timeout, "10.5s",
                        CloseAlertActionConfig.Fields.text, "some text"
                ),
                Map.of(
                        CloseAlertActionConfig.Fields.action, CloseAlertActionInstance.Action.cancel.name(),
                        CloseAlertActionConfig.Fields.timeout, "10.5s",
                        CloseAlertActionConfig.Fields.text, "some text"
                )
        );
    }

    @Override
    protected List<JsonNode> buildInvalidActionConfigs() throws Exception {
        return List.of(
                new TextNode(""),
                new TextNode("Text"),
                new TextNode("${invalid-placeholder}"),
                newObjectNode(),
                newObjectNode(Map.of(
                        CloseAlertActionConfig.Fields.action, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        CloseAlertActionConfig.Fields.action, new TextNode("")
                )),
                newObjectNode(Map.of(
                        CloseAlertActionConfig.Fields.action, new TextNode("${invalid-placeholder}"),
                        CloseAlertActionConfig.Fields.timeout, new TextNode("invalid-timeout")
                )),
                newObjectNode(Map.of(
                        CloseAlertActionConfig.Fields.timeout, new TextNode("invalid-timeout")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + CloseAlertActionConfig.Fields.action + "}"),
                newObjectNode(Map.of(
                        CloseAlertActionConfig.Fields.action, new TextNode("${" + CloseAlertActionConfig.Fields.action + "}")
                )),
                newObjectNode(Map.of(
                        CloseAlertActionConfig.Fields.action, new TextNode("${" + CloseAlertActionConfig.Fields.action + "}"),
                        CloseAlertActionConfig.Fields.timeout, new TextNode("${" + CloseAlertActionConfig.Fields.timeout + "}")
                )),
                newObjectNode(Map.of(
                        CloseAlertActionConfig.Fields.action, new TextNode("${" + CloseAlertActionConfig.Fields.action + "}"),
                        CloseAlertActionConfig.Fields.text, new TextNode("${" + CloseAlertActionConfig.Fields.text + "}"),
                        CloseAlertActionConfig.Fields.timeout, new TextNode("${" + CloseAlertActionConfig.Fields.timeout + "}")

                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, CloseAlertActionProcessor actionProcessor, CloseAlertActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATIONS_APP_URL);
        if (!isAlertPresent(driver)) {
            //ToDo need to add a button to test.verification
//            WebElement button = new WebDriverWait(
//                    driver,
//                    actionInstance.getTimeout().toSeconds()
//            ).until(
//                    ExpectedConditions.elementToBeClickable(By.cssSelector(SHOW_ALERT_BTN_CSS_SELECTOR))
//            );
//            button.click();
            driver.executeScript("window.prompt('Prompt Alert','Hello Perforator')");
        }
        assertTrue(isAlertPresent(driver));
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, CloseAlertActionProcessor actionProcessor, CloseAlertActionInstance actionInstance) throws Exception {
        assertFalse(isAlertPresent(driver));
    }

    private boolean isAlertPresent(RemoteWebDriver driver) {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException ex) {
            return false;
        }
    }

}