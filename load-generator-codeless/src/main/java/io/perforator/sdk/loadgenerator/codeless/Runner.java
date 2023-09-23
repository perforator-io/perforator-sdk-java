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

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Runner {

    public static void main(String[] args) throws Throwable {
        if (args == null || args.length == 0) {
            throw new RuntimeException("Config location is required");
        }

        Path configLocation = Path.of(args[0]);
        if (!Files.exists(configLocation)) {
            throw new FileNotFoundException("Config located at " + configLocation + " is not found");
        }

        new CodelessLoadGenerator(configLocation).run();
    }

}
