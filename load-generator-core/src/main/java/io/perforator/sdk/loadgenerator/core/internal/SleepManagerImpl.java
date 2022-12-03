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

import io.perforator.sdk.loadgenerator.core.Threaded;

final class SleepManagerImpl implements SleepManager {

    private static final int SLEEP_STEPS = 10;
    private static final long SLEEP_STEP_MIN = 1000;
    private static final long SLEEP_STEP_MAX = 10000;

    private final TimeProvider timeProvider;
    private final EventsRouter eventsRouter;

    public SleepManagerImpl(TimeProvider timeProvider, EventsRouter eventsRouter) {
        this.timeProvider = timeProvider;
        this.eventsRouter = eventsRouter;
    }

    @Override
    public void sleep(SuiteInstanceContextImpl context, long duration) {
        if (duration < 0) {
            return;
        }

        long maxTime = System.currentTimeMillis() + duration;
        long step = duration / SLEEP_STEPS;

        if (step < SLEEP_STEP_MIN) {
            step = SLEEP_STEP_MIN;
        } else if (step > SLEEP_STEP_MAX) {
            step = SLEEP_STEP_MAX;
        }

        while (System.currentTimeMillis() < maxTime) {
            eventsRouter.onSuiteInstanceKeepAlive(
                    timeProvider.getCurrentTime(),
                    context
            );

            long remaining = maxTime - System.currentTimeMillis();
            if (remaining <= 0) {
                Threaded.sleep(0);
            } else if (remaining < step) {
                Threaded.sleep(remaining);
            } else {
                Threaded.sleep(step);
            }
        }
    }

}
