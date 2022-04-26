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
package io.perforator.sdk.loadgenerator.core.internal;

import io.perforator.sdk.loadgenerator.core.context.RemoteWebDriverContext;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Objects;

final class RemoteWebDriverContextImpl implements RemoteWebDriverContext {
    
    private final long startedAt;
    private final LoadGeneratorContextImpl loadGeneratorContext;
    private final SuiteInstanceContextImpl suiteInstanceContext;
    private final String sessionID;
    private final String browserName;
    private final String browserVersion;
    private final RemoteWebDriver remoteWebDriver;

    RemoteWebDriverContextImpl(long startedAt, SuiteInstanceContextImpl suiteInstanceContext, RemoteWebDriver remoteWebDriver) {
        this.startedAt = startedAt;
        this.loadGeneratorContext = suiteInstanceContext.getLoadGeneratorContext();
        this.suiteInstanceContext = suiteInstanceContext;
        this.sessionID = remoteWebDriver.getSessionId().toString();
        this.browserName = remoteWebDriver.getCapabilities().getBrowserName();
        this.browserVersion = remoteWebDriver.getCapabilities().getVersion();
        this.remoteWebDriver = remoteWebDriver;
    }

    @Override
    public long getStartedAt() {
        return startedAt;
    }

    public LoadGeneratorContextImpl getLoadGeneratorContext() {
        return loadGeneratorContext;
    }
    
    public SuiteInstanceContextImpl getSuiteInstanceContext() {
        return suiteInstanceContext;
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.sessionID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteWebDriverContextImpl other = (RemoteWebDriverContextImpl) obj;
        return Objects.equals(this.sessionID, other.sessionID);
    }

}
