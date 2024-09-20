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

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Arrays;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TimeProviderImpl implements TimeProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeProviderImpl.class);

    private static final String[] TIME_SERVERS = new String[]{
            "time.cloudflare.com",
            "time.google.com",
            "time.windows.com",
            "time.facebook.com"
    };

    private final long offsetTime;

    public TimeProviderImpl() {
        this.offsetTime = buildOffset();
    }

    private static long buildOffset() {
        String ntpSyncEnabledFlag = System.getProperty("ntpSyncEnabled");
        if(ntpSyncEnabledFlag != null && ntpSyncEnabledFlag.equalsIgnoreCase("false")) {
            return 0;
        }
        
        NTPUDPClient timeClient = new NTPUDPClient();
        timeClient.setDefaultTimeout(Duration.ofSeconds(5));
        Exception lastException = null;

        for (String server : TIME_SERVERS) {
            try {
                InetAddress inetAddress = InetAddress.getByName(server);

                long[] offsets = new long[20];
                for (int i = 0; i < 20; i++) {
                    TimeInfo timeInfo = timeClient.getTime(inetAddress);
                    timeInfo.computeDetails();
                    offsets[i] = timeInfo.getOffset();
                }

                Arrays.sort(offsets);
                return offsets[10];
            } catch (IOException ex) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Can't synchronize time via {}", server, ex);
                }

                lastException = ex;
            } finally {
                timeClient.close();
            }
        }

        throw new RuntimeException(
                "Can't synchronize time via " + Arrays.toString(TIME_SERVERS),
                lastException
        );
    }

    @Override
    public final long getCurrentTime() {
        return System.currentTimeMillis() + offsetTime;
    }

}
