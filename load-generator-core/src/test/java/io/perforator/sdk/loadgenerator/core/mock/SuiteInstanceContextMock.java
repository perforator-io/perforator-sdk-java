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

import io.perforator.sdk.loadgenerator.core.context.SuiteConfigContext;
import io.perforator.sdk.loadgenerator.core.context.SuiteInstanceContext;
import java.util.UUID;

public class SuiteInstanceContextMock implements SuiteInstanceContext {

    private final SuiteConfigContextMock suiteConfigContext;
    private final String suiteInstanceID;
    private final int workerID;
    private final long iterationNumber;

    public SuiteInstanceContextMock(int workerID, long iterationNumber, SuiteConfigContextMock suiteConfigContext) {
        this(workerID, iterationNumber, suiteConfigContext, UUID.randomUUID().toString());
    }

    public SuiteInstanceContextMock(int workerID, long iterationNumber, SuiteConfigContextMock suiteConfigContext, String suiteInstanceID) {
        this.workerID = workerID;
        this.iterationNumber = iterationNumber;
        this.suiteConfigContext = suiteConfigContext;
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
    public SuiteConfigContext getSuiteConfigContext() {
        return suiteConfigContext;
    }

    @Override
    public int getWorkerID() {
        return workerID;
    }

}
