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
package io.perforator.sdk.loadgenerator.core.configs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.function.Function;

public interface ConfigBuilder<C extends Config, B extends ConfigBuilder<C, B>> {

    public static final List<Function<String, String>> DEFAULT_PROVIDERS = List.of(
            System::getProperty,
            System::getenv
    );

    C build();

    @JsonIgnore
    String getDefaultsPrefix();

    @JsonIgnore
    default List<Function<String, String>> getDefaultProviders() {
        return DEFAULT_PROVIDERS;
    }

    @JsonIgnore
    default C buildWithDefaults() {
        return applyDefaults(getDefaultsPrefix(), getDefaultProviders()).build();
    }

    @JsonIgnore
    default C buildWithDefaults(Function<String, String>... providers) {
        return applyDefaults(getDefaultsPrefix(), providers).build();
    }

    @JsonIgnore
    default C buildWithDefaults(List<Function<String, String>> providers) {
        return applyDefaults(getDefaultsPrefix(), providers).build();
    }

    @JsonIgnore
    default C buildWithDefaults(String prefix) {
        return applyDefaults(prefix, getDefaultProviders()).build();
    }

    @JsonIgnore
    default C buildWithDefaults(String prefix, Function<String, String>... providers) {
        return applyDefaults(prefix, providers).build();
    }

    @JsonIgnore
    default C buildWithDefaults(String prefix, List<Function<String, String>> providers) {
        return applyDefaults(prefix, providers).build();
    }

    @JsonIgnore
    default B applyDefaults() {
        return applyDefaults(getDefaultsPrefix(), getDefaultProviders());
    }

    @JsonIgnore
    default B applyDefaults(Function<String, String>... providers) {
        return applyDefaults(getDefaultsPrefix(), providers);
    }

    @JsonIgnore
    default B applyDefaults(List<Function<String, String>> providers) {
        return applyDefaults(getDefaultsPrefix(), providers);
    }

    @JsonIgnore
    default B applyDefaults(String prefix) {
        return applyDefaults(prefix, getDefaultProviders());
    }

    @JsonIgnore
    default B applyDefaults(String prefix, Function<String, String>... providers) {
        if (providers == null || providers.length == 0) {
            return (B) this;
        }
        return applyDefaults(prefix, List.of(providers));
    }

    @JsonIgnore
    default B applyDefaults(String prefix, List<Function<String, String>> providers) {
        if (providers == null || providers.isEmpty()) {
            return (B) this;
        }

        return (B) ConfigPopulator.applyDefaults(this, prefix, providers);
    }

}
