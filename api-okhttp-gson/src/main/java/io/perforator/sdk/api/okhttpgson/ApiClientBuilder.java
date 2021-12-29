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

import io.perforator.sdk.api.okhttpgson.invoker.ApiClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApiClientBuilder {
    
    public static String DEFAULT_API_BASE_URL = "https://api.perforator.io";
    public static Duration DEFAULT_HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    public static Duration DEFAULT_HTTP_READ_TIMEOUT = Duration.ofSeconds(30);
    
    private final String clientId;
    private final String clientSecret;
    private final String apiBaseUrl;
    private final ApiClient apiClient;
    private final Map<Class, Object> instances = new HashMap<>();

    public ApiClientBuilder(String clientId, String clientSecret) {
        this(clientId, clientSecret, DEFAULT_API_BASE_URL);
    }
    
    public ApiClientBuilder(String clientId, String clientSecret, String apiBaseUrl) {
        this(clientId, clientSecret, apiBaseUrl, DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
    }
    
    public ApiClientBuilder(String clientId, String clientSecret, String apiBaseUrl, Duration connectTimeout, Duration readTimeout) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.apiBaseUrl = apiBaseUrl;
        this.apiClient = new ApiClient(
                apiBaseUrl,
                clientId,
                clientSecret,
                Collections.EMPTY_MAP
        );
        
        if(connectTimeout != null) {
            this.apiClient.setConnectTimeout((int)connectTimeout.toMillis());
        }
        
        if(readTimeout != null) {
            this.apiClient.setReadTimeout((int)readTimeout.toMillis());
        }
        
        this.apiClient.setUserAgent(generateUserAgent());
    }
    
    private String generateUserAgent() {
        LinkedHashSet<Artifact> artifactsSet = new LinkedHashSet<>();
        
        if (getClass().getClassLoader() != null) {
            for (Package definedPackage : getClass().getClassLoader().getDefinedPackages()) {
                String vendor = definedPackage.getImplementationVendor();
                if(vendor == null || vendor.isBlank() || !vendor.contains("Perforator")) {
                    continue;
                }
                
                String title = definedPackage.getImplementationTitle();
                if(title == null || title.isBlank()) {
                    continue;
                }
                
                String version = definedPackage.getImplementationVersion();
                if(version == null || version.isBlank()) {
                    version = "unknown";
                }

                artifactsSet.add(new Artifact(title, version));
            }
        }
        
        if(artifactsSet.isEmpty()) {
            return "perforator-sdk-api-okhttp-gson/unknown";
        }
        
        List<Artifact> artifacts = new ArrayList<>(artifactsSet);
        Collections.sort(
                artifacts,
                (a,b) -> a.getArtifactName().compareTo(b.getArtifactName())
        );
        
        StringBuilder result = new StringBuilder();
        result.append(artifacts.get(0).getArtifactName());
        result.append("/");
        result.append(artifacts.get(0).getArtifactVersion());
        
        result.append(" (");
        result.append(System.getProperty("os.name", "unknown").trim());
        result.append("; ").append(System.getProperty("os.version", "unknown").trim());
        result.append("; ").append(System.getProperty("os.arch", "unknown").trim());
        result.append(")");
        
        if(artifacts.size() > 1) {
            for (int i = 1; i < artifacts.size(); i++) {
                result.append(" ");
                result.append(artifacts.get(i).getArtifactName());
                result.append("/");
                result.append(artifacts.get(i).getArtifactVersion());
            }
        }
        
        return result.toString();
    }
    
    public <T> T getApi(Class<T> apiClass) {
        if (!apiClass.getName().startsWith("io.perforator.sdk.api.okhttpgson.operations")) {
            throw new IllegalArgumentException(
                    "apiClass is incorrect - it should be from io.perforator.sdk.api.okhttpgson.operations package"
            );
        }

        return (T) instances.computeIfAbsent(
                apiClass,
                c -> {
                    try {
                        return apiClass.getConstructor(ApiClient.class).newInstance(apiClient);
                    } catch (Exception e) {
                        throw new IllegalStateException(
                                "Can't instantiate new api instance => " + apiClass
                        );
                    }
                }
        );
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }
    
    private static class Artifact {
        
        private final String artifactName;
        private final String artifactVersion;

        public Artifact(String artifactName, String artifactVersion) {
            this.artifactName = artifactName;
            this.artifactVersion = artifactVersion;
        }

        public String getArtifactName() {
            return artifactName;
        }

        public String getArtifactVersion() {
            return artifactVersion;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + Objects.hashCode(this.artifactName);
            hash = 47 * hash + Objects.hashCode(this.artifactVersion);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Artifact other = (Artifact) obj;
            if (!Objects.equals(this.artifactName, other.artifactName)) {
                return false;
            }
            if (!Objects.equals(this.artifactVersion, other.artifactVersion)) {
                return false;
            }
            return true;
        }
    }
    
}
