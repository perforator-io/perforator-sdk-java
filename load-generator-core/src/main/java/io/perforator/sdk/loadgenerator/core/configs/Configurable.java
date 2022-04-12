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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Interface for configuration classes which fields can get default values using
 * property providers.
 */
public interface Configurable {
    
    /**
     * Such prefix is concatenated with property name while polling property providers
     * for a default value.
     * @return  prefix to use while looking up a default value.
     */
    String getDefaultsPrefix();

    /**
     * Applies defaults on configurable fields from two property providers:
     * <ul>
     *   <li>{@link System#getProperty(java.lang.String) }</li>
     *   <li>{@link System#getenv(java.lang.String) }</li>
     * </ul>
     */
    default void applyDefaults() {
        applyDefaults(this, getDefaultsPrefix(), System::getProperty, System::getenv);
    }

    /**
     * Applies defaults on configurable fields from user-supplied property providers.
     * 
     * @param providers varargs of {@link Function functions} where to lookup up
     * for property defaults.
     */
    default void applyDefaults(Function<String, String>... providers) {
        applyDefaults(this, getDefaultsPrefix(), providers);
    }

    static Duration parseDuration(String duration) {
        if (duration.contains(" ")) {
            duration = duration.replace(" ", "");
        }

        return Duration.parse(formatDuration(duration));
    }

    private static void applyDefaults(Object instance, String prefix, Function<String, String>... providers) {
        if (instance == null) {
            return;
        }

        try {
            Class clazz = instance.getClass();
            while (clazz != Configurable.class && clazz != Object.class && clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }

                    applyDefaults(instance, field, prefix, providers);
                }
                clazz = clazz.getSuperclass();
            }
        } catch (SecurityException | ReflectiveOperationException e) {
            throw new RuntimeException("Can't apply defaults on " + instance.getClass());
        }
    }

    private static String formatDuration(String duration) {
        return (!duration.startsWith("pt") && !duration.startsWith("PT")) ? "pt" + duration : duration;
    }

    private static void applyDefaults(Object instance, Field field, String prefix, Function<String, String>... providers) throws ReflectiveOperationException {
        String defaultValue = getDefaultValue(field.getName(), prefix, providers);

        if (defaultValue == null) {
            return;
        }

        field.setAccessible(true);

        if (field.getType() == int.class) {
            field.setInt(instance, Integer.parseInt(defaultValue));
        } else if (field.getType() == Integer.class) {
            field.set(instance, Integer.valueOf(defaultValue));
        } else if (field.getType() == long.class) {
            field.setLong(instance, Long.parseLong(defaultValue));
        } else if (field.getType() == Long.class) {
            field.set(instance, Long.valueOf(defaultValue));
        } else if (field.getType() == double.class) {
            field.setDouble(instance, Double.parseDouble(defaultValue));
        } else if (field.getType() == Double.class) {
            field.set(instance, Double.valueOf(defaultValue));
        } else if (field.getType() == boolean.class) {
            field.setBoolean(instance, Boolean.parseBoolean(defaultValue));
        } else if (field.getType() == Boolean.class) {
            field.set(instance, Boolean.valueOf(defaultValue));
        } else if (field.getType() == String.class) {
            field.set(instance, defaultValue);
        } else if (field.getType() == Duration.class) {
            field.set(instance, parseDuration(defaultValue));
        } else if (field.getType() == WebDriverMode.class) {
            field.set(instance, WebDriverMode.valueOf(defaultValue));
        } else if (field.getType() == List.class) {
            field.set(instance, parseList(defaultValue));
        } else if (field.getType() == ChromeMode.class) {
            field.set(instance, ChromeMode.valueOf(defaultValue));
        } else if (field.getType() == Class.class) {
            field.set(instance, parseClass(instance, defaultValue));
        } else {
            throw new RuntimeException("Field " + field.getName() + " type " + field.getType() + " is not supported");
        }
    }

    private static Class parseClass(Object instance, String className) {
        try {
            return instance.getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Class " + className + " can't be loaded",
                    e
            );
        }
    }
    
    private static List<String> parseList(String items) {
        if(items == null || items.isBlank()) {
            return null;
        }
        
        return Arrays.stream(
                items.split(",")
        ).map(
                String::trim
        ).filter(
                i -> i != null && !i.isBlank()
        ).collect(
                Collectors.toList()
        );
    }

    private static String getDefaultValue(String fieldName, String prefix, Function<String, String>... providers) {
        List<String> permutations = buildNamesPermutations(fieldName, prefix);
        for (String name : permutations) {
            for (Function<String, String> provider : providers) {
                String result = provider.apply(name);
                if (result != null && !result.isBlank()) {
                    return result.trim();
                }
            }
        }

        return null;
    }

    private static List<String> buildNamesPermutations(String fieldName, String prefix) {
        List<String> result = new ArrayList<>();

        if (prefix != null) {
            result.add(prefix + fieldName);
            result.add(prefix + "." + fieldName);
            result.add(prefix + "_" + fieldName);

            result.add((prefix + fieldName).toLowerCase());
            result.add((prefix + "." + fieldName).toLowerCase());
            result.add((prefix + "_" + fieldName).toLowerCase());

            result.add((prefix + fieldName).toUpperCase());
            result.add((prefix + "." + fieldName).toUpperCase());
            result.add((prefix + "_" + fieldName).toUpperCase());
        }

        result.add(fieldName);
        result.add(fieldName.toLowerCase());
        result.add(fieldName.toUpperCase());

        return result;
    }

}
