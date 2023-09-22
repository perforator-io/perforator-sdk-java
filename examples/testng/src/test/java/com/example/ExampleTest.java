package com.example;

import io.perforator.sdk.loadgenerator.core.Perforator;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

public class ExampleTest {
    
    @Test
    public void verify() throws Exception {
        RemoteWebDriver driver = PerSuiteRemoteWebDriverManager.getRemoteWebDriver();
        
        Perforator.transactionally("Open landing page and await to be loaded", () -> {
            driver.navigate().to("https://verifications.perforator.io/");
            awaitToBeVisible(driver, "#async-container");
        });
        
        Perforator.transactionally("Verify page 1", () -> {
            click(driver, "ul.navbar-nav li:nth-child(1) a");
            awaitToBeVisible(driver, "#async-container");
        });
        
        Perforator.transactionally("Verify page 2", () -> {
            click(driver, "ul.navbar-nav li:nth-child(2) a");
            awaitToBeVisible(driver, "#async-container");
        });
        
    }
    
    private static WebElement awaitToBeVisible(RemoteWebDriver driver, String cssSelector) {
        return awaitToBeVisible(driver, cssSelector, Duration.ofSeconds(30));
    }
    
    private static WebElement awaitToBeVisible(RemoteWebDriver driver, String cssSelector, Duration timeout) {
        return new WebDriverWait(
                driver,
                timeout
        ).until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(cssSelector)
                )
        );
    }
    
    private static WebElement click(RemoteWebDriver driver, String cssSelector) {
        return click(driver, cssSelector, Duration.ofSeconds(30));
    }
    
    private static WebElement click(RemoteWebDriver driver, String cssSelector, Duration timeout) {
        WebElement element = new WebDriverWait(
                driver,
                timeout
        ).until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(cssSelector)
                )
        );

        element.click();
        
        return element;
    }
    
}
