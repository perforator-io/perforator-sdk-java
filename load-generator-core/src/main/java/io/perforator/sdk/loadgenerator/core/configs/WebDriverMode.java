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
package io.perforator.sdk.loadgenerator.core.configs;

/**
 * Enum defining the mode on how to start the browser(s) in the performance testing suite.
 */
public enum WebDriverMode {

    /**
     * Browser(s) are started in the Perforator cloud.
     */
    cloud,
    
    /**
     * Browser(s) are started using local chrome instance.
     */
    local

}
