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
package io.perforator.sdk.loadgenerator.embedded;

import io.perforator.sdk.loadgenerator.core.Threaded;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import org.openqa.selenium.remote.RemoteWebDriver;

public class EmbeddedSuiteProcessorMock extends AbstractSuiteProcessor {
    
    private static final String VERIFICATIONS_APP_URL = new LoadGeneratorConfig().getApiBaseUrl().replace("api", "verifications");
    
    @Override
    protected void processSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig, RemoteWebDriver remoteWebDriver) {
        remoteWebDriver.navigate().to(VERIFICATIONS_APP_URL + "/");
        Threaded.sleep(100);
    }
    
}
