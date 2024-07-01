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

import java.util.Map;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.codec.w3c.W3CHttpCommandCodec;

/**
 * Default W3CHttpCommandCodec implementation sends 48kb JS to remote browser to
 * determine if element is displayed or not, but remote driver has the same
 * scripts embedded.
 *
 * So, we need to override default codec using more optimized call(s).
 */
final class RemoteWebDriverCommandCodec extends W3CHttpCommandCodec {

    public RemoteWebDriverCommandCodec() {
        super.alias(
                DriverCommand.IS_ELEMENT_DISPLAYED,
                DriverCommand.IS_ELEMENT_DISPLAYED
        );
        super.defineCommand(
                DriverCommand.IS_ELEMENT_DISPLAYED,
                HttpMethod.GET,
                "/session/:sessionId/element/:id/displayed"
        );

        super.alias(
                DriverCommand.GET_ELEMENT_ATTRIBUTE,
                DriverCommand.GET_ELEMENT_ATTRIBUTE
        );
        super.defineCommand(
                DriverCommand.GET_ELEMENT_ATTRIBUTE,
                HttpMethod.GET,
                "/session/:sessionId/element/:id/attribute/:name"
        );
        super.defineCommand(
                "executeCdpCommand",
                HttpMethod.POST,
                "/session/:sessionId/goog/cdp/execute"
        );
    }

    @Override
    protected Map<String, ?> amendParameters(String name, Map<String, ?> parameters) {
        if (name.equals(DriverCommand.IS_ELEMENT_DISPLAYED) || name.equals(DriverCommand.GET_ELEMENT_ATTRIBUTE)) {
            return parameters;
        }

        return super.amendParameters(name, parameters);
    }

}
