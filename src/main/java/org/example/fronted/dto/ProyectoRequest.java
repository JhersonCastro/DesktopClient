package org.example.fronted.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProyectoRequest {
    private String titulo;
    private String modalidad;
    private String directorEmail;
    private String codirectorEmail;
    private String estudiante1Email;
    private String estudiante2Email;
    private String objetivoGeneral;
    private String objetivosEspecificos;
}
