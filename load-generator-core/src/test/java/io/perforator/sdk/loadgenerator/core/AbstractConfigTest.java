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

import io.perforator.sdk.loadgenerator.core.configs.Config;
import io.perforator.sdk.loadgenerator.core.configs.ConfigBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractConfigTest<T extends Config> {
    
    protected final Class<T> configClass;
    protected Class fieldsClass;

    public AbstractConfigTest(Class<T> configClass) {
        this.configClass = configClass;
    }
    
    protected abstract Map<String, String> buildFieldsForVerification() throws Exception;
    
    @BeforeAll
    public void setup() throws Exception {
        assertNotNull(configClass);
        
        fieldsClass = configClass.getClassLoader().loadClass(
                configClass.getName() + "$Fields"
        );
        assertNotNull(fieldsClass);
    }
    
    @Test
    public void verifyFieldNameConstants() throws Exception {
        for (Field field : fieldsClass.getDeclaredFields()) {
            assertTrue(Modifier.isPublic(field.getModifiers()));
            assertTrue(Modifier.isFinal(field.getModifiers()));
            assertTrue(Modifier.isStatic(field.getModifiers()));
            assertEquals(String.class, field.getType());
            
            assertNotNull(configClass.getDeclaredField(field.getName()));
        }
    }
    
    @Test
    public void verifyDefaultBuilder() throws Exception {
        String defaultsPrefix = newBuilder().getDefaultsPrefix();
        T defaultConfig = newConfigInstance();
        assertNotNull(defaultConfig);
        
        Map<String, String> verificationFields = buildFieldsForVerification();
        assertTrue(
                hasConfigFields(configClass, verificationFields.keySet()), 
                configClass + " doesn't have configurable fields: " + verificationFields
        );
        
        for (String fieldName : verificationFields.keySet()) {
            String fieldValue = verificationFields.get(fieldName);
            String systemPropertyName = defaultsPrefix + "." + fieldName;
            try {
                System.setProperty(systemPropertyName, fieldValue);
                T newConfig = newConfigInstance();
                assertFalse(
                        hasTheSameFieldValue(newConfig, defaultConfig, fieldName),
                        configClass + " has not propagated " + fieldName + " correctly"
                );
            } finally {
                System.getProperties().remove(systemPropertyName);
            }
        }
    }
    
    @Test
    public void verifyBuilderWithProvider() throws Exception {
        T defaultConfig = newConfigInstance(p -> null);
        assertNotNull(defaultConfig);
        
        Map<String, String> verificationFields = buildFieldsForVerification();
        assertTrue(
                hasConfigFields(configClass, verificationFields.keySet()), 
                configClass + " doesn't have configurable fields: " + verificationFields
        );
        
        for (String fieldName : verificationFields.keySet()) {
            String fieldValue = verificationFields.get(fieldName);
            Function<String, String> provider = p -> p.equals(fieldName) ? fieldValue : null;
            T newConfig = newConfigInstance(provider);
            
            assertFalse(
                    hasTheSameFieldValue(newConfig, defaultConfig, fieldName),
                    configClass + " has not propagated " + fieldName + " correctly"
            );
        }
    }
    
    protected ConfigBuilder<T, ?> newBuilder() throws Exception {
        for (Method method : configClass.getDeclaredMethods()) {
            if(!method.getName().equals("builder")) {
                continue;
            }
            
            if(!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            
            if(method.getParameterCount() > 0) {
                continue;
            }
            
            return (ConfigBuilder<T, ?>) method.invoke(null);
        }
        
        throw new IllegalArgumentException(
                "Config " + configClass + " doesn't have static builder method"
        );
    }
    
    protected T newConfigInstance() throws Exception {
        return newBuilder().applyDefaults().build();
    }
    
    protected T newConfigInstance(Function<String, String>... providers) throws Exception {
        return newBuilder().applyDefaults(providers).build();
    }
    
    protected static boolean hasConfigFields(Class configClass, Collection<String> fieldNames) throws Exception {
        for (String fieldName : fieldNames) {
            if(!hasConfigField(configClass, fieldName)) {
                return false;
            }
        }
        
        return true;
    }
    
    protected static boolean hasConfigField(Class configClass, String fieldName) throws Exception {
        Class current = configClass;
        
        while(current != null) {
            for (Field field : current.getDeclaredFields()) {
                if(Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                
                if(field.getName().equals(fieldName)) {
                    return true;
                }
            }
            
            current = current.getSuperclass();
        }
        
        return false;
    }
    
    protected static <T> boolean hasTheSameFieldValue(T obj1, T obj2, String fieldName) throws Exception {
        Class current = obj1.getClass();
        
        while(current != null) {
            for (Field field : current.getDeclaredFields()) {
                if(field.getName().equals(fieldName)) {
                    field.setAccessible(true);
                    
                    return Objects.equals(field.get(obj2), field.get(obj1));
                }
            }
            
            current = current.getSuperclass();
        }
        
        return false;
    }
    
}
