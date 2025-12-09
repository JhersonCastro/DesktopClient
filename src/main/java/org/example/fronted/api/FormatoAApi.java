package org.example.fronted.api;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

public class FormatoAApi extends ApiWebClient {

    public FormatoAApi() {
        super("http://localhost:8082"); // microservicio de proyectos
    }

    // =========================
    // SUBIR FORMATO A
    // =========================
    public Mono<Void> subirFormatoA(
            String titulo,
            String modalidad,
            String directorEmail,
            String codirectorEmail,
            String estudiante1Email,
            ByteArrayResource pdf,
            ByteArrayResource carta
    ) {
        WebClient.RequestHeadersSpec<?> req = webClient.post()
                .uri("/api/v1/proyectos/formatoA")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("titulo", titulo)
                        .with("modalidad", modalidad)
                        .with("directorEmail", directorEmail)
                        .with("codirectorEmail", codirectorEmail)
                        .with("estudiante1Email", estudiante1Email)
                        .with("pdf", pdf)
                        .with("carta", carta)
                );

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // =========================
    // EVALUAR FORMATO A
    // =========================
    public Mono<Map> evaluarFormatoA(Long idProyecto, boolean aprobado, String observaciones) {
        WebClient.RequestHeadersSpec<?> req = webClient.post()
                .uri(uri -> uri.path("/api/v1/proyectos/{idProyecto}/formatoA/evaluar")
                        .queryParam("aprobado", aprobado)
                        .queryParam("observaciones", observaciones)
                        .build(idProyecto));

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(Map.class);
    }

    // =========================
    // OBTENER FORMATOS A POR ESTUDIANTE
    // =========================
    public Mono<Map[]> obtenerFormatosAPorEstudiante(String email) {
        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri("/api/v1/proyectos/estudiante/{email}/formatoA", email);

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(Map[].class);
    }

    // =========================
    // REINTENTAR PROYECTO (Formato A)
    // =========================
    public Mono<Void> reintentarProyecto(Long id) {
        WebClient.RequestHeadersSpec<?> req = webClient.post()
                .uri("/api/v1/proyectos/{id}/reintentar", id);

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(Void.class);
    }
}