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
package io.perforator.sdk.loadgenerator.testng;

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for TestNG performance testing suites processed by
 * {@link TestNGLoadGenerator}.
 */
@ToString
@FieldNameConstants
public class TestNGSuiteConfig extends SuiteConfig {

    /**
     * File system location of TestNG suite.xml file
     */
    @Getter @Setter @FieldNameConstants.Include
    private String suiteXmlFile;

    /**
     * Default constructor looking up property defaults via the following providers:
     * <ul>
     *   <li>{@link System#getProperty(java.lang.String) }</li>
     *   <li>{@link System#getenv(java.lang.String) }</li>
     * </ul>
     */
    public TestNGSuiteConfig() {
        applyDefaults();
    }

    /**
     * Constructor looking up property defaults in user-supplied property providers.
     * @param defaultsProviders varargs of {@link Function functions} where to lookup up
     * for property defaults.
     */
    public TestNGSuiteConfig(Function<String, String>... defaultsProviders) {
        applyDefaults(defaultsProviders);
    }
    
}
