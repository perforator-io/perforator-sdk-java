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

public final class LoadGeneratorConfigValidator {

    private LoadGeneratorConfigValidator() {
    }

    public static LoadGeneratorConfig validate(LoadGeneratorConfig loadGeneratorConfig) {
        if (loadGeneratorConfig.getBrowserCloudHttpHeaders() != null && !loadGeneratorConfig.getBrowserCloudHttpHeaders().isEmpty()) {

            Pattern namePattern = Pattern.compile("^([\\w-]+)$");
            Pattern valuePattern = Pattern.compile("^([^;]+)$");

            for (Map.Entry<String, String> entry : loadGeneratorConfig.getBrowserCloudHttpHeaders().entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();

                if (name == null || !namePattern.matcher(name).find()) {
                    throw new RuntimeException(
                            "Bad '" + LoadGeneratorConfig.Fields.browserCloudHttpHeaders + "' field format." +
                                    " The header's name '" + name + "' is invalid."
                    );
                }
                if (value == null || !valuePattern.matcher(value).find()) {
                    throw new RuntimeException(
                            "Bad '" + LoadGeneratorConfig.Fields.browserCloudHttpHeaders + "' field format. " +
                                    "The header's value '" + value + "' is invalid."
                    );
                }

            }
        }

        if (loadGeneratorConfig.getBrowserCloudHosts() != null && !loadGeneratorConfig.getBrowserCloudHosts().isEmpty()) {

            Pattern hostPattern = Pattern.compile("^([/.:\\w]+)$");
            Pattern ipPattern = Pattern.compile("^((\\.|^)(25[0-5]|2[0-4]\\d|1\\d\\d|\\d\\d?|0)){4}$");

            for (Map.Entry<String, String> entry : loadGeneratorConfig.getBrowserCloudHosts().entrySet()) {
                String hostname = entry.getKey();
                String ip = entry.getValue();

                if (hostname == null || !hostPattern.matcher(hostname).find()) {
                    throw new RuntimeException(
                            "Bad '" + LoadGeneratorConfig.Fields.browserCloudHosts + "' field format." +
                                    " The hostname '" + hostname + "' is invalid."
                    );
                }
                if (ip == null || !ipPattern.matcher(ip).find()) {
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
