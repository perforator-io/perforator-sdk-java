package com.example;

import io.perforator.sdk.loadgenerator.embedded.AbstractSuiteProcessor;
import io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Example implementation of {@link io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteProcessor}.
 * 
 * This example is based on {@link AbstractSuiteProcessor} implementation,
 * so a new {@link RemoteWebDriver} is created automatically before processing 
 * suite instance, and the same driver is automatically closed at the end of the 
 * processing.
 * 
 * Additionally, you can <b>transactionally</b> measure performance of any 
 * block of code. So, its results will be automatically reported to the 
 * Perforator platform and you can analyze it using execution statistics web UI. 
 * Please just call one of the below methods  and supply lambda with your logic:
 * <ul>
 *   <li>{@link AbstractSuiteProcessor#transactionally(java.lang.String, java.lang.Runnable) }</li>
 *   <li>{@link AbstractSuiteProcessor#transactionally(java.lang.String, java.util.function.Supplier) }</li>
 *   <li>{@link AbstractSuiteProcessor#transactionally(java.lang.String, java.lang.Object, java.util.function.Consumer) }</li>
 *   <li>{@link AbstractSuiteProcessor#transactionally(java.lang.String, java.lang.Object, java.util.function.Function) }</li>
 * </ul>
 * 
 * Feel free to throw away all internal logic in this class and start your own 
 * implementation from the ground up.
 */
public class ExampleSuiteProcessor extends AbstractSuiteProcessor {

    @Override
    protected void processSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig, RemoteWebDriver driver) {
        transactionally("Open landing page and await to be loaded", () -> {
            driver.navigate().to("https://verifications.perforator.io/");
            awaitToBeVisible(driver, "#async-container");
        });
        
        transactionally("Verify page 1", () -> {
            click(driver, "ul.navbar-nav li:nth-child(1) a");
            awaitToBeVisible(driver, "#async-container");
        });
        
        transactionally("Verify page 1", () -> {
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
                timeout.toSeconds()
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
                timeout.toSeconds()
        ).until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(cssSelector)
                )
        );

        element.click();
        
        return element;
    }
    
}
