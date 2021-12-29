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

import io.perforator.sdk.loadgenerator.core.configs.Configurable;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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
    
    protected static final LoadGeneratorConfig DEFAULT_LOAD_GENERATOR_CONFIG = new LoadGeneratorConfig(p -> null);
    protected static final SuiteConfig DEFAULT_SUITE_CONFIG = new SuiteConfig(p -> null);
    
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
        for (Field field : defaultInstance.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            
            Parameter mojoParameter = mojoParameters.get(field.getName());
            assertNotNull(
                    mojoClass + "." + field.getName() + " is not defined",
                    mojoParameter
            );
            
            PlexusConfiguration mojoFieldConfig = mojoFieldConfigs.get(field.getName());
            assertNotNull(
                    mojoClass + "." + field.getName() + " doesn't have property binding",
                    mojoParameter
            );
            
            assertEquals(
                    mojoClass + "." + field.getName()
                    + " should have property defined as "
                    + defaultsPrefix + "." + field.getName(),
                    "${" + defaultsPrefix + "." + field.getName() + "}",
                    mojoFieldConfig.getValue()
            );
            
            if(field.getName().equals("name")) {
                assertEquals(
                        mojoClass + "." + field.getName() + " has incorrect default value",
                        "${project.build.finalName}",
                        mojoFieldConfig.getAttribute("default-value")
                );
            } else {
                verifyFieldDefaults(
                        defaultInstance,
                        mojoFieldConfig.getAttribute("default-value"),
                        field.getName()
                );
            }
        }
    }
    
    protected void verifyFieldDefaults(Object defaultConfigInstance, String mojoDefault , String fieldName) throws Exception {
        Field configField = defaultConfigInstance.getClass().getDeclaredField(fieldName);
        configField.setAccessible(true);

        Object fieldValue = configField.get(defaultConfigInstance);
        
        if((mojoDefault == null || mojoDefault.isBlank()) && fieldValue == null) {
            return;
        }

        if ((mojoDefault == null && fieldValue != null) || (mojoDefault != null && fieldValue == null)) {
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
                    Configurable.parseDuration(mojoDefault), 
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
    
}
