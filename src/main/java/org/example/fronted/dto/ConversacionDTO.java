package org.example.fronted.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConversacionDTO {
    private Long id;
    private String titulo;
    private List<ParticipanteDTO> participantes = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ultimoMensajeFecha;

    private String ultimoMensajePreview;
    private int mensajesNoLeidos;

    // Campos para UI
    @JsonIgnore
    private boolean activa; // Si es la conversación activa en la UI

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public List<ParticipanteDTO> getParticipantes() { return participantes; }
    public void setParticipantes(List<ParticipanteDTO> participantes) {
        this.participantes = participantes != null ? participantes : new ArrayList<>();
    }

    public LocalDateTime getUltimoMensajeFecha() { return ultimoMensajeFecha; }
    public void setUltimoMensajeFecha(LocalDateTime ultimoMensajeFecha) { this.ultimoMensajeFecha = ultimoMensajeFecha; }

    public String getUltimoMensajePreview() { return ultimoMensajePreview; }
    public void setUltimoMensajePreview(String ultimoMensajePreview) { this.ultimoMensajePreview = ultimoMensajePreview; }

    public int getMensajesNoLeidos() { return mensajesNoLeidos; }
    public void setMensajesNoLeidos(int mensajesNoLeidos) { this.mensajesNoLeidos = mensajesNoLeidos; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    // Métodos helper
    public String getTiempoUltimoMensaje() {
        if (ultimoMensajeFecha == null) return "";

        LocalDateTime ahora = LocalDateTime.now();
        long minutos = java.time.Duration.between(ultimoMensajeFecha, ahora).toMinutes();

        if (minutos < 1) return "Ahora";
        if (minutos < 60) return minutos + "m";
        if (minutos < 1440) return (minutos / 60) + "h";
        return (minutos / 1440) + "d";
    }

    public String getParticipantesNombres() {
        if (participantes.isEmpty()) return "";

        StringBuilder nombres = new StringBuilder();
        for (int i = 0; i < Math.min(participantes.size(), 3); i++) {
            if (i > 0) nombres.append(", ");
            nombres.append(participantes.get(i).getNombre());
        }

        if (participantes.size() > 3) {
            nombres.append(" y ").append(participantes.size() - 3).append(" más");
        }

        return nombres.toString();
    }

    // Clase interna para participantes
    public static class ParticipanteDTO {
        private String email;
        private String nombre;
        private String rol; // "ESTUDIANTE", "DOCENTE", "COORDINADOR", etc.

        // Constructores
        public ParticipanteDTO() {}

        public ParticipanteDTO(String email, String nombre) {
            this.email = email;
            this.nombre = nombre;
        }

        public ParticipanteDTO(String email, String nombre, String rol) {
            this.email = email;
            this.nombre = nombre;
            this.rol = rol;
        }

        // Getters y setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getRol() { return rol; }
        public void setRol(String rol) { this.rol = rol; }

        // Método helper
        public String getIniciales() {
            if (nombre == null || nombre.trim().isEmpty()) return "??";
            String[] partes = nombre.split(" ");
            if (partes.length >= 2) {
                return (partes[0].charAt(0) + "" + partes[1].charAt(0)).toUpperCase();
            }
            return nombre.substring(0, Math.min(2, nombre.length())).toUpperCase();
        }
    }
}