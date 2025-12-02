package org.example.fronted.util;

import org.example.fronted.models.Rol;
import org.example.fronted.models.User;
import org.example.fronted.observer.SessionObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton mejorado con patrón Observer
 * Mantiene compatibilidad con tu código existente
 */
public class SessionManager {
    private static SessionManager instance;

    // Campos existentes (para compatibilidad)
    private String token;
    private String email;
    private String rol;

    // Nuevo campo para User completo
    private User currentUser;

    // Lista de observadores
    private final List<SessionObserver> observers;

    private SessionManager() {
        this.observers = new ArrayList<>();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    // ============ MÉTODOS EXISTENTES (MANTENER COMPATIBILIDAD) ============

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getRol() {
        return rol;
    }

    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        this.token = null;
        this.email = null;
        this.rol = null;
        this.currentUser = null;
        notifyUserLoggedOut();
    }

    // ============ NUEVOS MÉTODOS CON USER COMPLETO ============

    /**
     * Login con objeto User completo
     */
    public void login(User user, String token) {
        this.currentUser = user;
        this.token = token;
        this.email = user.getEmail();
        this.rol = user.getRol() != null ? user.getRol().toString() : null;
        notifyUserLoggedIn(user);
    }

    /**
     * Login con datos básicos (mantener compatibilidad)
     */
    public void login(String email, String rol, String token) {
        this.email = email;
        this.rol = rol;
        this.token = token;
        // Crear User básico
        this.currentUser = new User();
        this.currentUser.setEmail(email);
        this.currentUser.setNombres(email.split("@")[0]); // Nombre temporal
        notifyUserLoggedIn(this.currentUser);
    }

    /**
     * Obtener usuario completo
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Actualizar datos del usuario
     */
    public void updateUser(User user) {
        this.currentUser = user;
        this.email = user.getEmail();
        this.rol = user.getRol() != null ? user.getRol().toString() : null;
        notifySessionUpdated(user);
    }

    // ============ MÉTODOS UTILITARIOS ============

    public String getUserFullName() {
        if (currentUser != null && currentUser.getNombreCompleto() != null) {
            return currentUser.getNombreCompleto();
        }
        return email != null ? email.split("@")[0] : "Usuario";
    }

    public boolean hasRole(String role) {
        if (rol == null) return false;
        return rol.equalsIgnoreCase(role);
    }
    public List<String> getRolesDisponibles() {
        List<String> rolesStr = new ArrayList<>();
        if (currentUser != null && currentUser.getRolesDisponibles() != null) {
            for (Rol rol : currentUser.getRolesDisponibles()) {
                rolesStr.add(rol.toString());
            }
        }
        return rolesStr;
    }
    public boolean tieneMultiplesRoles() {
        return currentUser != null && currentUser.tieneMultiplesRoles();
    }
    public void cambiarRol(String nuevoRol) {
        if (currentUser != null) {
            // Buscar el rol en la lista de roles disponibles
            for (Rol rol : currentUser.getRolesDisponibles()) {
                if (rol.toString().equals(nuevoRol)) {
                    currentUser.setRolActual(rol);
                    this.rol = nuevoRol; // Para compatibilidad
                    notifySessionUpdated(currentUser);
                    break;
                }
            }
        }
    }
    // ============ PATRÓN OBSERVER ============

    public void registerObserver(SessionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(SessionObserver observer) {
        observers.remove(observer);
    }

    private void notifyUserLoggedIn(User user) {
        for (SessionObserver observer : observers) {
            observer.onUserLoggedIn(user);
        }
    }

    private void notifyUserLoggedOut() {
        for (SessionObserver observer : observers) {
            observer.onUserLoggedOut();
        }
    }

    private void notifySessionUpdated(User user) {
        for (SessionObserver observer : observers) {
            observer.onSessionUpdated(user);
        }
    }

    // ============ PARA TESTING ============

    public static void clearInstance() {
        instance = null;
    }
}