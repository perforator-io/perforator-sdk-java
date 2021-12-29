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

import java.util.concurrent.atomic.AtomicLong;

class StatisticsContextImpl {
    
    private final AtomicLong suiteInstancesSuccessful = new AtomicLong(0);
    private final AtomicLong suiteInstancesFailed = new AtomicLong(0);
    private final AtomicLong suiteInstancesInProgress = new AtomicLong(0);
    private final AtomicLong transactionsSuccessful = new AtomicLong(0);
    private final AtomicLong transactionsFailed = new AtomicLong(0);
    private final AtomicLong transactionsInProgress = new AtomicLong(0);
    
    public long incrementSuiteInstancesSuccessful() {
        return suiteInstancesSuccessful.incrementAndGet();
    }
    
    public long decrementSuiteInstancesSuccessful() {
        return suiteInstancesSuccessful.decrementAndGet();
    }
    
    public long getSuiteInstancesSuccessful() {
        return suiteInstancesSuccessful.get();
    }
    
    public long incrementSuiteInstancesFailed() {
        return suiteInstancesFailed.incrementAndGet();
    }
    
    public long decrementSuiteInstancesFailed() {
        return suiteInstancesFailed.decrementAndGet();
    }
    
    public long getSuiteInstancesFailed() {
        return suiteInstancesFailed.get();
    }
    
    public long incrementSuiteInstancesInProgress() {
        return suiteInstancesInProgress.incrementAndGet();
    }
    
    public long decrementSuiteInstancesInProgress() {
        return suiteInstancesInProgress.decrementAndGet();
    }
    
    public long getSuiteInstancesInProgress() {
        return suiteInstancesInProgress.get();
    }
    
    public long incrementTransactionsSuccessful() {
        return transactionsSuccessful.incrementAndGet();
    }
    
    public long decrementTransactionsSuccessful() {
        return transactionsSuccessful.decrementAndGet();
    }
    
    public long getTransactionsSuccessful() {
        return transactionsSuccessful.get();
    }
    
    public long incrementTransactionsFailed() {
        return transactionsFailed.incrementAndGet();
    }
    
    public long decrementTransactionsFailed() {
        return transactionsFailed.decrementAndGet();
    }
    
    public long getTransactionsFailed() {
        return transactionsFailed.get();
    }
    
    public long incrementTransactionsInProgress() {
        return transactionsInProgress.incrementAndGet();
    }
    
    public long decrementTransactionsInProgress() {
        return transactionsInProgress.decrementAndGet();
    }
    
    public long getTransactionsInProgress() {
        return transactionsInProgress.get();
    }
    
}
