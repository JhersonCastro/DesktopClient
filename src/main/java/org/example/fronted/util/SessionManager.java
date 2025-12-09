package org.example.fronted.util;

import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
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
    /**
     * -- GETTER --
     *  Obtener usuario completo
     */
    // Nuevo campo para User completo
    @Getter
    @Setter
    private User currentUser;
    @Getter
    @Setter
    private String token;
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



    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        this.token = null;
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
        notifyUserLoggedIn(user);
    }

    /**
     * Actualizar datos del usuario
     */
    public void updateUser(User user) {
        this.currentUser = user;
        notifySessionUpdated(user);
    }

    // ============ MÉTODOS UTILITARIOS ============

    public String getUserFullName() {
        if (currentUser != null && currentUser.getNombreCompleto() != null) {
            return currentUser.getNombreCompleto();
        }
        return currentUser != null ? currentUser.getEmail().split("@")[0] : "Usuario";
    }


    public List<Rol> getRolesDisponibles() {
        List<Rol> rolesStr = new ArrayList<>();
        if (currentUser != null && currentUser.getRolesDisponibles() != null) {
            rolesStr.addAll(currentUser.getRolesDisponibles());
        }
        return rolesStr;
    }
    public boolean tieneMultiplesRoles() {
        return currentUser != null && currentUser.tieneMultiplesRoles();
    }
    public void cambiarRol(Rol nuevoRol) {
        if (currentUser != null) {
            // Buscar el rol en la lista de roles disponibles
            for (Rol rol : currentUser.getRolesDisponibles()) {
                if (rol.equals(nuevoRol)) {
                    currentUser.setRolActual(rol);
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
        Platform.runLater(() -> {
            for (SessionObserver observer : observers) {
                observer.onUserLoggedIn(user);
            }
        });
    }

    private void notifyUserLoggedOut() {
        Platform.runLater(() -> {
            for (SessionObserver observer : observers) {
                observer.onUserLoggedOut();
            }
        });
    }

    private void notifySessionUpdated(User user) {
        Platform.runLater(() -> {
            for (SessionObserver observer : observers) {
                observer.onSessionUpdated(user);
            }
        });
    }
    // ============ PARA TESTING ============

    public static void clearInstance() {
        instance = null;
    }
}