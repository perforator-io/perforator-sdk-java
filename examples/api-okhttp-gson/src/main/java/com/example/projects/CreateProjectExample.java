package com.example.projects;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.ProjectsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateProjectExample {

    Logger logger = LoggerFactory.getLogger(
            CreateProjectExample.class
    );
    
    String apiBaseUrl = ApiClientBuilder.DEFAULT_API_BASE_URL;
    
    //Please replace YOUR_CLIENT_ID with you own client id
    String apiClientId = "YOUR_CLIENT_ID";
    
    //Please replace YOUR_CLIENT_SECRET with you own client secret
    String apiClientSecret = "YOUR_CLIENT_SECRET";
    
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
        project.setName(
                "example.com - stress testing in prod"
        );
        project.setNotes(
                "Optional, but important content related to project purpose."
        );
        project = projectsApi.createProject(project);

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
        new CreateProjectExample().run();
    }

}
