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
import io.perforator.sdk.loadgenerator.testng.TestNGSuiteConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Executes Perforator performance test using TestNG-based suite(s).
 */
@Mojo(
        name = TestNGLoadGeneratorMojo.MOJO_NAME,
        threadSafe = true,
        defaultPhase = LifecyclePhase.NONE,
        requiresOnline = true,
        requiresDirectInvocation = true,
        requiresDependencyResolution = ResolutionScope.TEST
)
public class TestNGLoadGeneratorMojo extends AbstractLoadGeneratorMojo<LinkedHashMap<String, String>> {
    
    public static final String MOJO_NAME = "testng";
    
    /**
     * The name of the test suite.<br/>
     * Typically this name is used as a top-level transaction covering 
     * the whole suite instance execution.<br/>
     * You can ignore this field, and in such case, suite name will be 
     * auto-generated as the name/location of TestNG suite.xml.
     */
    @Parameter(
            required = false,
            defaultValue = "${project.build.finalName}",
            alias = SuiteConfig.Fields.name,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + SuiteConfig.Fields.name
    )
    protected String name;
    
    /**
     * File system location of TestNG suite.xml file
     */
    @Parameter(
            required = false,
            defaultValue = "",
            alias = TestNGSuiteConfig.Fields.suiteXmlFile,
            property = SuiteConfig.DEFAULTS_FIELD_PREFIX + "." + TestNGSuiteConfig.Fields.suiteXmlFile
    )
    protected String suiteXmlFile;
    
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
        
        if(suiteXmlFile != null && !suiteXmlFile.isBlank()) {
            if(name == null || name.isBlank() || name.equals(defaultProjectBuildName)) {
                name = getRelativeSuiteXmlPath(suiteXmlFile);
            }
        }
        
        if(suites != null) {
            for (LinkedHashMap<String, String> suite : suites) {
                String suiteSuiteXmlFile = suite.get("suiteXmlFile");
                String suiteName = suite.get("name");

                if (suiteSuiteXmlFile == null || suiteSuiteXmlFile.isEmpty()) {
                    continue;
                }

                if (suiteName == null || suiteName.isBlank() || suiteName.equals(defaultProjectBuildName)) {
                    suite.put("name", getRelativeSuiteXmlPath(suiteSuiteXmlFile));
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
                ClassNames.TESTNG_LOAD_GENERATOR
        );
    }

    @Override
    protected Class buildSuiteConfigClass(ClassLoader classLoader) throws MojoFailureException {
        return loadClass(classLoader, ClassNames.TESTNG_SUITE_CONFIG);
    }

    @Override
    protected Object buildSuiteConfigInstance(Class suiteConfigClass, LinkedHashMap<String, String> suiteParams) throws MojoFailureException {
        return buildMapBasedSuiteConfig(suiteConfigClass, suiteParams);
    }
    
    private String getRelativeSuiteXmlPath(String suiteXmlFile) {
        if(suiteXmlFile == null || suiteXmlFile.isBlank()) {
            return suiteXmlFile;
        }
        
        Path suiteXmlFilePath = Path.of(suiteXmlFile);
        if(!Files.exists(suiteXmlFilePath)) {
            return suiteXmlFile;
        }
        
        try {
            return suiteXmlFilePath.toRealPath().toString().replace(
                    project.getBasedir().toPath().toRealPath().toString() + File.separator,
                    ""
            );
        } catch(IOException e) {
            return suiteXmlFile;
        }
    }

}
