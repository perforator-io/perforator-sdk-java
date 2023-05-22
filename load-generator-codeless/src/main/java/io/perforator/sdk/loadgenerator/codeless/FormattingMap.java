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
package io.perforator.sdk.loadgenerator.codeless;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FormattingMap implements Map<String, String> {

    public static final FormattingMap EMPTY = new FormattingMap(new HashMap<>());

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private final HashMap<String, String> map;
    private final String availablePlaceholders;
    private final int hashCode;

    public FormattingMap(Map<String, String>... srcMaps) {
        this.map = new LinkedHashMap<>();
        
        if (srcMaps != null && srcMaps.length > 0) {
            for (Map<String, String> src : srcMaps) {
                for (Entry<String, String> entry : src.entrySet()) {
                    map.put(
                            entry.getKey().trim().intern(),
                            entry.getValue() != null ? entry.getValue().trim().intern() : null
                    );
                }
            }
        }

        map.replaceAll((key, value) -> format(value));

        this.hashCode = this.map.hashCode();
        this.availablePlaceholders = map.entrySet().stream().map(
                entry -> "\"" + entry.getKey() + "\" : \"" + entry.getValue() + "\""
        ).collect(
                Collectors.joining(", ")
        );
    }

    public String format(String src) {
        return format(src, true);
    }

    public String format(String src, boolean failOnMissingPlaceholder) {
        if (src == null || src.isBlank()) {
            return src;
        }

        return cache.computeIfAbsent(src, key -> {
            String result = key;

            for (Map.Entry<String, String> entry : map.entrySet()) {
                result = result.replace(
                        "${" + entry.getKey() + "}",
                        entry.getValue()
                );
            }

            if (failOnMissingPlaceholder && result.matches(".*\\$\\{.*\\}.*")) {
                throw new RuntimeException(
                        "Supplied value contains unresolvable placeholders: "
                                + "value = \"" + key + "\", "
                                + "placeholders = {" + availablePlaceholders + "}"
                );
            }

            return result;
        });
    }

    @Override
    public int hashCode() {
        return hashCode;
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
        final FormattingMap other = (FormattingMap) obj;
        if (this.hashCode != other.hashCode) {
            return false;
        }
        return Objects.equals(this.map, other.map);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return map.get(key);
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public Collection<String> values() {
        return Collections.unmodifiableCollection(map.values());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    @Override
    public String toString() {
        return map.toString();
    }
    
    

}
