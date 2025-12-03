package org.example.fronted.api;

import org.example.fronted.util.SessionManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class ApiWebClient {
    protected final WebClient webClient;

    // Constructor por defecto: sigue apuntando al user-microservice
    protected ApiWebClient() {
        this("http://localhost:8081");
    }

    // Constructor general: permite otros baseUrl (ej. proyectos)
    protected ApiWebClient(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    protected WebClient.RequestHeadersSpec<?> addAuthHeader(WebClient.RequestHeadersSpec<?> request) {
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request;
    }
}
