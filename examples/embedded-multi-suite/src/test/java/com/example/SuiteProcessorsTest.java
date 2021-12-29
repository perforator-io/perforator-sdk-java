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
package com.example;

import io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig;
import io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteProcessor;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example <b>JUnit</b> test to verify that the overall logic of the suite processor(s) 
 * works correctly.
 * 
 * This test is executed locally as a part of a usual `mvn test` flow utilizing
 * a local chrome instance for the RemoteWebDriver session.
 * 
 * Since this is just a unit test, it doesn't have any goal to generate a load
 * on the target system, but rather to verify the suite processor's functionality 
 * before such processor can be utilized in a real-world performance test.
 * 
 * Usually, running a full power performance test is not recommended before all 
 * unit tests are passed.
 * 
 * Failed unit test is the first major sign that something is broken with the 
 * integration, and you might make a situation even worse if you try executing it 
 * on a scale as a performance test. It can lead to incorrect load on the target 
 * system, and as a result, you can make a wrong conclusion from the performance 
 * test results.
 */
public class SuiteProcessorsTest {
    
    @ParameterizedTest
    @MethodSource("getSuiteProcessorClasses")
    public void verifySuiteProcessor(String className) throws Exception {
        Class suiteProcessorClass = assertDoesNotThrow(
                () -> getClass().getClassLoader().loadClass(className),
                "Invalid suite.processor class = " + className
        );

        Constructor defaultConstructor = assertDoesNotThrow(
                () -> suiteProcessorClass.getConstructor(),
                "suite.processor class = " + className + " should have default no args constructor"
        );

        Object instance = assertDoesNotThrow(
                () -> defaultConstructor.newInstance()
        );

        EmbeddedSuiteProcessor processor = assertDoesNotThrow(
                () -> (EmbeddedSuiteProcessor) instance,
                "suite.processor class = " + className + " should be an instance of " + EmbeddedSuiteProcessor.class
        );

        EmbeddedSuiteConfig config = new EmbeddedSuiteConfig();
        config.setProcessorClass(processor.getClass().getName());
        config.setName(processor.getClass().getName());
        
        String suiteInstanceID = UUID.randomUUID().toString();
        
        processor.onBeforeSuite(0, suiteInstanceID, config);
        processor.processSuite(0, suiteInstanceID, config);
        processor.onAfterSuite(0, suiteInstanceID, config, null);
    }
    
    /**
     * This method compiles a list of suite processor class names.
     * 
     * By default it looks up {@link System#getProperty(java.lang.String) } using 
     * "suite.processorClass" key.
     * 
     * @return a list of class names.
     */
    static List<String> getSuiteProcessorClasses() {
        String suiteProcessorValue = System.getProperty(
                "suite.processorClass"
        );
        
        if(suiteProcessorValue == null || suiteProcessorValue.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        
        List<String> result = new ArrayList<>();
        for (String className : suiteProcessorValue.split(",")) {
            if(className != null && !className.isBlank()) {
                result.add(className.trim());
            }
        }
        
        return result;
    }
    
}
