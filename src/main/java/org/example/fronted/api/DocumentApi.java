package org.example.fronted.api;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.io.File;

public class DocumentApi extends ApiWebClient.DocumentApiClient {

    /**
     * Subir un documento a un proyecto
     * @param proyectoId ID del proyecto
     * @param tipoDocumento Tipo de documento (ANTEPROYECTO, FORMATO_A, INFORME, etc.)
     * @param archivo Archivo a subir
     * @param metadata Metadatos adicionales (opcional)
     */
    public Mono<Boolean> subirDocumento(Long proyectoId, String tipoDocumento,
                                        File archivo, String metadata) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("archivo", new FileSystemResource(archivo));
        body.add("tipoDocumento", tipoDocumento);
        body.add("proyectoId", proyectoId.toString());

        if (metadata != null && !metadata.isEmpty()) {
            body.add("metadata", metadata);
        }

        return addAuthHeader(webClient.post()
                .uri("/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body)))
                .retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .onErrorResume(error -> {
                    System.err.println("Error subiendo documento: " + error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * Descargar un documento por ID
     */
    public Mono<File> descargarDocumento(Long documentoId) {
        return addAuthHeader(webClient.get()
                .uri("/download/{id}", documentoId))
                .retrieve()
                .bodyToMono(File.class)
                .onErrorResume(error -> {
                    System.err.println("Error descargando documento: " + error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Obtener documentos de un proyecto
     */
    public Mono<String> obtenerDocumentosPorProyecto(Long proyectoId) {
        return addAuthHeader(webClient.get()
                .uri("/proyecto/{id}", proyectoId))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(error -> {
                    System.err.println("Error obteniendo documentos: " + error.getMessage());
                    return Mono.just("[]");
                });
    }
}