package org.example.fronted.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.example.fronted.models.Rol;
import org.example.fronted.models.User;
import org.example.fronted.observer.SessionObserver;
import org.example.fronted.util.SessionManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class MainController implements SessionObserver{

    public Label userEmail;
    public HBox roleSelectorContainer;
    public ComboBox<Rol> roleComboBox;
    @FXML private StackPane contentPane;
    @FXML private HBox userMenu;
    @FXML private Label userName;

    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        System.out.println("MainController inicializado");
        // Cargar login por defecto al iniciar
        //loadLoginView();
        //loadView("/views/professor/formatA_new.fxml");
        sessionManager = SessionManager.getInstance();
        sessionManager.registerObserver(this);

        cargarUsuarioPruebaParaTesting_EliminarEnProduccion();
        updateUIFromSession();

    }
    public void cargarUsuarioPruebaParaTesting_EliminarEnProduccion() {
        System.out.println("🚀 INICIANDO MODO TESTING - USUARIO DE PRUEBA");
        System.out.println("⚠️  ADVERTENCIA: Este método debe eliminarse en producción");

        try {
            // Crear usuario de prueba con configuración completa
            User usuarioPrueba = new User();
            usuarioPrueba.setEmail("coordinador.prueba@unicauca.edu.co");
            usuarioPrueba.setNombres("Juan Carlos");
            usuarioPrueba.setApellidos("García Mendoza");
            usuarioPrueba.setPrograma("Ingeniería de Sistemas");
            usuarioPrueba.setCelular("312-123-4567");

            // Asignar múltiples roles para probar selector
            List<Rol> rolesUsuario =  Arrays.asList(Rol.COORDINADOR,
                    Rol.DOCENTE,
                    Rol.JEFE_DEPARTAMENTO,
                    Rol.ESTUDIANTE);
            usuarioPrueba.setRolesDisponibles(rolesUsuario);
            usuarioPrueba.setRolActual(Rol.COORDINADOR);

            // Token de autenticación simulado
            String tokenSimulado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjb29yZGluYWRvci5wcnVlYmEiLCJyb2wiOiJDT09SRElOQURPUiIsImlhdCI6MTYxNjIzOTAyMn0";

            // Iniciar sesión
            SessionManager.getInstance().login(usuarioPrueba, tokenSimulado);

            // Mostrar información en consola
            System.out.println("✅ Usuario de prueba cargado:");
            System.out.println("   📧 Email: " + usuarioPrueba.getEmail());
            System.out.println("   👤 Nombre: " + usuarioPrueba.getNombreCompleto());
            System.out.println("   🏫 Programa: " + usuarioPrueba.getPrograma());
            System.out.println("   📱 Celular: " + usuarioPrueba.getCelular());
            System.out.println("   🎭 Roles disponibles: " + rolesUsuario.size());
            System.out.println("   🎯 Rol actual: " + usuarioPrueba.getRolActual());
            System.out.println("   🔑 Token: " + tokenSimulado.substring(0, 30) + "...");


            // Cargar vista inicial según rol
            cargarVistaPorRol(usuarioPrueba.getRolActual());

        } catch (Exception e) {
            System.err.println("❌ Error al cargar usuario de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void cargarVistaPorRol(Rol rol) {
        switch (rol) {
            case COORDINADOR:
                loadDashboardCoordinatorView();
                break;
            case DOCENTE:
                loadDashboardDocenteView();
                break;
            case ESTUDIANTE:
                loadDashboardEstudianteView();
                break;
            case JEFE_DEPARTAMENTO:
                loadDashboardJefeView();
                break;
            default:
                break;
        }
    }
    private void updateUIFromSession() {
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getCurrentUser();

            userMenu.setVisible(true);

            if (user != null && user.getNombreCompleto() != null) {
                userName.setText(user.getNombreCompleto());
                userEmail.setText(user.getEmail());
            } else {
                userName.setText(sessionManager.getUserFullName());
                userEmail.setText(sessionManager.getEmail());
            }

            // Configurar selector de roles si tiene múltiples
            configurarSelectorRoles();

        } else {
            userMenu.setVisible(false);
        }
    }
    private void configurarSelectorRoles() {
        if (sessionManager.tieneMultiplesRoles()) {
            roleSelectorContainer.setVisible(true);

            // Cargar roles en el ComboBox
            List<Rol> roles = sessionManager.getRolesDisponibles();
            roleComboBox.setItems(FXCollections.observableArrayList(roles));

            // Seleccionar rol actual
            Rol rolActual = sessionManager.getRol();
            roleComboBox.setValue(rolActual);

        } else {
            roleSelectorContainer.setVisible(false);
        }
    }
    public void loadDashboardCoordinatorView() {
        loadView("/views/coordinator/dashboard_coordinator.fxml");
    }

    public void loadDashboardDocenteView() {
        loadView("/views/professor/dashboard_professor.fxml");
    }

    public void loadDashboardEstudianteView() {
        loadView("/views/student/dashboard_student.fxml");
    }

    public void loadDashboardJefeView() {
        loadView("/views/DepartmentHead/dashboard_DepartmentHead.fxml");
    }
    // ============ IMPLEMENTACIÓN OBSERVER ============

    @Override
    public void onUserLoggedIn(User user) {
        updateUIFromSession();
    }

    @Override
    public void onUserLoggedOut() {
        updateUIFromSession();
    }

    @Override
    public void onSessionUpdated(User user) {
        updateUIFromSession();
    }


    /**
     * Carga una vista dentro del área de contenido
     */
    void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Obtener el controlador
            Object controller = loader.getController();

            // Si el controlador extiende UIBase, inyectar MainController
            if (controller instanceof UIBase) {
                ((UIBase) controller).setMainController(this);
            }

            // Configurar otros controladores específicos si es necesario
            if (fxmlPath.contains("login.fxml") && controller instanceof LoginController) {
                LoginController loginController = (LoginController) controller;
                loginController.setMainController(this); // Si LoginController también extiende UIBase
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
        System.out.println("Cerrando sesión de: " + SessionManager.getInstance().getCurrentUser().getEmail());
        sessionManager.clearSession();

        loadView("/views/auth/login.fxml");
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

    public void handleRoleChange(ActionEvent actionEvent) {
            System.out.println("Role seleccionado: " + roleComboBox.getValue().toString());
            Rol nuevoRol = roleComboBox.getValue();
            if (nuevoRol != null) {
                sessionManager.cambiarRol(nuevoRol);

                // Cargar vista correspondiente al nuevo rol
                cargarVistaPorRol(nuevoRol);
            }
    }
}