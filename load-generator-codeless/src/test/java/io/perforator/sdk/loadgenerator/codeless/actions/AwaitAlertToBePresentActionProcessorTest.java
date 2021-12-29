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
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AwaitAlertToBePresentActionProcessorTest extends AbstractActionProcessorTest<AwaitAlertToBePresentActionConfig, AwaitAlertToBePresentActionInstance, AwaitAlertToBePresentActionProcessor> {

    public static final String SHOW_ALERT_BTN_CSS_SELECTOR = "#show-alert-btn";


    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(AwaitAlertToBePresentActionConfig.Fields.timeout, "invalid-timeout")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        AwaitAlertToBePresentActionConfig.Fields.timeout, "10.5s"
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
                        AwaitAlertToBePresentActionConfig.Fields.timeout, new TextNode("${invalid-timeout}")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + AwaitAlertToBePresentActionConfig.Fields.timeout + "}"),
                newObjectNode(Map.of(
                        AwaitAlertToBePresentActionConfig.Fields.timeout, new TextNode("${" + AwaitAlertToBePresentActionConfig.Fields.timeout + "}")
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, AwaitAlertToBePresentActionProcessor actionProcessor, AwaitAlertToBePresentActionInstance actionInstance) throws Exception {
        Alert alert = getAlert(driver);
        if (alert != null) {
            alert.accept();
        }
        driver.navigate().to(VERIFICATIONS_APP_URL);

        WebElement button = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(SHOW_ALERT_BTN_CSS_SELECTOR))
        );
        button.click();
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, AwaitAlertToBePresentActionProcessor actionProcessor, AwaitAlertToBePresentActionInstance actionInstance) throws Exception {
        assertNotNull(getAlert(driver));
    }

    private Alert getAlert(RemoteWebDriver driver) {
        try {
            return driver.switchTo().alert();
        } catch (NoAlertPresentException ex) {
            return null;
        }
    }
}