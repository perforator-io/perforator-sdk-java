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
package io.perforator.sdk.api.okhttpgson;

import io.perforator.sdk.api.okhttpgson.model.*;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsApiTest extends AbstractApiTest {
    
    @Test
    public void validateNamespaces() throws Exception {
        List<String> namespaces = getNamespaces(
                projectKey,
                executionKey
        );
        
        assertNotNull(namespaces);
        assertFalse(namespaces.isEmpty());
        
        for(String namespace : namespaces) {
            List<String> fields = getFields(projectKey, executionKey, namespace);
            assertNotNull(fields);
            assertFalse(fields.isEmpty());
            
            List<String> metrcis = getMetrics(projectKey, executionKey, namespace);
            assertNotNull(metrcis);
            assertFalse(metrcis.isEmpty());
        }
    }
    
    @Test
    public void validateUnifiedAnalyticsApiAccess() throws Exception {
        AnalyticsOverallStatisticsRequest transactions = new AnalyticsOverallStatisticsRequest();
        transactions.setNamespace(AnalyticsNamespace.TRANSACTIONS.getValue());
        transactions.setMetrics(List.of(
                TransactionsBasicMetrics.COUNT.getValue(), 
                TransactionsBasicMetrics.DURATION_AVG.getValue(), 
                TransactionsBasicMetrics.DURATION_MIN.getValue(), 
                TransactionsBasicMetrics.DURATION_MAX.getValue(), 
                TransactionsBasicMetrics.DURATION_P50.getValue(), 
                TransactionsBasicMetrics.DURATION_P75.getValue(), 
                TransactionsBasicMetrics.DURATION_P90.getValue(), 
                TransactionsBasicMetrics.DURATION_P95.getValue(), 
                TransactionsBasicMetrics.DURATION_P99.getValue(), 
                TransactionsBasicMetrics.DURATION_SD.getValue()
        ));
        
        AnalyticsOverallStatisticsRequest requests = new AnalyticsOverallStatisticsRequest();
        requests.setNamespace(AnalyticsNamespace.REQUESTS.getValue());
        requests.setMetrics(List.of(
                RequestsBasicMetrics.COUNT.getValue(), 
                RequestsBasicMetrics.DURATION_AVG.getValue(), 
                RequestsBasicMetrics.DURATION_MIN.getValue(), 
                RequestsBasicMetrics.DURATION_MAX.getValue(), 
                RequestsBasicMetrics.DURATION_P50.getValue(), 
                RequestsBasicMetrics.DURATION_P75.getValue(), 
                RequestsBasicMetrics.DURATION_P90.getValue(), 
                RequestsBasicMetrics.DURATION_P95.getValue(), 
                RequestsBasicMetrics.DURATION_P99.getValue(), 
                RequestsBasicMetrics.DURATION_SD.getValue()
        ));
        
        List<AnalyticsOverallStatisticsResult> response = analyticsApi.getOverallStatistics(
                projectKey, 
                executionKey, 
                List.of(transactions, requests)
        );
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(2, response.size());
    }
    
    private List<String> getNamespaces(String project, String execution) throws Exception {
        return analyticsApi.getNamespaces(
                project, 
                execution
        ).stream().collect(Collectors.toList());
    }
    
    private List<String> getFields(String project, String execution, String namespace) throws Exception {
        return analyticsApi.getNamespaceFields(
                project, 
                execution, 
                namespace
        ).stream().map(AnalyticsFieldInfo::getName).collect(Collectors.toList());
    }
    
    private List<String> getMetrics(String project, String execution, String namespace) throws Exception {
        return analyticsApi.getNamespaceMetrics(
                project, 
                execution, 
                namespace
        ).stream().map(AnalyticsMetricInfo::getName).collect(Collectors.toList());
    }
    
}
