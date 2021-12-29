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
package com.example.executions;

import com.example.AbstractExampleTest;
import io.perforator.sdk.api.okhttpgson.model.*;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class PatchExecutionExampleTest extends AbstractExampleTest<PatchExecutionExample> {
    
    private Execution initialExecution;
    
    @BeforeAll
    public void setup() throws Exception {
        initialExecution = getExecution();
    }
    
    @AfterAll
    public void revert() throws Exception {
        executionsApi.updateExecution(
                projectKey, 
                initialExecution.getUuid(), 
                initialExecution
        );
    }

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, initialExecution.getUuid()
        );
    }
    
}
