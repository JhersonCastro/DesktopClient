package org.example.fronted.dto;

import java.io.File;

public class SubirFormatoADTO {

    private String titulo;
    private String modalidad;           // INVESTIGACION o PRACTICA_PROFESIONAL
    private String directorEmail;
    private String codirectorEmail;     // opcional
    private String estudiante1Email;

    private File pdfFormatoA;           // archivo Formato A
    private File cartaAceptacion;       // opcional (para práctica)

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public String getDirectorEmail() { return directorEmail; }
    public void setDirectorEmail(String directorEmail) { this.directorEmail = directorEmail; }

    public String getCodirectorEmail() { return codirectorEmail; }
    public void setCodirectorEmail(String codirectorEmail) { this.codirectorEmail = codirectorEmail; }

    public String getEstudiante1Email() { return estudiante1Email; }
    public void setEstudiante1Email(String estudiante1Email) { this.estudiante1Email = estudiante1Email; }

    public File getPdfFormatoA() { return pdfFormatoA; }
    public void setPdfFormatoA(File pdfFormatoA) { this.pdfFormatoA = pdfFormatoA; }

    public File getCartaAceptacion() { return cartaAceptacion; }
    public void setCartaAceptacion(File cartaAceptacion) { this.cartaAceptacion = cartaAceptacion; }
}
