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
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@FieldNameConstants
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodelessLoadGeneratorConfig extends LoadGeneratorConfig {
    
    @Default
    String id = UUID.randomUUID().toString();
    
    public static abstract class CodelessLoadGeneratorConfigBuilder<C extends CodelessLoadGeneratorConfig, B extends CodelessLoadGeneratorConfigBuilder<C, B>> extends LoadGeneratorConfigBuilder<C, B> {

        @Override
        public String getDefaultsPrefix() {
            return DEFAULTS_FIELD_PREFIX;
        }

    }

}
