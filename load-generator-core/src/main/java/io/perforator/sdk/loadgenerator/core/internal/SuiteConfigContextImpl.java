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

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.context.SuiteConfigContext;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class SuiteConfigContextImpl implements SuiteConfigContext {

    private final SuiteConfig suiteConfig;
    private final StatisticsContextImpl statisticsContext;
    private final Set<SuiteInstanceContextImpl> suiteInstanceContexts;

    public SuiteConfigContextImpl(SuiteConfig suiteConfig) {
        this.suiteConfig = suiteConfig;
        this.statisticsContext = new StatisticsContextImpl();
        this.suiteInstanceContexts = ConcurrentHashMap.newKeySet();
    }

    @Override
    public SuiteConfig getSuiteConfig() {
        return this.suiteConfig;
    }

    public StatisticsContextImpl getStatisticsContext() {
        return statisticsContext;
    }

    public Set<SuiteInstanceContextImpl> getSuiteInstanceContexts() {
        return suiteInstanceContexts;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hash(this.suiteConfig);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SuiteConfigContextImpl other = (SuiteConfigContextImpl) obj;
        return Objects.equals(this.suiteConfig, other.suiteConfig);
    }
}