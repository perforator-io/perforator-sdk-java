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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ConfigPopulator {

    private static final Map<Class, List<Method>> BUILDER_MAPPING = new ConcurrentHashMap<>();

    private ConfigPopulator() {
    }

    public static ConfigBuilder applyDefaults(ConfigBuilder instance, String prefix, List<Function<String, String>> providers) {
        if (instance == null) {
            return null;
        }

        if (providers == null || providers.isEmpty()) {
            return instance;
        }

        List<Method> builderMethods = BUILDER_MAPPING.computeIfAbsent(
                instance.getClass(), c -> getBuilderMethods(c)
        );

        if (builderMethods == null || builderMethods.isEmpty()) {
            return instance;
        }

        ConfigBuilder result = instance;
        for (Method method : builderMethods) {
            try {
                result = (ConfigBuilder) applyDefaults(result, method, prefix, providers);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(
                        "Can't populate config field " + method.getName() + " for " + instance.getClass() + "(" + prefix + ")",
                        e
                );
            }
        }

        return result;
    }

    private static Object applyDefaults(ConfigBuilder instance, Method method, String prefix, List<Function<String, String>> providers) throws IllegalAccessException, InvocationTargetException {
        String defaultValue = getDefaultValue(method.getName(), prefix, providers);
        if (defaultValue == null || defaultValue.isBlank()) {
            return instance;
        }

        Class fieldType = method.getParameterTypes()[0];

        if (fieldType == int.class) {
            return method.invoke(
                    instance,
                    StringConverter.toInt(defaultValue)
            );
        } else if (fieldType == Integer.class) {
            return method.invoke(
                    instance,
                    StringConverter.toInteger(defaultValue)
            );
        } else if (fieldType == long.class) {
            return method.invoke(
                    instance,
                    StringConverter.toLong(defaultValue)
            );
        } else if (fieldType == Long.class) {
            return method.invoke(
                    instance,
                    StringConverter.toLongObject(defaultValue)
            );
        } else if (fieldType == double.class) {
            return method.invoke(
                    instance,
                    StringConverter.toDouble(defaultValue)
            );
        } else if (fieldType == Double.class) {
            return method.invoke(
                    instance,
                    StringConverter.toDoubleObject(defaultValue)
            );
        } else if (fieldType == boolean.class) {
            return method.invoke(
                    instance,
                    StringConverter.toBoolean(defaultValue)
            );
        } else if (fieldType == Boolean.class) {
            return method.invoke(
                    instance,
                    StringConverter.toBooleanObject(defaultValue)
            );
        } else if (fieldType == String.class) {
            return method.invoke(
                    instance,
                    StringConverter.toString(defaultValue)
            );
        } else if (fieldType == Duration.class) {
            return method.invoke(
                    instance,
                    StringConverter.toDuration(defaultValue)
            );
        } else if (fieldType == WebDriverMode.class) {
            return method.invoke(
                    instance,
                    StringConverter.toWebDriverMode(defaultValue)
            );
        } else if (fieldType == ChromeMode.class) {
            return method.invoke(
                    instance,
                    StringConverter.toChromeMode(defaultValue)
            );
        } else if (fieldType == Iterable.class) {
            return method.invoke(
                    instance,
                    StringConverter.toStringList(defaultValue)
            );
        } else if (fieldType == Collection.class) {
            return method.invoke(
                    instance,
                    StringConverter.toStringList(defaultValue)
            );
        } else if (fieldType == List.class) {
            return method.invoke(
                    instance,
                    StringConverter.toStringList(defaultValue)
            );
        } else if (fieldType == Map.class) {
            return method.invoke(
                    instance,
                    StringConverter.toStringMap(defaultValue)
            );
        } else if (fieldType == Class.class) {
            return method.invoke(
                    instance,
                    StringConverter.toClass(defaultValue)
            );
        } else {
            throw new RuntimeException("Field " + method.getName() + " type " + fieldType + " is not supported");
        }
    }
    
    private static List<Method> getBuilderMethods(Class<ConfigBuilder> builderClass) {
        List<Method> result = new ArrayList<>();
        Set<String> configFields = getFieldsFromBuilderClass(builderClass);

        for (String field : configFields) {
            Class classToCheck = builderClass;
            while (classToCheck != null && ConfigBuilder.class.isAssignableFrom(classToCheck)) {
                Method[] methods = classToCheck.getDeclaredMethods();
                Method foundMethod = null;

                for (Method method : methods) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        continue;
                    }
                    if (!Modifier.isPublic(method.getModifiers())) {
                        continue;
                    }
                    if (method.getParameterCount() != 1) {
                        continue;
                    }
                    if (method.getName().equals(field)) {
                        foundMethod = method;
                        break;
                    }
                }

                if (foundMethod != null) {
                    result.add(foundMethod);
                    break;
                } else {
                    classToCheck = classToCheck.getSuperclass();
                }
            }
        }

        return List.copyOf(result);
    }

    private static Set<String> getFieldsFromBuilderClass(Class<ConfigBuilder> builderClass) {
        String configClassName = builderClass.getName().replaceAll("\\$.*", "");
        Class configClass;
        try {
            configClass = Class.forName(configClassName);
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(
                    "Can't extract fields listing - config " + builderClass + " is not supported",
                    e
            );
        }
        
        return getFieldsFromConfigClass(configClass);
    }
    
    private static Set<String> getFieldsFromConfigClass(Class configClass) {
        if (configClass == null || !Config.class.isAssignableFrom(configClass)) {
            return Collections.EMPTY_SET;
        }

        Set<String> result = new HashSet<>();

        while (configClass != null) {
            if (Config.class.isAssignableFrom(configClass)) {
                String fieldsClassName = configClass.getName() + "$Fields";
                try {
                    Class fieldsClass = Class.forName(fieldsClassName);
                    Field[] fields = fieldsClass.getDeclaredFields();

                    for (Field field : fields) {
                        if (!Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        if (!Modifier.isFinal(field.getModifiers())) {
                            continue;
                        }
                        if (!field.getType().equals(String.class)) {
                            continue;
                        }

                        result.add((String) field.get(null));
                    }
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(
                            "Can't extract fields listing - config " + configClass + " is not supported",
                            e
                    );
                }
            }

            configClass = configClass.getSuperclass();
        }

        return Set.copyOf(result);
    }

    private static String getDefaultValue(String fieldName, String prefix, List<Function<String, String>> providers) {
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

        if (prefix != null && !prefix.isBlank()) {
            prefix = prefix.trim();
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
        }

        result.add(fieldName);
        result.add(fieldName.toLowerCase());
        result.add(fieldName.toUpperCase());

        return result;
    }

}
