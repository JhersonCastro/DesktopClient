package org.example.fronted.dto;

public class AsignarEvaluadoresDTO {

    private Long idProyecto;
    private String jefeDepartamentoEmail;
    private String evaluador1Email;
    private String evaluador2Email;

    public Long getIdProyecto() { return idProyecto; }

    public void setIdProyecto(Long idProyecto) { this.idProyecto = idProyecto; }

    public String getJefeDepartamentoEmail() { return jefeDepartamentoEmail; }

    public void setJefeDepartamentoEmail(String jefeDepartamentoEmail) { this.jefeDepartamentoEmail = jefeDepartamentoEmail; }

    public String getEvaluador1Email() { return evaluador1Email; }

    public void setEvaluador1Email(String evaluador1Email) { this.evaluador1Email = evaluador1Email; }

    public String getEvaluador2Email() { return evaluador2Email; }

    public void setEvaluador2Email(String evaluador2Email) { this.evaluador2Email = evaluador2Email; }
}
