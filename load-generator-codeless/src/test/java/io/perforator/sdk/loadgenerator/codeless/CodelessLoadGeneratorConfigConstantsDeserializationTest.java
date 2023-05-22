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
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class CodelessLoadGeneratorConfigConstantsDeserializationTest {

    @Test
    public void validateConstantsAsObject() throws Exception {
        CodelessLoadGeneratorConfig config = new ObjectMapper().readValue(
                "{\"constants\":{\"a\":5}}",
                CodelessLoadGeneratorConfig.class
        );

        assertNotNull(config);
        assertNotNull(config.getConstants());
        assertFalse(config.getConstants().isEmpty());
        assertEquals(1, config.getConstants().size());
        assertEquals("5", config.getConstants().get("a"));
    }

    @Test
    public void validateEmptyConstants() throws Exception {
        CodelessLoadGeneratorConfig config = new ObjectMapper().readValue(
                "{}",
                CodelessLoadGeneratorConfig.class
        );

        assertNotNull(config);
        assertNotNull(config.getConstants());
        assertTrue(config.getConstants().isEmpty());

        config = new ObjectMapper().readValue(
                "{\"constants\":{}}",
                CodelessLoadGeneratorConfig.class
        );

        assertNotNull(config);
        assertNotNull(config.getConstants());
        assertTrue(config.getConstants().isEmpty());
    }

    @Test
    public void validateComplexConstants() throws Exception {
        CodelessLoadGeneratorConfig config = new ObjectMapper().readValue(
                "{\"constants\":{\"a\":5,\"b\":\"ref - ${a}\"}}",
                CodelessLoadGeneratorConfig.class
        );

        assertNotNull(config);
        assertNotNull(config.getConstants());
        assertFalse(config.getConstants().isEmpty());
        assertEquals(2, config.getConstants().size());
        assertEquals("5", config.getConstants().get("a"));
        assertEquals("ref - 5", config.getConstants().get("b"));
    }

}
