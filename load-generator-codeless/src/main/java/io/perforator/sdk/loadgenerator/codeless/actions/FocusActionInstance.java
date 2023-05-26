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
package io.perforator.sdk.loadgenerator.codeless.actions;

import io.perforator.sdk.loadgenerator.codeless.config.SelectorType;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Duration;

@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class FocusActionInstance implements SelectorActionInstance<FocusActionConfig> {

    @FieldNameConstants.Include
    private final FocusActionConfig config;

    @FieldNameConstants.Include
    private final String selector;

    @FieldNameConstants.Include
    private final SelectorType selectorType;

    @FieldNameConstants.Include
    private final Duration timeout;
    
    @FieldNameConstants.Include
    private final boolean enabled;

    @Override
    public String toLoggingDetails() {
        return new StringBuilder()
                .append(selectorType).append(" = ").append(selectorType)
                .append(", ")
                .append(selector).append(" = ").append(selector)
                .append(", ")
                .append(Fields.timeout).append(" = ").append(timeout)
                .toString();
    }

}