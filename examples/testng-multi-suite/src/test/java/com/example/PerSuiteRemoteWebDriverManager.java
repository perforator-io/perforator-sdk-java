package com.example;

import io.perforator.sdk.loadgenerator.core.Perforator;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class PerSuiteRemoteWebDriverManager implements ISuiteListener {
    
    private static final ThreadLocal<RemoteWebDriver> REMOTE_WEB_DRIVER = new InheritableThreadLocal<>();

    @Override
    public void onStart(ISuite suite) {
        Perforator.transactionally(
                Perforator.OPEN_WEB_DRIVER_TRANSACTION_NAME, 
                () -> REMOTE_WEB_DRIVER.set(Perforator.startRemoteWebDriver())
        );
    }

    @Override
    public void onFinish(ISuite suite) {
        if(REMOTE_WEB_DRIVER.get() != null) {
            try {
                Perforator.transactionally(
                        Perforator.CLOSE_WEB_DRIVER_TRANSACTION_NAME, 
                        () -> REMOTE_WEB_DRIVER.get().quit()
                );
            } finally {
                REMOTE_WEB_DRIVER.remove();
            }
        }
    }
    
    public static RemoteWebDriver getRemoteWebDriver() {
        return REMOTE_WEB_DRIVER.get();
    }
    
}
