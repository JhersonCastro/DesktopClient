package org.example.fronted.dto;

public class UserResponseDTO {
    private String email;
    private String rol;

    public UserResponseDTO() {}

    public UserResponseDTO(String email, String rol) {
        this.email = email;
        this.rol = rol;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    @Override
    public String toString() {
        return "UserResponseDTO{email='" + email + "', rol='" + rol + "'}";
    }
}