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
package io.perforator.sdk.loadgenerator.core.mock;

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.context.SuiteContext;
import java.util.UUID;

public class SuiteContextMock implements SuiteContext {
    
    private final SuiteConfig suiteConfig;
    private final String suiteInstanceID;
    private final int workerID;
    private final long iterationNumber;
    
    public SuiteContextMock(int workerID, long iterationNumber, SuiteConfig suiteConfig) {
        this(workerID, iterationNumber, suiteConfig, UUID.randomUUID().toString());
    }

    public SuiteContextMock(int workerID, long iterationNumber, SuiteConfig suiteConfig, String suiteInstanceID) {
        this.workerID = workerID;
        this.iterationNumber = iterationNumber;
        this.suiteConfig = suiteConfig;
        this.suiteInstanceID = suiteInstanceID;
    }

    @Override
    public long getIterationNumber() {
        return iterationNumber;
    }

    @Override
    public String getSuiteInstanceID() {
        return suiteInstanceID;
    }

    @Override
    public SuiteConfig getSuiteConfig() {
        return suiteConfig;
    }

    @Override
    public int getWorkerID() {
        return workerID;
    }
    
}
