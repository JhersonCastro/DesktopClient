package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordBtn;
    @FXML private Label lblMessage;
    @FXML private Label emailError;
    @FXML private Label passwordError;

    private MainController mainController;
    private boolean passwordVisible = false;

    /**
     * Inyecta el MainController para permitir navegación
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        togglePasswordBtn.setText("👁");
        togglePasswordBtn.setOnAction(e -> togglePasswordVisibility());

        // Sincronizar campos de contraseña
        visiblePasswordField.textProperty().bindBidirectional(txtPassword.textProperty());
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            togglePasswordBtn.setText("🔒");
            visiblePasswordField.requestFocus();
        } else {
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            togglePasswordBtn.setText("👁");
            txtPassword.requestFocus();
        }
    }

    @FXML
    private void handleLogin() {
        clearMessages();

        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (validateForm(email, password)) {
            // Simular autenticación
            lblMessage.setText("Autenticando...");
            lblMessage.setStyle("-fx-text-fill: #17a2b8;");

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                if (authenticateUser(email, password)) {
                    lblMessage.setText("✓ Autenticación exitosa");
                    lblMessage.setStyle("-fx-text-fill: #28a745;");

                    // Navegar al dashboard después de éxito
                    PauseTransition successPause = new PauseTransition(Duration.seconds(1));
                    successPause.setOnFinished(event -> {
                        mainController.loadDashboardView(email);
                    });
                    successPause.play();

                } else {
                    lblMessage.setText("✗ Credenciales incorrectas");
                    lblMessage.setStyle("-fx-text-fill: #dc3545;");
                }
            });
            pause.play();
        }
    }

    @FXML
    private void handleRegister() {
        if (mainController != null) {
            mainController.loadRegisterView();
        }
    }

    @FXML
    private void handleForgotPassword() {
        lblMessage.setText("Sistema de recuperación en desarrollo...");
        lblMessage.setStyle("-fx-text-fill: #17a2b8;");
    }

    private boolean validateForm(String email, String password) {
        boolean isValid = true;

        if (email.isEmpty()) {
            emailError.setText("El usuario es obligatorio");
            emailError.setVisible(true);
            txtEmail.getStyleClass().add("error");
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailError.setText("Formato: usuario@unicauca.edu.co");
            emailError.setVisible(true);
            txtEmail.getStyleClass().add("error");
            isValid = false;
        } else {
            emailError.setVisible(false);
            txtEmail.getStyleClass().remove("error");
        }

        if (password.isEmpty()) {
            passwordError.setText("La clave de acceso es obligatoria");
            passwordError.setVisible(true);
            (passwordVisible ? visiblePasswordField : txtPassword).getStyleClass().add("error");
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setText("Mínimo 6 caracteres");
            passwordError.setVisible(true);
            (passwordVisible ? visiblePasswordField : txtPassword).getStyleClass().add("error");
            isValid = false;
        } else {
            passwordError.setVisible(false);
            (passwordVisible ? visiblePasswordField : txtPassword).getStyleClass().remove("error");
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@unicauca\\.edu\\.co$");
    }

    private boolean authenticateUser(String email, String password) {
        // Simulación de autenticación
        return email.contains("@unicauca.edu.co") && password.length() >= 6;
    }

    private void clearMessages() {
        lblMessage.setText("");
        emailError.setVisible(false);
        passwordError.setVisible(false);
        txtEmail.getStyleClass().remove("error");
        txtPassword.getStyleClass().remove("error");
        visiblePasswordField.getStyleClass().remove("error");
    }
}