package org.example.fronted.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ProyectoGrado {
    public ProyectoGrado() {}
    private Long id;
    private String titulo;
    private String modalidad;

    private String directorEmail;
    private String codirectorEmail;
    private String estudiante1Email;
    private String estudiante2Email;

    private String evaluador1Email;
    private String evaluador2Email;

    private String objetivoGeneral;
    private String objetivosEspecificos;

    private Integer numeroIntento;
    private String estadoActual;

    private Estado estado; // objeto anidado

    private String observacionesEvaluacion;

    private String fechaFormatoA;
    private String fechaAnteproyecto;

    private String formatoAToken;
    private String cartaToken;
    private String anteproyectoToken;

    private Integer intentos;
}
