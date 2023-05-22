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
package io.perforator.sdk.loadgenerator.codeless.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionConfig;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionProcessor;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionProcessorsRegistry;
import io.perforator.sdk.loadgenerator.core.configs.Configurable;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

@ToString
@FieldNameConstants
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodelessSuiteConfig extends SuiteConfig {
    
    public static final String DEFAULT_WEB_DRIVER_FLUENT_WAIT_TIMEOUT_S = "30s";
    public static final Duration DEFAULT_WEB_DRIVER_FLUENT_WAIT_TIMEOUT = Configurable.parseDuration(DEFAULT_WEB_DRIVER_FLUENT_WAIT_TIMEOUT_S);
    
    @Getter
    @Setter
    @FieldNameConstants.Include
    private Duration webDriverFluentWaitTimeout = DEFAULT_WEB_DRIVER_FLUENT_WAIT_TIMEOUT;

    @JsonDeserialize(using = PropsDeserializer.class)
    @Getter
    @Setter
    @FieldNameConstants.Include
    private List<FormattingMap> props = new ArrayList<>();

    @Getter
    @Setter
    @FieldNameConstants.Include
    private String propsFile;

    @JsonDeserialize(using = StepsDeserializer.class)
    @Getter
    @Setter
    @FieldNameConstants.Include
    private List<CodelessStepConfig> steps = new ArrayList<>();

    public CodelessSuiteConfig() {
        applyDefaults();
    }

    public CodelessSuiteConfig(Function<String, String>... defaultsProviders) {
        applyDefaults(defaultsProviders);
    }

    public static class PropsDeserializer extends JsonDeserializer<List<FormattingMap>> {

        @Override
        public List<FormattingMap> deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
            ObjectCodec oc = jp.getCodec();

            if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
                List<LinkedHashMap<String, String>> items = oc.readValue(
                        jp,
                        new TypeReference<List<LinkedHashMap<String, String>>>() {
                        }
                );

                if (items == null || items.isEmpty()) {
                    return Collections.EMPTY_LIST;
                }

                List<FormattingMap> result = new ArrayList<>(items.size());
                items.forEach(item -> result.add(new FormattingMap(item)));
                return result;
            }

            if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                LinkedHashMap<String, String> item = oc.readValue(
                        jp,
                        new TypeReference<LinkedHashMap<String, String>>() {
                        }
                );

                if (item == null || item.isEmpty()) {
                    return Collections.EMPTY_LIST;
                }

                List<FormattingMap> result = new ArrayList<>(1);
                result.add(new FormattingMap(item));
                return result;
            }

            throw JsonMappingException.from(
                    dc,
                    "'props' should be either an object or array"
            );
        }

    }

    public static class StepsDeserializer extends JsonDeserializer<List<CodelessStepConfig>> {

        @Override
        public List<CodelessStepConfig> deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
            ObjectCodec objectCodec = jp.getCodec();
            List<CodelessStepConfig> result = new ArrayList<>();

            LinkedHashMap<String, List<JsonNode>> steps = objectCodec.readValue(
                    jp,
                    new TypeReference<LinkedHashMap<String, List<JsonNode>>>() {
                    }
            );

            for (String stepName : steps.keySet()) {
                List<JsonNode> actionNodes = steps.get(stepName);
                List<ActionConfig> actionConfigs = new ArrayList<>();

                for (JsonNode actionNode : actionNodes) {
                    actionConfigs.addAll(convertActionConfigNode(dc, actionNode));
                }

                CodelessStepConfig stepConfig = new CodelessStepConfig();
                stepConfig.setName(stepName);
                stepConfig.setActions(actionConfigs);

                result.add(stepConfig);
            }

            return result;
        }

        private List<ActionConfig> convertActionConfigNode(DeserializationContext dc, JsonNode node) throws JsonMappingException {
            List<ActionProcessor> actionProcessors = ActionProcessorsRegistry.INSTANCE.getActionProcessors();
            List<ActionConfig> result = new ArrayList<>();

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String actionName = field.getKey();
                JsonNode actionValue = field.getValue();
                ActionConfig actionConfig = null;

                for (ActionProcessor actionProcessor : actionProcessors) {
                    if (actionProcessor.isActionSupported(actionName, actionValue)) {
                        actionConfig = actionProcessor.buildActionConfig(
                                actionName,
                                actionValue
                        );
                        break;
                    }
                }

                if (actionConfig != null) {
                    result.add(actionConfig);
                } else {
                    throw JsonMappingException.from(
                            dc,
                            "action '" + actionName + "' is not supported"
                    );
                }
            }

            return result;
        }

    }

}
