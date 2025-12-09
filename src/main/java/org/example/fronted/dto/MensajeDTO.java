package org.example.fronted.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

public class MensajeDTO {
    private Long id;
    private String contenido;
    private String remitenteEmail;
    private String remitenteNombre;
    private Long conversacionId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaEnvio;

    private boolean leido;

    // Campos adicionales para UI
    @JsonIgnore
    private boolean esPropio; // Si el mensaje es del usuario actual

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getRemitenteEmail() { return remitenteEmail; }
    public void setRemitenteEmail(String remitenteEmail) { this.remitenteEmail = remitenteEmail; }

    public String getRemitenteNombre() { return remitenteNombre; }
    public void setRemitenteNombre(String remitenteNombre) { this.remitenteNombre = remitenteNombre; }

    public Long getConversacionId() { return conversacionId; }
    public void setConversacionId(Long conversacionId) { this.conversacionId = conversacionId; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }

    public boolean isEsPropio() { return esPropio; }
    public void setEsPropio(boolean esPropio) { this.esPropio = esPropio; }

    // Métodos helper
    public String getHoraFormateada() {
        if (fechaEnvio == null) return "";
        return fechaEnvio.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getFechaFormateada() {
        if (fechaEnvio == null) return "";
        return fechaEnvio.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}