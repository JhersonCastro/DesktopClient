package org.example.fronted.api;

import org.springframework.web.reactive.function.client.WebClient;
public abstract class ApiWebClient {
    protected final WebClient webClient;

    protected ApiWebClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080") // Gateway base URL
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
