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

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Executes Perforator performance test using supplied processorClass.<br/>
 * Instance of processorClass is responsible for processing suite logic.<br/>
 * Supplied processorClass should implement io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteProcessor
 */
@Mojo(
        name = EmbeddedLoadGeneratorMojo.MOJO_NAME,
        threadSafe = true,
        defaultPhase = LifecyclePhase.NONE,
        requiresOnline = true,
        requiresDirectInvocation = true,
        requiresDependencyResolution = ResolutionScope.TEST
)
public class EmbeddedLoadGeneratorMojo extends AbstractLoadGeneratorMojo<LinkedHashMap<String, String>> {
    
    public static final String MOJO_NAME = "embedded";
    
    /**
     * The name of the test suite.<br/>
     * Typically this name is used as a top-level transaction covering 
     * the whole suite instance execution.<br/>
     * You can ignore this field, and in such case, suite name will be 
     * auto-generated as ${processorClass} if supplied, 
     * or defaulted to ${artifactId}-${version}.
     */
    @Parameter(
            required = false,
            defaultValue = "${project.build.finalName}",
            alias = SuiteConfig.Fields.name,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.name
    )
    protected String name;
    
    /**
     * The class name of the processor to execute while processing any suite instance.
     */
    @Parameter(
            required = false,
            defaultValue = "",
            alias = EmbeddedSuiteConfig.Fields.processorClass,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + EmbeddedSuiteConfig.Fields.processorClass
    )
    protected String processorClass;
    
    /**
     * This flag determines how processor instance should be instantiated:
     * <ul>
     * <li>true - single processor instance is instantiated for the whole duration of the performance test.</li>
     * <li>false - a new processor instance is instantiated for every suite instance run.</li>
     * </ul>
     */
    @Parameter(
            required = false,
            defaultValue = EmbeddedSuiteConfig.DEFAULT_PROCESSOR_SINGLETON_S,
            alias = EmbeddedSuiteConfig.Fields.processorSingleton,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + EmbeddedSuiteConfig.Fields.processorSingleton
    )
    protected String processorSingleton;
    
    /**
     * This parameter defines a list of suite configurations to be processed.
     */
    @Parameter(
            property = "suites",
            alias = "suites",
            required = false
    )
    protected LinkedHashMap<String, String>[] suites;

    @Override
    protected void preprocessAutowiredParameters() throws MojoFailureException {
        String defaultProjectBuildName = project.getBuild().getFinalName();
        
        if(processorClass != null && !processorClass.isBlank()) {
            if(name == null || name.isBlank() || name.equals(defaultProjectBuildName)) {
                name = processorClass;
            }
        }
        
        if(suites != null) {
            for (LinkedHashMap<String, String> suite : suites) {
                String suiteProcessorClass = suite.get("processorClass");
                String suiteName = suite.get("name");

                if (suiteProcessorClass == null || suiteProcessorClass.isEmpty()) {
                    continue;
                }

                if (suiteName == null || suiteName.isBlank() || suiteName.equals(defaultProjectBuildName)) {
                    suite.put("name", suiteProcessorClass);
                }
            }
        }
    }

    @Override
    protected List<LinkedHashMap<String, String>> getSuitesParams() throws MojoFailureException {
        if(suites != null && suites.length > 0) {
            return Arrays.asList(suites);
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    @Override
    protected Class buildLoadGeneratorClass(ClassLoader classLoader) throws MojoFailureException {
        return loadClass(
                classLoader, 
                ClassNames.EMBEDDED_LOAD_GENERATOR
        );
    }

    @Override
    protected Class buildSuiteConfigClass(ClassLoader classLoader) throws MojoFailureException {
        return loadClass(classLoader, ClassNames.EMBEDDED_SUITE_CONFIG);
    }

    @Override
    protected Object buildSuiteConfigInstance(Class suiteConfigClass, LinkedHashMap<String, String> suiteParams) throws MojoFailureException {
        return buildMapBasedSuiteConfig(suiteConfigClass, suiteParams);
    }

}
