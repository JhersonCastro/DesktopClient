package org.example.fronted.api;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

public class DebugApi extends ApiWebClient {

    public DebugApi() {
        // Ajusta la URL base al puerto donde corre tu aplicación
        super("http://localhost:8082");
    }

    // -------------------------
    // OBTENER INFORMACIÓN DE AUTENTICACIÓN
    // -------------------------
    public Mono<Map> obtenerAuthInfo() {
        WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri("/debug/auth");

        // Añadir cabecera de autenticación (ej. Bearer Token)
        request = addAuthHeader(request);

        return request
                .retrieve()
                .bodyToMono(Map.class);
    }
}
