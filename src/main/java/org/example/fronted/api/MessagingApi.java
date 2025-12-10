package org.example.fronted.api;

import org.example.fronted.config.ApiConfig;
import org.example.fronted.dto.MensajeDTO;
import org.example.fronted.dto.ConversacionDTO;
import org.example.fronted.models.MensajeInterno;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.util.List;

public class MessagingApi extends ApiWebClient.MessagingApiClient {

    /**
     * Obtener conversaciones del usuario
     */
    public Mono<List<ConversacionDTO>> obtenerConversaciones() {
        return addAuthHeader(webClient.get()
                .uri(ApiConfig.MessagingEndpoints.CONVERSATIONS))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ConversacionDTO>>() {})
                .onErrorResume(error -> {
                    System.err.println("Error obteniendo conversaciones: " + error.getMessage());
                    return Mono.just(List.of());
                });
    }

    /**
     * Obtener mensajes de una conversación
     */
    public Mono<List<MensajeDTO>> obtenerMensajes(Long conversacionId) {
        return addAuthHeader(webClient.get()
                .uri(ApiConfig.MessagingEndpoints.MESSAGES, conversacionId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MensajeDTO>>() {})
                .onErrorResume(error -> {
                    System.err.println("Error obteniendo mensajes: " + error.getMessage());
                    return Mono.just(List.of());
                });
    }

    /**
     * Enviar mensaje
     */
    public Mono<Boolean> enviarMensaje(MensajeDTO mensaje) {
        return addAuthHeader(webClient.post()
                .uri(ApiConfig.MessagingEndpoints.SEND)
                .bodyValue(mensaje))
                .retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .onErrorResume(error -> {
                    System.err.println("Error enviando mensaje: " + error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * Crear nueva conversación
     */
    public Mono<Long> crearConversacion(List<String> participantesEmails, String titulo) {
        return addAuthHeader(webClient.post()
                .uri(ApiConfig.MessagingEndpoints.START_CONVERSATION)
                .bodyValue(new CrearConversacionRequest(participantesEmails, titulo)))
                .retrieve()
                .bodyToMono(Long.class)
                .onErrorResume(error -> {
                    System.err.println("Error creando conversación: " + error.getMessage());
                    return Mono.just(-1L);
                });
    }

    // Clase interna para request
    private static class CrearConversacionRequest {
        private List<String> participantes;
        private String titulo;

        public CrearConversacionRequest(List<String> participantes, String titulo) {
            this.participantes = participantes;
            this.titulo = titulo;
        }

        public List<String> getParticipantes() { return participantes; }
        public String getTitulo() { return titulo; }
    }
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