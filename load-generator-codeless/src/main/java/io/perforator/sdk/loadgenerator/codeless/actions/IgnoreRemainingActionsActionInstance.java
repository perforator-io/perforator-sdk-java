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

@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class IgnoreRemainingActionsActionInstance implements ActionInstance<IgnoreRemainingActionsActionConfig> {

    @FieldNameConstants.Include
    private final IgnoreRemainingActionsActionConfig config;

    @FieldNameConstants.Include
    private final boolean enabled;

    @Override
    public String toLoggingDetails() {
        return new StringBuilder()
                .append(IgnoreRemainingActionsActionInstance.Fields.enabled).append(" = ").append(enabled)
                .toString();
    }
}
