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

    // NUEVO: ruta absoluta de secret file (Render)
    @Value("${docusign.privateKeyPath:}")
    private String privateKeyPath;

    // LEGADO: nombre de fichero en resources (solo útil en local)
    @Value("${docusign.privateKeyFilename:}")
    private String privateKeyFilename;

    @Value("${docusign.scopes}")
    private String scopes;

    @Bean
    public ApiClient apiClient() throws Exception {
        return createFreshApiClient();
    }

    public ApiClient createFreshApiClient() throws Exception {
        ApiClient apiClient = new ApiClient(basePath);
        apiClient.setOAuthBasePath(authServer);

        String privateKey = resolvePrivateKeyPem();
        List<String> scopeList = Arrays.asList(scopes.split("\\s+"));

        // JWT (1h aprox)
        OAuth.OAuthToken token = apiClient.requestJWTUserToken(
                clientId, userId, scopeList, privateKey.getBytes(StandardCharsets.UTF_8), 3600);

        apiClient.setAccessToken(token.getAccessToken(), token.getExpiresIn());
        return apiClient;
    }

    public String getAccountId() {
        return accountId;
    }

    private String resolvePrivateKeyPem() throws IOException {
        // 1) ENV con contenido PEM completo (incluyendo líneas BEGIN/END)
        String envPem = System.getenv("DOCUSIGN_PRIVATE_KEY");
        if (envPem != null && !envPem.isBlank()) {
            return envPem;
        }

        // 2) Secret file montado (Render) => docusign.privateKeyPath=/app/config/private_key.pem
        if (privateKeyPath != null && !privateKeyPath.isBlank()) {
            return Files.readString(Path.of(privateKeyPath), StandardCharsets.UTF_8);
        }

        // 3) Fallback local: buscar en resources por filename
        if (privateKeyFilename != null && !privateKeyFilename.isBlank()) {
            Path path = Path.of("src/main/resources/" + privateKeyFilename);
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }
        }

        throw new IOException("Private key PEM not found. Provide DOCUSIGN_PRIVATE_KEY (env) or docusign.privateKeyPath (file).");
    }
}
