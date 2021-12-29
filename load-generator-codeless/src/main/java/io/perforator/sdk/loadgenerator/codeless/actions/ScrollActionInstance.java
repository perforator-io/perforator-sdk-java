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
public class ScrollActionInstance implements ActionInstance<ScrollActionConfig> {

    @FieldNameConstants.Include
    private final ScrollActionConfig config;

    @FieldNameConstants.Include
    private final String cssSelector;

    @FieldNameConstants.Include
    private final Duration timeout;

    @Override
    public String toLoggingDetails() {
        return new StringBuilder()
                .append(ScrollActionInstance.Fields.cssSelector).append(" = ").append(cssSelector)
                .append(", ")
                .append(ScrollActionInstance.Fields.timeout).append(" = ").append(timeout)
                .toString();
    }

}