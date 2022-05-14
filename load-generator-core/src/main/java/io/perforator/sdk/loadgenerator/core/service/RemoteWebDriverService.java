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
package io.perforator.sdk.loadgenerator.core.service;

import io.perforator.sdk.loadgenerator.core.context.RemoteWebDriverContext;
import io.perforator.sdk.loadgenerator.core.context.SuiteInstanceContext;
import org.openqa.selenium.chrome.ChromeOptions;

//TODO: add javadoc
public interface RemoteWebDriverService<S extends SuiteInstanceContext, R extends RemoteWebDriverContext> {
    
    R startRemoteWebDriver(S suiteContext, ChromeOptions chromeOptions);
    
}
