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

import io.perforator.sdk.loadgenerator.core.Perforator;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class OpenVerificationSiteTestNG {

    @Test
    @Parameters({"page"})
    public void verify(String page) {
        RemoteWebDriver driver = Perforator.startRemoteWebDriver();
        driver.navigate().to(page);
        driver.quit();
    }

}
