package org.example.fronted.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class NotificacionDTO {
    private Long id;
    private String titulo;
    private String mensaje;
    private String tipo; // "INFO", "ALERTA", "EXITO", "ERROR", "PROYECTO", "DOCUMENTO"
    private boolean leida;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    private Long proyectoId; // Opcional: relacionado con un proyecto
    private Long documentoId; // Opcional: relacionado con un documento
    private String accionUrl; // Opcional: URL para acción
    private String remitenteEmail; // Quien envió la notificación

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Long getProyectoId() { return proyectoId; }
    public void setProyectoId(Long proyectoId) { this.proyectoId = proyectoId; }

    public Long getDocumentoId() { return documentoId; }
    public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

    public String getAccionUrl() { return accionUrl; }
    public void setAccionUrl(String accionUrl) { this.accionUrl = accionUrl; }

    public String getRemitenteEmail() { return remitenteEmail; }
    public void setRemitenteEmail(String remitenteEmail) { this.remitenteEmail = remitenteEmail; }

    // Método helper para mostrar tiempo relativo
    public String getTiempoRelativo() {
        if (fechaCreacion == null) return "";

        LocalDateTime ahora = LocalDateTime.now();
        long minutos = java.time.Duration.between(fechaCreacion, ahora).toMinutes();

        if (minutos < 1) return "Ahora mismo";
        if (minutos < 60) return minutos + " min ago";
        if (minutos < 1440) return (minutos / 60) + " horas ago";
        return (minutos / 1440) + " días ago";
    }
}