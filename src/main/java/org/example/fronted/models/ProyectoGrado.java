package org.example.fronted.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProyectoGrado {
    private Long id;
    private String titulo;
    private String modalidad;
    private String directorEmail;
    private String codirectorEmail;
    private String estudiante1Email;
    private String estudiante2Email;
    private String estadoActual;
    private int intentos;
    private String observacionesEvaluacion;
}
