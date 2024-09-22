package com.example.cloud_runners;

import com.example.AbstractExampleTest;
import io.perforator.sdk.api.okhttpgson.invoker.ApiException;
import io.perforator.sdk.api.okhttpgson.model.*;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class TerminateCloudRunnerExampleTest extends AbstractExampleTest<TerminateCloudRunnerExample> {

    private Execution execution;
    private CloudRunner cloudRunner;

    @BeforeAll
    public void setup() throws Exception {
        execution = executionsApi.createExecution(
                projectKey,
                new Execution()
        );

        cloudRunner = new CloudRunner();
        cloudRunner.setSshPublicKey("ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAILhKzVzL8RYBFDZblagiF1fPKLiI9JRYw8CrU2lrCwVN user@example.com");
        cloudRunner.setHardwareType(CloudRunnerHardwareType.T3_SMALL.toString());
        cloudRunner = cloudRunnersApi.startCloudRunner(
                projectKey,
                execution.getUuid(),
                cloudRunner
        );
    }

    @AfterAll
    public void revert() throws Exception {
        try {
            cloudRunnersApi.terminateCloudRunner(
                    projectKey,
                    execution.getUuid(),
                    cloudRunner.getUuid()
            );
        } catch (ApiException e) {
            //ignore
        }
    }

    @Override
    protected Map<String, Object> getAdditionalFields() throws Exception {
        return Map.of(
                PROJECT_KEY_FIELD, projectKey,
                EXECUTION_KEY_FIELD, execution.getUuid(),
                CLOUD_RUNNER_KEY, cloudRunner.getUuid()
        );
    }

}
