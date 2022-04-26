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
package io.perforator.sdk.loadgenerator.embedded;

import io.perforator.sdk.loadgenerator.core.AbstractLoadGenerator;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.context.SuiteInstanceContext;
import io.perforator.sdk.loadgenerator.core.service.IntegrationService;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;

//TODO: add javadoc
public class EmbeddedLoadGenerator extends AbstractLoadGenerator {
    
    private final HashMap<String, Class<EmbeddedSuiteProcessor>> processorClasses = new HashMap<>();
    private final HashMap<String, Constructor<EmbeddedSuiteProcessor>> processorConstructors = new HashMap<>();
    private final HashMap<String, EmbeddedSuiteProcessor> processorSingletons = new HashMap<>();
    
    public EmbeddedLoadGenerator(LoadGeneratorConfig loadGeneratorConfig, List<EmbeddedSuiteConfig> suiteConfigs) {
        this(null, loadGeneratorConfig, suiteConfigs);
    }

    private EmbeddedLoadGenerator(IntegrationService mediator, LoadGeneratorConfig loadGeneratorConfig, List<EmbeddedSuiteConfig> suiteConfigs) {
        super(mediator, loadGeneratorConfig, copy(suiteConfigs));
        
        if(suiteConfigs == null || suiteConfigs.isEmpty()) {
            throw new RuntimeException("suites are required");
        }
        
        for (EmbeddedSuiteConfig suiteConfig : suiteConfigs) {
            if(suiteConfig.getProcessorClass() == null) {
                throw new RuntimeException(
                        SuiteConfig.DEFAULTS_FIELD_PREFIX
                        + "." + EmbeddedSuiteConfig.Fields.processorClass
                        + " is required."
                );
            }
            
            Class<EmbeddedSuiteProcessor> processorClass = getProcessorClass(
                    getClass().getClassLoader(),
                    suiteConfig.getProcessorClass()
            );
            processorClasses.put(suiteConfig.getId(), processorClass);
            
            Constructor<EmbeddedSuiteProcessor> processorConstructor = getProcessorConstructor(
                    processorClass
            );
            processorConstructors.put(suiteConfig.getId(), processorConstructor);
            
            if(suiteConfig.isProcessorSingleton()) {
                processorSingletons.put(
                        suiteConfig.getId(), 
                        getProcessorInstance(processorConstructor)
                );
            }
        }
    }

    @Override
    protected void runSuite(SuiteInstanceContext suiteInstanceContext) {
        String suiteInstanceID = suiteInstanceContext.getSuiteInstanceID();
        EmbeddedSuiteConfig embeddedSuiteConfig = (EmbeddedSuiteConfig) suiteInstanceContext.getSuiteConfigContext().getSuiteConfig();
        
        EmbeddedSuiteProcessor processor;
        if(embeddedSuiteConfig.isProcessorSingleton()) {
            processor = processorSingletons.get(
                    embeddedSuiteConfig.getId()
            );
        } else {
            processor = getProcessorInstance(
                    processorConstructors.get(
                            embeddedSuiteConfig.getId()
                    )
            );
        }
        
        processor.onBeforeSuite(
                suiteInstanceContext.getIterationNumber(),
                suiteInstanceID, 
                embeddedSuiteConfig
        );
        
        Throwable suiteInstanceProcessingError = null;
        try {
            if(shouldBeFinished()){
                return;
            }
            processor.processSuite(
                    suiteInstanceContext.getIterationNumber(),
                    suiteInstanceID,
                    embeddedSuiteConfig
            );
        } catch(RuntimeException e) {
            suiteInstanceProcessingError = e;
            throw e;
        } finally {
            processor.onAfterSuite(
                    suiteInstanceContext.getIterationNumber(),
                    suiteInstanceID,
                    embeddedSuiteConfig, 
                    suiteInstanceProcessingError
            );
        }
    }
    
    private static Class<EmbeddedSuiteProcessor> getProcessorClass(ClassLoader classLoader, String processorClassName) {
        Class processorClass = null;
        
        try {
            processorClass = classLoader.loadClass(processorClassName);
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                    + "."
                    + EmbeddedSuiteConfig.Fields.processorClass
                    + "="
                    + processorClassName
                    + " can't be loaded",
                    e
            );
        }
        
        if(!EmbeddedSuiteProcessor.class.isAssignableFrom(processorClass)) {
            throw new RuntimeException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                    + "."
                    + EmbeddedSuiteConfig.Fields.processorClass
                    + "="
                    + processorClassName
                    + " should implement "
                    + EmbeddedSuiteProcessor.class
            );
        }
        
        return (Class<EmbeddedSuiteProcessor>) processorClass;
    }
    
    private static Constructor<EmbeddedSuiteProcessor> getProcessorConstructor(Class<EmbeddedSuiteProcessor> processorClass) {
        try {
            return processorClass.getConstructor();
        } catch(NoSuchMethodException e) {
            throw new RuntimeException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                    + "."
                    + EmbeddedSuiteConfig.Fields.processorClass
                    + "="
                    + processorClass
                    + " should have default no-args constructor",
                    e
            );
        }
    }
    
    private static EmbeddedSuiteProcessor getProcessorInstance(Constructor<EmbeddedSuiteProcessor> processorConstructor) {
        try {
            return processorConstructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                    + "."
                    + EmbeddedSuiteConfig.Fields.processorClass
                    + "="
                    + processorConstructor.getDeclaringClass()
                    + " can't be instantiated",
                    e
            );
        }
    }
    
}
