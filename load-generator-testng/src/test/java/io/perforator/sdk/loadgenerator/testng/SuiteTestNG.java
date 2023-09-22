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

import io.perforator.sdk.loadgenerator.core.Perforator;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

public class SuiteTestNG {

    private static final String VERIFICATIONS_APP_URL = LoadGeneratorConfig.builder().buildWithDefaults().getApiBaseUrl().replace("api", "verifications");
    private static final String ASYNC_CONTAINER_ID = "async-container";

    @Test(timeOut = 30000)
    public void openUrl() {
        RemoteWebDriver remoteWebDriver = Perforator.startRemoteWebDriver();
        remoteWebDriver.navigate().to(VERIFICATIONS_APP_URL + "/?delay=1000ms");

        WebElement element = new WebDriverWait(
                remoteWebDriver,
                Duration.ofSeconds(15)
        ).until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#" + ASYNC_CONTAINER_ID)
                )
        );

        Assertions.assertNotNull(element);
        Assertions.assertEquals(
                ASYNC_CONTAINER_ID,
                element.getAttribute("id")
        );

        remoteWebDriver.quit();
    }
}
