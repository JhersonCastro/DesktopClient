package org.example.fronted.dto;

import java.io.Serializable;

public class ProjectCardDTO implements Serializable {
    public Long id;
    public String titulo;
    public String estudiante;
    public String modalidad;
    public String director;

    public ProjectCardDTO(Long id,String titulo, String estudiante, String modalidad, String director) {
        this.id = id;
        this.titulo = titulo;
        this.estudiante = estudiante;
        this.modalidad = modalidad;
        this.director = director;
    }
}
