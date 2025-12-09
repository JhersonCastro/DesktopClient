package org.example.fronted.api;

import org.example.fronted.util.SessionManager;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

public class DocumentoApi extends ApiWebClient {

    public DocumentoApi() {
        super("http://localhost:8083");
    }

    // -------------------------
    // SUBIR DOCUMENTO
    // -------------------------
    public Mono<String> subirDocumento(Long idProyecto, String tipoDocumento, byte[] archivoBytes, String nombreArchivo) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("idProyecto", idProyecto.toString());
        builder.part("tipoDocumento", tipoDocumento);

        builder.part("archivo", new ByteArrayResource(archivoBytes) {
            @Override
            public String getFilename() {
                return nombreArchivo;
            }
        }).contentType(MediaType.APPLICATION_PDF);

        WebClient.RequestHeadersSpec<?> request = webClient.post()
                .uri("/api/documentos/subir")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build());

        request = addAuthHeader(request);

        return request
                .retrieve()
                .bodyToMono(String.class);
    }

    // -------------------------
    // DESCARGAR DOCUMENTO POR ID
    // -------------------------
    public Mono<byte[]> descargarDocumento(Long id) {
        WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri("/api/documentos/descargar/{id}", id);

        request = addAuthHeader(request);

        return request
                .retrieve()
                .bodyToMono(byte[].class);
    }

    // -------------------------
    // OBTENER DOCUMENTOS POR PROYECTO
    // -------------------------
    public Mono<List<Object>> obtenerDocumentosPorProyecto(Long idProyecto) {
        WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri("/api/documentos/proyecto/{idProyecto}", idProyecto);

        request = addAuthHeader(request);

        return request.retrieve()
                .bodyToFlux(Object.class)
                .collectList();
    }

    // -------------------------
    // DESCARGAR PLANTILLA FORMATO A
    // -------------------------
    public Mono<byte[]> descargarPlantillaFormatoA() {
        WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri("/api/documentos/plantilla/formato-a");

        request = addAuthHeader(request);

        return request
                .retrieve()
                .bodyToMono(byte[].class);
    }
}
