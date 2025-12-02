package org.example.fronted.models;

import java.util.List;
import java.util.ArrayList;

public class User {
    private String email;
    private String nombres;
    private String apellidos;
    private Rol rolActual;               // Rol activo actual
    private List<Rol> rolesDisponibles;  // Lista de roles disponibles
    private String programa;
    private String celular;

    // Constructor vacío
    public User() {
        this.rolesDisponibles = new ArrayList<>();
    }

    // Constructor con un solo rol (para compatibilidad)
    public User(String email, String nombres, String apellidos, Rol rol) {
        this.email = email;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.rolActual = rol;
        this.rolesDisponibles = new ArrayList<>();
        this.rolesDisponibles.add(rol);
    }

    // Constructor con múltiples roles
    public User(String email, String nombres, String apellidos, List<Rol> roles) {
        this.email = email;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.rolesDisponibles = roles;
        if (!roles.isEmpty()) {
            this.rolActual = roles.get(0); // Por defecto, primer rol
        }
    }

    // Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public Rol getRolActual() { return rolActual; }
    public void setRolActual(Rol rolActual) { this.rolActual = rolActual; }

    public List<Rol> getRolesDisponibles() { return rolesDisponibles; }
    public void setRolesDisponibles(List<Rol> rolesDisponibles) {
        this.rolesDisponibles = rolesDisponibles;
    }

    public String getPrograma() { return programa; }
    public void setPrograma(String programa) { this.programa = programa; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    // Métodos de conveniencia
    public void agregarRol(Rol rol) {
        if (!rolesDisponibles.contains(rol)) {
            rolesDisponibles.add(rol);
        }
    }
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
    public boolean tieneMultiplesRoles() {
        return rolesDisponibles != null && rolesDisponibles.size() > 1;
    }

    // Para compatibilidad con código existente
    public Rol getRol() {
        return rolActual;
    }

    public void setRol(Rol rol) {
        this.rolActual = rol;
    }
}