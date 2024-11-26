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
                Map.ofEntries(
                        Map.entry(CrawlerActionConfig.Fields.url, "invalid-url"),
                        Map.entry(CrawlerActionConfig.Fields.domains, "invalid-domain"),
                        Map.entry(CrawlerActionConfig.Fields.randomize, "invalid-randomize"),
                        Map.entry(CrawlerActionConfig.Fields.delay, "invalid-duration"),
                        Map.entry(CrawlerActionConfig.Fields.maxQueueSize, "invalid-integer"),
                        Map.entry(CrawlerActionConfig.Fields.maxVisitsPerUrl, "invalid-integer"),
                        Map.entry(CrawlerActionConfig.Fields.maxVisitsOverall, "invalid-integer"),
                        Map.entry(CrawlerActionConfig.Fields.maxDuration, "invalid-duration"),
                        Map.entry(CrawlerActionConfig.Fields.pageLoadTimeout, "invalid-duration"),
                        Map.entry(CrawlerActionConfig.Fields.enabled, "invalid-enabled"),
                        Map.entry(CrawlerActionConfig.Fields.scroll, "invalid-scroll-enabled"),
                        Map.entry(CrawlerActionConfig.Fields.scrollDelay, "invalid-scroll-delay"),
                        Map.entry(CrawlerActionConfig.Fields.click, "invalid-click-enabled"),
                        Map.entry(CrawlerActionConfig.Fields.clickDelay, "invalid-click-delay")
                )
        );
    }

    @Override
    protected List<Map<String, String>> buildValidSuiteProps() throws Exception {
        return List.of(
                Map.ofEntries(
                        Map.entry(CrawlerActionConfig.Fields.url, "https://verifications.perforator.io"),
                        Map.entry(CrawlerActionConfig.Fields.domains, "verifications.perforator.io"),
                        Map.entry(CrawlerActionConfig.Fields.linksExtractorScript, CrawlerActionProcessor.DEFAULT_LINKS_EXTRACTOR_SCRIPT),
                        Map.entry(CrawlerActionConfig.Fields.randomize, "true"),
                        Map.entry(CrawlerActionConfig.Fields.delay, "1s-2s"),
                        Map.entry(CrawlerActionConfig.Fields.maxQueueSize, "1000"),
                        Map.entry(CrawlerActionConfig.Fields.maxVisitsPerUrl, "1"),
                        Map.entry(CrawlerActionConfig.Fields.maxVisitsOverall, "5"),
                        Map.entry(CrawlerActionConfig.Fields.maxDuration, "15s"),
                        Map.entry(CrawlerActionConfig.Fields.enabled, "true"),
                        Map.entry(CrawlerActionConfig.Fields.scroll, "true"),
                        Map.entry(CrawlerActionConfig.Fields.scrollDelay, "0.5s-1s"),
                        Map.entry(CrawlerActionConfig.Fields.click, "true"),
                        Map.entry(CrawlerActionConfig.Fields.clickDelay, "0.5s-1s"),
                        Map.entry(CrawlerActionConfig.Fields.clickScript, CrawlerActionProcessor.DEFAULT_CLICK_SCRIPT)
                )
        );
    }

    @Override
    protected List<JsonNode> buildInvalidActionConfigs() throws Exception {
        return List.of(
                newObjectNode(Map.of(
                        CrawlerActionConfig.Fields.url, new TextNode("invalid-url")
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
                newObjectNode(
                        Map.ofEntries(
                                Map.entry(CrawlerActionConfig.Fields.url, new TextNode("${" + CrawlerActionConfig.Fields.url + "}")),
                                Map.entry(CrawlerActionConfig.Fields.domains, new ArrayNode(
                                        JsonNodeFactory.instance,
                                        List.of(new TextNode("${" + CrawlerActionConfig.Fields.domains + "}"))
                                )),
                                Map.entry(CrawlerActionConfig.Fields.randomize, new TextNode("${" + CrawlerActionConfig.Fields.randomize + "}")),
                                Map.entry(CrawlerActionConfig.Fields.delay, new TextNode("${" + CrawlerActionConfig.Fields.delay + "}")),
                                Map.entry(CrawlerActionConfig.Fields.maxQueueSize, new TextNode("${" + CrawlerActionConfig.Fields.maxQueueSize + "}")),
                                Map.entry(CrawlerActionConfig.Fields.maxVisitsPerUrl, new TextNode("${" + CrawlerActionConfig.Fields.maxVisitsPerUrl + "}")),
                                Map.entry(CrawlerActionConfig.Fields.maxVisitsOverall, new TextNode("${" + CrawlerActionConfig.Fields.maxVisitsOverall + "}")),
                                Map.entry(CrawlerActionConfig.Fields.maxDuration, new TextNode("${" + CrawlerActionConfig.Fields.maxDuration + "}")),
                                Map.entry(CrawlerActionConfig.Fields.scroll, new TextNode("${" + CrawlerActionConfig.Fields.scroll + "}")),
                                Map.entry(CrawlerActionConfig.Fields.scrollDelay, new TextNode("${" + CrawlerActionConfig.Fields.scrollDelay + "}")),
                                Map.entry(CrawlerActionConfig.Fields.click, new TextNode("${" + CrawlerActionConfig.Fields.click + "}")),
                                Map.entry(CrawlerActionConfig.Fields.clickDelay, new TextNode("${" + CrawlerActionConfig.Fields.clickDelay + "}")),
                                Map.entry(CrawlerActionConfig.Fields.clickScript, new TextNode("${" + CrawlerActionConfig.Fields.clickScript + "}"))
                        )
                )
        );
    }

    @Override
    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, CrawlerActionProcessor actionProcessor, CrawlerActionInstance actionInstance) throws Exception {
        assertNotEquals(actionInstance.getUrl(), driver.getCurrentUrl());
    }
}
