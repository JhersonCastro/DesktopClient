package org.example.fronted.api;

import org.example.fronted.dto.ProjectCardDTO;
import org.example.fronted.dto.ProyectoRequest;
import org.example.fronted.dto.StatsDocenteDTO;
import org.example.fronted.models.Estado;
import org.example.fronted.models.ProyectoGrado;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    // OBTENER PROYECTO POR ID
    // =========================
    public Mono<ProyectoGrado> obtenerPorId(Long id) {
        Map<String, Object> empty = new HashMap<>();

        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri("/api/v1/proyectos/{id}", id);

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {

                    ProyectoGrado p = new ProyectoGrado();

                    // básicos
                    p.setId(((Number) response.get("id")).longValue());
                    p.setTitulo((String) response.get("titulo"));
                    p.setModalidad((String) response.get("modalidad"));

                    // correos
                    p.setDirectorEmail((String) response.get("directorEmail"));
                    p.setCodirectorEmail((String) response.get("codirectorEmail"));
                    p.setEstudiante1Email((String) response.get("estudiante1Email"));
                    p.setEstudiante2Email((String) response.get("estudiante2Email"));
                    p.setEvaluador1Email((String) response.get("evaluador1Email"));
                    p.setEvaluador2Email((String) response.get("evaluador2Email"));

                    // objetivos
                    p.setObjetivoGeneral((String) response.get("objetivoGeneral"));
                    p.setObjetivosEspecificos((String) response.get("objetivosEspecificos"));

                    // estados
                    p.setNumeroIntento(
                            response.get("numeroIntento") != null
                                    ? ((Number) response.get("numeroIntento")).intValue()
                                    : null
                    );
                    p.setEstadoActual((String) response.get("estadoActual"));

                    // estado anidado
                    if (response.get("estado") != null) {
                        Map<String, Object> estadoMap =
                                (Map<String, Object>) response.get("estado");

                        Estado estado = new Estado();
                        estado.setNombreEstado((String) estadoMap.get("nombreEstado"));
                        p.setEstado(estado);
                    }

                    // observaciones
                    p.setObservacionesEvaluacion((String) response.get("observacionesEvaluacion"));

                    // fechas
                    p.setFechaFormatoA((String) response.get("fechaFormatoA"));
                    p.setFechaAnteproyecto((String) response.get("fechaAnteproyecto"));

                    // tokens
                    p.setFormatoAToken((String) response.get("formatoAToken"));
                    p.setCartaToken((String) response.get("cartaToken"));
                    p.setAnteproyectoToken((String) response.get("anteproyectoToken"));

                    // intentos
                    p.setIntentos(
                            response.get("intentos") != null
                                    ? ((Number) response.get("intentos")).intValue()
                                    : null
                    );

                    return p;
                });
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
    // OBTENER ANTEPROYECTOS POR JEFE
    // =========================
    public Mono<List<ProyectoGrado>> obtenerAnteproyectosPorJefe(String emailJefe) {
        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri("/api/v1/proyectos/anteproyectos/jefe/{emailJefe}", emailJefe);

        return addAuthHeader(req)
                .retrieve()
                .bodyToFlux(ProyectoGrado.class)
                .collectList();
    }
    // ===========================
    // OBTENER PROYECTOS DOCENTE
    // ===========================
    public Mono<List<ProjectCardDTO>> obtenerProyectosPorDocente(
            String emailDocente,
            String estado
    ) {
        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri("/api/v1/proyectos/docente/{emailDocente}", emailDocente);

        return addAuthHeader(req)
                .retrieve()
                .bodyToFlux(ProyectoGrado.class)

                // FILTRO POR ESTADO
                .filter(p -> estado == null || estado.equals(p.getEstadoActual()))

                // MAPEO A TU DTO
                .map(p -> new ProjectCardDTO(
                        p.getTitulo(),
                        unirEstudiantes(p),
                        p.getModalidad(),
                        p.getDirectorEmail()
                ))

                .collectList();
    }

    // =================================
    // OBTENER EVALUACIONES POR DOCENTE
    // =================================
    public Mono<List<ProjectCardDTO>> obtenerProyectosParaEvaluar(String emailDocente) {

        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri("/api/v1/proyectos/evaluador/{emailDocente}", emailDocente);

        return addAuthHeader(req)
                .retrieve()
                .bodyToFlux(ProyectoGrado.class)

                // FILTRAR SOLO LOS PROYECTOS DONDE EL DOCENTE ES EVALUADOR
                .filter(p -> emailDocente.equals(p.getEvaluador1Email())
                        || emailDocente.equals(p.getEvaluador2Email()))

                // MAPEAR AL DTO QUE VAS A MOSTRAR EN LA VISTA
                .map(p -> new ProjectCardDTO(
                        p.getTitulo(),
                        unirEstudiantes(p),
                        p.getModalidad(),
                        p.getDirectorEmail()
                ))

                .collectList();
    }

    // =============================
    // UNIR ESTUDIANTES PARA TARJETA
    // =============================

    private String unirEstudiantes(ProyectoGrado p) {
        return Stream.of(p.getEstudiante1Email(), p.getEstudiante2Email())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" / "));
    }

    // =============================
    // ESTADISTICAS DOCENTE
    // =============================

    public Mono<StatsDocenteDTO> obtenerEstadisticasDocente(String emailDocente) {

        Mono<List<ProyectoGrado>> proyectosDocente = addAuthHeader(
                webClient.get().uri("/api/v1/proyectos/docente/{email}", emailDocente)
        )
                .retrieve()
                .bodyToFlux(ProyectoGrado.class)
                .collectList();

        Mono<List<ProyectoGrado>> evaluacionesPendientes = addAuthHeader(
                webClient.get().uri("/api/v1/proyectos/evaluador/{email}", emailDocente)
        )
                .retrieve()
                .bodyToFlux(ProyectoGrado.class)
                .collectList();

        return Mono.zip(proyectosDocente, evaluacionesPendientes)
                .map(tuple -> {
                    List<ProyectoGrado> listaProyectos = tuple.getT1();
                    List<ProyectoGrado> listaPendientesEval = tuple.getT2();

                    long formatoAPendiente = listaProyectos.stream()
                            .filter(p -> emailDocente.equals(p.getDirectorEmail()) ||
                                    emailDocente.equals(p.getCodirectorEmail()))
                            .filter(p -> "FORMATO_A_PENDIENTE".equals(p.getEstadoActual()))
                            .count();

                    long formatoAAprobado = listaProyectos.stream()
                            .filter(p -> emailDocente.equals(p.getDirectorEmail()) ||
                                    emailDocente.equals(p.getCodirectorEmail()))
                            .filter(p -> "FORMATO_A_APROBADO".equals(p.getEstadoActual()))
                            .count();

                    long pendientesEvaluar = listaPendientesEval.size();

                    return new StatsDocenteDTO(
                            formatoAPendiente,
                            formatoAAprobado,
                            pendientesEvaluar
                    );
                });
    }




    // =========================
    // SUBIR ANTEPROYECTO
    // =========================
    public Mono<Void> subirAnteproyecto(Long idProyecto, String jefeDepartamentoEmail, ByteArrayResource anteproyectoPdf) {
        WebClient.RequestHeadersSpec<?> req = webClient.post()
                .uri("/api/v1/proyectos/{idProyecto}/anteproyecto", idProyecto)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("jefeDepartamentoEmail", jefeDepartamentoEmail)
                        .with("pdf", anteproyectoPdf)
                );

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // =========================
    // EVALUAR PROYECTO (evaluación general, no solo Formato A)
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