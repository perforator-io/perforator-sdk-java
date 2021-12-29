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

import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Duration;

@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class AwaitElementToBeEnabledActionInstance implements ActionInstance<AwaitElementToBeEnabledActionConfig> {

    @FieldNameConstants.Include
    private final AwaitElementToBeEnabledActionConfig config;

    @FieldNameConstants.Include
    private final String cssSelector;

    @FieldNameConstants.Include
    private final Duration timeout;

    @Override
    public String toLoggingDetails() {
        return new StringBuilder()
                .append(AwaitElementToBeEnabledActionInstance.Fields.cssSelector).append(" = ").append(cssSelector)
                .append(", ")
                .append(AwaitElementToBeEnabledActionInstance.Fields.timeout).append(" = ").append(timeout)
                .toString();
    }
}