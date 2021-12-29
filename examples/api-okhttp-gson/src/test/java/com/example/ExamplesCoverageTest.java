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

import com.google.common.reflect.ClassPath;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ExamplesCoverageTest {
    
    @ParameterizedTest
    @MethodSource("getExampleClassNames")
    public void verifyExampleHasDedicatedTest(String exampleClassName) throws Exception {
        String testClassName = exampleClassName + "Test";
        
        try {
            getClass().getClassLoader().loadClass(testClassName);
        } catch(ReflectiveOperationException e) {
            fail("Class " + exampleClassName + " should have dedicated test " + testClassName);
        }
    }
    
    public static List<String> getExampleClassNames() throws Exception {
        ClassPath classPath = ClassPath.from(
                ApiClientsExample.class.getClassLoader()
        );
        Set<ClassPath.ClassInfo> classes = classPath.getTopLevelClassesRecursive(
                ApiClientsExample.class.getPackageName()
        );
        
        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        
        return classes.stream().map(
                ClassPath.ClassInfo::getName
        ).filter(
                className -> !className.endsWith("Test")
        ).collect(
                Collectors.toList()
        );
    }
    
}
