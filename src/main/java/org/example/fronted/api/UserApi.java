package org.example.fronted.api;

import org.example.fronted.dto.RegistroUsuarioDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserApi extends ApiWebClient {

    public UserApi() {
        super(); // Usa localhost:8081 por defecto
    }

    public UserApi(String baseUrl) {
        super(baseUrl);
    }

    /**
     * Registra un usuario en el user-microservice.
     */
    public Mono<Boolean> registrarUsuario(RegistroUsuarioDTO dto) {
        String path = "/api/usuarios/registro";

        // Cuerpo JSON que espera el backend
        Map<String, Object> body = new HashMap<>();
        body.put("email", dto.getEmail());
        body.put("password", dto.getPassword());
        body.put("nombres", dto.getNombres());
        body.put("apellidos", dto.getApellidos());
        body.put("celular", dto.getCelular());
        body.put("programa", "INGENIERIA_SISTEMAS");
        body.put("roles", dto.getRoles());

        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorResume(error -> {
                    System.err.println("Error en registro: " + error.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<List<Map<String, Object>>> buscarUsuarios(String query) {
        String path = "/api/usuarios/search?q=" + query;

        // Construye la solicitud paso a paso
        WebClient.RequestHeadersUriSpec<?> requestUriSpec = webClient.get();
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = requestUriSpec.uri(path);
        requestHeadersSpec = requestHeadersSpec.accept(MediaType.APPLICATION_JSON);

        WebClient.ResponseSpec responseSpec = requestHeadersSpec.retrieve();

        return responseSpec
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .onErrorResume(error -> {
                    // Usa error.toString() en lugar de error.getMessage()
                    System.err.println("Error en búsqueda: " + error);
                    return Mono.just(Collections.emptyList());
                });
    }
}