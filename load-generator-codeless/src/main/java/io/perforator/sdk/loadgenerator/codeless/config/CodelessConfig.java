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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.perforator.sdk.loadgenerator.core.configs.Config;
import io.perforator.sdk.loadgenerator.core.configs.ConfigBuilder;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@FieldNameConstants
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodelessConfig implements Config {

    public static final String DEFAULTS_FIELD_PREFIX = "codelessConfig";

    @Default
    String id = UUID.randomUUID().toString();

    @JsonProperty(LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX)
    CodelessLoadGeneratorConfig loadGeneratorConfig;

    @JsonProperty("suites")
    @JsonDeserialize(using = SuitesDeserializer.class)
    @Singular
    List<CodelessSuiteConfig> suiteConfigs;

    public static abstract class CodelessConfigBuilder<C extends CodelessConfig, B extends CodelessConfigBuilder<C, B>> implements ConfigBuilder<C, B> {

        @Override
        public String getDefaultsPrefix() {
            return DEFAULTS_FIELD_PREFIX;
        }

    }

    public static class SuitesDeserializer extends JsonDeserializer<List<CodelessSuiteConfig>> {

        @Override
        public List<CodelessSuiteConfig> deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
            if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                List<CodelessSuiteConfig> result = new ArrayList<>();
                LinkedHashMap<String, CodelessSuiteConfig> mappedConfigs = jp.readValueAs(
                        new TypeReference<LinkedHashMap<String, CodelessSuiteConfig>>() {}
                );

                for (String suiteName : mappedConfigs.keySet()) {
                    CodelessSuiteConfig suite = mappedConfigs.get(suiteName);
                    result.add(
                            suite.toBuilder().name(suiteName).build()
                    );
                }

                return result;
            }

            if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
                return jp.readValueAs(
                        new TypeReference<List<CodelessSuiteConfig>>() {}
                );
            }

            throw new RuntimeException(
                    "suites should be either object or array"
            );
        }

    }

}
