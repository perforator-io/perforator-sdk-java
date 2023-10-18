package io.perforator.sdk.loadgenerator.codeless.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CrawlerActionProcessorTest extends AbstractActionProcessorTest<CrawlerActionConfig, CrawlerActionInstance, CrawlerActionProcessor>{

    public static final String CHECKED_LINK_CSS_SELECTOR = "#a";

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        CrawlerActionConfig.Fields.urls, "invalid-url",
                        CrawlerActionConfig.Fields.domains, "invalid-domain",
                        CrawlerActionConfig.Fields.randomizeVisits, "invalid-randomize",
                        CrawlerActionConfig.Fields.pageAnalysisDelay, "invalid-duration",
                        CrawlerActionConfig.Fields.linkVisitorDelay, "invalid-duration",
                        CrawlerActionConfig.Fields.maxVisitsPerLink, "invalid-integer",
                        CrawlerActionConfig.Fields.maxPages, "invalid-integer",
                        CrawlerActionConfig.Fields.maxDuration, "invalid-duration",
                        CrawlerActionConfig.Fields.enabled, "invalid-enabled"
                )
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        CrawlerActionConfig.Fields.urls, "https://verifications.perforator.io",
                        CrawlerActionConfig.Fields.domains, "verifications.perforator.io",
                        CrawlerActionConfig.Fields.linksSelector, CHECKED_LINK_CSS_SELECTOR,
                        CrawlerActionConfig.Fields.randomizeVisits, "true",
                        CrawlerActionConfig.Fields.pageAnalysisDelay, "1s",
                        CrawlerActionConfig.Fields.linkVisitorDelay, "0s-2s",
                        CrawlerActionConfig.Fields.maxVisitsPerLink, "1",
                        CrawlerActionConfig.Fields.maxPages, "5",
                        CrawlerActionConfig.Fields.maxDuration, "1m",
                        CrawlerActionConfig.Fields.enabled, "true"
                )
        );
    }

    @Override
    protected List<JsonNode> buildInvalidActionConfigs() throws Exception {
        return List.of(
                newObjectNode(Map.of(
                        CrawlerActionConfig.Fields.urls, new ArrayNode(
                                JsonNodeFactory.instance,
                                List.of(new TextNode("invalid-url"))
                        )
                )),
                newObjectNode(Map.of(
                        CrawlerActionConfig.Fields.domains, new ArrayNode(
                                JsonNodeFactory.instance,
                                List.of(new TextNode("invalid-domain"))
                        )
                ))
        );
    }

    @Override
    protected List<JsonNode> buildValidActionConfigs() throws Exception {
        return List.of(
                newObjectNode(Map.of(
                        CrawlerActionConfig.Fields.urls, new ArrayNode(
                                JsonNodeFactory.instance,
                                List.of(new TextNode("${" + CrawlerActionConfig.Fields.urls + "}"))
                        ),
                        CrawlerActionConfig.Fields.domains, new ArrayNode(
                                JsonNodeFactory.instance,
                                List.of(new TextNode("${" + CrawlerActionConfig.Fields.domains + "}"))
                        ),
                        CrawlerActionConfig.Fields.randomizeVisits, new TextNode("${" + CrawlerActionConfig.Fields.randomizeVisits + "}"),
                        CrawlerActionConfig.Fields.pageAnalysisDelay, new TextNode("${" + CrawlerActionConfig.Fields.pageAnalysisDelay + "}"),
                        CrawlerActionConfig.Fields.linkVisitorDelay, new TextNode("${" + CrawlerActionConfig.Fields.linkVisitorDelay + "}"),
                        CrawlerActionConfig.Fields.maxVisitsPerLink, new TextNode("${" + CrawlerActionConfig.Fields.maxVisitsPerLink + "}"),
                        CrawlerActionConfig.Fields.maxPages, new TextNode("${" + CrawlerActionConfig.Fields.maxPages + "}"),
                        CrawlerActionConfig.Fields.maxDuration, new TextNode("${" + CrawlerActionConfig.Fields.maxDuration + "}")
                ))
        );
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, CrawlerActionProcessor actionProcessor, CrawlerActionInstance actionInstance) throws Exception {
        assertNotEquals(actionInstance.getUrls().get(0), driver.getCurrentUrl());
    }
}
