package com.example;

import io.perforator.sdk.loadgenerator.embedded.AbstractSuiteProcessor;
import io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

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
public class ExampleSuiteProcessorA extends AbstractSuiteProcessor {

    @Override
    protected void processSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig, RemoteWebDriver driver) {
        driver.navigate().to("https://verifications.perforator.io/?delay=500ms");
        List<WebElement> links = driver.findElementsByCssSelector("nav ul li a.nav-link");

        if (links.isEmpty()) {
            throw new RuntimeException("Links are not found");
        }
    }
    
}
