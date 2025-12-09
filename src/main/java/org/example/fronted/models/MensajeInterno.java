package org.example.fronted.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MensajeInterno {
    private Long id;
    private String remitenteEmail;
    private String destinatariosEmail;
    private String asunto;
    private String cuerpo;
    private String fechaEnvio;
    private String rutaAdjunto; // o null si no hay adjunto
}
