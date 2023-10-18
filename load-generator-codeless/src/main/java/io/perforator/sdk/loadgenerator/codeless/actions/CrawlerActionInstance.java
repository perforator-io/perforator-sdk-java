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
import java.util.List;

@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class CrawlerActionInstance implements ActionInstance<CrawlerActionConfig> {

    @FieldNameConstants.Include
    private final CrawlerActionConfig config;

    @FieldNameConstants.Include
    private final List<String> urls;

    @FieldNameConstants.Include
    private final List<String> domains;

    @FieldNameConstants.Include
    private final String linksSelector;

    @FieldNameConstants.Include
    private final boolean randomizeVisits;

    @FieldNameConstants.Include
    private final Duration pageAnalysisDelay;

    @FieldNameConstants.Include
    private final Duration linkVisitorDelay;

    @FieldNameConstants.Include
    private final int maxVisitsPerLink;

    @FieldNameConstants.Include
    private final int maxPages;

    @FieldNameConstants.Include
    private final Duration maxDuration;

    @FieldNameConstants.Include
    private final boolean enabled;

    @Override
    public String toLoggingDetails() {
        return new StringBuilder()
                .append(CrawlerActionInstance.Fields.urls).append(" = ").append(urls == null ? "" : "[" + String.join(",", urls) + "]")
                .append(", ")
                .append(CrawlerActionInstance.Fields.domains).append(" = ").append(domains == null ? "" : "[" + String.join(",", domains) + "]")
                .append(", ")
                .append(CrawlerActionInstance.Fields.linksSelector).append(" = ").append(linksSelector)
                .append(", ")
                .append(CrawlerActionInstance.Fields.randomizeVisits).append(" = ").append(randomizeVisits)
                .append(", ")
                .append(CrawlerActionInstance.Fields.pageAnalysisDelay).append(" = ").append(pageAnalysisDelay)
                .append(", ")
                .append(CrawlerActionInstance.Fields.linkVisitorDelay).append(" = ").append(linkVisitorDelay)
                .append(", ")
                .append(CrawlerActionInstance.Fields.maxVisitsPerLink).append(" = ").append(maxVisitsPerLink)
                .append(", ")
                .append(CrawlerActionInstance.Fields.maxPages).append(" = ").append(maxPages)
                .append(", ")
                .append(CrawlerActionInstance.Fields.maxDuration).append(" = ").append(maxDuration)
                .toString();
    }
}
