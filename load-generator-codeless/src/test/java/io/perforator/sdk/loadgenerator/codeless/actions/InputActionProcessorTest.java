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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputActionProcessorTest extends AbstractActionProcessorTest<InputActionConfig, InputActionInstance, InputActionProcessor> {

    public static final String VERIFICATION_CSS_SELECTOR = "#file-input";
    public static final String FILE_NAME = "valid_1.yaml";
    public final String VERIFICATION_INPUT_VALUE = getFileFromResource("yaml/" + FILE_NAME).getAbsolutePath();

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(InputActionConfig.Fields.value, ""),
                Map.of(InputActionConfig.Fields.cssSelector, ""),
                Map.of(InputActionConfig.Fields.timeout, "invalid-timeout")

        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        InputActionConfig.Fields.value, VERIFICATION_INPUT_VALUE,
                        InputActionConfig.Fields.cssSelector, VERIFICATION_CSS_SELECTOR,
                        InputActionConfig.Fields.timeout, "10.5s"
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
                        InputActionConfig.Fields.value, new TextNode("${invalid-placeholder}"),
                        InputActionConfig.Fields.cssSelector, new TextNode(VERIFICATION_CSS_SELECTOR)
                )),
                newObjectNode(Map.of(
                        InputActionConfig.Fields.value, new TextNode(VERIFICATION_INPUT_VALUE),
                        InputActionConfig.Fields.cssSelector, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        InputActionConfig.Fields.value, new TextNode(""),
                        InputActionConfig.Fields.cssSelector, new TextNode(VERIFICATION_CSS_SELECTOR)
                )),
                newObjectNode(Map.of(
                        InputActionConfig.Fields.value, new TextNode(VERIFICATION_INPUT_VALUE),
                        InputActionConfig.Fields.cssSelector, new TextNode("")
                )),
                newObjectNode(Map.of(
                        InputActionConfig.Fields.value, new TextNode("${invalid-placeholder}"),
                        InputActionConfig.Fields.cssSelector, new TextNode("${invalid-placeholder}"),
                        InputActionConfig.Fields.timeout, new TextNode("invalid-timeout")
                )),
                newObjectNode(Map.of(
                        InputActionConfig.Fields.value, new TextNode(VERIFICATION_INPUT_VALUE),
                        InputActionConfig.Fields.cssSelector, new TextNode(VERIFICATION_CSS_SELECTOR),
                        InputActionConfig.Fields.timeout, new TextNode("invalid-timeout")
                ))

        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                newObjectNode(Map.of(
                        InputActionConfig.Fields.value, new TextNode("${" + InputActionConfig.Fields.value + "}"),
                        InputActionConfig.Fields.cssSelector, new TextNode("${" + InputActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        InputActionConfig.Fields.value, new TextNode("${" + InputActionConfig.Fields.value + "}"),
                        InputActionConfig.Fields.cssSelector, new TextNode("${" + InputActionConfig.Fields.cssSelector + "}"),
                        InputActionConfig.Fields.timeout, new TextNode("${" + InputActionConfig.Fields.timeout + "}")
                )),
                newObjectNode(Map.of(
                        InputActionConfig.Fields.value, new TextNode("${" + InputActionConfig.Fields.value + "}"),
                        InputActionConfig.Fields.cssSelector, new TextNode("${" + InputActionConfig.Fields.cssSelector + "}")
                )),
                newObjectNode(Map.of(
                        InputActionConfig.Fields.value, new TextNode("${" + InputActionConfig.Fields.value + "}"),
                        InputActionConfig.Fields.cssSelector, new TextNode("${" + InputActionConfig.Fields.cssSelector + "}"),
                        InputActionConfig.Fields.timeout, new TextNode("${" + InputActionConfig.Fields.timeout + "}")
                ))
        );
    }

    @Override
    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, InputActionProcessor actionProcessor, InputActionInstance actionInstance) throws Exception {
        driver.navigate().to(VERIFICATIONS_APP_URL);
        WebElement checkedElement = new WebDriverWait(
                driver,
                actionInstance.getTimeout().toSeconds()
        ).until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(VERIFICATION_CSS_SELECTOR))
        );
        assertFalse(checkedElement.getAttribute("value").contains(FILE_NAME));
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, InputActionProcessor actionProcessor, InputActionInstance actionInstance) throws Exception {
        WebElement webElement = driver.findElement(By.cssSelector(VERIFICATION_CSS_SELECTOR));
        assertTrue(webElement.getAttribute("value").contains(FILE_NAME));
    }

    private File getFileFromResource(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("File not found! " + fileName);
        } else {
            try {
                return new File(resource.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("URI Exception", e);
            }
        }
    }
}