package org.example.fronted.dto;

import java.io.Serializable;

public class ProjectCardDTO implements Serializable {
    public String titulo;
    public String estudiante;
    public String modalidad;
    public String director;

    public ProjectCardDTO(String titulo, String estudiante, String modalidad, String director) {
        this.titulo = titulo;
        this.estudiante = estudiante;
        this.modalidad = modalidad;
        this.director = director;
    }
}
