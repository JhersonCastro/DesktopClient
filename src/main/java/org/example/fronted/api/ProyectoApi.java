package org.example.fronted.api;

import org.example.fronted.dto.ProyectoRequest;
import org.example.fronted.models.ProyectoGrado;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

public class ProyectoApi extends ApiWebClient {

    public ProyectoApi() {
        super("http://localhost:8082"); // microservicio de proyectos
    }

    // =========================
    // CREAR PROYECTO
    // =========================
    public Mono<ProyectoGrado> crearProyecto(ProyectoRequest request) {
        WebClient.RequestHeadersSpec<?> req = webClient.post()
                .uri("/api/v1/proyectos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request);

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(ProyectoGrado.class);
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
    // OBTENER PROYECTO POR ID
    // =========================
    public Mono<ProyectoGrado> obtenerPorId(Long id) {
        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri("/api/v1/proyectos/{id}", id);

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(ProyectoGrado.class);
    }

    // =========================
    // OBTENER PROYECTOS POR ESTUDIANTE
    // =========================
    public Mono<List<ProyectoGrado>> obtenerPorEstudiante(String email) {
        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri("/api/v1/proyectos/estudiante/{email}", email);

        return addAuthHeader(req)
                .retrieve()
                .bodyToFlux(ProyectoGrado.class)
                .collectList();
    }

    // =========================
    // EVALUAR PROYECTO
    // =========================
    public Mono<Void> evaluarProyecto(Long id, boolean aprobado, String observaciones) {
        WebClient.RequestHeadersSpec<?> req = webClient.post()
                .uri(uri -> uri.path("/api/v1/proyectos/{id}/evaluar")
                        .queryParam("aprobado", aprobado)
                        .queryParam("observaciones", observaciones)
                        .build(id));

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // =========================
    // REINTENTAR PROYECTO
    // =========================
    public Mono<Void> reintentarProyecto(Long id) {
        WebClient.RequestHeadersSpec<?> req = webClient.post()
                .uri("/api/v1/proyectos/{id}/reintentar", id);

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // =========================
    // OBTENER ESTADO PROYECTO
    // =========================
    public Mono<String> obtenerEstado(Long id) {
        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri("/api/v1/proyectos/{id}/estado", id);

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(String.class);
    }

    // =========================
    // ASIGNAR EVALUADORES
    // =========================
    public Mono<Void> asignarEvaluadores(
            Long idProyecto,
            String jefeEmail,
            String evaluador1,
            String evaluador2
    ) {
        WebClient.RequestHeadersSpec<?> req = webClient.post()
                .uri(uri -> uri.path("/api/v1/proyectos/{id}/evaluadores")
                        .queryParam("jefeDepartamentoEmail", jefeEmail)
                        .queryParam("evaluador1Email", evaluador1)
                        .queryParam("evaluador2Email", evaluador2)
                        .build(idProyecto));

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
