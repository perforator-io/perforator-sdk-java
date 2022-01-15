package com.example.projects;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.ProjectsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatchProjectExample {

    Logger logger = LoggerFactory.getLogger(
            PatchProjectExample.class
    );
    
    String apiBaseUrl = ApiClientBuilder.DEFAULT_API_BASE_URL;
    
    //Please replace YOUR_CLIENT_ID with you own client id
    String apiClientId = "YOUR_CLIENT_ID";
    
    //Please replace YOUR_CLIENT_SECRET with you own client secret
    String apiClientSecret = "YOUR_CLIENT_SECRET";
    
    //Please replace YOUR_PROJECT_KEY with you own project key
    String projectKey = "YOUR_PROJECT_KEY";
    
    public void run() throws Exception {
        ApiClientBuilder builder = new ApiClientBuilder(
                apiClientId,
                apiClientSecret,
                apiBaseUrl
        );
        
        ProjectsApi projectsApi = builder.getApi(
                ProjectsApi.class
        );

        Project project = new Project();
        project.setNotes(
                "Optional, but important content related to project purpose."
                + " Project name should be preserved,"
                + " since it is omited during patch operation call."
        );
        
        project = projectsApi.patchProject(
                projectKey,
                project
        );

        logger.info(
                "Project: "
                + "key={}, "
                + "name={}, "
                + "notes={}, "
                + "createdAt={}, "
                + "updatedAt={}, ",
                project.getUuid(),
                project.getName(),
                project.getNotes(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public static void main(String[] args) throws Exception {
        new PatchProjectExample().run();
    }

}
