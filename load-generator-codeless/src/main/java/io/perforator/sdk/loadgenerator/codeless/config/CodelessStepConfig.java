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

import io.perforator.sdk.loadgenerator.codeless.actions.ActionConfig;
import io.perforator.sdk.loadgenerator.core.configs.Configurable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ToString
@FieldNameConstants
public class CodelessStepConfig implements Configurable {

    public static final String DEFAULTS_PREFIX = "step";

    @Getter
    @Setter
    @FieldNameConstants.Include
    private String name;

    @Getter
    @Setter
    @FieldNameConstants.Include
    private List<ActionConfig> actions = new ArrayList<>();

    public CodelessStepConfig() {
        applyDefaults();
    }

    public CodelessStepConfig(Function<String, String>... defaultsProviders) {
        applyDefaults(defaultsProviders);
    }

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

}
