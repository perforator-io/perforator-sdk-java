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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.function.Function;

@ToString
@FieldNameConstants
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodelessLoadGeneratorConfig extends LoadGeneratorConfig {

    public static final String DEFAULT_LOG_STEPS_S = "false";
    public static final boolean DEFAULT_LOG_STEPS = Boolean.parseBoolean(DEFAULT_LOG_STEPS_S);

    public static final String DEFAULT_LOG_ACTIONS_S = "false";
    public static final boolean DEFAULT_LOG_ACTIONS = Boolean.parseBoolean(DEFAULT_LOG_ACTIONS_S);

    @Getter
    @Setter
    @FieldNameConstants.Include
    private boolean logSteps = DEFAULT_LOG_STEPS;

    @Getter
    @Setter
    @FieldNameConstants.Include
    private boolean logActions = DEFAULT_LOG_ACTIONS;

    public CodelessLoadGeneratorConfig() {
        applyDefaults();
    }

    public CodelessLoadGeneratorConfig(Function<String, String>... defaultsProviders) {
        applyDefaults(defaultsProviders);
    }

}
