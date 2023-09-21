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
import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.codeless.RandomDuration;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.StringConverter;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public abstract class AbstractActionProcessor<T extends ActionConfig, V extends ActionInstance<T>> implements ActionProcessor<T, V> {

    protected final Class<T> actionConfigClass;
    protected final Class<V> actionInstanceClass;
    protected final String actionName;

    public AbstractActionProcessor(String actionName) {
        this.actionConfigClass = buildActionConfigClass(getClass());
        this.actionInstanceClass = buildActionInstanceClass(getClass());
        this.actionName = actionName;
    }

    private static <T> Class<T> buildActionConfigClass(Class clazz) {
        return (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private static <V> Class<V> buildActionInstanceClass(Class clazz) {
        return (Class<V>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[1];
    }

    @Override
    public String getSupportedActionName() {
        return actionName;
    }

    @Override
    public boolean isActionSupported(String actionName, JsonNode actionValue) {
        return this.actionName.equals(actionName);
    }

    @Override
    public void validateActionConfig(CodelessLoadGeneratorConfig loadGeneratorConfig, CodelessSuiteConfig suiteConfig, T actionConfig) {
        List<FormattingMap> formatters;
        if (suiteConfig.getProps() == null || suiteConfig.getProps().isEmpty()) {
            formatters = List.of(FormattingMap.EMPTY);
        } else {
            formatters = suiteConfig.getProps();
        }

        for (FormattingMap props : formatters) {
            V instance = buildActionInstance(loadGeneratorConfig, suiteConfig, props, actionConfig);

            if (instance == null) {
                throw new RuntimeException(
                        "Can't build new action instance for " + actionInstanceClass
                );
            }

            if (instance.getConfig() == null) {
                throw new RuntimeException(
                        "Action config should not be null in " + actionInstanceClass
                );
            }

            if (instance.getConfig() != actionConfig) {
                throw new RuntimeException(
                        "Action config should point to existing config in" + actionInstanceClass
                );
            }
        }
    }

    protected String getRequiredValueOrNestedField(String fieldName, JsonNode node) {
        if (node == null || node.isNull()) {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " is required"
            );
        }

        if (node.isArray()) {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " should not be array"
            );
        }

        String result = null;
        
        if (node.isValueNode()) {
            result = asText(fieldName, node);
        } else if (node.isObject() && node.has(fieldName)) {
            result = asText(fieldName, node.get(fieldName));
        }

        if (result == null || result.isBlank()) {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " is required"
            );
        }

        return result.trim();
    }

    protected String getRequiredNestedField(String fieldName, JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode() || node.isArray() || !node.has(fieldName)) {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " is required"
            );
        }

        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || field.isObject() || field.isArray()) {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " should be simple value"
            );
        }

        String result = asText(fieldName, field);
        if (result == null || result.isBlank()) {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " should not be empty"
            );
        }

        return result.trim();
    }

    protected String getOptionalValue(JsonNode node, String defaultValue) {
        if (node == null || node.isNull()) {
            throw new RuntimeException(
                    actionName
                            + ".value is required"
            );
        }

        if (node.isArray()) {
            throw new RuntimeException(
                    actionName
                            + ".value should not be array"
            );
        }

        if (node.isValueNode()) {
            return asText(actionName, node);
        }
        
        return defaultValue;
    }

    protected String getOptionalNestedField(String fieldName, JsonNode node, String defaultValue) {
        if (node == null || node.isNull() || node.isValueNode() || node.isArray() || !node.has(fieldName)) {
            return defaultValue;
        }

        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull() || field.isObject() || field.isArray()) {
            return defaultValue;
        }

        String result = asText(fieldName, field);
        if (result == null || result.isBlank()) {
            return defaultValue;
        }

        return result.trim();
    }

    protected String buildStringForActionInstance(String fieldName, String configValue, FormattingMap formatter) {
        return buildStringForActionInstance(fieldName, configValue, formatter, true);
    }

    protected String buildStringForActionInstance(String fieldName, String configValue, FormattingMap formatter, boolean required) {

        if (!required && configValue == null) {
            return null;
        }

        String formattedValue = formatter.format(configValue);

        if (required && (formattedValue == null || formattedValue.isEmpty())) {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " = "
                            + configValue
                            + " => "
                            + formattedValue
                            + " should be present"
            );
        }

        if (!required && (formattedValue == null || formattedValue.isEmpty())) {
            return null;
        }

        return formattedValue;
    }

    protected Duration buildDurationForActionInstance(String fieldName, String configDuration, Duration defaultDuration, FormattingMap formatter) {
        return buildDurationForActionInstance(fieldName, configDuration, defaultDuration, formatter, true);
    }

    protected Duration buildDurationForActionInstance(String fieldName, String configDuration, Duration defaultDuration, FormattingMap formatter, boolean required) {
        Duration result = defaultDuration;
        
        if (configDuration != null && !configDuration.isBlank()) {
            String formattedTimeout = formatter.format(configDuration);

            if (formattedTimeout != null && !formattedTimeout.isBlank()) {
                try {
                    result = StringConverter.toDuration(
                            formattedTimeout
                    );
                } catch (RuntimeException e) {
                    throw new RuntimeException(
                            actionName
                            + "."
                            + fieldName
                            + " = "
                            + configDuration
                            + " => "
                            + formattedTimeout
                            + " is invalid",
                            e
                    );
                }
            }
        }
        
        if (required && result == null) {
            throw new RuntimeException(
                    actionName
                    + "."
                    + fieldName
                    + " is required"
            );
        }
        
        return result;
    }

    protected RandomDuration buildRandomDurationForActionInstance(String fieldName, String configDuration, FormattingMap formatter) {
        return buildRandomDurationForActionInstance(fieldName, configDuration, formatter, true);
    }

    protected RandomDuration buildRandomDurationForActionInstance(String fieldName, String configDuration, FormattingMap formatter, boolean required) {
        String formattedTimeout = formatter.format(configDuration);

        if (formattedTimeout.contains("-")) {
            String[] minmax = formattedTimeout.split("-");

            Duration from = buildDurationForActionInstance(fieldName, minmax[0], null, formatter, required);
            Duration to = buildDurationForActionInstance(fieldName, minmax[1], null, formatter, required);

            return new RandomDuration(
                    from,
                    to
            );
        } else {
            return new RandomDuration(
                    buildDurationForActionInstance(fieldName, configDuration, null, formatter, required)
            );
        }
    }
    
    protected boolean buildEnabledForActionInstance(String fieldName, String configEnabled, FormattingMap formatter) {
        String formattedResult = formatter.format(configEnabled);
        
        if(formattedResult == null || formattedResult.isBlank()) {
            return true;
        } else if(formattedResult.equalsIgnoreCase("true")) {
            return true;
        } else if(formattedResult.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " = "
                            + configEnabled
                            + " is invalid => only true or false values are suported"
            );
        }
    }

    protected String buildUrlForActionInstance(String fieldName, String configUrl, FormattingMap formatter) {
        return buildUrlForActionInstance(fieldName, configUrl, formatter, true);
    }

    protected String buildUrlForActionInstance(String fieldName, String configUrl, FormattingMap formatter, boolean required) {
        String formattedUrl = formatter.format(configUrl);

        if (required && (formattedUrl == null || formattedUrl.isBlank())) {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " = "
                            + configUrl
                            + " => "
                            + formattedUrl
                            + " should be present"
            );
        }

        if (!required && (formattedUrl == null || formattedUrl.isBlank())) {
            return null;
        }

        try {
            return new URL(formattedUrl).toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    actionName
                            + "."
                            + fieldName
                            + " = "
                            + configUrl
                            + " => "
                            + formattedUrl
                            + " is invalid",
                    e
            );
        }
    }
    
    protected final String asText(JsonNode node) {
        if (node.isBoolean() || node.isNumber()) {
            return node.asText();
        } else if (node.isValueNode()) {
            return node.textValue();
        } else {
            throw new RuntimeException(
                    actionName
                    + " is invalid => " + node
            );
        }
    }
    
    protected final String asText(String fieldName, JsonNode node) {
        if (node.isBoolean() || node.isNumber()) {
            return node.asText();
        } else if (node.isValueNode()) {
            return node.textValue();
        } else {
            throw new RuntimeException(
                    actionName
                    + "."
                    + fieldName
                    + " is invalid => " + node
            );
        }
    }

    public Class<T> getActionConfigClass() {
        return actionConfigClass;
    }

    public Class<V> getActionInstanceClass() {
        return actionInstanceClass;
    }

    public String getActionName() {
        return actionName;
    }
}
