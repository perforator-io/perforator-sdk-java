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
package com.example.projects;

import com.example.AbstractExampleTest;
import io.perforator.sdk.api.okhttpgson.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractProjectExampleTest<T> extends AbstractExampleTest<T> {
    
    protected Project initialProject;
    
    @BeforeAll
    public void setup() throws Exception {
        initialProject = projectsApi.getProject(
                projectKey
        );
    }
    
    @AfterAll
    public void cleanup() throws Exception {
        projectsApi.updateProject(
                projectKey,
                initialProject
        );
    }
    
}
