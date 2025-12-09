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
        String path = "/api/usuarios/registro";


        // Cuerpo JSON que espera el backend (DocenteRequest, EstudianteRequest, etc.)
        Map<String, Object> body = new HashMap<>();
        body.put("email", dto.getEmail());
        body.put("password", dto.getPassword());
        body.put("nombres", dto.getNombres());
        body.put("apellidos", dto.getApellidos());
        body.put("celular", dto.getCelular());
        // De momento fijamos el programa, o luego lo lees de un ComboBox
        body.put("programa", "INGENIERIA_SISTEMAS");
        body.put("roles", dto.getRoles());
        return webClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorResume(error -> {
                    System.err.println("Error en registro: " + error.getMessage());
                    return Mono.just(false);
                });

    }
}
