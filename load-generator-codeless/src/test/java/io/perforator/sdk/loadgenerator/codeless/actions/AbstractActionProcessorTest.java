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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.perforator.sdk.loadgenerator.codeless.FormattingMap;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import io.perforator.sdk.loadgenerator.core.RemoteWebDriverHelper;
import io.perforator.sdk.loadgenerator.core.configs.ChromeMode;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractActionProcessorTest<T extends ActionConfig, V extends ActionInstance<T>, P extends AbstractActionProcessor<T, V>> {
    
    protected static final String VERIFICATIONS_APP_URL = new LoadGeneratorConfig().getApiBaseUrl().replace("api", "verifications");
    protected static final boolean CHROME_BROWSER_AVAILABLE = isChromeBrowserAvailable();

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final Class<T> actionConfigClass;
    protected final Class<V> actionInstanceClass;
    protected final Class<P> actionProcessorClass;

    public AbstractActionProcessorTest() {
        this.actionConfigClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.actionInstanceClass = (Class<V>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.actionProcessorClass = (Class<P>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];
    }

    public AbstractActionProcessorTest(Class<T> actionConfigClass, Class<V> actionInstanceClass, Class<P> actionProcessorClass) {
        this.actionConfigClass = actionConfigClass;
        this.actionInstanceClass = actionInstanceClass;
        this.actionProcessorClass = actionProcessorClass;
    }

    protected static RemoteWebDriver createHeadlessLocalChromeDriver() {
        CodelessSuiteConfig suiteConfig = new CodelessSuiteConfig();
        suiteConfig.setWebDriverMode(WebDriverMode.local);
        suiteConfig.setChromeMode(ChromeMode.headless);
        return RemoteWebDriverHelper.createLocalChromeDriver(null, suiteConfig);
    }

    private static boolean isChromeBrowserAvailable() {
        RemoteWebDriver driver = null;
        try {
            driver = createHeadlessLocalChromeDriver();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    protected abstract List<Map<String, String>> buildInvalidSuiteProps() throws Exception;

    protected abstract List<Map<String, String>> buildValidSuiteProps() throws Exception;

    protected abstract List<JsonNode> buildInvalidActionConfigs() throws Exception;

    protected abstract List<JsonNode> buildValidActionConfigs() throws Exception;

    protected void onBeforeActionInstanceProcessing(RemoteWebDriver driver, P actionProcessor, V actionInstance) throws Exception {
    }

    protected void onAfterActionInstanceProcessing(RemoteWebDriver driver, P actionProcessor, V actionInstance) throws Exception {
    }

    @Test
    public final void actionProcessorShouldHaveNoArgsConstructor() throws Exception {
        assertNotNull(newActionProcessorInstance());
    }

    @Test
    public final void verifyInvalidSuitePropsAndInvalidActions() throws Exception {
        CodelessLoadGeneratorConfig loadGeneratorConfig = new CodelessLoadGeneratorConfig();
        CodelessSuiteConfig suiteConfig = new CodelessSuiteConfig();

        P actionProcessor = newActionProcessorInstance();
        assertNotNull(actionProcessor);

        List<Map<String, String>> invalidSuiteProps = buildInvalidSuiteProps();
        assertNotNull(invalidSuiteProps);
        assertFalse(invalidSuiteProps.isEmpty());
        suiteConfig.setProps(toFormatters(invalidSuiteProps));

        List<JsonNode> invalidActionConfigs = buildInvalidActionConfigs();
        assertNotNull(invalidActionConfigs);
        assertFalse(invalidActionConfigs.isEmpty());

        for (JsonNode invalidActionConfig : invalidActionConfigs) {
            assertThrows(
                    RuntimeException.class,
                    () -> {
                        T actionConfig = actionProcessor.buildActionConfig(
                                actionProcessor.getActionName(),
                                invalidActionConfig
                        );

                        actionProcessor.validateActionConfig(
                                suiteConfig,
                                actionConfig
                        );
                    },
                    "Invalid suite props and invalid action should result in validation exception: "
                            + "invalidSuiteProps = " + invalidSuiteProps
                            + ", invalidActionConfig = " + invalidActionConfig
            );
        }
    }

    @Test
    public void verifyInvalidSuitePropsAndValidActions() throws Exception {
        CodelessLoadGeneratorConfig loadGeneratorConfig = new CodelessLoadGeneratorConfig();
        CodelessSuiteConfig suiteConfig = new CodelessSuiteConfig();

        P actionProcessor = newActionProcessorInstance();
        assertNotNull(actionProcessor);

        List<Map<String, String>> invalidSuiteProps = buildInvalidSuiteProps();
        assertNotNull(invalidSuiteProps);
        assertFalse(invalidSuiteProps.isEmpty());
        suiteConfig.setProps(toFormatters(invalidSuiteProps));

        List<JsonNode> validActionConfigs = buildValidActionConfigs();
        assertNotNull(validActionConfigs);
        assertFalse(validActionConfigs.isEmpty());

        for (JsonNode validActionConfig : validActionConfigs) {
            T actionConfig = actionProcessor.buildActionConfig(
                    actionProcessor.getActionName(),
                    validActionConfig
            );

            assertThrows(
                    RuntimeException.class,
                    () -> actionProcessor.validateActionConfig(
                            suiteConfig,
                            actionConfig
                    ),
                    "Invalid suite props and valid action should result in validation exception: "
                            + "invalidSuiteProps = " + invalidSuiteProps
                            + ", validActionConfig = " + validActionConfig
            );
        }
    }

    @Test
    public final void verifyValidSuitePropsAndInvalidActions() throws Exception {
        CodelessLoadGeneratorConfig loadGeneratorConfig = new CodelessLoadGeneratorConfig();
        CodelessSuiteConfig suiteConfig = new CodelessSuiteConfig();

        P actionProcessor = newActionProcessorInstance();
        assertNotNull(actionProcessor);

        List<Map<String, String>> validSuiteProps = buildValidSuiteProps();
        assertNotNull(validSuiteProps);
        assertFalse(validSuiteProps.isEmpty());
        suiteConfig.setProps(toFormatters(validSuiteProps));

        List<JsonNode> invalidActionConfigs = buildInvalidActionConfigs();
        assertNotNull(invalidActionConfigs);
        assertFalse(invalidActionConfigs.isEmpty());

        for (JsonNode invalidActionConfig : invalidActionConfigs) {
            assertThrows(
                    RuntimeException.class,
                    () -> {
                        T actionConfig = actionProcessor.buildActionConfig(
                                actionProcessor.getActionName(),
                                invalidActionConfig
                        );

                        actionProcessor.validateActionConfig(
                                suiteConfig,
                                actionConfig
                        );
                    },
                    "Valid suite props and invalid action should result in validation exception: "
                            + "validSuiteProps = " + validSuiteProps
                            + ", invalidActionConfig = " + invalidActionConfig
            );
        }
    }

    @Test
    public final void verifyValidSuitePropsAndValidActions() throws Exception {
        CodelessLoadGeneratorConfig loadGeneratorConfig = new CodelessLoadGeneratorConfig();
        CodelessSuiteConfig suiteConfig = new CodelessSuiteConfig();

        P actionProcessor = newActionProcessorInstance();
        assertNotNull(actionProcessor);

        List<Map<String, String>> validSuiteProps = buildValidSuiteProps();
        assertNotNull(validSuiteProps);
        assertFalse(validSuiteProps.isEmpty());
        suiteConfig.setProps(toFormatters(validSuiteProps));

        List<JsonNode> validActionConfigs = buildValidActionConfigs();
        assertNotNull(validActionConfigs);
        assertFalse(validActionConfigs.isEmpty());

        for (JsonNode validActionConfig : validActionConfigs) {
            T actionConfig = actionProcessor.buildActionConfig(
                    actionProcessor.getActionName(),
                    validActionConfig
            );

            actionProcessor.validateActionConfig(
                    suiteConfig,
                    actionConfig
            );

            assertNotNull(actionConfig);

            for (FormattingMap formatter : toFormatters(validSuiteProps)) {
                V actionInstance = actionProcessor.buildActionInstance(
                        suiteConfig,
                        formatter,
                        actionConfig
                );
                assertNotNull(actionInstance);
            }
        }
    }

    @Test
    public final void verifyActionInstanceProcessing() throws Exception {
        assumeTrue(CHROME_BROWSER_AVAILABLE);

        CodelessLoadGeneratorConfig loadGeneratorConfig = new CodelessLoadGeneratorConfig();
        CodelessSuiteConfig suiteConfig = new CodelessSuiteConfig();

        P actionProcessor = newActionProcessorInstance();
        assertNotNull(actionProcessor);

        List<Map<String, String>> validSuiteProps = buildValidSuiteProps();
        assertNotNull(validSuiteProps);
        assertFalse(validSuiteProps.isEmpty());
        suiteConfig.setProps(toFormatters(validSuiteProps));

        List<JsonNode> validActionConfigs = buildValidActionConfigs();
        assertNotNull(validActionConfigs);
        assertFalse(validActionConfigs.isEmpty());
        RemoteWebDriver driver = createHeadlessLocalChromeDriver();

        try {
            for (JsonNode validActionConfig : validActionConfigs) {
                T actionConfig = actionProcessor.buildActionConfig(
                        actionProcessor.getActionName(),
                        validActionConfig
                );

                actionProcessor.validateActionConfig(
                        suiteConfig,
                        actionConfig
                );

                assertNotNull(actionConfig);

                for (FormattingMap formatter : toFormatters(validSuiteProps)) {
                    V actionInstance = actionProcessor.buildActionInstance(
                            suiteConfig,
                            formatter,
                            actionConfig
                    );
                    assertNotNull(actionInstance);

                    onBeforeActionInstanceProcessing(
                            driver,
                            actionProcessor,
                            actionInstance
                    );

                    actionProcessor.processActionInstance(
                            driver,
                            actionInstance
                    );

                    onAfterActionInstanceProcessing(
                            driver,
                            actionProcessor,
                            actionInstance
                    );
                }
            }
        } finally {
            driver.quit();
        }
    }

    protected List<FormattingMap> toFormatters(List<Map<String, String>> items) {
        return items.stream().map(FormattingMap::new).collect(Collectors.toList());
    }

    protected final ObjectNode newObjectNode() throws Exception {
        return new ObjectNode(objectMapper.getNodeFactory());
    }

    protected final ObjectNode newObjectNode(Map<String, JsonNode> fields) throws Exception {
        return new ObjectNode(objectMapper.getNodeFactory(), fields);
    }

    protected final TextNode newTextNode(String value) throws Exception {
        return new TextNode(value);
    }

    protected final P newActionProcessorInstance() throws Exception {
        Constructor<P> processorConstructor = actionProcessorClass.getConstructor();
        assertNotNull(processorConstructor);

        P processorInstance = processorConstructor.newInstance();
        assertNotNull(processorInstance);
        assertEquals(actionConfigClass, processorInstance.getActionConfigClass());
        assertEquals(actionInstanceClass, processorInstance.getActionInstanceClass());

        return processorInstance;
    }

}
