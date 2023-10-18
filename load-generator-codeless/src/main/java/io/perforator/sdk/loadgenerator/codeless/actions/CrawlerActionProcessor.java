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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
@AutoService(ActionProcessor.class)
public class CrawlerActionProcessor extends AbstractActionProcessor<CrawlerActionConfig, CrawlerActionInstance> {

    private final static String DOMAIN_REGEX = "^((?!-)[a-zA-Z0-9@:%._\\+~#?&//=]{1,256}(?<!-)\\.)+[a-z]{2,6}";

    public static final String DEFAULT_LINKS_SELECTOR = "a[href]:not([href^='javascript']):not([href^='void']):not([href='#'])";
    public static final String DEFAULT_RANDOMIZE_VISITS = "true";
    public static final String DEFAULT_PAGE_ANALYSIS_DELAY = "5s";
    public static final String DEFAULT_MAX_DURATION = "15m";
    public static final String DEFAULT_MAX_PAGES = "1000";
    public static final String DEFAULT_MAX_VISITS_PER_LINK = "1";
    public static final String DEFAULT_LINK_VISITOR_DELAY = "5s-10s";

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
                    boolean urlIsValid = isValidUrl(url);
                    if (!urlIsValid) {
                        throw new RuntimeException(
                                "Action '" + actionConfig.getActionName() + "' should have a valid all values in the 'urls' parameter. " +
                                        "Url " + url + " is invalid!"
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
                    boolean domainIsValid = isValidDomain(domain);
                    if (!domainIsValid) {
                        throw new RuntimeException(
                                "Action '" + actionConfig.getActionName() + "' should have a valid all values in the 'domains' parameter.  " +
                                        "Domain " + domain + " is invalid!"
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
                .linksSelector(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.linksSelector,
                                actionValue,
                                DEFAULT_LINKS_SELECTOR
                        )
                )
                .randomizeVisits(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.randomizeVisits,
                                actionValue,
                                DEFAULT_RANDOMIZE_VISITS
                        )
                )
                .pageAnalysisDelay(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.pageAnalysisDelay,
                                actionValue,
                                DEFAULT_PAGE_ANALYSIS_DELAY
                        )
                )
                .linkVisitorDelay(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.linkVisitorDelay,
                                actionValue,
                                DEFAULT_LINK_VISITOR_DELAY
                        )
                )
                .maxVisitsPerLink(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.maxVisitsPerLink,
                                actionValue,
                                DEFAULT_MAX_VISITS_PER_LINK
                        )
                )
                .maxPages(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.maxPages,
                                actionValue,
                                DEFAULT_MAX_PAGES
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
                .linksSelector(
                        buildStringForActionInstance(
                                CrawlerActionInstance.Fields.linksSelector,
                                actionConfig.getLinksSelector(),
                                formatter
                        )
                )
                .randomizeVisits(
                        buildBooleanForActionInstance(
                                CrawlerActionInstance.Fields.randomizeVisits,
                                actionConfig.getRandomizeVisits(),
                                formatter
                        )
                )
                .pageAnalysisDelay(
                        buildDurationForActionInstance(
                                CrawlerActionInstance.Fields.pageAnalysisDelay,
                                actionConfig.getPageAnalysisDelay(),
                                formatter
                        )
                )
                .linkVisitorDelay(
                        buildRandomDurationForActionInstance(
                                CrawlerActionInstance.Fields.linkVisitorDelay,
                                actionConfig.getLinkVisitorDelay(),
                                formatter
                        ).random()
                )
                .maxVisitsPerLink(
                        buildIntegerForActionInstance(
                                CrawlerActionInstance.Fields.maxVisitsPerLink,
                                actionConfig.getMaxVisitsPerLink(),
                                formatter
                        )
                )
                .maxPages(
                        buildIntegerForActionInstance(
                                CrawlerActionInstance.Fields.maxPages,
                                actionConfig.getMaxPages(),
                                formatter
                        )
                )
                .maxDuration(
                        buildDurationForActionInstance(
                                CrawlerActionInstance.Fields.maxDuration,
                                actionConfig.getMaxDuration(),
                                formatter
                        )
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
        List<String> preparedStartingUrls = prepareStartingUrls(
                driver,
                actionInstance.getUrls()
        );

        List<String> preparedAllowedDomains = prepareAllowedDomains(
                preparedStartingUrls,
                actionInstance.getDomains()
        );

        Queue<String> notVisitedUrls = new LinkedList<>(preparedStartingUrls);
        crawlPages(
                driver,
                notVisitedUrls,
                preparedAllowedDomains,
                actionInstance
        );
    }

    private void crawlPages(
            RemoteWebDriver driver,
            Queue<String> notVisitedUrls,
            List<String> allowedDomains,
            CrawlerActionInstance actionInstance
    ) {
        long startProcessTimestamp = System.currentTimeMillis();
        int visitsPageCount = 0;
        Map<String, Integer> openedPageHistory = new HashMap<>();

        while (!notVisitedUrls.isEmpty()) {
            if (startProcessTimestamp + actionInstance.getMaxDuration().toMillis() < System.currentTimeMillis()) {
                return;
            }

            if (visitsPageCount >= actionInstance.getMaxPages()) {
                return;
            }

            String url = notVisitedUrls.poll();
            openedPageHistory.putIfAbsent(url, 0);
            if (openedPageHistory.get(url) >= actionInstance.getMaxVisitsPerLink()) {
                continue;
            }

            List<String> collectedUrls = openPageAndCollectUrls(
                    driver,
                    url,
                    actionInstance.getPageAnalysisDelay(),
                    actionInstance.getLinkVisitorDelay(),
                    actionInstance.getLinksSelector()
            );
            openedPageHistory.merge(url, 1, Integer::sum);
            visitsPageCount++;

            List<String> preparedUrls = prepareUrlList(
                    collectedUrls,
                    allowedDomains,
                    actionInstance.isRandomizeVisits()
            );

            if (!preparedUrls.isEmpty()) {
                notVisitedUrls.addAll(preparedUrls);
            }
        }
    }

    private List<String> prepareStartingUrls(RemoteWebDriver driver, List<String> urls) {
        List<String> result = new ArrayList<>();
        if (urls == null) {
            result.add(driver.getCurrentUrl());
        } else {
            result.addAll(urls);
        }

        return result;
    }

    private List<String> prepareAllowedDomains(List<String> startingUrls, List<String> domains) {
        List<String> results = new ArrayList<>();

        if (domains == null) {
            for (String url : startingUrls) {
                String urlsHost = getHostFromUrl(url);
                if (urlsHost != null) {
                    results.add(urlsHost);
                }
            }
        } else {
            results.addAll(domains);
        }

        return results;
    }

    private List<String> prepareUrlList(
            List<String> urls,
            List<String> allowedDomain,
            boolean randomizeVisits
    ) {
        List<String> result = new ArrayList<>();

        for (String url : urls) {
            url = trimUrl(url);

            String urlHost = getHostFromUrl(url);
            if (urlHost == null) {
                continue;
            }
            if (allowedDomain.contains(urlHost)) {
                result.add(url);
            }
        }

        if (randomizeVisits) {
            Collections.shuffle(result);
        }

        return result;
    }

    private List<String> openPageAndCollectUrls(
            RemoteWebDriver driver,
            String url,
            Duration pageAnalysisDelay,
            Duration linkVisitorDelay,
            String linksSelector
    ) {
        Perforator.sleep(linkVisitorDelay.toMillis());

        driver.navigate().to(url);

        Perforator.sleep(pageAnalysisDelay.toMillis());

        List<String> results = new ArrayList<>();
        List<WebElement> linkElements = driver.findElements(By.cssSelector(linksSelector));
        for (WebElement webElement : linkElements) {
            String strUrl = webElement.getAttribute("href");
            if (isValidUrl(strUrl)) {
                results.add(strUrl);
            }
        }
        return results;
    }

    private String getHostFromUrl(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private boolean isValidDomain(String domain) {
        Pattern p = Pattern.compile(DOMAIN_REGEX);

        if (domain == null) {
            return false;
        }

        Matcher m = p.matcher(domain);

        return m.matches();
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private String trimUrl(String url) {
        return url.trim();
    }
}
