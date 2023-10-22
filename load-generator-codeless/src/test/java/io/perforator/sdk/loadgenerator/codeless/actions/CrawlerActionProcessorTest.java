package io.perforator.sdk.loadgenerator.codeless.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CrawlerActionProcessorTest extends AbstractActionProcessorTest<CrawlerActionConfig, CrawlerActionInstance, CrawlerActionProcessor>{

    @Override
    protected List<Map<String, String>> buildInvalidSuiteProps() throws Exception {
        return List.of(
                Map.of(
                        CrawlerActionConfig.Fields.urls, "invalid-url",
                        CrawlerActionConfig.Fields.domains, "invalid-domain",
                        CrawlerActionConfig.Fields.randomize, "invalid-randomize",
                        CrawlerActionConfig.Fields.delay, "invalid-duration",
                        CrawlerActionConfig.Fields.maxQueueSize, "invalid-integer",
                        CrawlerActionConfig.Fields.maxVisitsPerUrl, "invalid-integer",
                        CrawlerActionConfig.Fields.maxVisitsOverall, "invalid-integer",
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
                        CrawlerActionConfig.Fields.linksExtractorScript, CrawlerActionProcessor.DEFAULT_LINKS_EXTRACTOR_SCRIPT,
                        CrawlerActionConfig.Fields.randomize, "true",
                        CrawlerActionConfig.Fields.delay, "1s-2s",
                        CrawlerActionConfig.Fields.maxQueueSize, "1000",
                        CrawlerActionConfig.Fields.maxVisitsPerUrl, "1",
                        CrawlerActionConfig.Fields.maxVisitsOverall, "5",
                        CrawlerActionConfig.Fields.maxDuration, "15s",
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
                        CrawlerActionConfig.Fields.randomize, new TextNode("${" + CrawlerActionConfig.Fields.randomize + "}"),
                        CrawlerActionConfig.Fields.delay, new TextNode("${" + CrawlerActionConfig.Fields.delay + "}"),
                        CrawlerActionConfig.Fields.maxQueueSize, new TextNode("${" + CrawlerActionConfig.Fields.maxQueueSize + "}"),
                        CrawlerActionConfig.Fields.maxVisitsPerUrl, new TextNode("${" + CrawlerActionConfig.Fields.maxVisitsPerUrl + "}"),
                        CrawlerActionConfig.Fields.maxVisitsOverall, new TextNode("${" + CrawlerActionConfig.Fields.maxVisitsOverall + "}"),
                        CrawlerActionConfig.Fields.maxDuration, new TextNode("${" + CrawlerActionConfig.Fields.maxDuration + "}")
                ))
        );
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, CrawlerActionProcessor actionProcessor, CrawlerActionInstance actionInstance) throws Exception {
        assertNotEquals(actionInstance.getUrls().get(0), driver.getCurrentUrl());
    }
}
