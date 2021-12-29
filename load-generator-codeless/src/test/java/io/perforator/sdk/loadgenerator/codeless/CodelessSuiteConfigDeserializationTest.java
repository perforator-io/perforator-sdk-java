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
package io.perforator.sdk.loadgenerator.codeless;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionConfig;
import io.perforator.sdk.loadgenerator.codeless.actions.OpenActionConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodelessSuiteConfigDeserializationTest {

    @Test
    public void validatePropsAsObject() throws Exception {
        CodelessSuiteConfig suite = new ObjectMapper().readValue(
                "{\"props\":{\"a\":5}}",
                CodelessSuiteConfig.class
        );

        assertNotNull(suite);
        assertNotNull(suite.getProps());
        assertFalse(suite.getProps().isEmpty());
        assertEquals(1, suite.getProps().size());
        assertEquals("5", suite.getProps().get(0).get("a"));
    }

    @Test
    public void validatePropsAsArray() throws Exception {
        CodelessSuiteConfig suite = new ObjectMapper().readValue(
                "{\"props\":[{\"a\":5},{\"b\":\"6\"}]}",
                CodelessSuiteConfig.class
        );

        assertNotNull(suite);
        assertNotNull(suite.getProps());
        assertFalse(suite.getProps().isEmpty());
        assertEquals(2, suite.getProps().size());
        assertEquals("5", suite.getProps().get(0).get("a"));
        assertEquals("6", suite.getProps().get(1).get("b"));
    }

    @Test
    public void validateEmptyProps() throws Exception {
        CodelessSuiteConfig suite = new ObjectMapper().readValue(
                "{}",
                CodelessSuiteConfig.class
        );

        assertNotNull(suite);
        assertNotNull(suite.getProps());
        assertTrue(suite.getProps().isEmpty());

        suite = new ObjectMapper().readValue(
                "{\"props\":[]}",
                CodelessSuiteConfig.class
        );

        assertNotNull(suite);
        assertNotNull(suite.getProps());
        assertTrue(suite.getProps().isEmpty());

        suite = new ObjectMapper().readValue(
                "{\"props\":{}}",
                CodelessSuiteConfig.class
        );

        assertNotNull(suite);
        assertNotNull(suite.getProps());
        assertTrue(suite.getProps().isEmpty());
    }

    @Test
    public void validateSteps() throws Exception {
        CodelessSuiteConfig suite = new ObjectMapper().readValue(
                "{\"steps\":{}}",
                CodelessSuiteConfig.class
        );

        assertNotNull(suite);
        assertNotNull(suite.getSteps());
        assertTrue(suite.getSteps().isEmpty());

        suite = new ObjectMapper().readValue(
                "{\"steps\":{\"step1\":[{},{}]}}",
                CodelessSuiteConfig.class
        );

        assertNotNull(suite);
        assertNotNull(suite.getSteps());
        assertFalse(suite.getSteps().isEmpty());
        assertEquals(1, suite.getSteps().size());
        assertEquals("step1", suite.getSteps().get(0).getName());
        assertNotNull(suite.getSteps().get(0).getActions());
        assertTrue(suite.getSteps().get(0).getActions().isEmpty());

        suite = new ObjectMapper().readValue(
                "{\"steps\":{\"step1\":[{\"open\":\"url1\"}]}}",
                CodelessSuiteConfig.class
        );

        assertNotNull(suite);
        assertNotNull(suite.getSteps());
        assertFalse(suite.getSteps().isEmpty());
        assertEquals(1, suite.getSteps().size());
        assertEquals("step1", suite.getSteps().get(0).getName());

        List<ActionConfig> actionConfigs = suite.getSteps().get(0).getActions();
        assertNotNull(actionConfigs);
        assertFalse(actionConfigs.isEmpty());
        assertEquals(1, actionConfigs.size());
        assertEquals("open", actionConfigs.get(0).getActionName());
        assertEquals(OpenActionConfig.class, actionConfigs.get(0).getClass());
        assertEquals("url1", ((OpenActionConfig) actionConfigs.get(0)).getUrl());
    }

}
