package org.example.fronted.api;

import org.example.fronted.dto.LoginRequestDTO;
import org.example.fronted.dto.UserResponseDTO;
import org.example.fronted.models.Rol;
import org.example.fronted.models.User;
import org.example.fronted.util.SessionManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthApi extends ApiWebClient {

    public Mono<Boolean> login(String email, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        return webClient.post()
                .uri("/api/auth/login")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status == HttpStatus.UNAUTHORIZED,
                        response -> Mono.error(new RuntimeException("Usuario o contraseña incorrectos")))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {

                    String token = (String) response.get("token");
                    String userEmail = (String) response.get("email");
                    String nombres = (String) response.get("nombres");
                    String apellidos = (String) response.get("apellidos");

                    List<String> rolesStr = (List<String>) response.get("roles");

                    List<Rol> roles = new ArrayList<>();
                    if (rolesStr != null) {
                        for (String r : rolesStr) {
                            roles.add(Rol.valueOf(r));
                        }
                    }

                    // Crear usuario completo
                    User user = new User();
                    user.setEmail(userEmail);
                    user.setNombres(nombres);
                    user.setApellidos(apellidos);
                    user.setRolesDisponibles(roles);
                    if (!roles.isEmpty()) {
                        user.setRolActual(roles.get(0));
                    }

                    // ✅ Guardar sesión correctamente
                    SessionManager.getInstance().login(user, token);

                    System.out.println("Login exitoso!");
                    System.out.println("Usuario: " + user.getNombreCompleto());
                    System.out.println("Roles: " + roles);

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