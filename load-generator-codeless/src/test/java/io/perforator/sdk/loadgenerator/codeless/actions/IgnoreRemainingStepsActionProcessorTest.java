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

public class IgnoreRemainingStepsActionProcessorTest extends AbstractActionProcessorTest<IgnoreRemainingStepsActionConfig, IgnoreRemainingStepsActionInstance, IgnoreRemainingStepsActionProcessor> {

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        IgnoreRemainingStepsActionConfig.Fields.enabled, "invalid-enabled"
                )
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        IgnoreRemainingStepsActionConfig.Fields.enabled, "true"
                ),
                Map.of(
                        IgnoreRemainingStepsActionConfig.Fields.enabled, "false"
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
                        IgnoreRemainingStepsActionConfig.Fields.enabled, new TextNode("${invalid-placeholder}")
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                new TextNode("${" + IgnoreRemainingStepsActionConfig.Fields.enabled + "}"),
                newObjectNode(Map.of(
                        IgnoreRemainingStepsActionConfig.Fields.enabled, new TextNode("${" + IgnoreRemainingStepsActionConfig.Fields.enabled + "}")
                ))
        );
    }
}