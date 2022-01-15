package com.example;

import java.nio.file.Files;
import java.nio.file.Path;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openqa.selenium.By;

public abstract class AbstractBaseTest {
    
    protected static final int DEFAULT_EXPLICIT_WAIT = 15;
    
    protected static final String NAVIGATION_URL_BASE = "https://verifications.perforator.io";
    protected static final String LINKS_CONTAINER_SELECTOR = "nav.navbar ul.navbar-nav";
    protected static final String LINKS_ITEM_SELECTOR = "ul.navbar-nav li.nav-item a.nav-link";
    protected static final String FILE_UPLOAD_CONTAINER_SELECTOR = "#file-input";
    protected static final String FILE_UPLOAD_BUTTON_SELECTOR = "#upload-btn";
    protected static final String FILE_UPLOAD_SUCCESS_SELECTOR = "#upload-success-span";

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected RemoteWebDriver getRemoteWebDriver() {
        return PerSuiteRemoteWebDriverManager.getRemoteWebDriver();
    }
    
    protected void openUrlAndWaitForPageLoad(RemoteWebDriver driver, String url) {
        driver.navigate().to(url);
        waitForPageLoad(driver);
    }

    protected void waitForPageLoad(RemoteWebDriver driver) {
        new WebDriverWait(driver, DEFAULT_EXPLICIT_WAIT).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
        );
    }
    
    protected WebElement waitForElementToBePresent(RemoteWebDriver driver, String cssSelector) {
        return new WebDriverWait(driver, DEFAULT_EXPLICIT_WAIT).until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector))
        );
    }
    
    protected WebElement waitForElementToBeVisible(RemoteWebDriver driver, String cssSelector) {
        return new WebDriverWait(driver, DEFAULT_EXPLICIT_WAIT).until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector))
        );
    }
    
    protected WebElement waitForElementToBeClickable(RemoteWebDriver driver, String cssSelector) {
        return new WebDriverWait(driver, DEFAULT_EXPLICIT_WAIT).until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector))
        );
    }
    
    protected Path buildFilePath(String location) {
        Path result = Path.of(location);
        
        if(Files.exists(result)) {
            return result;
        }
        
        result = Path.of("src", "test", "resources", location);
        if(Files.exists(result)) {
            return result;
        }
        
        result = Path.of("src", "main", "resources", location);
        if(Files.exists(result)) {
            return result;
        }
        
        result = Path.of("target", "test-classes", location);
        if(Files.exists(result)) {
            return result;
        }
        
        result = Path.of("target", location);
        if(Files.exists(result)) {
            return result;
        }
        
        throw new RuntimeException("Can't find file " + location);
    }
    
    protected String buildFullURL(String page, String delay) {
        StringBuilder urlBuilder = new StringBuilder(NAVIGATION_URL_BASE);
        
        if(page == null || page.isEmpty()) {
            urlBuilder.append("/");
        } else if(!page.startsWith("/")) {
            urlBuilder.append("/").append(page);
        } else {
            urlBuilder.append(page);
        }
        
        if(delay != null && !delay.isEmpty()) {
            urlBuilder.append("?delay=").append(delay);
        }
        
        return urlBuilder.toString();
    }
    
}
