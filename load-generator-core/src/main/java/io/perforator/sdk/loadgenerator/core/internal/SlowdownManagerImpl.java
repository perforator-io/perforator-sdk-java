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

import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;

final class SlowdownManagerImpl implements SlowdownManager {
    
    private final boolean slowdownEnabled;
    private final TimeProvider timeProvider;
    
    public SlowdownManagerImpl(TimeProvider timeProvider, LoadGeneratorConfig loadGeneratorConfig) {
        this.slowdownEnabled = loadGeneratorConfig.isSlowdown();
        this.timeProvider = timeProvider;
    }
    
    @Override
    public long getSlowdownTimeout(SuiteContextImpl suiteContext, Throwable suiteError) {
        if(!slowdownEnabled || suiteError == null) {
            return 0;
        }
        
        return Math.max(1000l, (timeProvider.getCurrentTime() - suiteContext.getStartedAt()) / 2);
    }

}
