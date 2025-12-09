package org.example.fronted.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.fronted.api.AuthApi;
import org.example.fronted.util.SessionManager;
import reactor.core.scheduler.Schedulers;
import javafx.application.Platform;

public class LoginController extends UIBase {
    public Button loginButton;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordBtn;
    @FXML private Label lblMessage;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    private AuthApi authApi;

    private boolean passwordVisible = false;


    @FXML
    public void initialize() {
        authApi = new AuthApi();

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
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Ingrese email y contraseña");
            return;
        }

        loginButton.setDisable(true);
        lblMessage.setVisible(false);

        authApi.login(email, password)
                .publishOn(Schedulers.fromExecutor(Platform::runLater))
                .subscribe(success -> {
                    loginButton.setDisable(false);

                    if (success) {
                        System.out.println("Login exitoso!");
                        // Aquí iría navegar al dashboard
                        mainController.onUserLoggedIn(SessionManager.getInstance().getCurrentUser());
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
    public void showError(String message) {
        lblMessage.setText(message);
        lblMessage.setVisible(true);
    }
    @FXML
    private void handleRegister() {
        if (mainController != null) {
            loadView("/views/auth/Register.fxml");
        }
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