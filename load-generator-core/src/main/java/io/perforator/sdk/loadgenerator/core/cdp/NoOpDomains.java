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
package io.perforator.sdk.loadgenerator.core.cdp;

import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.idealized.Domains;
import org.openqa.selenium.devtools.idealized.Events;
import org.openqa.selenium.devtools.idealized.Javascript;
import org.openqa.selenium.devtools.idealized.Network;
import org.openqa.selenium.devtools.idealized.log.Log;
import org.openqa.selenium.devtools.idealized.target.Target;

public class NoOpDomains implements Domains {

    private static final String ERROR_MESSAGE = "Perforator doesn't support CDP yet";

    @Override
    public Events<?, ?> events() {
        throw new DevToolsException(ERROR_MESSAGE);
    }

    @Override
    public Javascript<?, ?> javascript() {
        throw new DevToolsException(ERROR_MESSAGE);
    }

    @Override
    public Network<?, ?> network() {
        throw new DevToolsException(ERROR_MESSAGE);
    }

    @Override
    public Target target() {
        throw new DevToolsException(ERROR_MESSAGE);
    }

    @Override
    public Log log() {
        throw new DevToolsException(ERROR_MESSAGE);
    }

}
