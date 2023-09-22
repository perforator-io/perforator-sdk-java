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
package io.perforator.sdk.loadgenerator.core.mock;

import io.perforator.sdk.loadgenerator.core.context.RemoteWebDriverContext;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteWebDriverContextMock implements RemoteWebDriverContext {
    
    private final long startedAt;
    private final String sessionID;
    private final String browserName;
    private final String browserVersion;
    private final RemoteWebDriver remoteWebDriver;

    public RemoteWebDriverContextMock(RemoteWebDriver remoteWebDriver) {
        this.startedAt = System.currentTimeMillis();
        this.sessionID = remoteWebDriver.getSessionId().toString();
        this.browserName = remoteWebDriver.getCapabilities().getBrowserName();
        this.browserVersion = remoteWebDriver.getCapabilities().getBrowserVersion();
        this.remoteWebDriver = remoteWebDriver;
    }

    @Override
    public long getStartedAt() {
        return startedAt;
    }

    @Override
    public String getSessionID() {
        return sessionID;
    }

    @Override
    public String getBrowserName() {
        return browserName;
    }

    @Override
    public String getBrowserVersion() {
        return browserVersion;
    }

    @Override
    public RemoteWebDriver getRemoteWebDriver() {
        return remoteWebDriver;
    }
    
}
