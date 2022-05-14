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
package io.perforator.sdk.loadgenerator.codeless;

import io.perforator.sdk.loadgenerator.codeless.config.*;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigFactoryTest {

    @ParameterizedTest
    @MethodSource("getInvalidConfigs")
    public void verifyNotValidConfig(Path path) throws Exception {
        assertThrows(
                Exception.class,
                () -> {
                    CodelessConfig config = CodelessConfigFactory.INSTANCE.getCodelessConfig(path);
                    new CodelessLoadGenerator(config);
                },
                "Config at " + path + " should be invalid"
        );
    }

    @ParameterizedTest
    @MethodSource("getValidConfigs")
    public void verifyValidConfig(Path path) throws Exception {
        CodelessConfig config = assertDoesNotThrow(
                () -> {
                    CodelessConfig c = CodelessConfigFactory.INSTANCE.getCodelessConfig(path);
                    new CodelessLoadGenerator(c);
                    return c;
                },
                "Config at " + path + " should be valid"
        );
        
        assertFalse(config.getSuiteConfigs().isEmpty());
        for (CodelessSuiteConfig suiteConfig : config.getSuiteConfigs()) {
            assertFalse(suiteConfig.getSteps().isEmpty());
            
            for (CodelessStepConfig step : suiteConfig.getSteps()) {
                assertFalse(step.getActions().isEmpty());
                assertTrue(step.getActions().size() >= 2);
            }
        }
    }
    
    @ParameterizedTest
    @MethodSource("getValidConfigs")
    public void verifyDefaultsOverrideConfigProperties(Path path) throws Exception {
        try {
            CodelessConfig config = CodelessConfigFactory.INSTANCE.getCodelessConfig(
                    path
            );
            assertNotNull(config);
            assertNotNull(config.getLoadGeneratorConfig());
            assertNotNull(config.getSuiteConfigs());
            
            assertEquals(true, config.getLoadGeneratorConfig().isConcurrencyAutoAdjustment());
            
            for (CodelessSuiteConfig suiteConfig : config.getSuiteConfigs()) {
                assertNotEquals(Duration.ofSeconds(17), suiteConfig.getRampUp());
            }
            
            System.setProperty(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampUp, 
                    "17s"
            );
            System.setProperty(
                    LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.concurrencyAutoAdjustment, 
                    "false"
            );
            
            config = CodelessConfigFactory.INSTANCE.getCodelessConfig(
                    path
            );
            assertNotNull(config);
            assertNotNull(config.getLoadGeneratorConfig());
            assertNotNull(config.getSuiteConfigs());
            
            assertEquals(false, config.getLoadGeneratorConfig().isConcurrencyAutoAdjustment());
            
            for (CodelessSuiteConfig suiteConfig : config.getSuiteConfigs()) {
                assertEquals(Duration.ofSeconds(17), suiteConfig.getRampUp());
            }
        } finally {
            System.clearProperty(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.rampUp
            );
            System.clearProperty(
                    LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX + "." + LoadGeneratorConfig.Fields.concurrencyAutoAdjustment
            );
        }
    }

    private List<Path> getValidConfigs() {
        return getYamlConfigs(true);
    }

    private List<Path> getInvalidConfigs() {
        return getYamlConfigs(false);
    }

    private List<Path> getYamlConfigs(Boolean valid) {
        File folder = getFileFromResource("yaml");
        FilenameFilter fileFilter = null;
        if (valid != null) {
            fileFilter = (dir, name) -> valid != name.contains("not_valid");
        }
        return Arrays.stream(folder.listFiles(fileFilter))
                .map(File::toPath)
                .collect(Collectors.toList());
    }

    private File getFileFromResource(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("File not found! " + fileName);
        } else {
            try {
                return new File(resource.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("URI Exception", e);
            }
        }
    }
}