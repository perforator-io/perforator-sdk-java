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

//TODO: add javadoc
public final class Threaded {

    public static final boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    public static final boolean isInterruptionException(Throwable e) {
        Throwable curr = e;

        while (curr != null) {
            if (curr instanceof InterruptedException) {
                return true;
            } else {
                curr = curr.getCause();
            }
        }

        return false;
    }

    public static final InterruptedException getInteruptionCause(Throwable e) {
        Throwable curr = e;

        while (curr != null) {
            if (curr instanceof InterruptedException) {
                return (InterruptedException) curr;
            } else {
                curr = curr.getCause();
            }
        }

        return null;
    }

    public static final void sleep(long millis) {
        sleep(millis, null);
    }

    public static final void sleep(long millis, String interruptionMessage) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            if (interruptionMessage != null) {
                throw new RuntimeException(interruptionMessage, e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

}
