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

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringConverter {

    private StringConverter() {
    }

    public static String toString(String val) {
        return val != null ? val.trim() : null;
    }

    public static final boolean toBoolean(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to boolean"
            );
        }

        val = val.trim();

        if (val.equalsIgnoreCase("y") || val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("true")) {
            return true;
        }

        if (val.equalsIgnoreCase("n") || val.equalsIgnoreCase("no") || val.equalsIgnoreCase("false")) {
            return false;
        }

        throw new IllegalArgumentException(
                "Can't convert " + val + " to boolean"
        );
    }

    public static final Boolean toBooleanObject(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to boolean"
            );
        }

        return Boolean.valueOf(val.trim());
    }

    public static final int toInt(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to int"
            );
        }

        return Integer.parseInt(val.trim());
    }

    public static final Integer toInteger(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to int"
            );
        }

        return Integer.valueOf(val.trim());
    }

    public static final long toLong(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to long"
            );
        }

        return Long.parseLong(val.trim());
    }

    public static final Long toLongObject(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to long"
            );
        }

        return Long.valueOf(val.trim());
    }

    public static final double toDouble(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to double"
            );
        }

        return Double.parseDouble(val.trim());
    }

    public static final Double toDoubleObject(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to double"
            );
        }

        return Double.valueOf(val.trim());
    }

    public static final Duration toDuration(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to duration"
            );
        }

        val = val.trim();

        if (val.contains(" ")) {
            val = val.replace(" ", "");
        }

        if (!val.startsWith("pt") && !val.startsWith("PT")) {
            val = "pt" + val;
        }

        return Duration.parse(val);
    }

    public static final List<String> toStringList(String val) {
        return toStringList(val, ",");
    }

    public static final List<String> toStringList(String val, String delimiter) {
        if (val == null || val.isBlank()) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.stream(
                val.split(delimiter)
        ).map(
                String::trim
        ).filter(
                i -> i != null && !i.isBlank()
        ).collect(
                Collectors.toList()
        );
    }

    public static final Map<String, String> toStringMap(String val) {
        if (val == null || val.isBlank()) {
            return Collections.EMPTY_MAP;
        }

        Map<String, String> map = new HashMap<>();

        Pattern pattern = Pattern.compile("(?<key>[^;]+)=(?<value>[^;]+)");
        Matcher matcher = pattern.matcher(val);

        while (matcher.find()) {
            String key = matcher.group("key");
            String value = matcher.group("value");
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException(
                        "Can't parse '" + val + "'. Key is null"
                );
            }
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(
                        "Can't parse '" + val + "'. Value is null for key " + key
                );
            }
            map.put(key.trim(), value.trim());
        }

        return Map.copyOf(map);
    }

    public static final Class toClass(String val) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to class"
            );
        }

        try {
            return Thread.currentThread().getContextClassLoader().loadClass(
                    val.trim()
            );
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Class " + val + " can't be loaded",
                    e
            );
        }
    }

    public static ChromeMode toChromeMode(String val) {
        return toEnum(ChromeMode.class, val);
    }

    public static WebDriverMode toWebDriverMode(String val) {
        return toEnum(WebDriverMode.class, val);
    }

    public static <T extends Enum<T>> T toEnum(Class<T> enumClass, String val) {
        if (enumClass == null) {
            throw new IllegalArgumentException(
                    "Class is required"
            );
        }

        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(
                    "Can't convert empty string to enum"
            );
        }

        //Special case where the same enum is defined across different class loaders
        Class<T> reloadedEnumClass = toClass(enumClass.getName());
        return Enum.valueOf(reloadedEnumClass, val.trim());
    }

}
