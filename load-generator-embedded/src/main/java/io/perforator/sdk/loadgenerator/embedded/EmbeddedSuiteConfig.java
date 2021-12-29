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
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * Configuration for embedded performance testing suites processed by
 * {@link EmbeddedLoadGenerator}.
 */
@ToString
@FieldNameConstants
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
    @Getter @Setter @FieldNameConstants.Include
    protected String processorClass;
    
    /**
     * This flag determines how processor instance should be instantiated:
     * <ul>
     * <li>true - single processor instance is instantiated for the whole duration of the performance test.</li>
     * <li>false - a new processor instance is instantiated for every suite instance run.</li>
     * </ul>
     */
    @Getter @Setter @FieldNameConstants.Include
    protected boolean processorSingleton = DEFAULT_PROCESSOR_SINGLETON;
    
    /**
     * Default constructor looking up property defaults via the following providers:
     * <ul>
     *   <li>{@link System#getProperty(java.lang.String) }</li>
     *   <li>{@link System#getenv(java.lang.String) }</li>
     * </ul>
     */
    public EmbeddedSuiteConfig() {
        applyDefaults();
        applyNameDefaults();
    }
    
    /**
     * Constructor looking up property defaults in user-supplied property providers.
     * @param defaultsProviders varargs of {@link Function functions} where to lookup up
     * for property defaults.
     */
    public EmbeddedSuiteConfig(Function<String, String>... defaultsProviders) {
        applyDefaults(defaultsProviders);
        applyNameDefaults();
    }
    
    private void applyNameDefaults() {
        if(processorClass == null || processorClass.isBlank()) {
            return;
        }
        
        if(name == null || name.isBlank()) {
            setName(processorClass);
        }
    }
    
}
