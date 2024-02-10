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

import io.perforator.sdk.loadgenerator.core.configs.Config;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.StringConverter;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class AbstractMojoTest<T extends AbstractLoadGeneratorMojo> extends AbstractMojoTestCase {
    
    protected static final LoadGeneratorConfig DEFAULT_LOAD_GENERATOR_CONFIG = LoadGeneratorConfig.builder().build();
    protected static final SuiteConfig DEFAULT_SUITE_CONFIG = SuiteConfig.builder().build();
    
    protected final Class<T> mojoClass;
    protected final String mojoName;
    
    protected MojoExecution mojoExecution;
    protected MojoDescriptor mojoDescriptor;
    protected PluginDescriptor pluginDescriptor;
    protected Map<String, Parameter> mojoParameters;
    protected Map<String, PlexusConfiguration> mojoFieldConfigs;

    public AbstractMojoTest(Class<T> mojoClass, String mojoName) {
        this.mojoClass = mojoClass;
        this.mojoName = mojoName;
    }
    
    @BeforeAll
    public void setup() throws Exception {
        super.setUp();
        
        mojoExecution = newMojoExecution(mojoName);
        assertNotNull(mojoExecution);
        
        mojoDescriptor = mojoExecution.getMojoDescriptor();
        assertNotNull(mojoDescriptor);
        
        pluginDescriptor = mojoDescriptor.getPluginDescriptor();
        assertNotNull(pluginDescriptor);
        
        mojoParameters = mojoDescriptor.getParameterMap();
        assertNotNull(mojoParameters);
        assertFalse(mojoParameters.isEmpty());
        
        assertNotNull(mojoDescriptor.getMojoConfiguration());
        assertTrue(mojoDescriptor.getMojoConfiguration().getChildCount() > 0);
        
        mojoFieldConfigs = new HashMap<>();
        for (PlexusConfiguration fieldConfig : mojoDescriptor.getMojoConfiguration().getChildren()) {
            mojoFieldConfigs.put(fieldConfig.getName(), fieldConfig);
        }
    }
    
    @Test
    public void verifyMojoDescription() throws Exception {
        assertNotNull(
                mojoClass + " should have description",
                mojoDescriptor.getDescription()
        );
        assertFalse(
                mojoClass + " should have non-empty description",
                mojoDescriptor.getDescription().isBlank()
        );
    }
    
    @Test
    public void verifyParametersDescription() throws Exception {
        for (Parameter mojoParameter : mojoParameters.values()) {
            String name = mojoParameter.getName();

            if (!name.startsWith(LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX) || !name.startsWith(SuiteConfig.DEFAULTS_FIELD_PREFIX)) {
                continue;
            }
            
            assertTrue(
                    mojoClass + "." + mojoParameter.getName() + " should have description",
                    mojoParameter.getDescription() != null && !mojoParameter.getDescription().isBlank()
            );
        }
    }
    
    @Test
    public void verifyParametersNaming() throws Exception {
        for (Parameter mojoParameter : mojoParameters.values()) {
            String name = mojoParameter.getName();
            
            if (!name.startsWith(LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX) || !name.startsWith(SuiteConfig.DEFAULTS_FIELD_PREFIX)) {
                continue;
            }
            
            assertEquals(
                    mojoClass + "." + name
                    + " should have alias defined as "
                    + name,
                    name,
                    mojoParameter.getAlias()
            );
        }
    }
    
    @Test
    public void verifyParametersType() throws Exception {
        for (Parameter mojoParameter : mojoParameters.values()) {
            String name = mojoParameter.getName();

            if (!name.startsWith(LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX) || !name.startsWith(SuiteConfig.DEFAULTS_FIELD_PREFIX)) {
                continue;
            }

            assertEquals(
                    mojoClass + "." + name + " should be String",
                    String.class.getName(),
                    mojoParameter.getType()
            );
        }
    }
    
    @Test
    public void verifyLoadGeneratorConfigProperties() throws Exception {
        verifyDefaults(
                DEFAULT_LOAD_GENERATOR_CONFIG,
                LoadGeneratorConfig.DEFAULTS_FIELD_PREFIX
        );
    }
    
    @Test
    public void verifySuiteConfigProperties() throws Exception {
        verifyDefaults(
                DEFAULT_SUITE_CONFIG,
                SuiteConfig.DEFAULTS_FIELD_PREFIX
        );
    }
    
    protected void verifyDefaults(Object defaultInstance, String defaultsPrefix) throws Exception {
        Set<String> instanceFields = getFieldNamesFromConfigClass(defaultInstance.getClass());
        for (String field : instanceFields) {
            if(field.equalsIgnoreCase("id") || field.equalsIgnoreCase(SuiteConfig.Fields.webDriverUserAgent)) {
                //skip fields validation intentionally
                continue;
            }
            Parameter mojoParameter = mojoParameters.get(field);
            assertNotNull(
                    mojoClass + "." + field + " is not defined",
                    mojoParameter
            );
            
            PlexusConfiguration mojoFieldConfig = mojoFieldConfigs.get(field);
            assertNotNull(
                    mojoClass + "." + field + " doesn't have property binding",
                    mojoParameter
            );
            
            assertEquals(
                    mojoClass + "." + field
                    + " should have property defined as "
                    + defaultsPrefix + "." + field,
                    "${" + defaultsPrefix + "." + field + "}",
                    mojoFieldConfig.getValue()
            );
            
            if(field.equals("name")) {
                assertEquals(
                        mojoClass + "." + field + " has incorrect default value",
                        "${project.build.finalName}",
                        mojoFieldConfig.getAttribute("default-value")
                );
            } else {
                verifyFieldDefaults(
                        defaultInstance,
                        mojoFieldConfig.getAttribute("default-value"),
                        field
                );
            }
        }
    }
    
    protected void verifyFieldDefaults(Object defaultConfigInstance, String mojoDefault , String fieldName) throws Exception {
        Field configField = getFieldsFromConfigClass(defaultConfigInstance.getClass()).get(fieldName);
        configField.setAccessible(true);

        Object fieldValue = configField.get(defaultConfigInstance);
        
        if((mojoDefault == null || mojoDefault.isBlank()) && fieldValue == null) {
            return;
        }
        
        boolean mojoDefaultEmpty = mojoDefault == null || mojoDefault.isBlank();
        boolean fieldValueEmpty = fieldValue == null;
        
        if(fieldValue != null) {
            if(Map.class.isAssignableFrom(fieldValue.getClass())) {
                fieldValueEmpty = ((Map)fieldValue).isEmpty();
            } else if(List.class.isAssignableFrom(fieldValue.getClass())) {
                fieldValueEmpty = ((List)fieldValue).isEmpty();
            }
        }
        
        if(mojoDefaultEmpty && fieldValueEmpty) {
            return;
        }

        if (mojoDefaultEmpty != fieldValueEmpty) {
            fail(
                    mojoClass + "." + fieldName
                    + " should have default value = "
                    + fieldValue
            );
        }
        
        if(configField.getType() == Duration.class) {
            assertEquals(
                    mojoClass + "." + fieldName
                    + " should have default value = "
                    + fieldValue,
                    StringConverter.toDuration(mojoDefault), 
                    fieldValue
            );
        } else {
            assertEquals(
                    mojoClass + "." + fieldName
                    + " should have default value = "
                    + fieldValue,
                    mojoDefault, 
                    fieldValue.toString()
            );
        }
    }
    
    protected static Set<String> getFieldNamesFromConfigClass(Class configClass) throws Exception {
        if (configClass == null || !Config.class.isAssignableFrom(configClass)) {
            return Collections.EMPTY_SET;
        }

        Set<String> result = new HashSet<>();

        while (configClass != null) {
            if (Config.class.isAssignableFrom(configClass)) {
                String fieldsClassName = configClass.getName() + "$Fields";
                Class fieldsClass = Class.forName(fieldsClassName);
                Field[] fields = fieldsClass.getDeclaredFields();

                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (!Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    if (!field.getType().equals(String.class)) {
                        continue;
                    }

                    result.add((String) field.get(null));
                }
            }

            configClass = configClass.getSuperclass();
        }

        return Set.copyOf(result);
    }
    
    protected static Map<String, Field> getFieldsFromConfigClass(Class configClass) throws Exception {
        if (configClass == null || !Config.class.isAssignableFrom(configClass)) {
            return Collections.EMPTY_MAP;
        }

        Map<String, Field> result = new HashMap<>();
        
        Set<String> fieldNames = getFieldNamesFromConfigClass(configClass);
        for (String fieldName : fieldNames) {
            Class classToCheck = configClass;
            while(classToCheck != null && Config.class.isAssignableFrom(classToCheck)) {
                Field[] fields = classToCheck.getDeclaredFields();
                Field foundField = null;
                for (Field field : fields) {
                    if(field.getName().equals(fieldName)) {
                        foundField = field;
                        break;
                    }
                }
                
                if(foundField != null) {
                    result.put(fieldName, foundField);
                    break;
                }
                
                classToCheck = classToCheck.getSuperclass();
            }
        }

        return Map.copyOf(result);
    }
    
}
