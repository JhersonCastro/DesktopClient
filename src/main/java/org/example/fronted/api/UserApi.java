package org.example.fronted.api;

import org.example.fronted.dto.RegistroUsuarioDTO;
import org.example.fronted.models.Rol;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class UserApi extends ApiWebClient {

    /**
     * Registra un usuario en el user-microservice.
     * Según el rolPrincipal decide a qué endpoint llamar:
     *
     * ESTUDIANTE           -> /api/usuarios/estudiantes
     * COORDINADOR          -> /api/usuarios/coordinadores
     * JEFE_DEPARTAMENTO    -> /api/usuarios/jefes
     * DOCENTE (director/jurado) -> /api/usuarios/docentes
     */
    public Mono<Boolean> registrarUsuario(RegistroUsuarioDTO dto) {

        Rol rol = dto.getRolPrincipal();
        String path;

        if (rol == Rol.ESTUDIANTE) {
            path = "/api/usuarios/estudiantes";
        } else if (rol == Rol.COORDINADOR) {
            path = "/api/usuarios/coordinadores";
        } else if (rol == Rol.JEFE_DEPARTAMENTO) {
            path = "/api/usuarios/jefes";
        } else {
            // Director / Jurado / cualquier otro caso de docente
            path = "/api/usuarios/docentes";
        }

        // Cuerpo JSON que espera el backend (DocenteRequest, EstudianteRequest, etc.)
        Map<String, Object> body = new HashMap<>();
        body.put("email", dto.getEmail());
        body.put("password", dto.getPassword());
        body.put("nombres", dto.getNombres());
        body.put("apellidos", dto.getApellidos());
        body.put("celular", dto.getCelular());
        // De momento fijamos el programa, o luego lo lees de un ComboBox
        body.put("programa", "INGENIERIA_SISTEMAS");

        // Para docentes el backend exige tipoDocente, ponemos uno por defecto
        if (path.endsWith("/docentes")) {
            body.put("tipoDocente", "TIEMPO_COMPLETO"); // Ajusta si luego tienes un selector
        }

        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .onErrorResume(error -> {
                    System.err.println("Error en registro: " + error.getMessage());
                    return Mono.just(false);
                });
    }
}
