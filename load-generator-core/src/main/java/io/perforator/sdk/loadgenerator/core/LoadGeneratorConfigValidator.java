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
package io.perforator.sdk.loadgenerator.core;

import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;

import java.util.Map;
import java.util.regex.Pattern;

final class LoadGeneratorConfigValidator {
    
    private static final Pattern HOST_NAME_PATTERN = Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");
    private static final Pattern IP_V4_PATTERN = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    private static final Pattern HEADER_NAME_PATTERN = Pattern.compile("[^\\s:]+");

    private LoadGeneratorConfigValidator() {
    }

    public static LoadGeneratorConfig validate(LoadGeneratorConfig loadGeneratorConfig) {
        if (loadGeneratorConfig.getBrowserCloudHttpHeaders() != null && !loadGeneratorConfig.getBrowserCloudHttpHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : loadGeneratorConfig.getBrowserCloudHttpHeaders().entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                
                if (name == null || name.isBlank()) {
                    throw new RuntimeException(
                            "Bad '"
                            + LoadGeneratorConfig.Fields.browserCloudHttpHeaders
                            + "' field format - header name is required"
                    );
                }

                if (!HEADER_NAME_PATTERN.matcher(name).matches()) {
                    throw new RuntimeException(
                            "Bad '" + LoadGeneratorConfig.Fields.browserCloudHttpHeaders + "' field format." +
                                    " The header's name '" + name + "' is invalid."
                    );
                }
                if (value == null) {
                    throw new RuntimeException(
                            "Bad '" + LoadGeneratorConfig.Fields.browserCloudHttpHeaders + "' field format. "
                            + "Header '" + name + "' value is required"
                    );
                }
            }
        }

        if (loadGeneratorConfig.getBrowserCloudHosts() != null && !loadGeneratorConfig.getBrowserCloudHosts().isEmpty()) {
            for (Map.Entry<String, String> entry : loadGeneratorConfig.getBrowserCloudHosts().entrySet()) {
                String hostname = entry.getKey();
                String ip = entry.getValue();

                if (hostname == null || !HOST_NAME_PATTERN.matcher(hostname).matches()) {
                    throw new RuntimeException(
                            "Bad '" + LoadGeneratorConfig.Fields.browserCloudHosts + "' field format." +
                                    " The hostname '" + hostname + "' is invalid."
                    );
                }
                if (ip == null || !IP_V4_PATTERN.matcher(ip).matches()) {
                    throw new RuntimeException(
                            "Bad '" + LoadGeneratorConfig.Fields.browserCloudHosts + "' field format. " +
                                    "The IP address '" + ip + "' is invalid."
                    );
                }

            }
        }

        return loadGeneratorConfig;
    }
}
