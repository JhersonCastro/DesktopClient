package org.example.fronted.api;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import reactor.core.publisher.Mono;

public class DocumentoApi extends ApiWebClient {

    public DocumentoApi() {
        super("http://localhost:8083");
    }

    public Mono<String> subirDocumento(Long idProyecto, String tipoDocumento, byte[] archivoBytes, String nombreArchivo) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("idProyecto", idProyecto.toString());
        builder.part("tipoDocumento", tipoDocumento);

        builder.part("archivo", new ByteArrayResource(archivoBytes) {
            @Override
            public String getFilename() {
                return nombreArchivo;
            }
        }).contentType(MediaType.APPLICATION_OCTET_STREAM);

        return webClient.post()
                .uri("/api/documentos/subir")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<byte[]> descargarDocumento(Long id) {
        return webClient.get()
                .uri("/api/documentos/descargar/{id}", id)
                .retrieve()
                .bodyToMono(byte[].class);
    }
}
