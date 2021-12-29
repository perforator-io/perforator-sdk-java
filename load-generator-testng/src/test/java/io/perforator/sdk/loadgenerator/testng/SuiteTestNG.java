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
import io.perforator.sdk.loadgenerator.core.Threaded;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;

public class SuiteTestNG {
    
    private static final String VERIFICATIONS_APP_URL = new LoadGeneratorConfig().getApiBaseUrl().replace("api", "verifications");

    @Test(timeOut = 30000)
    public void openUrl() {
        RemoteWebDriver remoteWebDriver = Perforator.startRemoteWebDriver();
        remoteWebDriver.navigate().to(VERIFICATIONS_APP_URL + "/");
        Threaded.sleep(1000);
        remoteWebDriver.quit();
    }
}