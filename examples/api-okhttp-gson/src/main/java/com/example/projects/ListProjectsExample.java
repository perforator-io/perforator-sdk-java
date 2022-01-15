package com.example.projects;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.ProjectsApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListProjectsExample {

    Logger logger = LoggerFactory.getLogger(
            ListProjectsExample.class
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

        List<Project> projects = projectsApi.listProjects();
        logger.info("There are {} projects", projects.size());

        for (Project project : projects) {
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
    }

    public static void main(String[] args) throws Exception {
        new ListProjectsExample().run();
    }

}
