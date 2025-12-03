package org.example.fronted.api;

import org.example.fronted.dto.SubirFormatoADTO;
import org.example.fronted.dto.SubirFormatoAResponseDTO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

public class ProjectApi extends ApiWebClient {

    public ProjectApi() {
        // baseUrl del project-microservice
        super("http://localhost:8082/api/v1/proyectos");
    }

    public Mono<SubirFormatoAResponseDTO> subirFormatoA(SubirFormatoADTO dto) {

        MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
        data.add("titulo", dto.getTitulo());
        data.add("modalidad", dto.getModalidad());
        data.add("directorEmail", dto.getDirectorEmail());

        if (dto.getCodirectorEmail() != null && !dto.getCodirectorEmail().isBlank()) {
            data.add("codirectorEmail", dto.getCodirectorEmail());
        }

        data.add("estudiante1Email", dto.getEstudiante1Email());

        if (dto.getPdfFormatoA() == null) {
            return Mono.error(new IllegalArgumentException("El PDF de Formato A es obligatorio"));
        }
        data.add("pdf", new FileSystemResource(dto.getPdfFormatoA()));

        if (dto.getCartaAceptacion() != null) {
            data.add("carta", new FileSystemResource(dto.getCartaAceptacion()));
        }

        var request = webClient.post()
                .uri("/formatoA")
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return addAuthHeader(request)   // devuelve RequestBodySpec
                .body(BodyInserters.fromMultipartData(data))
                .retrieve()
                .bodyToMono(SubirFormatoAResponseDTO.class);
    }
}
