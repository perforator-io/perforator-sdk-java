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
    private final String url;

    @FieldNameConstants.Include
    private final List<String> domains;

    @FieldNameConstants.Include
    private final String linksExtractorScript;

    @FieldNameConstants.Include
    private final String randomize;
    
    @FieldNameConstants.Include
    private final String scroll;
    
    @FieldNameConstants.Include
    private final String scrollScript;
    
    @FieldNameConstants.Include
    private final String scrollDelay;
    
    @FieldNameConstants.Include
    private final String click;
    
    @FieldNameConstants.Include
    private final String clickScript;
    
    @FieldNameConstants.Include
    private final String clickDelay;
    
    @FieldNameConstants.Include
    private final String delay;

    @FieldNameConstants.Include
    private final String maxVisitsPerUrl;

    @FieldNameConstants.Include
    private final String maxVisitsOverall;
    
    @FieldNameConstants.Include
    private final String maxQueueSize;

    @FieldNameConstants.Include
    private final String maxDuration;
    
    @FieldNameConstants.Include
    private final String pageLoadTimeout;
    
    @FieldNameConstants.Include
    private final String scriptTimeout;

    @FieldNameConstants.Include
    private final String enabled;
}
