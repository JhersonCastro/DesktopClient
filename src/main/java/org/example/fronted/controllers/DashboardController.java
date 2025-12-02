package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label userEmailLabel;
    @FXML private VBox dashboardContent;

    private MainController mainController;
    private String currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DashboardController inicializado");
    }

    /**
     * Inyecta el MainController para permitir navegación
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Configura los datos del usuario
     */
    public void setUserData(String email, String username) {
        this.currentUser = email;

        if (welcomeLabel != null) {
            welcomeLabel.setText("¡Bienvenido, " + username + "!");
        }

        if (userEmailLabel != null) {
            userEmailLabel.setText(email);
        }

        System.out.println("Dashboard configurado para: " + email);
    }

    /**
     * Extrae el nombre de usuario del email
     */
    private String extractUsername(String email) {
        if (email == null || email.isEmpty()) {
            return "Usuario";
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, atIndex);
        }
        return email;
    }

    /**
     * Maneja el cierre de sesión
     */
    @FXML
    private void handleLogout() {
        if (mainController != null) {
            mainController.loadLoginView();
        }
    }

    /**
     * Métodos para diferentes funcionalidades del dashboard
     */
    @FXML
    private void handleProfile() {
        System.out.println("Perfil del usuario: " + currentUser);
        // Aquí cargarías la vista de perfil
    }

    @FXML
    private void handleSettings() {
        System.out.println("Configuración del usuario");
        // Aquí cargarías la vista de configuración
    }

    @FXML
    private void handleNotifications() {
        System.out.println("Notificaciones");
        // Aquí cargarías la vista de notificaciones
    }

    /**
     * Devuelve el usuario actual
     */
    public String getCurrentUser() {
        return currentUser;
    }
}