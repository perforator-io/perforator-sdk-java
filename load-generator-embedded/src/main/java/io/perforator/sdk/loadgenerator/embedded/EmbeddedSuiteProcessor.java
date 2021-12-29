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
package io.perforator.sdk.loadgenerator.embedded;

/**
 * Interface for suite instance processors to be invoked by 
 * {@link io.perforator.sdk.loadgenerator.embedded.EmbeddedLoadGenerator EmbeddedLoadGenerator}
 */
public interface EmbeddedSuiteProcessor {
    
    /**
     * Optional method to be executed before processing suite instance.
     * @param iterationNumber suite iteration number. It starts from 0 incrementing 
     * for every new suite instance execution. Such counter is maintained on a 
     * suite level, so counters from different suites are incremented independently.
     * @param suiteInstanceID ID of the invoked suite instance.
     * @param suiteConfig {@link EmbeddedSuiteConfig configuration} of the invoked suite instance.
     */
    default void onBeforeSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig){}
    
    /**
     * Method containing actual logic to process suite instance.
     * @param iterationNumber suite iteration number. It starts from 0 incrementing 
     * for every new suite instance execution. Such counter is maintained on a 
     * suite level, so counters from different suites are incremented independently.
     * @param suiteInstanceID ID of the invoked suite instance.
     * @param suiteConfig {@link EmbeddedSuiteConfig configuration} of the invoked suite instance.
     */
    void processSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig);
    
    /**
     * Optional method to be executed when suite instance processing is completed.
     * @param iterationNumber suite iteration number. It starts from 0 incrementing 
     * for every new suite instance execution. Such counter is maintained on a 
     * suite level, so counters from different suites are incremented independently.
     * @param suiteInstanceID ID of the invoked suite instance.
     * @param suiteConfig {@link EmbeddedSuiteConfig configuration} of the invoked suite instance.
     * @param optionalSuiteProcessingError optional error if invoked
     * {@link EmbeddedSuiteProcessor#processSuite(long, java.lang.String, io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig) processSuite}
     * method resulted with exception.
     */
    default void onAfterSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig, Throwable optionalSuiteProcessingError){}
    
}
