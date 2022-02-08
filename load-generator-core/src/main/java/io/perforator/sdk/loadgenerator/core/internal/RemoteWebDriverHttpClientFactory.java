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

import java.net.URL;
import org.openqa.selenium.remote.http.HttpClient;

final class RemoteWebDriverHttpClientFactory implements HttpClient.Factory {

    private final RemoteWebDriverHttpClient client;

    public RemoteWebDriverHttpClientFactory(SuiteContextImpl suiteContext) {
        this.client = new RemoteWebDriverHttpClient(suiteContext);
    }

    @Override
    public HttpClient createClient(URL url) {
        return client;
    }

    @Override
    public HttpClient.Builder builder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanupIdleClients() {
    }

}
