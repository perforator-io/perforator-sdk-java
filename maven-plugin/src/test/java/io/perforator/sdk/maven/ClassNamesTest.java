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
package io.perforator.sdk.maven;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClassNamesTest {
    
    @Test
    public void verify() throws Exception {
        for (Field field : ClassNames.class.getDeclaredFields()) {
            assertTrue(
                    Modifier.isStatic(field.getModifiers()), 
                    ClassNames.class.getName() + "." + field.getName() + " should be static"
            );
            assertEquals(
                    String.class,
                    field.getType(),
                    ClassNames.class.getName() + "." + field.getName() + " should be String"
            );
            
            String fieldValue = (String)field.get(null);
            assertNotNull(fieldValue);
            
            Class clazz = getClass().getClassLoader().loadClass(fieldValue);
            assertNotNull(clazz);
        }
    }
    
}
