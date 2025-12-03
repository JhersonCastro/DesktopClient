package org.example.fronted.api;

import org.example.fronted.dto.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ProjectApi extends ApiWebClient {

    public ProjectApi() {
        // Ajusta si tu project-microservice expone otro path base
        super("http://localhost:8082/api/v1/proyectos");
    }

    // ================= 1. SUBIR FORMATO A (DOCENTE) =================

    public Mono<SubirFormatoAResponseDTO> subirFormatoA(SubirFormatoADTO dto) {

        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        data.add("titulo", dto.getTitulo());
        data.add("modalidad", dto.getModalidad());
        data.add("directorEmail", dto.getDirectorEmail());

        if (dto.getCodirectorEmail() != null && !dto.getCodirectorEmail().isBlank()) {
            data.add("codirectorEmail", dto.getCodirectorEmail());
        }

        data.add("estudiante1Email", dto.getEstudiante1Email());

        if (dto.getPdfFormatoA() == null) {
            return Mono.error(new IllegalArgumentException("El PDF de Formato A es obligatorio"));
        }
        data.add("pdf", new FileSystemResource(dto.getPdfFormatoA()));

        if (dto.getCartaAceptacion() != null) {
            data.add("carta", new FileSystemResource(dto.getCartaAceptacion()));
        }

        WebClient.RequestHeadersSpec<?> spec = addAuthHeader(
                webClient.post()
                        .uri("/formatoA")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(data))
        );

        return spec.retrieve()
                .bodyToMono(SubirFormatoAResponseDTO.class);
    }

    // ================= 2. PROYECTOS POR ESTUDIANTE =================

    /**
     * GET /estudiante/{email}
     * Lista los proyectos de un estudiante (pantalla "Mis Proyectos").
     */
    public Mono<List<ProyectoDTO>> obtenerProyectosDeEstudiante(String emailEstudiante) {
        WebClient.RequestHeadersSpec<?> spec = addAuthHeader(
                webClient.get()
                        .uri("/estudiante/{email}", emailEstudiante)
        );

        return spec.retrieve()
                .bodyToFlux(ProyectoDTO.class)
                .collectList();
    }

    // ================= 3. TODOS LOS PROYECTOS (COORDINADOR) =================

    /**
     * GET /
     * Lista todos los proyectos (para coordinador / admin).
     */
    public Mono<List<ProyectoDTO>> obtenerTodosProyectos() {
        WebClient.RequestHeadersSpec<?> spec = addAuthHeader(
                webClient.get()
                        .uri("")
        );

        return spec.retrieve()
                .bodyToFlux(ProyectoDTO.class)
                .collectList();
    }

    // ================= 4. ANTEPROYECTOS PARA JEFE DEPARTAMENTO =================

    /**
     * GET /anteproyectos/jefe/{emailJefe}
     * Devuelve anteproyectos para el Jefe de Departamento.
     */
    public Mono<List<ProyectoDTO>> obtenerAnteproyectosPorJefe(String emailJefe) {
        WebClient.RequestHeadersSpec<?> spec = addAuthHeader(
                webClient.get()
                        .uri("/anteproyectos/jefe/{email}", emailJefe)
        );

        return spec.retrieve()
                .bodyToFlux(ProyectoDTO.class)
                .collectList();
    }

    // ================= 5. ASIGNAR EVALUADORES (JEFE) =================

    /**
     * POST /{idProyecto}/evaluadores
     * El backend espera parámetros: jefeDepartamentoEmail, evaluador1Email, evaluador2Email
     */
    public Mono<Boolean> asignarEvaluadores(AsignarEvaluadoresDTO dto) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("jefeDepartamentoEmail", dto.getJefeDepartamentoEmail());
        form.add("evaluador1Email", dto.getEvaluador1Email());
        form.add("evaluador2Email", dto.getEvaluador2Email());

        WebClient.RequestHeadersSpec<?> spec = addAuthHeader(
                webClient.post()
                        .uri("/{idProyecto}/evaluadores", dto.getIdProyecto())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData(form))
        );

        return spec.retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .onErrorResume(err -> Mono.just(false));
    }

    // ================= 6. EVALUAR FORMATO A (COORDINADOR) =================

    /**
     * POST /{idProyecto}/evaluar?aprobado=true/false&observaciones=...
     */
    public Mono<Boolean> evaluarFormatoA(EvaluarFormatoADTO dto) {
        WebClient.RequestHeadersSpec<?> spec = addAuthHeader(
                webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/{idProyecto}/evaluar")
                                .queryParam("aprobado", dto.isAprobado())
                                .queryParam("observaciones", dto.getObservaciones())
                                .build(dto.getIdProyecto()))
        );

        return spec.retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .onErrorResume(err -> Mono.just(false));
    }

    // ================= 7. REINTENTAR FORMATO A (DOCENTE) =================

    /**
     * POST /{idProyecto}/reintentar
     */
    public Mono<Boolean> reintentarFormatoA(Long idProyecto) {
        WebClient.RequestHeadersSpec<?> spec = addAuthHeader(
                webClient.post()
                        .uri("/{idProyecto}/reintentar", idProyecto)
        );

        return spec.retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .onErrorResume(err -> Mono.just(false));
    }

    // ================= 8. OBTENER PROYECTO POR ID =================

    /**
     * GET /{idProyecto}
     */
    public Mono<ProyectoDTO> obtenerProyectoPorId(Long idProyecto) {
        WebClient.RequestHeadersSpec<?> spec = addAuthHeader(
                webClient.get()
                        .uri("/{idProyecto}", idProyecto)
        );

        return spec.retrieve()
                .bodyToMono(ProyectoDTO.class);
    }
}
