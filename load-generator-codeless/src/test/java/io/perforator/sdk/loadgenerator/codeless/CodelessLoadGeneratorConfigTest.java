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
package io.perforator.sdk.loadgenerator.codeless;

import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.AbstractConfigTest;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;

import java.util.Map;
import java.util.UUID;

public class CodelessLoadGeneratorConfigTest extends AbstractConfigTest<CodelessLoadGeneratorConfig> {

    public CodelessLoadGeneratorConfigTest() {
        super(CodelessLoadGeneratorConfig.class);
    }

    @Override
    protected Map<String, String> buildFieldsForVerification() throws Exception {
        return Map.of(
                LoadGeneratorConfig.Fields.apiClientId, UUID.randomUUID().toString()
        );
    }

}
