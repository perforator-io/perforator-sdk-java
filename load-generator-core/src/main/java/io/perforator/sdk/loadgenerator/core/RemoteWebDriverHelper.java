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
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

//TODO: add javadoc
public class RemoteWebDriverHelper {

    private static boolean webDriverManagerInitialized = false;

    public static RemoteWebDriver createLocalChromeDriver() {
        return createLocalChromeDriver(null);
    }

    public static RemoteWebDriver createLocalChromeDriver(Capabilities capabilities) {
        SuiteConfig suiteConfig = new SuiteConfig();
        if (suiteConfig.getWebDriverMode() == WebDriverMode.cloud) {
            suiteConfig.setWebDriverMode(WebDriverMode.local);
        }

        return createLocalChromeDriver(capabilities, suiteConfig);
    }

    public static RemoteWebDriver createLocalChromeDriver(Capabilities capabilities, SuiteConfig suiteConfig) {
        if (!webDriverManagerInitialized) {
            synchronized (RemoteWebDriverHelper.class) {
                if (!webDriverManagerInitialized) {
                    WebDriverManager.chromedriver().setup();
                    webDriverManagerInitialized = true;
                }
            }
        }

        ChromeOptions chromeOptions = buildChromeOptions(capabilities);

        if (suiteConfig.getChromeMode() == ChromeMode.headless) {
            chromeOptions.setHeadless(true);
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-dev-shm-usage");
        }

        return applyDefaults(
                new ChromeDriver(
                        buildChromeDriverService(suiteConfig.isChromeDriverSilent()), 
                        chromeOptions
                ),
                suiteConfig
        );
    }

    public static ChromeOptions buildChromeOptions(Capabilities capabilities) {
        ChromeOptions result = new ChromeOptions();

        if (capabilities != null) {
            result.merge(capabilities);
        }

        return result;
    }

    public static RemoteWebDriver applyDefaults(RemoteWebDriver remoteWebDriver, SuiteConfig suiteConfig) {
        if (remoteWebDriver == null) {
            return null;
        }

        if (suiteConfig == null) {
            return remoteWebDriver;
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
                    suiteConfig.getWebDriverSessionImplicitlyWait().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        if (suiteConfig.getWebDriverSessionPageLoadTimeout() != null) {
            remoteWebDriver.manage().timeouts().pageLoadTimeout(
                    suiteConfig.getWebDriverSessionPageLoadTimeout().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        if (suiteConfig.getWebDriverSessionScriptTimeout() != null) {
            remoteWebDriver.manage().timeouts().setScriptTimeout(
                    suiteConfig.getWebDriverSessionScriptTimeout().toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        return remoteWebDriver;
    }
    
    private static ChromeDriverService buildChromeDriverService(boolean silent) {
        if(silent) {
            System.setProperty(
                    ChromeDriverService.CHROME_DRIVER_SILENT_OUTPUT_PROPERTY,
                    "true"
            );
        }
        
        ChromeDriverService result = ChromeDriverService.createDefaultService();
        
        if(silent) {
            result.sendOutputTo(OutputStream.nullOutputStream());
        }
        
        return result;
    }

}
