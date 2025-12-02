package org.example.fronted.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.example.fronted.util.SessionManager;

public abstract class ApiWebClient {
    protected final WebClient webClient;

    protected ApiWebClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8081") // Cambiado a user-microservice
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