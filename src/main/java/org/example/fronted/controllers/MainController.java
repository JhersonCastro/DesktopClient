package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.io.IOException;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private HBox userMenu;
    @FXML private Label userName;

    private String currentUser = null;

    @FXML
    public void initialize() {
        System.out.println("MainController inicializado");
        // Cargar login por defecto al iniciar
        loadLoginView();
    }

    /**
     * Carga la vista de login
     */
    public void loadLoginView() {
        loadView("/views/auth/login.fxml");
        userMenu.setVisible(false); // Ocultar menú de usuario
        currentUser = null;
        System.out.println("Vista de login cargada");
    }

    /**
     * Carga la vista de dashboard
     */
    public void loadDashboardView(String email) {
        try {
            currentUser = email;
            userName.setText(extractUsername(email));
            userMenu.setVisible(true);

            System.out.println("Cargando dashboard para: " + email);

            // Cargar FXML del dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard/dashboard.fxml"));
            Parent dashboardView = loader.load();

            // Configurar controlador
            DashboardController dashboardController = loader.getController();
            dashboardController.setMainController(this);
            dashboardController.setUserData(email, extractUsername(email));

            // Mostrar en el área de contenido
            contentPane.getChildren().clear();
            contentPane.getChildren().add(dashboardView);

            System.out.println("Dashboard cargado exitosamente");

        } catch (IOException e) {
            System.err.println("Error al cargar dashboard: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, mostrar vista de error simple
            showErrorView("No se pudo cargar el dashboard. Intente nuevamente.");
        }
    }

    /**
     * Carga una vista dentro del área de contenido
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Si es el login, configurar referencia al MainController
            if (fxmlPath.contains("login.fxml")) {
                LoginController loginController = loader.getController();
                loginController.setMainController(this);
            }

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

        } catch (IOException e) {
            System.err.println("Error al cargar vista: " + e.getMessage());
            e.printStackTrace();
            showErrorView("Error al cargar la vista: " + fxmlPath);
        }
    }
    /**
     * Carga la vista de registro
     */
    public void loadRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/register.fxml"));
            Parent registerView = loader.load();

            // Configurar controlador
            RegisterController registerController = loader.getController();
            registerController.setMainController(this);

            contentPane.getChildren().clear();
            contentPane.getChildren().add(registerView);

            System.out.println("Vista de registro cargada");

        } catch (IOException e) {
            System.err.println("Error al cargar registro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Muestra una vista de error simple
     */
    private void showErrorView(String message) {
        javafx.scene.control.Label errorLabel = new javafx.scene.control.Label(message);
        errorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 14px; -fx-padding: 20px;");
        contentPane.getChildren().clear();
        contentPane.getChildren().add(errorLabel);
    }

    /**
     * Maneja el cierre de sesión
     */
    @FXML
    private void handleLogout() {
        System.out.println("Cerrando sesión de: " + currentUser);
        loadLoginView();
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
     * Devuelve el usuario actual
     */
    public String getCurrentUser() {
        return currentUser;
    }
}