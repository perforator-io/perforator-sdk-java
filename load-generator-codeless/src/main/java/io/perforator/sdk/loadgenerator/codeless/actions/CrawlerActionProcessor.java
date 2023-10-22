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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.service.AutoService;
import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import io.perforator.sdk.loadgenerator.core.Perforator;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.openqa.selenium.remote.RemoteWebDriver;

@SuppressWarnings("rawtypes")
@AutoService(ActionProcessor.class)
public class CrawlerActionProcessor extends AbstractActionProcessor<CrawlerActionConfig, CrawlerActionInstance> {

    private static final String DOMAIN_REGEX = "^((?!-)[a-zA-Z0-9@:%._\\+~#?&//=]{1,256}(?<!-)\\.)+[a-z]{2,6}";
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(DOMAIN_REGEX);
    
    public static final String DEFAULT_RANDOMIZE = "true";
    public static final String DEFAULT_MAX_VISITS_OVERALL = "1024";
    public static final String DEFAULT_MAX_VISITS_PER_URL = "1";
    public static final String DEFAULT_MAX_QUEUE_SIZE = "4096";
    public static final String DEFAULT_MAX_DURATION = "5m";
    public static final String DEFAULT_DELAY = "5s";
    
    public static final String DEFAULT_LINKS_EXTRACTOR_SCRIPT = ""
            + "const result = [];"
            + "const links = document.querySelectorAll(\"a[href]:not([href^='javascript']):not([href^='void']):not([href='#'])\");"
            + "for(var i=0; i < links.length; i++){"
            + "    result.push(links[i].href);"
            + "}"
            + "return result;";

    public CrawlerActionProcessor() {
        super(CrawlerActionConfig.DEFAULT_ACTION_NAME);
    }

    @Override
    public void validateActionConfig(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, CrawlerActionConfig actionConfig) {
        super.validateActionConfig(loadGeneratorConfig, suiteConfig, actionConfig);

        List<FormattingMap> formatters;
        if (suiteConfig.getProps() == null || suiteConfig.getProps().isEmpty()) {
            formatters = List.of(FormattingMap.EMPTY);
        } else {
            formatters = suiteConfig.getProps();
        }

        for (FormattingMap formatter : formatters) {
            List<String> urls = buildStringListForActionInstance(
                    CrawlerActionConfig.Fields.urls,
                    actionConfig.getUrls(),
                    formatter,
                    false
            );
            if (urls != null) {
                for (String url : urls) {
                    if (url == null || url.isBlank()) {
                        continue;
                    }

                    try {
                        new URI(url.trim()).toURL();
                    } catch (URISyntaxException | MalformedURLException e) {
                        throw new RuntimeException(
                                "Action '" + actionConfig.getActionName() + "' has invalid url => " + url
                        );
                    }
                }
            }

            List<String> domains = buildStringListForActionInstance(
                    CrawlerActionConfig.Fields.domains,
                    actionConfig.getDomains(),
                    formatter,
                    false
            );

            if (domains != null) {
                for (String domain : domains) {
                    if (domain == null || domain.isBlank()) {
                        continue;
                    }

                    if (!DOMAIN_PATTERN.matcher(domain.trim()).matches()) {
                        throw new RuntimeException(
                                "Action '" + actionConfig.getActionName() + "' has invalid domain => " + domain
                        );
                    }
                }
            }
        }
    }

    @Override
    public CrawlerActionConfig buildActionConfig(String actionName, JsonNode actionValue) {
        return CrawlerActionConfig.builder()
                .urls(
                        getOptionalNestedFields(
                                CrawlerActionConfig.Fields.urls,
                                actionValue,
                                null
                        )
                )
                .domains(
                        getOptionalNestedFields(
                                CrawlerActionConfig.Fields.domains,
                                actionValue,
                                null
                        )
                )
                .linksExtractorScript(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.linksExtractorScript,
                                actionValue,
                                DEFAULT_LINKS_EXTRACTOR_SCRIPT
                        )
                )
                .randomize(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.randomize,
                                actionValue,
                                DEFAULT_RANDOMIZE
                        )
                )
                .delay(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.delay,
                                actionValue,
                                DEFAULT_DELAY
                        )
                )
                .maxVisitsPerUrl(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.maxVisitsPerUrl,
                                actionValue,
                                DEFAULT_MAX_VISITS_PER_URL
                        )
                )
                .maxVisitsOverall(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.maxVisitsOverall,
                                actionValue,
                                DEFAULT_MAX_VISITS_OVERALL
                        )
                )
                .maxQueueSize(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.maxQueueSize,
                                actionValue,
                                DEFAULT_MAX_QUEUE_SIZE
                        )
                )
                .maxDuration(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.maxDuration,
                                actionValue,
                                DEFAULT_MAX_DURATION
                        )
                )
                .enabled(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.enabled,
                                actionValue,
                                "true"
                        )
                )
                .build();
    }

    @Override
    public CrawlerActionInstance buildActionInstance(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, FormattingMap formatter, CrawlerActionConfig actionConfig) {
        return CrawlerActionInstance.builder()
                .config(
                        actionConfig
                )
                .urls(
                        buildStringListForActionInstance(
                                CrawlerActionInstance.Fields.urls,
                                actionConfig.getUrls(),
                                formatter,
                                false
                        )
                )
                .domains(
                        buildStringListForActionInstance(
                                CrawlerActionInstance.Fields.domains,
                                actionConfig.getDomains(),
                                formatter,
                                false
                        )
                )
                .linksExtractorScript(
                        buildStringForActionInstance(
                                CrawlerActionInstance.Fields.linksExtractorScript,
                                actionConfig.getLinksExtractorScript(),
                                formatter
                        )
                )
                .randomize(
                        buildBooleanForActionInstance(
                                CrawlerActionInstance.Fields.randomize,
                                actionConfig.getRandomize(),
                                formatter
                        )
                )
                .delay(
                        buildRandomDurationForActionInstance(
                                CrawlerActionInstance.Fields.delay,
                                actionConfig.getDelay(),
                                formatter
                        ).random()
                )
                .maxVisitsPerUrl(
                        buildIntegerForActionInstance(
                                CrawlerActionInstance.Fields.maxVisitsPerUrl,
                                actionConfig.getMaxVisitsPerUrl(),
                                formatter
                        )
                )
                .maxVisitsOverall(
                        buildIntegerForActionInstance(
                                CrawlerActionInstance.Fields.maxVisitsOverall,
                                actionConfig.getMaxVisitsOverall(),
                                formatter
                        )
                )
                .maxQueueSize(
                        buildIntegerForActionInstance(
                                CrawlerActionInstance.Fields.maxQueueSize,
                                actionConfig.getMaxQueueSize(),
                                formatter
                        )
                )
                .maxDuration(
                        buildRandomDurationForActionInstance(
                                CrawlerActionInstance.Fields.maxDuration,
                                actionConfig.getMaxDuration(),
                                formatter
                        ).random()
                )
                .enabled(
                        buildEnabledForActionInstance(
                                CrawlerActionInstance.Fields.enabled,
                                actionConfig.getEnabled(),
                                formatter
                        )
                )
                .build();
    }

    @Override
    public void processActionInstance(RemoteWebDriver driver, CrawlerActionInstance actionInstance) {
        long endTime = System.currentTimeMillis() + actionInstance.getMaxDuration().toMillis();
        
        List<String> urls = new ArrayList<>();
        if (actionInstance.getUrls() == null || actionInstance.getUrls().isEmpty()) {
            Perforator.sleep(actionInstance.getDelay().toMillis());
            urls.addAll(
                    collectUrlsFromThePage(
                            driver,
                            actionInstance.getLinksExtractorScript()
                    )
            );
        } else {
            urls.addAll(actionInstance.getUrls());
        }

        Set<String> domains = new HashSet<>();
        if (actionInstance.getDomains() == null || actionInstance.getDomains().isEmpty()) {
            for (String url : urls) {
                try {
                    String domain = new URI(url).toURL().getHost();
                    if (domain != null && !domain.isBlank()) {
                        domains.add(domain);
                    }
                } catch (URISyntaxException | MalformedURLException e) {
                    //ignore
                }
            }
        } else {
            domains.addAll(actionInstance.getDomains());
        }

        CrawlerQueue crawlerQueue = CrawlerQueue.newInstance(
                domains,
                actionInstance.isRandomize(),
                actionInstance.getMaxQueueSize(),
                actionInstance.getMaxVisitsOverall(),
                actionInstance.getMaxVisitsPerUrl()
        );
        crawlerQueue.pushAll(urls);

        String url;
        while ((url = crawlerQueue.poll()) != null) {
            if (endTime <= System.currentTimeMillis()) {
                crawlerQueue.destroy();
                return;
            }

            driver.navigate().to(url);
            Perforator.sleep(actionInstance.getDelay().toMillis());

            if (endTime <= System.currentTimeMillis()) {
                crawlerQueue.destroy();
                return;
            }

            crawlerQueue.pushAll(
                    collectUrlsFromThePage(
                            driver,
                            actionInstance.getLinksExtractorScript()
                    )
            );
        }
    }

    private Set<String> collectUrlsFromThePage(
            RemoteWebDriver driver,
            String linksExtractorScript
    ) {
        Object scriptResult = driver.executeScript(linksExtractorScript);
        
        if(scriptResult == null) {
            return Collections.EMPTY_SET;
        } else if(scriptResult.getClass() == String.class) {
            return Set.of(scriptResult.toString());
        } else if(scriptResult instanceof Collection) {
            Collection items = (Collection)scriptResult;
            
            if(items.isEmpty()) {
                return Collections.EMPTY_SET;
            }
            
            Set<String> result = new LinkedHashSet<>();
            for (Object item : items) {
                if(item == null) {
                    continue;
                }
                
                result.add(item.toString().trim());
            }
            
            return result;
        }
        
        return Collections.EMPTY_SET;
    }

}
