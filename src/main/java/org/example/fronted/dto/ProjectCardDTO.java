package org.example.fronted.dto;

import java.io.Serializable;

public class ProjectCardDTO implements Serializable {
    private Long id;
    private String titulo;
    private String estudiante;
    private String modalidad;
    private String director;
    private String estado;
    private String tipo;

    // Constructor para compatibilidad
    public ProjectCardDTO(String titulo, String estudiante, String modalidad, String director) {
        this.titulo = titulo;
        this.estudiante = estudiante;
        this.modalidad = modalidad;
        this.director = director;
    }

    // Constructor vacío
    public ProjectCardDTO() {}

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getEstudiante() { return estudiante; }
    public void setEstudiante(String estudiante) { this.estudiante = estudiante; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}