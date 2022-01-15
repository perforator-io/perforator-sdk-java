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
