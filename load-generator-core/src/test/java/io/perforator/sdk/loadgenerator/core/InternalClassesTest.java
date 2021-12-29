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
package io.perforator.sdk.loadgenerator.core;

import com.google.common.reflect.ClassPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InternalClassesTest {
    
    private static final String BASE_PACKAGE_NAME = "io.perforator.sdk.loadgenerator.core.internal";
    
    private final ClassLoader classLoader = getClass().getClassLoader();
    
    @Test
    public void internalClassesShouldNotBeVisibleOutside() throws Exception {
        Set<ClassPath.ClassInfo> classes = ClassPath.from(
                classLoader
        ).getTopLevelClasses(
                BASE_PACKAGE_NAME
        );
        
        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        
        for (ClassPath.ClassInfo classInfo : classes) {
            assertNotNull(classInfo.getName());
            
            Class clazz = classLoader.loadClass(classInfo.getName());
            assertNotNull(clazz);
            assertFalse(
                    Modifier.isPublic(clazz.getModifiers()),
                    clazz + " should not be public"
            );
            
        }
    }
    
}
