package org.example.fronted.dto;

public class EvaluarFormatoADTO {

    private Long idProyecto;
    private boolean aprobado;
    private String observaciones;

    public Long getIdProyecto() { return idProyecto; }

    public void setIdProyecto(Long idProyecto) { this.idProyecto = idProyecto; }

    public boolean isAprobado() { return aprobado; }

    public void setAprobado(boolean aprobado) { this.aprobado = aprobado; }

    public String getObservaciones() { return observaciones; }

    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
