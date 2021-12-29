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

import io.perforator.sdk.loadgenerator.core.AbstractLoadGeneratorTest;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class TestNGLoadGeneratorTest extends AbstractLoadGeneratorTest<TestNGLoadGenerator, LoadGeneratorConfig, TestNGSuiteConfig> {

    public TestNGLoadGeneratorTest() {
        super(TestNGLoadGenerator.class, LoadGeneratorConfig.class, TestNGSuiteConfig.class);
    }

    @Override
    protected TestNGSuiteConfig buildDefaultSuiteConfig() throws Exception {
        TestNGSuiteConfig result = super.buildDefaultSuiteConfig();
        result.setName("Testing Suite");
        result.setSuiteXmlFile(getFileFromResource("suite.xml").getAbsolutePath());
        return result;
    }

    private File getFileFromResource(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("File not found! " + fileName);
        } else {
            try {
                return new File(resource.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("URI Exception", e);
            }
        }
    }
}
