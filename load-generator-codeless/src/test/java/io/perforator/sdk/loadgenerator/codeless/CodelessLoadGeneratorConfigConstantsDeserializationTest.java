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
import io.perforator.sdk.loadgenerator.codeless.config.CodelessConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CodelessLoadGeneratorConfigConstantsDeserializationTest {

    @Test
    public void validateConstantsAsObject() throws Exception {
        CodelessConfig config = new ObjectMapper().readValue(
                "{\"variables\":{\"a\":5}}",
                CodelessConfig.class
        );

        assertNotNull(config);
        assertNotNull(config.getVariables());
        assertFalse(config.getVariables().isEmpty());
        assertEquals(1, config.getVariables().size());
        assertEquals("5", config.getVariables().get("a"));
    }

    @Test
    public void validateEmptyConstants() throws Exception {
        CodelessConfig config = new ObjectMapper().readValue(
                "{}",
                CodelessConfig.class
        );

        assertNotNull(config);
        assertNotNull(config.getVariables());
        assertTrue(config.getVariables().isEmpty());

        config = new ObjectMapper().readValue(
                "{\"variables\":{}}",
                CodelessConfig.class
        );

        assertNotNull(config);
        assertNotNull(config.getVariables());
        assertTrue(config.getVariables().isEmpty());
    }

    @Test
    public void validateComplexConstants() throws Exception {
        CodelessConfig config = new ObjectMapper().readValue(
                "{\"variables\":{\"a\":5,\"b\":\"ref - ${a}\"}}",
                CodelessConfig.class
        );

        assertNotNull(config);
        assertNotNull(config.getVariables());
        assertFalse(config.getVariables().isEmpty());
        assertEquals(2, config.getVariables().size());
        assertEquals("5", config.getVariables().get("a"));
        assertEquals("ref - 5", config.getVariables().get("b"));
    }

}
