package org.example.fronted.dto;

public class EvaluarFormatoADTO {

    private Long idProyecto;
    private boolean aprobado;
    private String observaciones;
    private String evaluadorEmail; // <-- AGREGAR ESTE CAMPO

    // Getters y setters existentes...
    public Long getIdProyecto() { return idProyecto; }
    public void setIdProyecto(Long idProyecto) { this.idProyecto = idProyecto; }

    public boolean isAprobado() { return aprobado; }
    public void setAprobado(boolean aprobado) { this.aprobado = aprobado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // Nuevo getter y setter
    public String getEvaluadorEmail() { return evaluadorEmail; }
    public void setEvaluadorEmail(String evaluadorEmail) { this.evaluadorEmail = evaluadorEmail; }
}