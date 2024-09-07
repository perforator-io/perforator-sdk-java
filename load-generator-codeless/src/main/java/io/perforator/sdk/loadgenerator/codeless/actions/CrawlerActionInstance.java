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

import io.perforator.sdk.loadgenerator.codeless.RandomDuration;
import java.time.Duration;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class CrawlerActionInstance implements ActionInstance<CrawlerActionConfig> {

    @FieldNameConstants.Include
    private final CrawlerActionConfig config;

    @FieldNameConstants.Include
    private final String url;

    @FieldNameConstants.Include
    private final List<String> domains;

    @FieldNameConstants.Include
    private final String linksExtractorScript;

    @FieldNameConstants.Include
    private final boolean randomize;

    @FieldNameConstants.Include
    private final RandomDuration delay;

    @FieldNameConstants.Include
    private final int maxVisitsPerUrl;

    @FieldNameConstants.Include
    private final int maxVisitsOverall;
    
    @FieldNameConstants.Include
    private final int maxQueueSize;

    @FieldNameConstants.Include
    private final Duration maxDuration;
    
    @FieldNameConstants.Include
    private final Duration pageLoadTimeout;
    
    @FieldNameConstants.Include
    private final Duration scriptTimeout;

    @FieldNameConstants.Include
    private final boolean enabled;

    @Override
    public String toLoggingDetails() {
        return new StringBuilder()
                .append(CrawlerActionInstance.Fields.url).append(" = ").append(url)
                .append(", ")
                .append(CrawlerActionInstance.Fields.domains).append(" = ").append(domains == null ? "" : "[" + String.join(",", domains) + "]")
                .append(", ")
                .append(CrawlerActionInstance.Fields.linksExtractorScript).append(" = ").append(linksExtractorScript)
                .append(", ")
                .append(CrawlerActionInstance.Fields.randomize).append(" = ").append(randomize)
                .append(", ")
                .append(CrawlerActionInstance.Fields.delay).append(" = ").append(delay)
                .append(", ")
                .append(CrawlerActionInstance.Fields.maxVisitsPerUrl).append(" = ").append(maxVisitsPerUrl)
                .append(", ")
                .append(CrawlerActionInstance.Fields.maxVisitsOverall).append(" = ").append(maxVisitsOverall)
                .append(", ")
                .append(CrawlerActionInstance.Fields.maxQueueSize).append(" = ").append(maxQueueSize)
                .append(", ")
                .append(CrawlerActionInstance.Fields.maxDuration).append(" = ").append(maxDuration)
                .append(", ")
                .append(CrawlerActionInstance.Fields.pageLoadTimeout).append(" = ").append(pageLoadTimeout)
                .append(", ")
                .append(CrawlerActionInstance.Fields.scriptTimeout).append(" = ").append(scriptTimeout)
                .toString();
    }
}
