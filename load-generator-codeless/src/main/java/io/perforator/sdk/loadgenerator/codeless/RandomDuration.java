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

import java.time.Duration;
import java.util.SplittableRandom;

public class RandomDuration {

    private final Duration from;
    private final Duration to;

    public RandomDuration(Duration from) {
        this(from, null);
    }

    public RandomDuration(Duration from, Duration to) {
        this.from = from;
        this.to = to;
    }

    public Duration random() {
        if (from == null && to == null) {
            return Duration.ZERO;
        } else if (from == null) {
            return to;
        } else if (to == null) {
            return from;
        } else {
            return Duration.ofMillis(
                    new SplittableRandom().nextLong(
                            from.toMillis(),
                            to.toMillis()
                    )
            );
        }
    }

    public Duration getFrom() {
        return from;
    }

    public Duration getTo() {
        return to;
    }

    @Override
    public String toString() {
        if (from == null && to == null) {
            return "0s";
        } else if (from == null) {
            return to.toString();
        } else if (to == null) {
            return from.toString();
        } else {
            return from + "-" + to;
        }
    }
    
}
