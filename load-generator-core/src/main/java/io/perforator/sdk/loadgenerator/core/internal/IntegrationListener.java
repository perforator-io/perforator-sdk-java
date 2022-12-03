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

interface IntegrationListener {

    default void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {}

    default void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {}

    default void onSuiteInstanceStarted(long timestamp, SuiteInstanceContextImpl context) {}

    default void onSuiteInstanceFinished(long timestamp, SuiteInstanceContextImpl context, Throwable error) {}
    
    default void onSuiteInstanceKeepAlive(long timestamp, SuiteInstanceContextImpl context) {}

    default void onTransactionStarted(long timestamp, TransactionContextImpl context) {}

    default void onTransactionFinished(long timestamp, TransactionContextImpl context, Throwable error) {}

    default void onRemoteWebDriverStarted(long timestamp, RemoteWebDriverContextImpl context) {}

    default void onRemoteWebDriverFinished(long timestamp, RemoteWebDriverContextImpl context, Throwable error) {}

    default void onHeartbeat(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {}

}
