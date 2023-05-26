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

import java.util.List;
import java.util.Map;

public class SleepActionProcessorTest extends AbstractActionProcessorTest<SleepActionConfig, SleepActionInstance, SleepActionProcessor> {

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        SleepActionConfig.Fields.timeout, "invalid-timeout",
                        SleepActionConfig.Fields.enabled, "true"
                ),
                Map.of(
                        SleepActionConfig.Fields.timeout, "0.1s",
                        SleepActionConfig.Fields.enabled, "invalid-enabled"
                )
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        SleepActionConfig.Fields.timeout, "0.1s-0.5s",
                        SleepActionConfig.Fields.enabled, "true"
                ),
                Map.of(
                        SleepActionConfig.Fields.timeout, "0.3s",
                        SleepActionConfig.Fields.enabled, "false"
                )
        );
    }

    @Override
    protected List<JsonNode> buildInvalidActionConfigs() throws Exception {
        return List.of(
                new TextNode(""),
                new TextNode("invalid-timeout"),
                new TextNode("${invalid-placeholder}"),
                newObjectNode(),
                newObjectNode(Map.of(
                        SleepActionConfig.Fields.timeout, new TextNode("")
                )),
                newObjectNode(Map.of(
                        SleepActionConfig.Fields.timeout, new TextNode("invalid-timeout")
                )),
                newObjectNode(Map.of(
                        SleepActionConfig.Fields.timeout, new TextNode("${invalid-placeholder}")
                )),
                newObjectNode(Map.of(
                        SleepActionConfig.Fields.timeout, new TextNode("0.1s"),
                        SleepActionConfig.Fields.enabled, new TextNode("invalid")
                )),
                newObjectNode(Map.of(
                        SleepActionConfig.Fields.timeout, new TextNode("0.1s"),
                        SleepActionConfig.Fields.enabled, new TextNode("${invalid-placeholder}")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + SleepActionConfig.Fields.timeout + "}"),
                newObjectNode(Map.of(
                        SleepActionConfig.Fields.timeout, new TextNode("${" + SleepActionConfig.Fields.timeout + "}"),
                        SleepActionConfig.Fields.enabled, new TextNode("${" + SleepActionConfig.Fields.enabled + "}")
                ))
        );
    }
}