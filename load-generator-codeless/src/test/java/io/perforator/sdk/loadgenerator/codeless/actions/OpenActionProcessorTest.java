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

public class OpenActionProcessorTest extends AbstractActionProcessorTest<OpenActionConfig, OpenActionInstance, OpenActionProcessor> {

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(OpenActionConfig.Fields.url, ""),
                Map.of(OpenActionConfig.Fields.url, "invalid-url"),
                Map.of(OpenActionConfig.Fields.timeout, "invalid-timeout")
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        OpenActionConfig.Fields.url, VERIFICATIONS_APP_URL + "/", 
                        OpenActionConfig.Fields.timeout, "17.5s"
                )
        );
    }

    @Override
    protected List<JsonNode> buildInvalidActionConfigs() throws Exception {
        return List.of(
                new TextNode(""),
                new TextNode("invalid-url"),
                new TextNode("${invalid-placeholder}"),
                newObjectNode(),
                newObjectNode(Map.of(
                        OpenActionConfig.Fields.url, new TextNode("")
                )),
                newObjectNode(Map.of(
                        OpenActionConfig.Fields.url, new TextNode("invalid-url")
                )),
                newObjectNode(Map.of(
                        OpenActionConfig.Fields.url, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        OpenActionConfig.Fields.url, new TextNode(VERIFICATIONS_APP_URL),
                        OpenActionConfig.Fields.timeout, new TextNode("invalid-timeout")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + OpenActionConfig.Fields.url + "}"),
                newObjectNode(Map.of(
                        OpenActionConfig.Fields.url, new TextNode("${" + OpenActionConfig.Fields.url + "}")
                )),
                newObjectNode(Map.of(
                        OpenActionConfig.Fields.url, new TextNode("${" + OpenActionConfig.Fields.url + "}"),
                        OpenActionConfig.Fields.timeout, new TextNode("${" + OpenActionConfig.Fields.timeout + "}")
                ))
        );
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, OpenActionProcessor actionProcessor, OpenActionInstance actionInstance) throws Exception {
        assertEquals(actionInstance.getUrl(), driver.getCurrentUrl());
    }

}
