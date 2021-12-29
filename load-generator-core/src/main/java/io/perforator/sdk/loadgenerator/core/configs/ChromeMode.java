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
 * Enum defining the mode on how to start local chrome instance.
 */
public enum ChromeMode {
    
    /**
     * Chrome browser is started in headless mode.
     * <br>
     * This mode is useful when you run your testes where rendering UI is not available,
     * for example on a server or as a part of CI/CD pipeline.
     * <br>
     * @see <a href="https://developers.google.com/web/updates/2017/04/headless-chrome">https://developers.google.com/web/updates/2017/04/headless-chrome</a>
     */
    headless,
    
    /**
     * Chrome browser is started with full rendering UI attached.
     * <br>
     * This mode should be used only in environments where UI is available,
     * for example on your desktop/laptop.
     */
    headful
    
}
