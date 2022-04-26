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

import io.perforator.sdk.loadgenerator.core.context.TransactionContext;

import java.util.Objects;

final class TransactionContextImpl implements TransactionContext {

    private final long startedAt;
    private final LoadGeneratorContextImpl loadGeneratorContext;
    private final SuiteInstanceContextImpl suiteContext;
    private final TransactionContextImpl parentTransactionContext;
    private final String transactionID;
    private final String transactionName;

    TransactionContextImpl(long startedAt, SuiteInstanceContextImpl suiteContext, TransactionContextImpl parentTransactionContext, String transactionID, String transactionName) {
        this.startedAt = startedAt;
        this.loadGeneratorContext = suiteContext.getLoadGeneratorContext();
        this.suiteContext = suiteContext;
        this.parentTransactionContext = parentTransactionContext;
        this.transactionID = transactionID;
        this.transactionName = transactionName;
    }

    @Override
    public long getStartedAt() {
        return startedAt;
    }

    public LoadGeneratorContextImpl getLoadGeneratorContext() {
        return loadGeneratorContext;
    }
    
    public SuiteInstanceContextImpl getSuiteContext() {
        return suiteContext;
    }

    @Override
    public TransactionContextImpl getParentTransactionContext() {
        return parentTransactionContext;
    }

    @Override
    public String getTransactionID() {
        return transactionID;
    }

    @Override
    public String getTransactionName() {
        return transactionName;
    }

    @Override
    public boolean isNested() {
        return parentTransactionContext != null;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + Objects.hashCode(this.transactionID);
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
        final TransactionContextImpl other = (TransactionContextImpl) obj;
        return Objects.equals(this.transactionID, other.transactionID);
    }

}
