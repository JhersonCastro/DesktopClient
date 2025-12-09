package org.example.fronted.api;

import org.example.fronted.models.MensajeInterno;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

public class MessagingApi extends ApiWebClient {

    public MessagingApi() {
        super("http://localhost:8085"); // microservicio de mensajería
    }

    // Enviar mensaje con adjunto opcional
    public Mono<String> enviarMensaje(
            String remitenteEmail,
            String destinatariosEmail,
            String asunto,
            String cuerpo,
            byte[] archivoBytes,
            String nombreArchivo
    ) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("remitenteEmail", remitenteEmail);
        builder.part("destinatariosEmail", destinatariosEmail);
        builder.part("asunto", asunto);
        builder.part("cuerpo", cuerpo);

        if (archivoBytes != null) {
            builder.part("documentoAdjunto", new ByteArrayResource(archivoBytes) {
                @Override
                public String getFilename() {
                    return nombreArchivo;
                }
            }).contentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        return addAuthHeader(
                webClient.post()
                        .uri("/api/mensajes/enviar")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .bodyValue(builder.build())
        )
                .retrieve()
                .bodyToMono(String.class);
    }

    // Obtener mensajes enviados
    public Mono<List<MensajeInterno>> getMensajesEnviados(String email) {
        return addAuthHeader(
                webClient.get()
                        .uri("/api/mensajes/enviados/{email}", email)
        )
                .retrieve()
                .bodyToFlux(MensajeInterno.class)
                .collectList();
    }

    // Obtener mensajes recibidos
    public Mono<List<MensajeInterno>> getMensajesRecibidos(String email) {
        return addAuthHeader(
                webClient.get()
                        .uri("/api/mensajes/recibidos/{email}", email)
        )
                .retrieve()
                .bodyToFlux(MensajeInterno.class)
                .collectList();
    }
}
