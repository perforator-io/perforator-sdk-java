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

import java.util.List;

@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class CrawlerActionConfig implements ActionConfig {

    public static final String DEFAULT_ACTION_NAME = "crawler";

    @FieldNameConstants.Include
    private final String actionName = DEFAULT_ACTION_NAME;

    @FieldNameConstants.Include
    private final List<String> urls;

    @FieldNameConstants.Include
    private final List<String> domains;

    @FieldNameConstants.Include
    private final String linksSelector;

    @FieldNameConstants.Include
    private final String randomizeVisits;

    @FieldNameConstants.Include
    private final String pageAnalysisDelay;

    @FieldNameConstants.Include
    private final String linkVisitorDelay;

    @FieldNameConstants.Include
    private final String maxVisitsPerLink;

    @FieldNameConstants.Include
    private final String maxPages;

    @FieldNameConstants.Include
    private final String maxDuration;

    @FieldNameConstants.Include
    private final String enabled;
}
