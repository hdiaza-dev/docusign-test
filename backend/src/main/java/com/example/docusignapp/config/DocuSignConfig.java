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

    @Value("${docusign.privateKeyPath:}")     // NUEVO: ruta absoluta (secret file)
    private String privateKeyPath;

    @Value("${docusign.privateKeyFilename:}") // LEGADO: resources/local
    private String privateKeyFilename;

    @Value("${docusign.scopes}")
    private String scopes;

    @Bean
    public ApiClient apiClient() throws Exception {
        ApiClient apiClient = new ApiClient(basePath);
        apiClient.setOAuthBasePath(authServer);

        String privateKey = resolvePrivateKeyPem();
        List<String> scopeList = Arrays.asList(scopes.split("\\s+"));

        OAuth.OAuthToken token = apiClient.requestJWTUserToken(
                clientId, userId, scopeList, privateKey.getBytes(StandardCharsets.UTF_8), 3600);

        apiClient.setAccessToken(token.getAccessToken(), token.getExpiresIn());
        return apiClient;
    }

    public String getAccountId() {
        return accountId;
    }

    public ApiClient createFreshApiClient() throws Exception {
        ApiClient freshClient = new ApiClient(basePath);
        freshClient.setOAuthBasePath(authServer);

        String privateKey = resolvePrivateKeyPem();
        List<String> scopeList = Arrays.asList(scopes.split("\\s+"));

        OAuth.OAuthToken token = freshClient.requestJWTUserToken(
                clientId, userId, scopeList, privateKey.getBytes(StandardCharsets.UTF_8), 3600);

        freshClient.setAccessToken(token.getAccessToken(), token.getExpiresIn());
        return freshClient;
    }

    private String resolvePrivateKeyPem() throws IOException {
        // 1) ENV (contenido PEM completo)
        String envPem = System.getenv("DOCUSIGN_PRIVATE_KEY");
        if (envPem != null && !envPem.isBlank()) return envPem;

        // 2) Secret file montado
        if (privateKeyPath != null && !privateKeyPath.isBlank()) {
            Path p = Path.of(privateKeyPath);
            try {
                return Files.readString(p, StandardCharsets.UTF_8);
            } catch (IOException e) {
                boolean exists = Files.exists(p);
                throw new IOException("Couldn't read PEM at " + privateKeyPath +
                        " (exists=" + exists + ")", e);
            }
        }

        // 3) Fallback local para desarrollo
        if (privateKeyFilename != null && !privateKeyFilename.isBlank()) {
            Path path = Path.of("src/main/resources/" + privateKeyFilename);
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }
        }

        throw new IOException("Private key PEM not found. Provide DOCUSIGN_PRIVATE_KEY or docusign.privateKeyPath.");
    }
}
