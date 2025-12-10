package org.example.fronted.dto;

import java.io.File;

public class SubirAnteproyectoDTO {

    private Long idProyecto;
    private String jefeDepartamentoEmail;
    private File anteproyectoPdf;

    public Long getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(Long idProyecto) {
        this.idProyecto = idProyecto;
    }

    public String getJefeDepartamentoEmail() {
        return jefeDepartamentoEmail;
    }

    public void setJefeDepartamentoEmail(String jefeDepartamentoEmail) {
        this.jefeDepartamentoEmail = jefeDepartamentoEmail;
    }

    public File getAnteproyectoPdf() {
        return anteproyectoPdf;
    }

    public void setAnteproyectoPdf(File anteproyectoPdf) {
        this.anteproyectoPdf = anteproyectoPdf;
    }
}
