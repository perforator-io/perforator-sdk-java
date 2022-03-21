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

import io.perforator.sdk.loadgenerator.core.configs.Configurable;
import java.time.Duration;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class OpenActionConfig implements ActionConfig {

    public static final String DEFAULT_ACTION_NAME = "open";
    public static final String DEFAULT_TIMEOUT = "30s";
    public static final Duration DEFAULT_TIMEOUT_AS_DURATION = Configurable.parseDuration(DEFAULT_TIMEOUT);

    @FieldNameConstants.Include
    private final String actionName = DEFAULT_ACTION_NAME;

    @FieldNameConstants.Include
    private final String url;

    @FieldNameConstants.Include
    private final String timeout;

}
