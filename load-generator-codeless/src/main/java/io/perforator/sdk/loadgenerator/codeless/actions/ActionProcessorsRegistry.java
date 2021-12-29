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
package io.perforator.sdk.loadgenerator.codeless.actions;

import java.util.*;
import java.util.stream.Collectors;

public final class ActionProcessorsRegistry {

    public static final ActionProcessorsRegistry INSTANCE = new ActionProcessorsRegistry();

    private final List<ActionProcessor> actionProcessors;
    private final Map<String, ActionProcessor> mappedProcessors;

    private ActionProcessorsRegistry() {
        this.actionProcessors = ServiceLoader.load(
                ActionProcessor.class,
                getClass().getClassLoader()
        ).stream().map(
                ServiceLoader.Provider::get
        ).collect(
                Collectors.toUnmodifiableList()
        );

        Map<String, ActionProcessor> mapping = new HashMap<>();

        for (ActionProcessor actionProcessor : actionProcessors) {
            String actionName = actionProcessor.getSupportedActionName();

            if (mapping.containsKey(actionName)) {
                throw new RuntimeException(
                        "Action processors should not have duplicate names: "
                                + actionName
                );
            }

            mapping.put(actionName, actionProcessor);
        }

        this.mappedProcessors = Collections.unmodifiableMap(mapping);
    }

    public List<ActionProcessor> getActionProcessors() {
        return actionProcessors;
    }

    public ActionProcessor getActionProcessorByName(String actionName) {
        return mappedProcessors.get(actionName);
    }

}
