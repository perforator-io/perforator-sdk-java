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
package io.perforator.sdk.loadgenerator.core.internal;

import io.perforator.sdk.loadgenerator.core.context.SuiteInstanceContext;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

final class SuiteInstanceContextImpl implements SuiteInstanceContext {
    
    private final int workerID;
    private final long startedAt;
    private final long iterationNumber;
    private final String suiteInstanceID;
    private final LoadGeneratorContextImpl loadGeneratorContext;
    private final SuiteConfigContextImpl suiteConfigContext;
    private final ConcurrentLinkedDeque<TransactionContextImpl> transactions = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<String, RemoteWebDriverContextImpl> drivers = new ConcurrentHashMap<>();

    public SuiteInstanceContextImpl(int workerID, long startedAt, long iterationNumber, LoadGeneratorContextImpl loadGeneratorContext, SuiteConfigContextImpl suiteConfigContext) {
        this.workerID = workerID;
        this.startedAt = startedAt;
        this.iterationNumber = iterationNumber;
        this.loadGeneratorContext = loadGeneratorContext;
        this.suiteInstanceID = UUID.randomUUID().toString();
        this.suiteConfigContext = suiteConfigContext;
    }

    @Override
    public int getWorkerID() {
        return workerID;
    }

    @Override
    public long getIterationNumber() {
        return iterationNumber;
    }
    
    public LoadGeneratorContextImpl getLoadGeneratorContext() {
        return loadGeneratorContext;
    }

    public ConcurrentLinkedDeque<TransactionContextImpl> getTransactions() {
        return transactions;
    }

    public ConcurrentHashMap<String, RemoteWebDriverContextImpl> getDrivers() {
        return drivers;
    }

    public long getStartedAt() {
        return startedAt;
    }

    @Override
    public String getSuiteInstanceID() {
        return suiteInstanceID;
    }

    @Override
    public SuiteConfigContextImpl getSuiteConfigContext() {
        return suiteConfigContext;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.suiteInstanceID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SuiteInstanceContextImpl other = (SuiteInstanceContextImpl) obj;
        return Objects.equals(this.suiteInstanceID, other.suiteInstanceID);
    }

}
