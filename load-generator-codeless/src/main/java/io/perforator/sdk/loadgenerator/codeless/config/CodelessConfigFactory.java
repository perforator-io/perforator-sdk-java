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
package io.perforator.sdk.loadgenerator.codeless.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;
import io.perforator.sdk.loadgenerator.core.configs.Configurable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Duration;

//TODO: add javadoc
public final class CodelessConfigFactory {

    public static final CodelessConfigFactory INSTANCE = new CodelessConfigFactory();

    private final ObjectMapper objectMapper;

    private CodelessConfigFactory() {
        this.objectMapper = new ObjectMapper(new YAMLFactory());

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(Duration.class, new CustomDurationDeserializer());
        objectMapper.registerModule(javaTimeModule);
    }

    public CodelessConfig getCodelessConfig(Path configPath) throws IOException {
        CodelessConfig result = objectMapper.readValue(
                configPath.toFile(),
                CodelessConfig.class
        );
        
        if(result.getLoadGeneratorConfig() != null && result.getLoadGeneratorConfig().isPrioritizeSystemProperties()) {
            result.getLoadGeneratorConfig().applyDefaults();
            
            if (result.getSuiteConfigs() != null && !result.getSuiteConfigs().isEmpty()) {
                result.getSuiteConfigs().forEach(
                        CodelessSuiteConfig::applyDefaults
                );
            }
        }
        
        return result;
    }

    private static class CustomDurationDeserializer extends DurationDeserializer {

        @Override
        protected Duration _fromString(JsonParser parser, DeserializationContext ctxt, String value0) throws IOException {
            if (value0 == null || value0.isBlank()) {
                return null;
            }
            try {
                return Configurable.parseDuration(value0);
            } catch (DateTimeException e) {
                return _handleDateTimeException(ctxt, e, value0);
            }
        }
    }

}
