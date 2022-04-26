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

import java.util.List;

final class EventsRouterImpl implements EventsRouter {

    private List<IntegrationListener> loadGeneratorStartedListeners;
    private List<IntegrationListener> loadGeneratorFinishedListeners;
    private List<IntegrationListener> suiteInstanceStartedListeners;
    private List<IntegrationListener> suiteInstanceFinishedListeners;
    private List<IntegrationListener> transactionStartedListeners;
    private List<IntegrationListener> transactionFinishedListeners;
    private List<IntegrationListener> remoteWebDriverStartedListeners;
    private List<IntegrationListener> remoteWebDriverFinishedListeners;
    private List<IntegrationListener> heartbeatListeners;

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        loadGeneratorStartedListeners.forEach(
                listener -> listener.onLoadGeneratorStarted(timestamp, loadGeneratorContext)
        );
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        loadGeneratorFinishedListeners.forEach(
                listener -> listener.onLoadGeneratorFinished(timestamp, loadGeneratorContext, error)
        );
    }

    @Override
    public void onSuiteInstanceStarted(long timestamp, SuiteInstanceContextImpl context) {
        suiteInstanceStartedListeners.forEach(
                listener -> listener.onSuiteInstanceStarted(timestamp, context)
        );
    }

    @Override
    public void onSuiteInstanceFinished(long timestamp, SuiteInstanceContextImpl context, Throwable error) {
        suiteInstanceFinishedListeners.forEach(
                listener -> listener.onSuiteInstanceFinished(timestamp, context, error)
        );
    }

    @Override
    public void onTransactionStarted(long timestamp, TransactionContextImpl context) {
        transactionStartedListeners.forEach(
                listener -> listener.onTransactionStarted(timestamp, context)
        );
    }

    @Override
    public void onTransactionFinished(long timestamp, TransactionContextImpl context, Throwable error) {
        transactionFinishedListeners.forEach(
                listener -> listener.onTransactionFinished(timestamp, context, error)
        );
    }

    @Override
    public void onRemoteWebDriverStarted(long timestamp, RemoteWebDriverContextImpl context) {
        remoteWebDriverStartedListeners.forEach(
                listener -> listener.onRemoteWebDriverStarted(timestamp, context)
        );
    }

    @Override
    public void onRemoteWebDriverFinished(long timestamp, RemoteWebDriverContextImpl context, Throwable error) {
        remoteWebDriverFinishedListeners.forEach(
                listener -> listener.onRemoteWebDriverFinished(timestamp, context, error)
        );
    }

    @Override
    public void onHeartbeat(long timestamp, LoadGeneratorContextImpl context) {
        heartbeatListeners.forEach(
                listener -> listener.onHeartbeat(timestamp, context)
        );
    }

    public void setLoadGeneratorStartedListeners(List<IntegrationListener> loadGeneratorStartedListeners) {
        this.loadGeneratorStartedListeners = loadGeneratorStartedListeners;
    }

    public void setLoadGeneratorFinishedListeners(List<IntegrationListener> loadGeneratorFinishedListeners) {
        this.loadGeneratorFinishedListeners = loadGeneratorFinishedListeners;
    }

    public void setSuiteInstanceStartedListeners(List<IntegrationListener> suiteInstanceStartedListeners) {
        this.suiteInstanceStartedListeners = suiteInstanceStartedListeners;
    }

    public void setSuiteInstanceFinishedListeners(List<IntegrationListener> suiteInstanceFinishedListeners) {
        this.suiteInstanceFinishedListeners = suiteInstanceFinishedListeners;
    }

    public void setTransactionStartedListeners(List<IntegrationListener> transactionStartedListeners) {
        this.transactionStartedListeners = transactionStartedListeners;
    }

    public void setTransactionFinishedListeners(List<IntegrationListener> transactionFinishedListeners) {
        this.transactionFinishedListeners = transactionFinishedListeners;
    }

    public void setRemoteWebDriverStartedListeners(List<IntegrationListener> remoteWebDriverStartedListeners) {
        this.remoteWebDriverStartedListeners = remoteWebDriverStartedListeners;
    }

    public void setRemoteWebDriverFinishedListeners(List<IntegrationListener> remoteWebDriverFinishedListeners) {
        this.remoteWebDriverFinishedListeners = remoteWebDriverFinishedListeners;
    }

    public void setHeartbeatListeners(List<IntegrationListener> heartbeatListeners) {
        this.heartbeatListeners = heartbeatListeners;
    }

}
