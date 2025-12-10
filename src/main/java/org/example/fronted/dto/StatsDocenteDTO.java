package org.example.fronted.dto;

public class StatsDocenteDTO {
    public long formatoAPendiente;
    public long formatoAAprobado;
    public long pendientesEvaluar;

    public StatsDocenteDTO(long formatoAPendiente, long formatoAAprobado, long pendientesEvaluar) {
        this.formatoAPendiente = formatoAPendiente;
        this.formatoAAprobado = formatoAAprobado;
        this.pendientesEvaluar = pendientesEvaluar;
    }

    public StatsDocenteDTO() {
    }
}


