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

import java.util.Timer;
import java.util.TimerTask;

final class HeartbeatManagerImpl implements HeartbeatManager {

    private final TimeProvider timeProvider;
    private final EventsRouter eventsRouter;
    private final Timer heartbeatTimer;

    public HeartbeatManagerImpl(TimeProvider timeProvider, EventsRouter eventsRouter) {
        this.timeProvider = timeProvider;
        this.eventsRouter = eventsRouter;
        this.heartbeatTimer = new Timer();
    }

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        long delay = loadGeneratorContext.getLoadGeneratorConfig().getEventsFlushInterval().toMillis();
        heartbeatTimer.schedule(
                new HeartbeatTask(loadGeneratorContext),
                delay,
                delay
        );
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        heartbeatTimer.cancel();
    }

    private class HeartbeatTask extends TimerTask {

        private final LoadGeneratorContextImpl loadGeneratorContext;

        public HeartbeatTask(LoadGeneratorContextImpl loadGeneratorContext) {
            this.loadGeneratorContext = loadGeneratorContext;
        }

        @Override
        public void run() {
            eventsRouter.onHeartbeat(
                    timeProvider.getCurrentTime(),
                    loadGeneratorContext
            );
        }

    }

}
