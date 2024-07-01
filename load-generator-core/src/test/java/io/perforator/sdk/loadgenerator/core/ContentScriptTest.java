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
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ContentScriptTest {

    @Test
    public void verifyContentScriptLoading() throws Exception {
        RemoteWebDriver driver = RemoteWebDriverHelper.createLocalChromeDriver(
                new ChromeOptions(),
                SuiteConfig.builder()
                        .applyDefaults()
                        .webDriverMode(WebDriverMode.local)
                        .webDriverContentScript("window.alert('AHAHAHA');")
                        .build()
        );

        try {
            driver.navigate().to("https://www.perforator.io/");
            driver.switchTo().alert().accept();
        } finally {
            driver.quit();
        }
    }

}
