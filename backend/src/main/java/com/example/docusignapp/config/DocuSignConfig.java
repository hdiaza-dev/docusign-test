package com.example.docusignapp.config;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.auth.OAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "docusign.enabled", havingValue = "true")
public class DocuSignConfig {

    @Value("${docusign.basePath}")
    private String basePath;

    @Value("${docusign.authServer}")
    private String authServer;

    @Value("${docusign.clientId}")
    private String clientId;

    @Value("${docusign.userId}")
    private String userId;

    @Value("${docusign.accountId}")
    private String accountId;

    @Value("${docusign.privateKeyFilename}")
    private String privateKeyFilename;

    @Value("${docusign.scopes}")
    private String scopes;

    public ApiClient createFreshApiClient() throws Exception {
        ApiClient apiClient = new ApiClient(basePath);
        apiClient.setOAuthBasePath(authServer);

        String privateKey = loadPrivateKeyPem(privateKeyFilename);
        List<String> scopeList = Arrays.asList(scopes.split("\s+"));

        // Request JWT token (valid for ~1 hour). The SDK handles grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer
        OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken(
                clientId, userId, scopeList, privateKey.getBytes(StandardCharsets.UTF_8), 3600);

        apiClient.setAccessToken(oAuthToken.getAccessToken(), oAuthToken.getExpiresIn());
        return apiClient;
    }

    @Bean
    public ApiClient apiClient() throws Exception {
        return createFreshApiClient();
    }

    public String getAccountId() {
        return accountId;
    }

    private String loadPrivateKeyPem(String filename) throws IOException {
        try {
            Path path = Path.of("src/main/resources/" + filename);
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IOException("Private key PEM file not found: " + filename + ". Place it under src/main/resources/", ex);
        }
    }
}
