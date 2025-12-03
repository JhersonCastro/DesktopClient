package org.example.fronted.dto;

import org.example.fronted.models.Rol;

public class RegistroUsuarioDTO {

    private String nombres;
    private String apellidos;
    private String email;
    private String password;
    private String celular;
    private Rol rolPrincipal; // DOCENTE, ESTUDIANTE, COORDINADOR, JEFE_DEPARTAMENTO

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public Rol getRolPrincipal() {
        return rolPrincipal;
    }

    public void setRolPrincipal(Rol rolPrincipal) {
        this.rolPrincipal = rolPrincipal;
    }
}
