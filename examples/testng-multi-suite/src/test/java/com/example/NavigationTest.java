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
package com.example;

import io.perforator.sdk.loadgenerator.core.Perforator;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class NavigationTest extends AbstractBaseTest {
    
    @Test
    @Parameters({"page", "delay"})
    public void visitPage(String page, String delay) {
        RemoteWebDriver driver = getRemoteWebDriver();
        String url = buildFullURL(page, delay);
        
        WebElement link = Perforator.transactionally("page: " + url, () -> {
            openUrlAndWaitForPageLoad(driver, url);
            waitForElementToBeVisible(driver, LINKS_CONTAINER_SELECTOR);
            
            List<WebElement> links = driver.findElementsByCssSelector(LINKS_ITEM_SELECTOR);
            if(links == null || links.isEmpty()) {
                throw new RuntimeException("Navigation link " + LINKS_ITEM_SELECTOR + " is not present");
            }
            
            return links.get((int)(Math.random() * links.size()));
        });
        
        Perforator.transactionally("page: " + link.getAttribute("href"), () -> {
            link.click();
            waitForPageLoad(driver);
            waitForElementToBeVisible(driver, LINKS_CONTAINER_SELECTOR);
        });
    }

}
