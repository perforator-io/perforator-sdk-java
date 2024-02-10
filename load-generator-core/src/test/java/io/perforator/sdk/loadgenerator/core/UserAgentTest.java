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

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.chrome.ChromeOptions;

public class UserAgentTest {

    @Test
    public void verifyLoadAgentWithoutOverride() throws Exception {
        RemoteWebDriver driver = RemoteWebDriverHelper.createLocalChromeDriver();
        try {
            String userAgent = (String) driver.executeScript("return navigator.userAgent");
            assertNotNull(userAgent);
            assertTrue(userAgent.contains("Chrome"));
        } finally {
            driver.quit();
        }
    }

    @Test
    public void verifySimpleLoadAgentOverride() throws Exception {
        String requestedUserAgent = "Perforator/" + UUID.randomUUID();
        RemoteWebDriver driver = RemoteWebDriverHelper.createLocalChromeDriver(
                new ChromeOptions(),
                SuiteConfig.builder()
                        .applyDefaults()
                        .webDriverMode(WebDriverMode.local)
                        .webDriverUserAgent(requestedUserAgent)
                        .build()
        );
        try {
            String remoteUserAgent = (String) driver.executeScript("return navigator.userAgent");
            assertNotNull(remoteUserAgent);
            assertEquals(requestedUserAgent, remoteUserAgent);
        } finally {
            driver.quit();
        }
    }

    @Test
    public void verifyComplexLoadAgentOverride() throws Exception {
        String requestedUserAgent = ""
                + "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/121.0.0.0 "
                + "Safari/537.36 "
                + "Perforator/" + UUID.randomUUID();
        RemoteWebDriver driver = RemoteWebDriverHelper.createLocalChromeDriver(
                new ChromeOptions(),
                SuiteConfig.builder()
                        .applyDefaults()
                        .webDriverMode(WebDriverMode.local)
                        .webDriverUserAgent(requestedUserAgent)
                        .build()
        );
        try {
            String remoteUserAgent = (String) driver.executeScript("return navigator.userAgent");
            assertNotNull(remoteUserAgent);
            assertEquals(requestedUserAgent, remoteUserAgent);
        } finally {
            driver.quit();
        }
    }

}
