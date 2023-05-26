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
public class ClickActionConfig implements SelectorActionConfig {

    public static final String DEFAULT_ACTION_NAME = "click";

    @FieldNameConstants.Include
    private final String actionName = DEFAULT_ACTION_NAME;

    @FieldNameConstants.Include
    private final String selector;

    @FieldNameConstants.Include
    private final String cssSelector;

    @FieldNameConstants.Include
    private final String xpathSelector;

    @FieldNameConstants.Include
    private final String timeout;
    
    @FieldNameConstants.Include
    private final String enabled;

}
