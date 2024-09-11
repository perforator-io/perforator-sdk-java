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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
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
    public static final String DEFAULT_SCROLL = "false";
    public static final String DEFAULT_SCROLL_DELAY = "5s";
    public static final String DEFAULT_CLICK = "false";
    public static final String DEFAULT_CLICK_DELAY = "5s";
    
    public static final String DEFAULT_SCROLL_SCRIPT = ""
            + "const domains=arguments[0];"
            + "window.scrollTo(0,document.documentElement.scrollHeight);";
    
    public static final String DEFAULT_CLICK_SCRIPT = ""
            + "const domains=arguments[0];"
            + "const result = [];"
            + "const links = document.querySelectorAll(\"a[href]:not([href^='javascript']):not([href^='void']):not([href^='#'])\");"
            + "const maxChecks = Math.min(links.length, 512);"
            + "for(var i=0; i < maxChecks; i++){"
            + "    if(links[i].checkVisibility({ opacityProperty: true, visibilityProperty: true, contentVisibilityAuto: true,})) {"
            + "        try {"
            + "            let url = new URL(links[i].href);"
            + "            for(var j=0; j < domains.length; j++){"
            + "                if(url.hostname === domains[j]) {"
            + "                    result.push(links[i]);"
            + "                }"
            + "            }"
            + "        } catch (error) {}"
            + "    }"
            + "}"
            + "return result;";
    
    public static final String DEFAULT_LINKS_EXTRACTOR_SCRIPT = ""
            + "const domains=arguments[0];"
            + "const result = [];"
            + "const links = document.querySelectorAll(\"a[href]:not([href^='javascript']):not([href^='void']):not([href='#'])\");"
            + "const maxChecks = Math.min(links.length, 512);"
            + "for(var i=0; i < maxChecks; i++){"
            + "    if(links[i].checkVisibility({ opacityProperty: true, visibilityProperty: true, contentVisibilityAuto: true,})) {"
            + "        try {"
            + "            let url = new URL(links[i].href);"
            + "            for(var j=0; j < domains.length; j++){"
            + "                if(url.hostname === domains[j]) {"
            + "                    result.push(links[i].href);"
            + "                }"
            + "            }"
            + "        } catch (error) {}"
            + "    }"
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
            String url = buildStringForActionInstance(
                    CrawlerActionConfig.Fields.url,
                    actionConfig.getUrl(),
                    formatter,
                    false
            );
            if (url != null && !url.isBlank()) {
                try {
                    new URI(url.trim()).toURL();
                } catch (URISyntaxException | MalformedURLException e) {
                    throw new RuntimeException(
                            "Action '" + actionConfig.getActionName() + "' has invalid url => " + url
                    );
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
                .url(
                        getOptionalValueOrNestedField(
                                CrawlerActionConfig.Fields.url,
                                actionValue
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
                .scroll(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.scroll,
                                actionValue,
                                DEFAULT_SCROLL
                        )
                )
                .scrollScript(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.scrollScript,
                                actionValue,
                                DEFAULT_SCROLL_SCRIPT
                        )
                )
                .scrollDelay(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.scrollDelay,
                                actionValue,
                                DEFAULT_SCROLL_DELAY
                        )
                )
                .click(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.click,
                                actionValue,
                                DEFAULT_CLICK
                        )
                )
                .clickScript(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.clickScript,
                                actionValue,
                                DEFAULT_CLICK_SCRIPT
                        )
                )
                .clickDelay(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.clickDelay,
                                actionValue,
                                DEFAULT_CLICK_DELAY
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
                .pageLoadTimeout(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.pageLoadTimeout,
                                actionValue,
                                null
                        )
                )
                .scriptTimeout(
                        getOptionalNestedField(
                                CrawlerActionConfig.Fields.scriptTimeout,
                                actionValue,
                                null
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
                .url(
                        buildStringForActionInstance(
                                CrawlerActionInstance.Fields.url,
                                actionConfig.getUrl(),
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
                .scroll(
                        buildBooleanForActionInstance(
                                CrawlerActionInstance.Fields.scroll,
                                actionConfig.getScroll(),
                                formatter
                        )
                )
                .scrollScript(
                        buildStringForActionInstance(
                                CrawlerActionInstance.Fields.scrollScript,
                                actionConfig.getScrollScript(),
                                formatter
                        )
                )
                .scrollDelay(
                        buildRandomDurationForActionInstance(
                                CrawlerActionInstance.Fields.scrollDelay,
                                actionConfig.getScrollDelay(),
                                formatter
                        )
                )
                .click(
                        buildBooleanForActionInstance(
                                CrawlerActionInstance.Fields.click,
                                actionConfig.getClick(),
                                formatter
                        )
                )
                .clickScript(
                        buildStringForActionInstance(
                                CrawlerActionInstance.Fields.clickScript,
                                actionConfig.getClickScript(),
                                formatter
                        )
                )
                .clickDelay(
                        buildRandomDurationForActionInstance(
                                CrawlerActionInstance.Fields.clickDelay,
                                actionConfig.getClickDelay(),
                                formatter
                        )
                )
                .delay(
                        buildRandomDurationForActionInstance(
                                CrawlerActionInstance.Fields.delay,
                                actionConfig.getDelay(),
                                formatter
                        )
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
                .pageLoadTimeout(
                        buildDurationForActionInstance(
                                CrawlerActionInstance.Fields.pageLoadTimeout,
                                actionConfig.getPageLoadTimeout(),
                                suiteConfig.getWebDriverSessionPageLoadTimeout(),
                                formatter,
                                false
                        )
                )
                .scriptTimeout(
                        buildDurationForActionInstance(
                                CrawlerActionInstance.Fields.scriptTimeout,
                                actionConfig.getScriptTimeout(),
                                suiteConfig.getWebDriverSessionScriptTimeout(),
                                formatter,
                                false
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
        long endTime = System.currentTimeMillis() + actionInstance.getMaxDuration().toMillis();

        if (actionInstance.getPageLoadTimeout() != null) {
            driver.manage().timeouts().pageLoadTimeout(
                    actionInstance.getPageLoadTimeout()
            );
        }

        if (actionInstance.getScriptTimeout() != null) {
            driver.manage().timeouts().scriptTimeout(
                    actionInstance.getScriptTimeout()
            );
        }

        String startingUrl;
        boolean skipNavigation;
        if (actionInstance.getUrl() == null || actionInstance.getUrl().isBlank()) {
            startingUrl = driver.getCurrentUrl();
            if (startingUrl == null || startingUrl.isBlank()) {
                throw new RuntimeException(
                        "Action '" + actionInstance.getConfig().getActionName() + "' can't be started - there is no page openned"
                );
            }
            skipNavigation = true;
        } else {
            startingUrl = actionInstance.getUrl();
            skipNavigation = false;
        }

        Set<String> domains = new HashSet<>();
        if (actionInstance.getDomains() == null || actionInstance.getDomains().isEmpty()) {
            try {
                String domain = new URI(startingUrl).toURL().getHost();
                if (domain != null && !domain.isBlank()) {
                    domains.add(domain);
                }
            } catch (URISyntaxException | MalformedURLException e) {
                throw new RuntimeException(
                        "Action '" + actionInstance.getConfig().getActionName() + "' can't be started - domain is invalid: " + startingUrl
                );
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
        crawlerQueue.push(startingUrl);

        String url;
        while ((url = crawlerQueue.poll()) != null) {
            if (endTime <= System.currentTimeMillis()) {
                crawlerQueue.destroy();
                return;
            }

            if(skipNavigation) {
                skipNavigation = false;
            } else {
                driver.navigate().to(url);
            }

            if (endTime <= System.currentTimeMillis()) {
                crawlerQueue.destroy();
                return;
            }
            
            long actionDelay = actionInstance.getDelay().random().toMillis();
            long actionCutOffTime = System.currentTimeMillis() + actionDelay;
            if (actionCutOffTime < endTime) {
                Perforator.sleep(actionDelay);
                crawlerQueue.pushAll(
                        collectUrlsFromThePage(
                                driver,
                                domains,
                                actionInstance.getLinksExtractorScript()
                        )
                );
            } else {
                crawlerQueue.destroy();
                return;
            }

            if (actionInstance.isScroll()) {
                long scrollDelay = actionInstance.getScrollDelay().random().toMillis();
                long scrollCutOffTime = System.currentTimeMillis() + scrollDelay;
                if (scrollCutOffTime < endTime) {
                    driver.executeScript(actionInstance.getScrollScript(), domains);
                    Perforator.sleep(scrollDelay);
                } else {
                    crawlerQueue.destroy();
                    return;
                }
            }
            
            if (actionInstance.isClick()) {
                long clickDelay = actionInstance.getClickDelay().random().toMillis();
                long clickCutOffTime = System.currentTimeMillis() + clickDelay;
                if (clickCutOffTime < endTime) {
                    boolean clicked = randomClick(
                            driver, 
                            domains,
                            actionInstance.getClickScript(), 
                            clickCutOffTime
                    );
                    if(clicked) {
                        Perforator.sleep(clickDelay);
                    }
                } else {
                    crawlerQueue.destroy();
                    return;
                }
            }
        }
    }
    
    private boolean randomClick(
            RemoteWebDriver driver,
            Set<String> domains,
            String clickScript,
            long cutOffTime
    ) {
        List<WebElement> elements = (List<WebElement>) driver.executeScript(
                clickScript,
                domains
        );

        if (elements == null || elements.isEmpty()) {
            return false;
        }

        Collections.shuffle(elements);
        for (WebElement element : elements) {
            if (System.currentTimeMillis() >= cutOffTime) {
                return false;
            }

            try {
                driver.executeScript(
                        "arguments[0].scrollIntoView();arguments[0].removeAttribute('target');",
                        element
                );
                element.click();
                return true;
            } catch (StaleElementReferenceException | InvalidElementStateException e) {
                //ignore and continue
            }
        }
        
        return false;
    }

    private Set<String> collectUrlsFromThePage(
            RemoteWebDriver driver,
            Set<String> domains,
            String linksExtractorScript
    ) {
        Object scriptResult = driver.executeScript(linksExtractorScript, domains);
        
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
