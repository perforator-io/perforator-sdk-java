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
import com.google.common.reflect.ClassPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ActionProcessorsServiceLoaderTest {

    private static <T> Class<T> getActionConfigClass(Class clazz) {
        return (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private static <V> Class<V> getActionInstanceClass(Class clazz) {
        return (Class<V>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[1];
    }

    @Test
    public void verifyAllActionProcessorsAreLoaded() throws Exception {
        List<ActionProcessor> actionProcessors = getActionProcessors();
        assertNotNull(actionProcessors);
        assertFalse(actionProcessors.isEmpty());

        Set<Class> loadedProcessorClasses = actionProcessors.stream().map(
                ActionProcessor::getClass
        ).collect(
                Collectors.toSet()
        );

        Set<ClassPath.ClassInfo> classesToCheck = ClassPath.from(
                getClass().getClassLoader()
        ).getTopLevelClassesRecursive(
                ActionProcessor.class.getPackageName()
        );

        for (ClassPath.ClassInfo classToCheck : classesToCheck) {
            Class clazz = getClass().getClassLoader().loadClass(
                    classToCheck.getName()
            );

            if (clazz.isInterface()) {
                continue;
            }

            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            if (!ActionProcessor.class.isAssignableFrom(clazz)) {
                continue;
            }

            assertTrue(
                    loadedProcessorClasses.contains(clazz),
                    "Action processor on class path "
                            + clazz
                            + " should be found via ServiceLoader"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getActionProcessors")
    public void verifyActionProcessor(ActionProcessor processor) throws Exception {
        assertNotNull(processor);

        String testClassName = processor.getClass().getName() + "Test";

        Class testClass = assertDoesNotThrow(
                () -> getClass().getClassLoader().loadClass(testClassName),
                "Action processor "
                        + processor.getClass().getName()
                        + " should have dedicated test "
                        + testClassName
        );

        assertTrue(
                AbstractActionProcessorTest.class.isAssignableFrom(testClass),
                testClassName + " should extend " + AbstractActionProcessorTest.class.getName()
        );

        Class<ActionConfig> actionConfigClass = getActionConfigClass(processor.getClass());
        assertNotNull(actionConfigClass);
        assertEquals(0, actionConfigClass.getConstructors().length);

        Class<ActionInstance> actionInstanceClass = getActionInstanceClass(processor.getClass());
        assertNotNull(actionInstanceClass);
        assertEquals(0, actionInstanceClass.getConstructors().length);

        Class current = actionConfigClass;
        Set<String> configFields = new HashSet<>();

        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                assertTrue(
                        Modifier.isPrivate(field.getModifiers()),
                        current + "." + field.getName() + " should be private"
                );

                assertTrue(
                        Modifier.isFinal(field.getModifiers()),
                        current + "." + field.getName() + " should be final"
                );

                if (field.getName().equals("details")) {
                    assertEquals(
                            JsonNode.class,
                            field.getType(),
                            current + "." + field.getName() + " should be JsonNode"
                    );
                } else {
                    assertEquals(
                            String.class,
                            field.getType(),
                            current + "." + field.getName() + " should be String"
                    );
                }

                configFields.add(field.getName());
            }

            current = current.getSuperclass();
        }

        current = actionInstanceClass;

        boolean isActionInstanceClassSelectorType = Arrays.asList(actionInstanceClass.getInterfaces())
                .contains(SelectorActionInstance.class);

        boolean isActionConfigClassSelectorType = Arrays.asList(actionConfigClass.getInterfaces())
                .contains(SelectorActionConfig.class);

        Set<String> instanceFields = new HashSet<>();
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                assertTrue(
                        Modifier.isPrivate(field.getModifiers()),
                        current + "." + field.getName() + " should be private"
                );

                assertTrue(
                        Modifier.isFinal(field.getModifiers()),
                        current + "." + field.getName() + " should be final"
                );

                instanceFields.add(field.getName());
            }

            current = current.getSuperclass();
        }
        if(isActionInstanceClassSelectorType){
            assertTrue(
                    instanceFields.contains("selectorType"),
                    actionInstanceClass+ " should have private final SelectorType selectorType"
            );
        }
        if(isActionConfigClassSelectorType){
            assertTrue(
                    configFields.contains("cssSelector"),
                    actionConfigClass+ " should have private final String cssSelector"
            );
        }
        if(isActionConfigClassSelectorType){
            assertTrue(
                    configFields.contains("xpathSelector"),
                    actionConfigClass+ " should have private final String xpathSelector"
            );
        }
        for (String instanceField : instanceFields) {
            if (instanceField.equals("config")) {
                continue;
            }
            if(isActionInstanceClassSelectorType && instanceField.equals("selectorType")){
                continue;
            }
            assertTrue(
                    configFields.contains(instanceField),
                    actionConfigClass + " should have private final String " + instanceField
            );
        }

        for (String configField : configFields) {
            if (configField.equals("details") || configField.equals("actionName")) {
                continue;
            }
            if(isActionConfigClassSelectorType && configField.equals("cssSelector")){
                continue;
            }
            if(isActionConfigClassSelectorType && configField.equals("xpathSelector")){
                continue;
            }
            assertTrue(
                    instanceFields.contains(configField),
                    actionInstanceClass + " should have private final " + configField
            );
        }
    }

    private List<ActionProcessor> getActionProcessors() {
        return ServiceLoader.load(
                ActionProcessor.class,
                getClass().getClassLoader()
        ).stream().map(
                ServiceLoader.Provider::get
        ).collect(
                Collectors.toList()
        );
    }

}
