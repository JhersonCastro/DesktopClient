package org.example.fronted.api;

import org.example.fronted.dto.LoginRequestDTO;
import org.example.fronted.dto.UserResponseDTO;
import org.example.fronted.models.Rol;
import org.example.fronted.util.SessionManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import java.util.Map;

public class AuthApi extends ApiWebClient {

    public Mono<Boolean> login(String email, String password) {
        LoginRequestDTO request = new LoginRequestDTO(email, password);

        System.out.println("Login: " + email);

        return webClient.post()
                .uri("/api/auth/login")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED,
                        response -> {
                            System.out.println("Credenciales incorrectas");
                            return Mono.error(new RuntimeException("Usuario o contraseña incorrectos"));
                        })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    System.out.println("Respuesta: " + response);

                    String token = (String) response.get("token");
                    Rol rol = (Rol) response.get("rol");
                    String userEmail = (String) response.get("email");

                    SessionManager.getInstance().setToken(token);
                    SessionManager.getInstance().setEmail(userEmail);
                    SessionManager.getInstance().setRol(rol);

                    System.out.println("Login exitoso!");
                    System.out.println("Email: " + userEmail);
                    System.out.println("Rol: " + rol);
                    System.out.println("Token: " + token.substring(0, Math.min(20, token.length())) + "...");

                    return true;
                })
                .onErrorResume(error -> {
                    System.err.println("Error: " + error.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<UserResponseDTO> getCurrentUser() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            return Mono.error(new RuntimeException("No hay usuario logueado"));
        }

        return addAuthHeader(webClient.get()
                .uri("/api/auth/me"))
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED,
                        response -> {
                            SessionManager.getInstance().clearSession();
                            return Mono.error(new RuntimeException("Sesion expirada"));
                        })
                .bodyToMono(UserResponseDTO.class);
    }

    public Mono<Boolean> logout() {
        return addAuthHeader(webClient.post()
                .uri("/api/auth/logout"))
                .retrieve()
                .bodyToMono(Void.class)
                .map(response -> {
                    SessionManager.getInstance().clearSession();
                    System.out.println("Logout exitoso");
                    return true;
                })
                .onErrorResume(error -> {
                    SessionManager.getInstance().clearSession();
                    System.err.println("Error en logout: " + error.getMessage());
                    return Mono.just(true);
                });
    }
}