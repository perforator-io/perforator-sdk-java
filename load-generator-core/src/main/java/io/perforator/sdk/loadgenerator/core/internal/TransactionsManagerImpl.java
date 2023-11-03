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

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TransactionsManagerImpl implements TransactionsManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsManagerImpl.class);

    private final TimeProvider timeProvider;
    private final EventsRouter eventsRouter;

    public TransactionsManagerImpl(TimeProvider timeProvider, EventsRouter eventsRouter) {
        this.timeProvider = timeProvider;
        this.eventsRouter = eventsRouter;
    }

    @Override
    public TransactionContextImpl startTransaction(SuiteInstanceContextImpl suiteInstanceContext, String transactionName) {
        if (suiteInstanceContext == null) {
            throw new IllegalArgumentException(
                    "Can't start new transaction - suiteContext should not be blank"
            );
        }

        if (transactionName == null || transactionName.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't start new transaction - transactionName should not be blank"
            );
        }

        TransactionContextImpl rootTransaction = suiteInstanceContext.getTransactions().peekLast();

        long timestamp = timeProvider.getCurrentTime();
        TransactionContextImpl result = new TransactionContextImpl(
                timestamp,
                suiteInstanceContext,
                rootTransaction,
                rootTransaction == null ? suiteInstanceContext.getSuiteInstanceID() : UUID.randomUUID().toString(),
                transactionName
        );

        suiteInstanceContext.getTransactions().push(result);

        eventsRouter.onTransactionStarted(timestamp, result);

        return result;
    }

    @Override
    public void finishTransaction(TransactionContextImpl transactionContext, Throwable transactionError) {
        if (transactionContext == null) {
            throw new IllegalArgumentException(
                    "Can't finish transaction - transactionContext should not be blank"
            );
        }

        if (transactionContext.getSuiteContext().getTransactions().remove(transactionContext)) {
            eventsRouter.onTransactionFinished(
                    timeProvider.getCurrentTime(),
                    transactionContext,
                    transactionError
            );
        }
        
        if(transactionError != null && transactionContext.getSuiteContext().isLogFailedTransactions()) {
            LOGGER.error(
                    "Failed transaction - '{}' : '{}' => ", 
                    transactionContext.getTransactionID(),
                    transactionContext.getTransactionName(),
                    transactionError
            );
        }
    }

    @Override
    public void onSuiteInstanceStarted(long timestamp, SuiteInstanceContextImpl context) {
        startTransaction(
                context,
                "suite - " + context.getSuiteConfigContext().getSuiteConfig().getName()
        );
    }

    @Override
    public void onSuiteInstanceFinished(long timestamp, SuiteInstanceContextImpl context, Throwable error) {
        ConcurrentLinkedDeque<TransactionContextImpl> transactions = context.getTransactions();
        TransactionContextImpl transaction;
        while ((transaction = transactions.poll()) != null) {
            eventsRouter.onTransactionFinished(
                    timestamp,
                    transaction,
                    error
            );
        }
    }

}
