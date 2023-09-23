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

public interface StatisticsService {
    
    long getActiveSuiteInstancesCount();
    long getSuccessfulSuiteInstancesCount();
    long getFailedSuiteInstancesCount();
    
    long getActiveTransactionsCount();
    long getSuccessfulTransactionsCount();
    long getFailedTransactionsCount();
    long getActiveTopLevelTransactionsCount();
    long getActiveNestedTransactionsCount();
    long getActiveSessionsCount();
    
}
