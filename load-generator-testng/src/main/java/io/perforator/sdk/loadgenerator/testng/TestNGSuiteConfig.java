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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Configuration for TestNG performance testing suites processed by
 * {@link TestNGLoadGenerator}.
 */
@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@FieldNameConstants
@Jacksonized
public class TestNGSuiteConfig extends SuiteConfig {

    /**
     * File system location of TestNG suite.xml file
     */
    String suiteXmlFile;
    
    public static abstract class TestNGSuiteConfigBuilder<C extends TestNGSuiteConfig, B extends TestNGSuiteConfigBuilder<C, B>> extends SuiteConfigBuilder<C, B> {

        private String name;

        @Override
        public B name(String name) {
            super.name(name);
            this.name = name;
            return (B) this;
        }

        public B suiteXmlFile(String suiteXmlFile) {
            if (suiteXmlFile != null && !suiteXmlFile.isBlank() && (name == null || name.isBlank())) {
                this.name(suiteXmlFile.trim());
            }
            this.suiteXmlFile = suiteXmlFile;
            return (B) this;
        }

    }
    
}
