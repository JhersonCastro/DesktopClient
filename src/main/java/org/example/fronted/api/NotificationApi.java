package org.example.fronted.api;

import org.example.fronted.config.ApiConfig;
import org.example.fronted.dto.NotificacionDTO;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.util.List;

public class NotificationApi extends ApiWebClient.NotificationApiClient {

    /**
     * Obtener notificaciones del usuario actual
     */
    public Mono<List<NotificacionDTO>> obtenerNotificaciones() {
        return addAuthHeader(webClient.get()
                .uri(ApiConfig.NotificationEndpoints.BY_USER))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<NotificacionDTO>>() {})
                .onErrorResume(error -> {
                    System.err.println("Error obteniendo notificaciones: " + error.getMessage());
                    return Mono.just(List.of());
                });
    }

    /**
     * Marcar notificación como leída
     */
    public Mono<Boolean> marcarComoLeida(Long notificacionId) {
        return addAuthHeader(webClient.put()
                .uri(ApiConfig.NotificationEndpoints.MARK_READ, notificacionId))
                .retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .onErrorResume(error -> {
                    System.err.println("Error marcando notificación: " + error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * Obtener conteo de notificaciones no leídas
     */
    public Mono<Integer> obtenerConteoNoLeidas() {
        return addAuthHeader(webClient.get()
                .uri(ApiConfig.NotificationEndpoints.UNREAD_COUNT))
                .retrieve()
                .bodyToMono(Integer.class)
                .onErrorResume(error -> {
                    System.err.println("Error obteniendo conteo: " + error.getMessage());
                    return Mono.just(0);
                });
    }
}