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
package io.perforator.sdk.loadgenerator.core.internal;

import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

final class InfoMessagesManagerImpl implements InfoMessagesManager {

    public static final String START_BANNER = "\n" +
            " -------------------------------------------------------------------------------------------------------------------------------\n" +
            "|                                                                                                                               |\n" +
            "|    |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||    |\n" +
            "|                                                                                                                               |\n" +
            "|                                              Load generation is about to start.                                               |\n" +
            "|                           Please open the link below in the browser to see statistics in real-time.                           |\n" +
            "|                                                                                                                               |\n" +
            "|%s|\n" +
            "|                                                                                                                               |\n" +
            "|    |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||    |\n" +
            "|                                                                                                                               |\n" +
            " -------------------------------------------------------------------------------------------------------------------------------\n";
    public static final String FINISH_BANNER = "\n" +
            " -------------------------------------------------------------------------------------------------------------------------------\n" +
            "|                                                                                                                               |\n" +
            "|    |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||    |\n" +
            "|                                                                                                                               |\n" +
            "|                                                   Load generation completed.                                                  |\n" +
            "|                           Please open the link below in the browser to review detailed statistics.                            |\n" +
            "|                                                                                                                               |\n" +
            "|%s|\n" +
            "|                                                                                                                               |\n" +
            "|    |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||    |\n" +
            "|                                                                                                                               |\n" +
            " -------------------------------------------------------------------------------------------------------------------------------\n";
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoMessagesManagerImpl.class);
    private final List<String> startBannerShowedByConfigId = new ArrayList();

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        if (loadGeneratorContext.isLocalOnly()) {
            return;
        }
        LoadGeneratorConfig config = loadGeneratorContext.getLoadGeneratorConfig();
        LOGGER.info(
                formatBanner(START_BANNER, getUrl(config))
        );
        startBannerShowedByConfigId.add(loadGeneratorContext.getLoadGeneratorConfig().getId());
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        if (loadGeneratorContext.isLocalOnly() || !startBannerShowedByConfigId.contains(loadGeneratorContext.getLoadGeneratorConfig().getId())) {
            return;
        }
        LoadGeneratorConfig config = loadGeneratorContext.getLoadGeneratorConfig();
        LOGGER.info(
                formatBanner(FINISH_BANNER, getUrl(config))
        );
    }

    private String getUrl(LoadGeneratorConfig config) {
        return config.getApiBaseUrl().replace("api", "app") +
                "/statistics/" +
                config.getProjectKey() + "/" +
                config.getExecutionKey() +
                "";
    }

    private String formatBanner(String banner, String url) {
        int lineSize = 0;
        int urlSize = url.length();
        for (String line : banner.split("\n")) {
            if (line.length() > urlSize) {
                lineSize = line.replace("|", "").length();
                break;
            }
        }
        int spacesCount = (lineSize - urlSize) / 2;
        String spaces = " ".repeat(Math.max(0, spacesCount));
        String urlLine = spaces + url + spaces;
        return String.format(banner, urlLine);
    }
}