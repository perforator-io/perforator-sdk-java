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
package io.perforator.sdk.loadgenerator.core.service;

import io.perforator.sdk.loadgenerator.core.context.SuiteContext;
import io.perforator.sdk.loadgenerator.core.context.TransactionContext;

//TODO: add javadoc
public interface TransactionsService<S extends SuiteContext, T extends TransactionContext> {
    
    T startTransaction(S suiteContext, String transactionName);

    void finishTransaction(T transactionContext, Throwable transactionError);
}
