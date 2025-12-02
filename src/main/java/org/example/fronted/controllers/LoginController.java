package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.fronted.api.AuthApi;
import reactor.core.scheduler.Schedulers;
import javafx.application.Platform;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private AuthApi authApi;

    @FXML
    public void initialize() {
        authApi = new AuthApi();
        errorLabel.setVisible(false);

        // Enter para login
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private ProgressIndicator progressIndicator;

    // Y en el método setLoading:
    private void setLoading(boolean loading) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(loading);
        }
        // resto del código...
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Ingrese email y contraseña");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        authApi.login(email, password)
                .publishOn(Schedulers.fromExecutor(Platform::runLater))
                .subscribe(success -> {
                    loginButton.setDisable(false);

                    if (success) {
                        System.out.println("Login exitoso!");
                        // Aquí iría navegar al dashboard
                        showError("Login exitoso! (Falta navegar)");
                    } else {
                        showError("Credenciales incorrectas");
                    }
                }, error -> {
                    Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        showError("Error: " + error.getMessage());
                    });
                });
    }

    // En LoginController.java, añade este método:
    public void setMainController(MainController mainController) {
        // Puede quedar vacío si no necesitas referencias
        System.out.println("MainController establecido");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}