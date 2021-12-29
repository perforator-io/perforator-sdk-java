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
package io.perforator.sdk.loadgenerator.core;

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import java.util.Map;
import java.util.UUID;

public class SuiteConfigTest extends AbstractConfigTest<SuiteConfig>{
    
    public SuiteConfigTest() {
        super(SuiteConfig.class);
    }

    @Override
    protected Map<String, String> buildFieldsForVerification() throws Exception {
        return Map.of(
                SuiteConfig.Fields.name, UUID.randomUUID().toString(),
                SuiteConfig.Fields.webDriverUseLocalFileDetector, !SuiteConfig.DEFAULT_WEB_DRIVER_USE_LOCAL_FILE_DETECTOR + ""
        );
    }
    
}
