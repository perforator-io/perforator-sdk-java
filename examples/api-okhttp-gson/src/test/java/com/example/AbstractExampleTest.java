package com.example;

import io.perforator.sdk.api.okhttpgson.ApiClientBuilder;
import io.perforator.sdk.api.okhttpgson.model.*;
import io.perforator.sdk.api.okhttpgson.operations.BrowserCloudsApi;
import io.perforator.sdk.api.okhttpgson.operations.CloudRunnersApi;
import io.perforator.sdk.api.okhttpgson.operations.ExecutionsApi;
import io.perforator.sdk.api.okhttpgson.operations.ProjectsApi;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractExampleTest<T> {

    protected static final String API_BASE_URL_ENV_VARIABLE = "LOADGENERATOR_APIBASEURL";
    protected static final String API_CLIENT_ID_ENV_VARIABLE = "LOADGENERATOR_APICLIENTID";
    protected static final String API_CLIENT_SECRET_ENV_VARIABLE = "LOADGENERATOR_APICLIENTSECRET";
    protected static final String PROJECT_KEY_ENV_VARIABLE = "LOADGENERATOR_PROJECTKEY";
    
    protected static final String API_BASE_URL_FIELD = "apiBaseUrl";
    protected static final String API_CLIENT_ID_FIELD = "apiClientId";
    protected static final String API_CLIENT_SECRET_FIELD = "apiClientSecret";
    protected static final String PROJECT_KEY_FIELD = "projectKey";
    protected static final String EXECUTION_KEY_FIELD = "executionKey";
    protected static final String BROWSER_CLOUD_KEY = "browserCloudKey";
    protected static final String CLOUD_RUNNER_KEY = "cloudRunnerKey";
    
    protected final String apiBaseUrl;
    protected final String apiClientId;
    protected final String apiClientSecret;
    protected final String projectKey;
    
    protected final Class<T> exampleClass;
    
    protected final ApiClientBuilder apiClientBuilder;
    protected final ProjectsApi projectsApi;
    protected final ExecutionsApi executionsApi;
    protected final BrowserCloudsApi browserCloudsApi;
    protected final CloudRunnersApi cloudRunnersApi;

    public AbstractExampleTest() {
        assumeTrue(hasRequiredVariable(API_BASE_URL_ENV_VARIABLE));
        assumeTrue(hasRequiredVariable(API_CLIENT_ID_ENV_VARIABLE));
        assumeTrue(hasRequiredVariable(API_CLIENT_SECRET_ENV_VARIABLE));
        assumeTrue(hasRequiredVariable(PROJECT_KEY_ENV_VARIABLE));
        
        this.apiBaseUrl = getRequiredVariable(
                API_BASE_URL_ENV_VARIABLE
        );
        this.apiClientId = getRequiredVariable(
                API_CLIENT_ID_ENV_VARIABLE
        );
        this.apiClientSecret = getRequiredVariable(
                API_CLIENT_SECRET_ENV_VARIABLE
        );
        this.projectKey = getRequiredVariable(
                PROJECT_KEY_ENV_VARIABLE
        );
        this.exampleClass = buildExampleClass(
                getClass()
        );
        this.apiClientBuilder = new ApiClientBuilder(
                apiClientId, 
                apiClientSecret, 
                apiBaseUrl
        );
        
        this.projectsApi = apiClientBuilder.getApi(ProjectsApi.class);
        this.executionsApi = apiClientBuilder.getApi(ExecutionsApi.class);
        this.browserCloudsApi = apiClientBuilder.getApi(BrowserCloudsApi.class);
        this.cloudRunnersApi = apiClientBuilder.getApi(CloudRunnersApi.class);
    }
    
    protected abstract Map<String, Object> getAdditionalFields() throws Exception;
    
    @Test
    public void verifyExampleStructure() throws Exception {
        assertNotNull(getExampleConstructor());
        assertNotNull(getExampleRunMethod());
        assertNotNull(getExampleMainMethod());
        
        assertNotNull(getExampleField(API_BASE_URL_FIELD));
        assertNotNull(getExampleField(API_CLIENT_ID_FIELD));
        assertNotNull(getExampleField(API_CLIENT_SECRET_FIELD));
        
        Map<String, Object> additionalFields = getAdditionalFields();
        
        for (String fieldName : additionalFields.keySet()) {
            assertNotNull(getExampleField(fieldName));
        }
        
        assertEquals(getClass().getName(), exampleClass.getName() + "Test");
    }
    
    @Test
    public void verifyExampleRunning() throws Exception {
        Map<String, Object> allFields = new HashMap<>();
        allFields.put(API_BASE_URL_FIELD, apiBaseUrl);
        allFields.put(API_CLIENT_ID_FIELD, apiClientId);
        allFields.put(API_CLIENT_SECRET_FIELD, apiClientSecret);
        allFields.putAll(getAdditionalFields());
        
        T example = newExampleInstance();
        applyFields(example, allFields);
        Method runMethod = getExampleRunMethod();
        runMethod.invoke(example);
    }
    
    protected T newExampleInstance() throws ReflectiveOperationException {
        return getExampleConstructor().newInstance();
    }
    
    protected Constructor<T> getExampleConstructor() throws ReflectiveOperationException {
        return exampleClass.getConstructor();
    }
    
    protected Method getExampleRunMethod() throws ReflectiveOperationException {
        return exampleClass.getDeclaredMethod("run");
    }
    
    protected Method getExampleMainMethod() throws ReflectiveOperationException {
        return exampleClass.getMethod("main", String[].class);
    }
    
    protected Field getExampleField(String fieldName) throws ReflectiveOperationException {
        return exampleClass.getDeclaredField(fieldName);
    }
    
    protected void applyFields(T instance, Map<String, Object> values) throws ReflectiveOperationException {
        if(values == null || values.isEmpty()) {
            return;
        }
        
        
        for (String fieldName : values.keySet()) {
            Field field = getExampleField(fieldName);
            field.setAccessible(true);
            field.set(instance, values.get(fieldName));
        }
    }
    
    protected Execution getExecution() throws Exception {
        List<Execution> executions = executionsApi.listExecutions(
                projectKey
        );
        assertNotNull(executions);
        assertFalse(executions.isEmpty());
        
        return executions.get(0);
    }
    
    protected static String getRequiredVariable(String name) {
        String result = System.getenv(name);
        
        if(result != null && !result.isBlank()) {
            return result;
        }
        
        throw new RuntimeException("Env variable '" + name + "' is required to run the test");
    }
    
    protected static boolean hasRequiredVariable(String name) {
        String value = System.getenv(name);
        return value != null && !value.isBlank();
    }
    
    private static <T> Class<T> buildExampleClass(Class clazz) {
        return (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
    }

}
