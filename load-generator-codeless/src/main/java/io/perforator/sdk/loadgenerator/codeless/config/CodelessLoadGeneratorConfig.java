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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@ToString
@FieldNameConstants
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodelessLoadGeneratorConfig extends LoadGeneratorConfig {

    @JsonDeserialize(using = ConstantsDeserializer.class)
    @Getter
    @Setter
    @FieldNameConstants.Include
    private FormattingMap constants = FormattingMap.EMPTY;

    public CodelessLoadGeneratorConfig() {
        applyDefaults();
    }

    public CodelessLoadGeneratorConfig(Function<String, String>... defaultsProviders) {
        applyDefaults(defaultsProviders);
    }

    public static class ConstantsDeserializer extends JsonDeserializer<FormattingMap> {

        @Override
        public FormattingMap deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
            ObjectCodec oc = jp.getCodec();

            if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                LinkedHashMap<String, String> item = oc.readValue(
                        jp,
                        new TypeReference<LinkedHashMap<String, String>>() {}
                );

                if (item == null || item.isEmpty()) {
                    return FormattingMap.EMPTY;
                }

                return new FormattingMap(item);
            }

            throw JsonMappingException.from(
                    dc,
                    "'constants' should be an object"
            );
        }

    }

}
