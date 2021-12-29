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
package io.perforator.sdk.api.okhttpgson;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LimitsApiTest extends AbstractApiTest {
    
    @Test
    public void validateLimits() throws Exception {
        Map<String, Integer> limits = limitsApi.getLimits();
        assertNotNull(limits);
        assertFalse(limits.isEmpty());
    }
    
}
