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

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import lombok.AccessLevel;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Configuration for embedded performance testing suites processed by
 * {@link EmbeddedLoadGenerator}.
 */
@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true, cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@FieldNameConstants
@Jacksonized
public class EmbeddedSuiteConfig extends SuiteConfig {
    
    /**
     * String representation of default value for 
     * <b>{@link EmbeddedSuiteConfig#processorSingleton processorSingleton}</b>
     * property.
     */
    public static final String DEFAULT_PROCESSOR_SINGLETON_S = "false";
    
    /**
     * Default value(<b>{@value EmbeddedSuiteConfig#DEFAULT_PROCESSOR_SINGLETON_S}</b>) 
     * for 
     * <b>{@link EmbeddedSuiteConfig#processorSingleton processorSingleton}</b> 
     * property.
     */
    public static final boolean DEFAULT_PROCESSOR_SINGLETON = Boolean.parseBoolean(DEFAULT_PROCESSOR_SINGLETON_S);
    
    /**
     * The class name of the processor to execute while processing any suite instance.
     */
    String processorClass;
    
    /**
     * This flag determines how processor instance should be instantiated:
     * <ul>
     * <li>true - single processor instance is instantiated for the whole duration of the performance test.</li>
     * <li>false - a new processor instance is instantiated for every suite instance run.</li>
     * </ul>
     */
    @Default
    boolean processorSingleton = DEFAULT_PROCESSOR_SINGLETON;
    
    public static abstract class EmbeddedSuiteConfigBuilder<C extends EmbeddedSuiteConfig, B extends EmbeddedSuiteConfigBuilder<C, B>> extends SuiteConfigBuilder<C, B> {

        private String name;

        @Override
        public B name(String name) {
            super.name(name);
            this.name = name;
            return (B) this;
        }

        public B processorClass(String processorClass) {
            if (processorClass != null && !processorClass.isBlank() && (name == null || name.isBlank())) {
                this.name(processorClass.trim());
            }
            this.processorClass = processorClass;
            return (B) this;
        }

    }
    
}