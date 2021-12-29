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
import io.perforator.sdk.loadgenerator.core.configs.WebDriverMode;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import okhttp3.OkHttpClient;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static io.perforator.sdk.loadgenerator.core.Threaded.sleep;

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
            CustomHttpCommandExecutor executor = new CustomHttpCommandExecutor(
                    suiteContext,
                    suiteContext.getLoadGeneratorContext().getBrowserCloudContext().getSeleniumHubURL(),
                    suiteContext.getLoadGeneratorContext().getOkHttpClient()
            );

            remoteWebDriver = new RemoteWebDriver(
                    executor,
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

    private static class CustomHttpClientFactory implements HttpClient.Factory {

        final CustomSeleniumHttpClient client;

        public CustomHttpClientFactory(SuiteContextImpl suiteContext, OkHttpClient httpClient, URL seleniumHUB) {
            this.client = new CustomSeleniumHttpClient(
                    suiteContext,
                    httpClient,
                    seleniumHUB
            );
        }

        @Override
        public HttpClient createClient(URL url) {
            return client;
        }

        @Override
        public HttpClient.Builder builder() {
            throw new IllegalStateException("unsupported");
        }

        @Override
        public void cleanupIdleClients() {
        }

    }

    private static class CustomSeleniumHttpClient extends org.openqa.selenium.remote.internal.OkHttpClient {

        private final SuiteContextImpl suiteContext;

        public CustomSeleniumHttpClient(SuiteContextImpl suiteContext, OkHttpClient client, URL url) {
            super(client, url);
            this.suiteContext = suiteContext;
        }

        @Override
        public HttpResponse execute(HttpRequest request) throws IOException {
            if (isNewSessionRequest(request)) {
                return processNewSessionRequest(request);
            } else if (isTerminateSessionRequest(request)) {
                return processTerminateSessionRequest(request);
            } else {
                return super.execute(request);
            }
        }

        private boolean isNewSessionRequest(HttpRequest request) {
            return request.getMethod() == HttpMethod.POST && request.getUri().endsWith("/session");
        }

        private boolean isTerminateSessionRequest(HttpRequest request) {
            if (request.getMethod() != HttpMethod.DELETE) {
                return false;
            }

            String[] segments = request.getUri().split("/");
            if (segments.length < 2) {
                return false;
            }

            return "session".equals(segments[segments.length - 2]);
        }

        private HttpResponse processNewSessionRequest(HttpRequest request) throws IOException {
            HttpResponse response;

            int networkExceptionCount = 0;
            int toManyRequestsCounter = 0;
            long maxRetryTime = System.currentTimeMillis() + suiteContext.getSuiteConfig().getWebDriverCreateSessionRetryTimeout().toMillis();
            while (true) {
                try {
                    response = super.execute(request);

                    if (response.getStatus() == 200) {
                        return response;
                    }

                    if (response.getStatus() == 429) {
                        String retryAfter = response.getHeader("Retry-After");
                        if (retryAfter == null || retryAfter.isEmpty()) {
                            retryAfter = "1";
                        }

                        sleep(Integer.parseInt(retryAfter) * 1000L);
                        toManyRequestsCounter++;
                    }

                    if (maxRetryTime < System.currentTimeMillis()
                            && toManyRequestsCounter >= suiteContext.getSuiteConfig().getWebDriverCreateSessionRetryMinAttempts()) {
                        return response;
                    }
                } catch (IOException e) {
                    networkExceptionCount++;

                    if (networkExceptionCount > suiteContext.getSuiteConfig().getWebDriverCreateSessionRetryMinAttempts()) {
                        throw e;
                    } else {
                        sleep(1000L * networkExceptionCount);
                    }
                }
            }
        }

        private HttpResponse processTerminateSessionRequest(HttpRequest request) throws IOException {
            long maxRetryTime = System.currentTimeMillis() + suiteContext.getSuiteConfig().getWebDriverDeleteSessionRetryTimeout().toMillis();
            int networkExceptionCount = 0;
            while (true) {
                try {
                    HttpResponse response = super.execute(request);
                    if (response.getStatus() == 200) {
                        return response;
                    }

                    if (response.getStatus() == 404) {
                        response.setStatus(200);
                        return response;
                    }

                    if (maxRetryTime < System.currentTimeMillis()) {
                        return response;
                    }
                    sleep(1000);
                } catch (IOException e) {
                    networkExceptionCount++;
                    if (networkExceptionCount > suiteContext.getSuiteConfig().getWebDriverDeleteSessionRetryMinAttempts()) {
                        throw e;
                    } else {
                        sleep(1000L * networkExceptionCount);
                    }
                }
            }
        }

    }

    private class CustomHttpCommandExecutor extends HttpCommandExecutor {

        private final SuiteContextImpl suiteContext;

        public CustomHttpCommandExecutor(SuiteContextImpl suiteContext, URL remoteURL, OkHttpClient httpClient) {
            super(Collections.EMPTY_MAP,
                    remoteURL,
                    new CustomHttpClientFactory(
                            suiteContext,
                            httpClient,
                            remoteURL
                    )
            );
            this.suiteContext = suiteContext;
        }

        @Override
        public Response execute(Command command) throws IOException {
            IOException commandExecutionError = null;

            try {
                return super.execute(command);
            } catch (IOException e) {
                commandExecutionError = e;
            } finally {
                if (command.getName().equals(DriverCommand.QUIT)) {
                    String sessionID = command.getSessionId().toString();

                    RemoteWebDriverContextImpl remoteWebDriverContext = suiteContext.getDrivers().remove(
                            sessionID
                    );

                    if (remoteWebDriverContext != null) {
                        eventsRouter.onRemoteWebDriverFinished(
                                timeProvider.getCurrentTime(),
                                remoteWebDriverContext,
                                commandExecutionError
                        );
                    }
                }
            }
            throw commandExecutionError;
        }
    }
}
