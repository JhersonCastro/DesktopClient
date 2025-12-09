package org.example.fronted.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;

@Setter
@Getter
public class User {
    // Getters y Setters
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