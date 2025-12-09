package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.fronted.api.AuthApi;
import org.example.fronted.api.UserApi;
import org.example.fronted.dto.RegistroUsuarioDTO;
import org.example.fronted.models.Rol;
import org.example.fronted.models.User;
import org.example.fronted.util.SessionManager;
import reactor.core.scheduler.Schedulers;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class RegisterController extends UIBase implements Initializable {

    // Campos del formulario
    @FXML private TextField txtNames;
    @FXML private TextField txtLastNames;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private CheckBox chkEstudiante;
    @FXML private CheckBox chkDirector;
    @FXML private CheckBox chkCoordinador;
    @FXML private CheckBox chkJurado;
    @FXML private CheckBox chkJefeDepartamento;
    @FXML private TextField txtPhone;
    @FXML private CheckBox chkTerms;
    @FXML private Button btnRegister;

    // Etiquetas de error
    @FXML private Label lblNamesError;
    @FXML private Label lblLastNamesError;
    @FXML private Label lblEmailError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblConfirmPasswordError;
    @FXML private Label lblRolesError;
    @FXML private Label lblPhoneError;
    @FXML private Label lblRegisterMessage;

    // Patrones de validación con regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@unicauca\\.edu\\.co$"
    );
    private static final Pattern NAMES_PATTERN = Pattern.compile(
            "^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]{2,50}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^3[0-9]{9}$"
    );
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_NUMBER = Pattern.compile(".*\\d.*");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    // Fachada hacia el microservicio de usuarios
    private final UserApi userApi = new UserApi();
    private final AuthApi authApi = new AuthApi();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("RegisterController inicializado");

        // Configurar validación en tiempo real
        setupFieldValidation();

        // Configurar lógica de roles exclusivos
        setupRoleExclusivity();

        // Configurar estado inicial del botón
        updateRegisterButtonState();

        // Habilitar/deshabilitar botón según términos
        chkTerms.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateRegisterButtonState();
        });
    }

    /**
     * Inyecta el MainController para permitir navegación
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Configura la lógica de exclusividad de roles
     */
    private void setupRoleExclusivity() {
        // Si se selecciona Estudiante, deshabilitar otros roles
        chkEstudiante.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                chkDirector.setSelected(false);
                chkCoordinador.setSelected(false);
                chkJurado.setSelected(false);
                chkJefeDepartamento.setSelected(false);

                chkDirector.setDisable(true);
                chkCoordinador.setDisable(true);
                chkJurado.setDisable(true);
                chkJefeDepartamento.setDisable(true);
            } else {
                chkDirector.setDisable(false);
                chkCoordinador.setDisable(false);
                chkJurado.setDisable(false);
                chkJefeDepartamento.setDisable(false);
            }
            validateRoles();
        });

        // Si se selecciona cualquier otro rol, deshabilitar Estudiante
        CheckBox[] professionalRoles = {chkDirector, chkCoordinador, chkJurado, chkJefeDepartamento};
        for (CheckBox role : professionalRoles) {
            role.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    chkEstudiante.setSelected(false);
                    chkEstudiante.setDisable(true);
                } else {
                    // Solo habilitar Estudiante si ningún otro rol está seleccionado
                    boolean anyProfessionalRoleSelected = chkDirector.isSelected() ||
                            chkCoordinador.isSelected() ||
                            chkJurado.isSelected() ||
                            chkJefeDepartamento.isSelected();
                    chkEstudiante.setDisable(anyProfessionalRoleSelected);
                }
                validateRoles();
            });
        }
    }

    /**
     * Configura la validación en tiempo real de los campos
     */
    private void setupFieldValidation() {
        // Validar nombres con regex
        txtNames.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(lblNamesError, "Los nombres son obligatorios", txtNames);
            } else if (!NAMES_PATTERN.matcher(newVal.trim()).matches()) {
                showFieldError(lblNamesError, "Solo letras y espacios (2-50 caracteres)", txtNames);
            } else {
                clearFieldError(lblNamesError, txtNames);
            }
            updateRegisterButtonState();
        });

        // Validar apellidos con regex
        txtLastNames.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(lblLastNamesError, "Los apellidos son obligatorios", txtLastNames);
            } else if (!NAMES_PATTERN.matcher(newVal.trim()).matches()) {
                showFieldError(lblLastNamesError, "Solo letras y espacios (2-50 caracteres)", txtLastNames);
            } else {
                clearFieldError(lblLastNamesError, txtLastNames);
            }
            updateRegisterButtonState();
        });

        // Validar email con regex
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                showFieldError(lblEmailError, "El correo electrónico es obligatorio", txtEmail);
            } else if (!EMAIL_PATTERN.matcher(newVal.trim()).matches()) {
                showFieldError(lblEmailError, "Use formato: usuario@unicauca.edu.co", txtEmail);
            } else {
                clearFieldError(lblEmailError, txtEmail);
            }
            updateRegisterButtonState();
        });

        // Validar contraseña con múltiples criterios
        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePasswordWithRegex(newVal);
            // Validar confirmación si ya hay texto
            if (!txtConfirmPassword.getText().isEmpty()) {
                validateConfirmPassword();
            }
            updateRegisterButtonState();
        });

        // Validar confirmación de contraseña
        txtConfirmPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            validateConfirmPassword();
            updateRegisterButtonState();
        });

        // Validar teléfono (opcional) con regex
        txtPhone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty() && !PHONE_PATTERN.matcher(newVal.trim()).matches()) {
                showFieldError(lblPhoneError, "Formato: 3 seguido de 9 dígitos (ej: 3123456789)", txtPhone);
            } else {
                clearFieldError(lblPhoneError, txtPhone);
            }
            updateRegisterButtonState();
        });

        // Validar roles cuando cambian
        CheckBox[] roleCheckboxes = {chkEstudiante, chkDirector, chkCoordinador, chkJurado, chkJefeDepartamento};
        for (CheckBox checkbox : roleCheckboxes) {
            checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                validateRoles();
                updateRegisterButtonState();
            });
        }
    }

    /**
     * Valida la contraseña con múltiples criterios usando regex
     */
    private void validatePasswordWithRegex(String password) {
        if (password.isEmpty()) {
            showFieldError(lblPasswordError, "La contraseña es obligatoria", txtPassword);
        } else if (password.length() < 8) {
            showFieldError(lblPasswordError, "Mínimo 8 caracteres", txtPassword);
        } else if (!PASSWORD_UPPERCASE.matcher(password).matches()) {
            showFieldError(lblPasswordError, "Debe contener al menos una mayúscula", txtPassword);
        } else if (!PASSWORD_LOWERCASE.matcher(password).matches()) {
            showFieldError(lblPasswordError, "Debe contener al menos una minúscula", txtPassword);
        } else if (!PASSWORD_NUMBER.matcher(password).matches()) {
            showFieldError(lblPasswordError, "Debe contener al menos un número", txtPassword);
        } else if (!PASSWORD_SPECIAL.matcher(password).matches()) {
            showFieldError(lblPasswordError, "Debe contener al menos un carácter especial (!@#$%...)", txtPassword);
        } else {
            clearFieldError(lblPasswordError, txtPassword);
        }
    }

    /**
     * Valida que las contraseñas coincidan
     */
    private void validateConfirmPassword() {
        String password = txtPassword.getText();
        String confirm = txtConfirmPassword.getText();

        if (confirm.isEmpty()) {
            showFieldError(lblConfirmPasswordError, "Confirme la contraseña", txtConfirmPassword);
        } else if (!confirm.equals(password)) {
            showFieldError(lblConfirmPasswordError, "Las contraseñas no coinciden", txtConfirmPassword);
        } else {
            clearFieldError(lblConfirmPasswordError, txtConfirmPassword);
        }
    }

    /**
     * Valida que al menos un rol esté seleccionado
     */
    private void validateRoles() {
        boolean anySelected = chkEstudiante.isSelected() ||
                chkDirector.isSelected() ||
                chkCoordinador.isSelected() ||
                chkJurado.isSelected() ||
                chkJefeDepartamento.isSelected();

        if (!anySelected) {
            lblRolesError.setText("Seleccione al menos un rol");
            lblRolesError.setVisible(true);
        } else {
            lblRolesError.setVisible(false);
        }
    }

    /**
     * Actualiza el estado del botón de registro
     */
    private void updateRegisterButtonState() {
        boolean isFormValid = isFormValid();

        // Aquí había un bug: siempre lo ponías en false.
        btnRegister.setDisable(!isFormValid);

        if (isFormValid) {
            btnRegister.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
        } else {
            btnRegister.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        }
    }

    /**
     * Verifica si el formulario es válido (versión rápida usada solo para habilitar botón)
     */
    private boolean isFormValid() {
        // Validar nombres
        if (txtNames.getText().trim().isEmpty() || !NAMES_PATTERN.matcher(txtNames.getText().trim()).matches()) {
            return false;
        }

        // Validar apellidos
        if (txtLastNames.getText().trim().isEmpty() || !NAMES_PATTERN.matcher(txtLastNames.getText().trim()).matches()) {
            return false;
        }

        // Validar email
        if (txtEmail.getText().trim().isEmpty() || !EMAIL_PATTERN.matcher(txtEmail.getText().trim()).matches()) {
            return false;
        }

        // Validar contraseña
        String password = txtPassword.getText();
        if (password.isEmpty() || password.length() < 8 ||
                !PASSWORD_UPPERCASE.matcher(password).matches() ||
                !PASSWORD_LOWERCASE.matcher(password).matches() ||
                !PASSWORD_NUMBER.matcher(password).matches() ||
                !PASSWORD_SPECIAL.matcher(password).matches()) {
            return false;
        }

        // Validar confirmación de contraseña
        if (!txtConfirmPassword.getText().equals(password)) {
            return false;
        }

        // Validar teléfono (opcional)
        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            return false;
        }

        // Validar roles
        if (!chkEstudiante.isSelected() &&
                !chkDirector.isSelected() &&
                !chkCoordinador.isSelected() &&
                !chkJurado.isSelected() &&
                !chkJefeDepartamento.isSelected()) {
            return false;
        }

        // Validar términos
        if (!chkTerms.isSelected()) {
            return false;
        }

        return true;
    }

    /**
     * Maneja el registro del usuario (click en REGISTRARSE)
     */
    @FXML
    private void handleRegister() {
        clearMessages();

        // Validar todos los campos (validación completa)
        boolean isValid = validateForm();

        if (!isValid) {
            showMessage("Por favor, corrija los errores en el formulario", "error");
            return;
        }

        // Resolver rol principal según los checkboxes
        Set<Rol> roles = obtenerTodosLosRoles();
        if (roles.isEmpty()) {
            showMessage("Debe seleccionar al menos un rol", "error");
            return;
        }

        // Construir DTO para el microservicio
        RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
        dto.setNombres(txtNames.getText().trim());
        dto.setApellidos(txtLastNames.getText().trim());
        dto.setEmail(txtEmail.getText().trim());
        dto.setPassword(txtPassword.getText());
        dto.setCelular(txtPhone.getText().trim());
        dto.setRoles(roles);

        System.out.println("=== DATOS VALIDADOS PARA REGISTRO ===");
        System.out.println("Nombres: " + dto.getNombres());
        System.out.println("Apellidos: " + dto.getApellidos());
        System.out.println("Email: " + dto.getEmail());
        System.out.println("Teléfono: " + dto.getCelular());
        for (int i = 0; i<dto.getRoles().toArray().length; i++){
            System.out.println("Tiene el rol de " + dto.getRoles().toArray()[i]);
        }
        System.out.println("======================================");

        // Llamar al microservicio
        btnRegister.setDisable(true);
        showMessage("Registrando usuario...", "info");

        userApi.registrarUsuario(dto)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(success -> Platform.runLater(() -> {
                            btnRegister.setDisable(false);
                            if (success) {
                                showMessage("✓ Registro exitoso. Ahora puede iniciar sesión.", "success");
                                // Aquí puedes navegar a login si ya tienes el mecanismo:
                                authApi.login(dto.getEmail(), dto.getPassword())
                                        .publishOn(Schedulers.fromExecutor(Platform::runLater))
                                        .subscribe(success1 -> {
                                            if (success1) {
                                                mainController.onUserLoggedIn(SessionManager.getInstance().getCurrentUser());
                                            } else {
                                                showError("Algo ocurrio al ingresar de manera automatica, intentalo de manera manual");
                                            }
                                        }, error -> {
                                            Platform.runLater(() -> {
                                                showError("Error: " + error.getMessage());
                                            });
                                        });
                            } else {
                                showMessage("No se pudo completar el registro. Intente nuevamente.", "error");
                            }
                        }),
                        error -> Platform.runLater(() -> {
                            btnRegister.setDisable(false);
                            showMessage("Error al registrar: " + error.getMessage(), "error");
                        }));
    }

    /**
     * Regresa a la pantalla de login
     */
    @FXML
    private void handleBackToLogin() {
        loadView("/views/auth/login.fxml");
    }

    /**
     * Valida todo el formulario (validación completa)
     */
    private boolean validateForm() {
        boolean isValid = true;

        // Validar nombres
        if (txtNames.getText().trim().isEmpty()) {
            showFieldError(lblNamesError, "Los nombres son obligatorios", txtNames);
            isValid = false;
        } else if (!NAMES_PATTERN.matcher(txtNames.getText().trim()).matches()) {
            showFieldError(lblNamesError, "Solo letras y espacios (2-50 caracteres)", txtNames);
            isValid = false;
        }

        // Validar apellidos
        if (txtLastNames.getText().trim().isEmpty()) {
            showFieldError(lblLastNamesError, "Los apellidos son obligatorios", txtLastNames);
            isValid = false;
        } else if (!NAMES_PATTERN.matcher(txtLastNames.getText().trim()).matches()) {
            showFieldError(lblLastNamesError, "Solo letras y espacios (2-50 caracteres)", txtLastNames);
            isValid = false;
        }

        // Validar email
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            showFieldError(lblEmailError, "El correo electrónico es obligatorio", txtEmail);
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            showFieldError(lblEmailError, "Use formato: usuario@unicauca.edu.co", txtEmail);
            isValid = false;
        }

        // Validar contraseña
        String password = txtPassword.getText();
        if (password.isEmpty()) {
            showFieldError(lblPasswordError, "La contraseña es obligatoria", txtPassword);
            isValid = false;
        } else {
            validatePasswordWithRegex(password);
            if (lblPasswordError.isVisible()) {
                isValid = false;
            }
        }

        // Validar confirmación de contraseña
        String confirmPassword = txtConfirmPassword.getText();
        if (!confirmPassword.equals(password)) {
            showFieldError(lblConfirmPasswordError, "Las contraseñas no coinciden", txtConfirmPassword);
            isValid = false;
        }

        // Validar roles
        if (!chkEstudiante.isSelected() &&
                !chkDirector.isSelected() &&
                !chkCoordinador.isSelected() &&
                !chkJurado.isSelected() &&
                !chkJefeDepartamento.isSelected()) {
            lblRolesError.setText("Seleccione al menos un rol");
            lblRolesError.setVisible(true);
            isValid = false;
        }

        // Validar teléfono si se ingresó
        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            showFieldError(lblPhoneError, "Formato: 3 seguido de 9 dígitos (ej: 3123456789)", txtPhone);
            isValid = false;
        }

        // Validar términos
        if (!chkTerms.isSelected()) {
            showMessage("Debe aceptar los términos y condiciones", "error");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Obtiene todos los roles que han sido seleccionados
     */
    private Set<Rol> obtenerTodosLosRoles() {
        Map<CheckBox, Rol> mapaRoles = Map.of(
                chkEstudiante, Rol.ESTUDIANTE,
                chkCoordinador, Rol.COORDINADOR,
                chkJefeDepartamento, Rol.JEFE_DEPARTAMENTO
        );

        Set<Rol> roles = new HashSet<>();

        // Roles directos
        mapaRoles.forEach((check, rol) -> {
            if (check.isSelected()) {
                roles.add(rol);
            }
        });

        // Casos especiales (Director / Jurado → DOCENTE)
        if (chkDirector.isSelected() || chkJurado.isSelected()) {
            roles.add(Rol.DOCENTE);
        }

        return roles;
    }


    /**
     * Muestra un error en un campo específico
     */
    private void showFieldError(Label errorLabel, String message, TextInputControl field) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        if (!field.getStyleClass().contains("error")) {
            field.getStyleClass().add("error");
        }
    }

    /**
     * Limpia el error de un campo específico
     */
    private void clearFieldError(Label errorLabel, TextInputControl field) {
        errorLabel.setVisible(false);
        field.getStyleClass().remove("error");
    }

    /**
     * Obtiene los roles seleccionados (solo para logs)
     */
    private String getSelectedRoles() {
        StringBuilder roles = new StringBuilder();

        if (chkEstudiante.isSelected()) roles.append("Estudiante, ");
        if (chkDirector.isSelected()) roles.append("Director, ");
        if (chkCoordinador.isSelected()) roles.append("Coordinador, ");
        if (chkJurado.isSelected()) roles.append("Jurado, ");
        if (chkJefeDepartamento.isSelected()) roles.append("Jefe Depto, ");

        if (roles.length() > 0) {
            roles.setLength(roles.length() - 2); // Quitar última coma y espacio
        }

        return roles.toString();
    }

    /**
     * Muestra un mensaje general en la parte inferior
     */
    private void showMessage(String message, String type) {
        lblRegisterMessage.setText(message);
        lblRegisterMessage.getStyleClass().removeAll("success", "error", "info");
        lblRegisterMessage.getStyleClass().add(type);
        lblRegisterMessage.setVisible(true);
    }

    /**
     * Limpia todos los mensajes
     */
    private void clearMessages() {
        lblRegisterMessage.setVisible(false);
        lblRegisterMessage.getStyleClass().removeAll("success", "error", "info");
    }
}
