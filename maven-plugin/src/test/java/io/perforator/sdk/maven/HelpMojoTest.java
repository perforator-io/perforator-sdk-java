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
package io.perforator.sdk.maven;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HelpMojoTest extends AbstractMojoTestCase {
    
    @BeforeAll
    public void setup() throws Exception {
        super.setUp();
    }
    
    @Test
    public void verify() throws Exception {
        MojoExecution mojoExecution = newMojoExecution("help");
        assertNotNull(mojoExecution);
    }
    
}
