package org.example.fronted.util;

public class SessionManager {
    private static SessionManager instance;
    private String token;
    private String email;
    private String rol;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getToken() { return token; }

    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }

    public void setRol(String rol) { this.rol = rol; }
    public String getRol() { return rol; }

    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        this.token = null;
        this.email = null;
        this.rol = null;
    }
}