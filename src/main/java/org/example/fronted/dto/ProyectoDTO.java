package org.example.fronted.dto;

public class ProyectoDTO {

    private Long id;
    private String titulo;
    private String modalidad;
    private String estado;              // nombre del estado (EnPrimeraEvaluacion, Aprobado, etc.)
    private String estudiante1Email;
    private String estudiante2Email;
    private String directorEmail;
    private String codirectorEmail;

    // Getters y setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }

    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getModalidad() { return modalidad; }

    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public String getEstado() { return estado; }

    public void setEstado(String estado) { this.estado = estado; }

    public String getEstudiante1Email() { return estudiante1Email; }

    public void setEstudiante1Email(String estudiante1Email) { this.estudiante1Email = estudiante1Email; }

    public String getEstudiante2Email() { return estudiante2Email; }

    public void setEstudiante2Email(String estudiante2Email) { this.estudiante2Email = estudiante2Email; }

    public String getDirectorEmail() { return directorEmail; }

    public void setDirectorEmail(String directorEmail) { this.directorEmail = directorEmail; }

    public String getCodirectorEmail() { return codirectorEmail; }

    public void setCodirectorEmail(String codirectorEmail) { this.codirectorEmail = codirectorEmail; }
}
