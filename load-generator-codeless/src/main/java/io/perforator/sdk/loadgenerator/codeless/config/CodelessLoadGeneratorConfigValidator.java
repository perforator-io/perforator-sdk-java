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
package io.perforator.sdk.loadgenerator.codeless.config;


import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;

import java.util.Map;
import java.util.regex.Pattern;

public final class CodelessLoadGeneratorConfigValidator {

    private CodelessLoadGeneratorConfigValidator() {
    }

    public static CodelessLoadGeneratorConfig validate(CodelessLoadGeneratorConfig loadGeneratorConfig) {
        if(loadGeneratorConfig.getBrowserCloudHttpHeaders() != null && !loadGeneratorConfig.getBrowserCloudHttpHeaders().isEmpty()){

            Pattern namePattern = Pattern.compile("^([\\w\\d-]+)$");
            Pattern valuePattern = Pattern.compile("^([^;]+)$");

            for(Map.Entry<String, String> entry: loadGeneratorConfig.getBrowserCloudHttpHeaders().entrySet()){
                String name = entry.getKey();
                String value = entry.getValue();

                if(name == null || !namePattern.matcher(name).find()){
                    throw new RuntimeException(
                            "Bad '" + LoadGeneratorConfig.Fields.browserCloudHttpHeaders + "' field format." +
                                    " The header's name '" + name + "' is invalid."
                    );
                }
                if(value == null || !valuePattern.matcher(value).find()){
                    throw new RuntimeException(
                            "Bad '" + LoadGeneratorConfig.Fields.browserCloudHttpHeaders + "' field format. " +
                                    "The header's value '" + value + "' is invalid."
                    );
                }

            }
        }

        return loadGeneratorConfig;
    }
}
