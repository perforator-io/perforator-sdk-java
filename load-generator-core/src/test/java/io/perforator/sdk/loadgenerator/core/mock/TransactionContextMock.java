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

import io.perforator.sdk.loadgenerator.core.context.TransactionContext;
import java.util.UUID;

public class TransactionContextMock implements TransactionContext {
    
    private final long startedAt;
    private final String transactionID;
    private final String transactionName;
    private final TransactionContextMock parentTransactionContext;
    
    public TransactionContextMock(String transactionName) {
        this(UUID.randomUUID().toString(), transactionName, null);
    }
    
    public TransactionContextMock(String transactionName, TransactionContextMock parentTransactionContext) {
        this(UUID.randomUUID().toString(), transactionName, parentTransactionContext);
    }

    public TransactionContextMock(String transactionID, String transactionName, TransactionContextMock parentTransactionContext) {
        this.startedAt = System.currentTimeMillis();
        this.transactionID = transactionID;
        this.transactionName = transactionName;
        this.parentTransactionContext = parentTransactionContext;
    }

    @Override
    public long getStartedAt() {
        return startedAt;
    }

    @Override
    public TransactionContext getParentTransactionContext() {
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
    
}
