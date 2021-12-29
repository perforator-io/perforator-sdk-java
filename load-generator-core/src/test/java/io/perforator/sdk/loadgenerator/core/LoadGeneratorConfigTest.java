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

import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import java.util.Map;
import java.util.UUID;

public class LoadGeneratorConfigTest extends AbstractConfigTest<LoadGeneratorConfig> {
    
    public LoadGeneratorConfigTest() {
        super(LoadGeneratorConfig.class);
    }

    @Override
    protected Map<String, String> buildFieldsForVerification() throws Exception {
        return Map.of(
                LoadGeneratorConfig.Fields.apiClientId, UUID.randomUUID().toString(),
                LoadGeneratorConfig.Fields.logWorkerID, !LoadGeneratorConfig.DEFAULT_LOG_WORKER_ID + ""
        );
    }
    
}
