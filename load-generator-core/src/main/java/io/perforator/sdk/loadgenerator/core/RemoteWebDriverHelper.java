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

import io.github.bonigarcia.wdm.WebDriverManager;
import io.perforator.sdk.loadgenerator.core.configs.ChromeMode;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteWebDriverHelper {

    private static final ReentrantLock LOCAL_DRIVER_START_LOCK = new ReentrantLock();
    private static boolean webDriverManagerInitialized = false;

    public static RemoteWebDriver createLocalChromeDriver() {
        return createLocalChromeDriver(null);
    }

    public static RemoteWebDriver createLocalChromeDriver(ChromeOptions chromeOptions) {
        return createLocalChromeDriver(
                chromeOptions,
                SuiteConfig.builder()
                        .applyDefaults()
                        .webDriverMode(WebDriverMode.local)
                        .build()
        );
    }

    public static RemoteWebDriver createLocalChromeDriver(ChromeOptions chromeOptions, SuiteConfig suiteConfig) {
        if (chromeOptions == null) {
            chromeOptions = new ChromeOptions();
        }

        if (suiteConfig.getChromeMode() == ChromeMode.headless) {
            chromeOptions.addArguments(
                    "--headless=new",
                    "--no-sandbox",
                    "--disable-gpu",
                    "--disable-dev-shm-usage"
            );
        }

        chromeOptions.setAcceptInsecureCerts(
                suiteConfig.isWebDriverAcceptInsecureCerts()
        );
        
        if (suiteConfig.isWebDriverHttpsUpgrades()) {
            chromeOptions.addArguments("--enable-features=HttpsUpgrades");
        } else {
            chromeOptions.addArguments("--disable-features=HttpsUpgrades");
        }
        
        String webDriverUserAgent = suiteConfig.getWebDriverUserAgent();
        if(webDriverUserAgent != null && !webDriverUserAgent.isBlank()) {
            chromeOptions.addArguments("--user-agent=" + webDriverUserAgent.trim());
        }

        if (!webDriverManagerInitialized) {
            LOCAL_DRIVER_START_LOCK.lock();
            try {
                if (!webDriverManagerInitialized) {
                    WebDriverManager.chromedriver().setup();
                    webDriverManagerInitialized = true;
                }
            } finally {
                LOCAL_DRIVER_START_LOCK.unlock();
            }
        }

        LOCAL_DRIVER_START_LOCK.lock();
        try {
            return applyDefaults(
                    new ChromeDriver(chromeOptions),
                    suiteConfig
            );
        } finally {
            LOCAL_DRIVER_START_LOCK.unlock();
        }
    }

    public static RemoteWebDriver applyDefaults(RemoteWebDriver remoteWebDriver, SuiteConfig suiteConfig) {
        if (remoteWebDriver == null) {
            return null;
        }

        if (suiteConfig == null) {
            return remoteWebDriver;
        }
        
        String contentScript = suiteConfig.getWebDriverContentScript();
        if (contentScript != null && !contentScript.isBlank()) {
            Command cdpCommand = new Command(
                    remoteWebDriver.getSessionId(),
                    "executeCdpCommand",
                    Map.of(
                            "cmd", "Page.addScriptToEvaluateOnNewDocument",
                            "params", Map.of("source", contentScript)
                    )
            );

            try {
                remoteWebDriver.getCommandExecutor().execute(cdpCommand);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Can't execute " + SuiteConfig.Fields.webDriverContentScript + ": " + contentScript,
                        e
                );
            }
        }

        if (suiteConfig.isWebDriverUseLocalFileDetector() && suiteConfig.getWebDriverMode() == WebDriverMode.cloud) {
            remoteWebDriver.setFileDetector(
                    new LocalFileDetector()
            );
        }

        remoteWebDriver.manage().window().setSize(
                new Dimension(
                        suiteConfig.getWebDriverWindowWidth(),
                        suiteConfig.getWebDriverWindowHeight()
                )
        );

        if (suiteConfig.getWebDriverSessionImplicitlyWait() != null) {
            remoteWebDriver.manage().timeouts().implicitlyWait(
                    suiteConfig.getWebDriverSessionImplicitlyWait()
            );
        }

        if (suiteConfig.getWebDriverSessionPageLoadTimeout() != null) {
            remoteWebDriver.manage().timeouts().pageLoadTimeout(
                    suiteConfig.getWebDriverSessionPageLoadTimeout()
            );
        }

        if (suiteConfig.getWebDriverSessionScriptTimeout() != null) {
            remoteWebDriver.manage().timeouts().scriptTimeout(
                    suiteConfig.getWebDriverSessionScriptTimeout()
            );
        }

        return remoteWebDriver;
    }

}
