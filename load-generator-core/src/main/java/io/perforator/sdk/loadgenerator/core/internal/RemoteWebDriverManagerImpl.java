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

import io.perforator.sdk.loadgenerator.core.RemoteWebDriverHelper;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RemoteWebDriverManagerImpl implements RemoteWebDriverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteWebDriverManagerImpl.class);

    private final TimeProvider timeProvider;
    private final EventsRouter eventsRouter;

    public RemoteWebDriverManagerImpl(TimeProvider timeProvider, EventsRouter eventsRouter) {
        this.timeProvider = timeProvider;
        this.eventsRouter = eventsRouter;
    }

    @Override
    public void onSuiteInstanceFinished(long timestamp, SuiteContextImpl context, Throwable error) {
        for (RemoteWebDriverContextImpl driverContext : context.getDrivers().values()) {
            try {
                driverContext.getRemoteWebDriver().quit();
            } catch (RuntimeException exception) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Can't terminate remote browser", exception);
                }
            }
        }
    }

    @Override
    public RemoteWebDriverContextImpl startRemoteWebDriver(SuiteContextImpl suiteContext, Capabilities capabilities) {
        if (suiteContext == null) {
            throw new IllegalArgumentException(
                    "Can't start selenium web driver - suiteInstanceID should not be blank"
            );
        }

        SuiteConfig suiteConfig = suiteContext.getSuiteConfig();

        RemoteWebDriver remoteWebDriver;
        if (suiteConfig.getWebDriverMode() == WebDriverMode.cloud) {
            RemoteWebDriverCommandExecutor commandExecutor = new RemoteWebDriverCommandExecutor(
                    timeProvider,
                    eventsRouter,
                    suiteContext
            );

            remoteWebDriver = new RemoteWebDriver(
                    commandExecutor,
                    RemoteWebDriverHelper.buildChromeOptions(capabilities)
            );

            RemoteWebDriverHelper.applyDefaults(
                    remoteWebDriver,
                    suiteConfig
            );
        } else if (suiteConfig.getWebDriverMode() == WebDriverMode.local) {
            remoteWebDriver = RemoteWebDriverHelper.createLocalChromeDriver(
                    capabilities,
                    suiteConfig
            );
        } else {
            throw new RuntimeException(
                    "webDriverMode " + suiteConfig.getWebDriverMode() + " is not supported"
            );
        }

        long timestamp = timeProvider.getCurrentTime();
        RemoteWebDriverContextImpl result = new RemoteWebDriverContextImpl(
                timestamp,
                suiteContext,
                remoteWebDriver
        );
        suiteContext.getDrivers().put(
                result.getSessionID(),
                result
        );

        eventsRouter.onRemoteWebDriverStarted(timestamp, result);

        return result;
    }

}
