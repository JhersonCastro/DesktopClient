package org.example.fronted.dto;

import lombok.Data;
import org.example.fronted.models.Rol;

import java.util.HashSet;
import java.util.Set;

@Data
public class RegistroUsuarioDTO {
    private String email;

    private String password;

    private String nombres;

    private String apellidos;

    private String celular;

    private String programa;

    private Set<Rol> roles = new HashSet<>();
}