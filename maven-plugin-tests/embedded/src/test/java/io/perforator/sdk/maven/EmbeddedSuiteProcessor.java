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

import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.embedded.AbstractSuiteProcessor;
import io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;

public class EmbeddedSuiteProcessor extends AbstractSuiteProcessor {

    public static final String BASE_URL = LoadGeneratorConfig.builder().buildWithDefaults().getApiBaseUrl().replace("api", "verifications");;

    @Override
    protected void processSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig, RemoteWebDriver driver) {

        transactionally("Visit page 1", () -> {
            driver.navigate().to(BASE_URL + "/?delay=500ms");
            List<WebElement> links = driver.findElementsByCssSelector("nav ul li a.nav-link");

            if (links.isEmpty()) {
                throw new RuntimeException("Links are not found");
            }
        });

        transactionally("Visit page 2", () -> {
            driver.navigate().to(BASE_URL + "/vobis?delay=1000ms");
            List<WebElement> links = driver.findElementsByCssSelector("nav ul li a.nav-link");

            if (links.isEmpty()) {
                throw new RuntimeException("Links are not found");
            }
        });
    }

}