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

import java.util.ArrayList;
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
        // Convertimos el DTO a Map directamente
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("titulo", request.getTitulo());
        bodyMap.put("modalidad", request.getModalidad());
        bodyMap.put("directorEmail", request.getDirectorEmail());
        bodyMap.put("codirectorEmail", request.getCodirectorEmail());
        bodyMap.put("estudiante1Email", request.getEstudiante1Email());
        bodyMap.put("estudiante2Email", request.getEstudiante2Email());
        bodyMap.put("objetivoGeneral", request.getObjetivoGeneral());
        bodyMap.put("objetivosEspecificos", request.getObjetivosEspecificos());

        return addAuthHeader(
                webClient.post()
                        .uri("/api/v1/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(bodyMap)
        )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {}) // recibimos como Map
                .map(json -> {
                    ProyectoGrado proyecto = new ProyectoGrado();

                    proyecto.setId(json.get("id") != null ? ((Number) json.get("id")).longValue() : null);
                    proyecto.setTitulo((String) json.get("titulo"));
                    proyecto.setModalidad((String) json.get("modalidad"));
                    proyecto.setDirectorEmail((String) json.get("directorEmail"));
                    proyecto.setCodirectorEmail((String) json.get("codirectorEmail"));
                    proyecto.setEstudiante1Email((String) json.get("estudiante1Email"));
                    proyecto.setEstudiante2Email((String) json.get("estudiante2Email"));
                    proyecto.setEvaluador1Email((String) json.get("evaluador1Email"));
                    proyecto.setEvaluador2Email((String) json.get("evaluador2Email"));
                    proyecto.setObjetivoGeneral((String) json.get("objetivoGeneral"));
                    proyecto.setObjetivosEspecificos((String) json.get("objetivosEspecificos"));
                    proyecto.setNumeroIntento(json.get("numeroIntento") != null ? ((Number) json.get("numeroIntento")).intValue() : null);
                    proyecto.setEstadoActual((String) json.get("estadoActual"));

                    // Estado anidado
                    if (json.get("estado") instanceof Map<?, ?> estadoMap) {
                        Estado estado = new Estado();
                        Map<String, Object> map = (Map<String, Object>) estadoMap;
                        estado.setNombreEstado((String) map.get("nombreEstado"));
                        proyecto.setEstado(estado);
                    }

                    proyecto.setObservacionesEvaluacion((String) json.get("observacionesEvaluacion"));
                    proyecto.setFechaFormatoA((String) json.get("fechaFormatoA"));
                    proyecto.setFechaAnteproyecto((String) json.get("fechaAnteproyecto"));
                    proyecto.setFormatoAToken((String) json.get("formatoAToken"));
                    proyecto.setCartaToken((String) json.get("cartaToken"));
                    proyecto.setAnteproyectoToken((String) json.get("anteproyectoToken"));
                    proyecto.setIntentos(json.get("intentos") != null ? ((Number) json.get("intentos")).intValue() : null);

                    return proyecto;
                });
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
    public Mono<List<ProyectoGrado>> obtenerProyectosPendientes() {
        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri(uri -> uri.path("/api/v1/proyectos/formatoA/pendientes").build());

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(lista -> {
                    List<ProyectoGrado> proyectos = new ArrayList<>();

                    for (Map<String, Object> data : lista) {
                        ProyectoGrado p = new ProyectoGrado();

                        p.setId(Long.parseLong(data.get("id").toString()));
                        p.setTitulo((String) data.get("titulo"));
                        p.setModalidad((String) data.get("modalidad"));
                        p.setDirectorEmail((String) data.get("directorEmail"));
                        p.setCodirectorEmail((String) data.get("codirectorEmail"));
                        p.setEstudiante1Email((String) data.get("estudiante1Email"));
                        p.setEstudiante2Email((String) data.get("estudiante2Email"));

                        p.setEstadoActual((String) data.get("estadoActual"));
                        p.setObservacionesEvaluacion((String) data.get("observacionesEvaluacion"));

                        // Estado anidado
                        if (data.get("estado") instanceof Map) {
                            Map<String, Object> estadoMap = (Map<String, Object>) data.get("estado");
                            if (estadoMap.get("nombreEstado") != null) {
                                Estado estado = new Estado();
                                estado.setNombreEstado((String) estadoMap.get("nombreEstado"));
                                p.setEstado(estado);
                            }
                        }

                        proyectos.add(p);
                    }

                    return proyectos;
                });
    }
    public Mono<List<ProyectoGrado>> obtenerProyectosRechazados() {
        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri(uri -> uri.path("/api/v1/proyectos/formatoA/rechazados").build());

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(lista -> {
                    List<ProyectoGrado> proyectos = new ArrayList<>();

                    for (Map<String, Object> data : lista) {
                        ProyectoGrado p = new ProyectoGrado();

                        p.setId(Long.parseLong(data.get("id").toString()));
                        p.setTitulo((String) data.get("titulo"));
                        p.setModalidad((String) data.get("modalidad"));
                        p.setDirectorEmail((String) data.get("directorEmail"));
                        p.setCodirectorEmail((String) data.get("codirectorEmail"));
                        p.setEstudiante1Email((String) data.get("estudiante1Email"));
                        p.setEstudiante2Email((String) data.get("estudiante2Email"));

                        p.setEstadoActual((String) data.get("estadoActual"));
                        p.setObservacionesEvaluacion((String) data.get("observacionesEvaluacion"));

                        // Estado anidado
                        if (data.get("estado") instanceof Map) {
                            Map<String, Object> estadoMap = (Map<String, Object>) data.get("estado");
                            if (estadoMap.get("nombreEstado") != null) {
                                Estado estado = new Estado();
                                estado.setNombreEstado((String) estadoMap.get("nombreEstado"));
                                p.setEstado(estado);
                            }
                        }

                        proyectos.add(p);
                    }

                    return proyectos;
                });
    }
    public Mono<List<ProyectoGrado>> obtenerProyectosAprobados() {
        WebClient.RequestHeadersSpec<?> req = webClient.get()
                .uri(uri -> uri.path("/api/v1/proyectos/formatoA/aprobados").build());

        return addAuthHeader(req)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(lista -> {
                    List<ProyectoGrado> proyectos = new ArrayList<>();

                    for (Map<String, Object> data : lista) {
                        ProyectoGrado p = new ProyectoGrado();

                        p.setId(Long.parseLong(data.get("id").toString()));
                        p.setTitulo((String) data.get("titulo"));
                        p.setModalidad((String) data.get("modalidad"));
                        p.setDirectorEmail((String) data.get("directorEmail"));
                        p.setCodirectorEmail((String) data.get("codirectorEmail"));
                        p.setEstudiante1Email((String) data.get("estudiante1Email"));
                        p.setEstudiante2Email((String) data.get("estudiante2Email"));

                        p.setEstadoActual((String) data.get("estadoActual"));
                        p.setObservacionesEvaluacion((String) data.get("observacionesEvaluacion"));

                        // Estado anidado
                        if (data.get("estado") instanceof Map) {
                            Map<String, Object> estadoMap = (Map<String, Object>) data.get("estado");
                            if (estadoMap.get("nombreEstado") != null) {
                                Estado estado = new Estado();
                                estado.setNombreEstado((String) estadoMap.get("nombreEstado"));
                                p.setEstado(estado);
                            }
                        }

                        proyectos.add(p);
                    }

                    return proyectos;
                });
    }


}