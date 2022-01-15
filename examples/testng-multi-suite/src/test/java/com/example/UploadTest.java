package com.example;

import io.perforator.sdk.loadgenerator.core.Perforator;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.nio.file.Path;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Parameters;

public class UploadTest extends AbstractBaseTest {

    @Test
    @Parameters({"page", "delay", "file"})
    public void uploadFile(String page, String delay, String file) throws Exception {
        RemoteWebDriver driver = getRemoteWebDriver();
        String url = buildFullURL(page, delay);
        Path filePath = buildFilePath(file);
        
        WebElement fileUploadContainer = Perforator.transactionally("page: " + url, () -> {
            openUrlAndWaitForPageLoad(driver, url);
            return waitForElementToBeVisible(driver, FILE_UPLOAD_CONTAINER_SELECTOR);
        });
        
        Perforator.transactionally("upload: " + delay, () -> {
            fileUploadContainer.sendKeys(filePath.toAbsolutePath().toString());
            WebElement uploadButton = waitForElementToBeClickable(driver, FILE_UPLOAD_BUTTON_SELECTOR);
            uploadButton.click();
            
            waitForElementToBeVisible(driver, FILE_UPLOAD_SUCCESS_SELECTOR);
        });
    }

}
