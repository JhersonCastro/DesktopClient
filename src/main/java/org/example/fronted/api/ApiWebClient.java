package org.example.fronted.api;

import org.example.fronted.util.SessionManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class ApiWebClient {

    protected final WebClient webClient;

    // Por defecto: user-microservice
    protected ApiWebClient() {
        this("http://localhost:8081");
    }

    // General: para otros microservicios
    protected ApiWebClient(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // Para GET/DELETE/etc.
    protected WebClient.RequestHeadersSpec<?> addAuthHeader(WebClient.RequestHeadersSpec<?> request) {
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request;
    }

    // Para POST/PUT con body (RequestBodySpec)
    protected WebClient.RequestBodySpec addAuthHeader(WebClient.RequestBodySpec request) {
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request;
    }
}
