package org.example.fronted.api;

import org.example.fronted.config.ApiConfig;
import org.example.fronted.util.SessionManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class ApiWebClient {
    protected final WebClient webClient;

    // Constructor que recibe baseUrl
    protected ApiWebClient(String baseUrl) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(cfg -> cfg
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024) // 10MB
                )
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();
    }

    // Constructor para user-service por defecto (mantener compatibilidad)
    protected ApiWebClient() {
        this(ApiConfig.USER_SERVICE_URL);
    }

    // Factory methods para cada servicio
    public static UserApiClient forUserService() {
        return new UserApiClient();
    }

    public static ProjectApiClient forProjectService() {
        return new ProjectApiClient();
    }

    public static DocumentApiClient forDocumentService() {
        return new DocumentApiClient();
    }

    public static NotificationApiClient forNotificationService() {
        return new NotificationApiClient();
    }

    public static MessagingApiClient forMessagingService() {
        return new MessagingApiClient();
    }

    protected WebClient.RequestHeadersSpec<?> addAuthHeader(WebClient.RequestHeadersSpec<?> request) {
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request;
    }

    // ============ CLASES INTERNAS PARA CADA SERVICIO ============

    public static class UserApiClient extends ApiWebClient {
        public UserApiClient() {
            super(ApiConfig.USER_SERVICE_URL);
        }
    }

    public static class ProjectApiClient extends ApiWebClient {
        public ProjectApiClient() {
            super(ApiConfig.PROJECT_SERVICE_URL);
        }
    }

    public static class DocumentApiClient extends ApiWebClient {
        public DocumentApiClient() {
            super(ApiConfig.DOCUMENT_SERVICE_URL);
        }
    }

    public static class NotificationApiClient extends ApiWebClient {
        public NotificationApiClient() {
            super(ApiConfig.NOTIFICATION_SERVICE_URL);
        }
    }

    public static class MessagingApiClient extends ApiWebClient {
        public MessagingApiClient() {
            super(ApiConfig.MESSAGING_SERVICE_URL);
        }
    }
}