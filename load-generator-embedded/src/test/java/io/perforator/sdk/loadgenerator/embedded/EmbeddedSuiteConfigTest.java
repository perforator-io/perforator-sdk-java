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
package io.perforator.sdk.loadgenerator.embedded;

import io.perforator.sdk.loadgenerator.core.AbstractConfigTest;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import java.util.Map;
import java.util.UUID;

public class EmbeddedSuiteConfigTest extends AbstractConfigTest<EmbeddedSuiteConfig> {

    public EmbeddedSuiteConfigTest() {
        super(EmbeddedSuiteConfig.class);
    }

    @Override
    protected Map<String, String> buildFieldsForVerification() throws Exception {
        return Map.of(
                EmbeddedSuiteConfig.Fields.processorSingleton, !EmbeddedSuiteConfig.DEFAULT_PROCESSOR_SINGLETON + "",
                SuiteConfig.Fields.name, UUID.randomUUID().toString(),
                SuiteConfig.Fields.webDriverUseLocalFileDetector, !SuiteConfig.DEFAULT_WEB_DRIVER_USE_LOCAL_FILE_DETECTOR + ""
        );
    }
    
}
