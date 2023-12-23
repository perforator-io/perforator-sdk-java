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
import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.core.configs.StringConverter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
        CodelessConfig codelessConfig = objectMapper.readValue(
                configPath.toFile(),
                CodelessConfig.class
        );
        
        List<CodelessSuiteConfig> suiteConfigs = codelessConfig.getSuiteConfigs();
        CodelessLoadGeneratorConfig loadGeneratorConfig = codelessConfig.getLoadGeneratorConfig();
        
        if(loadGeneratorConfig == null) {
            loadGeneratorConfig = CodelessLoadGeneratorConfig.builder().buildWithDefaults();
        } else {
            loadGeneratorConfig = loadGeneratorConfig.toBuilder().buildWithDefaults();
        }
        
        if (suiteConfigs != null && !suiteConfigs.isEmpty()) {
            suiteConfigs = new ArrayList<>();
            for (CodelessSuiteConfig suiteConfig : codelessConfig.getSuiteConfigs()) {
                List<FormattingMap> formatters = buildFormatters(suiteConfig, codelessConfig.getVariables());
                
                suiteConfigs.add(
                        suiteConfig.toBuilder().props(formatters).buildWithDefaults(
                                System::getProperty,
                                System::getenv,
                                codelessConfig.getVariables()::get
                        )
                );
            }
        }
        
        return CodelessConfig.builder()
                .loadGeneratorConfig(loadGeneratorConfig)
                .suiteConfigs(suiteConfigs)
                .build();
    }
    
    private static List<FormattingMap> buildFormatters(CodelessSuiteConfig suite, FormattingMap constants) {
        if(suite == null) {
            return null;
        }
        
        List<FormattingMap> formattersFromProps = suite.getProps();
        List<FormattingMap> formattersFromFile = CSVUtils.parseToFormattingMapList(suite.getPropsFile());
        List<FormattingMap> formattersToReturn = new ArrayList<>();
        
        if(formattersFromProps != null && !formattersFromProps.isEmpty()) {
            for (FormattingMap formatter : formattersFromProps) {
                if(constants != null && !constants.isEmpty()) {
                    formattersToReturn.add(new FormattingMap(constants, formatter));
                } else {
                    formattersToReturn.add(formatter);
                }
            }
        }
        
        if(formattersFromFile != null && !formattersFromFile.isEmpty()) {
            for (FormattingMap formatter : formattersFromFile) {
                if(constants != null && !constants.isEmpty()) {
                    formattersToReturn.add(new FormattingMap(constants, formatter));
                } else {
                    formattersToReturn.add(formatter);
                }
            }
        }
        
        if(formattersToReturn.isEmpty() && constants != null && !constants.isEmpty()) {
            formattersToReturn.add(constants);
        }
        
        return formattersToReturn;
    }

    private static class CustomDurationDeserializer extends DurationDeserializer {

        @Override
        protected Duration _fromString(JsonParser parser, DeserializationContext ctxt, String value0) throws IOException {
            if (value0 == null || value0.isBlank()) {
                return null;
            }
            try {
                return StringConverter.toDuration(value0);
            } catch (DateTimeException e) {
                return _handleDateTimeException(ctxt, e, value0);
            }
        }
    }
}
