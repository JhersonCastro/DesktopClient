package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import org.example.fronted.util.SessionManager;

public class DashboardCoordinadorController {

    @FXML private Label pendientesCount;
    @FXML private Label aprobadosCount;
    @FXML private Label rechazadosCount;

    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        loadStatistics();
    }

    private void loadStatistics() {
        // Aquí iría la lógica para cargar las estadísticas reales desde el backend
        // Por ahora, simulamos datos
        pendientesCount.setText("12");
        aprobadosCount.setText("45");
        rechazadosCount.setText("8");
    }

    // ============ HANDLERS DE BOTONES ============

    @FXML
    private void verProyectosPendientes() {
        showAlert("Navegación", "Mostrando proyectos pendientes...");
        // Aquí iría la navegación a la vista detallada de proyectos pendientes
    }

    @FXML
    private void verProyectosAprobados() {
        showAlert("Navegación", "Mostrando proyectos aprobados...");
        // Aquí iría la navegación a la vista detallada de proyectos aprobados
    }

    @FXML
    private void verProyectosRechazados() {
        showAlert("Navegación", "Mostrando proyectos rechazados...");
        // Aquí iría la navegación a la vista detallada de proyectos rechazados
    }

    @FXML
    private void nuevoProyecto() {
        showAlert("Nuevo Proyecto", "Creando nuevo proyecto...");
        // Aquí iría la lógica para crear un nuevo proyecto
    }

    @FXML
    private void generarReportes() {
        showAlert("Reportes", "Generando reportes...");
        // Aquí iría la lógica para generar reportes
    }

    @FXML
    private void abrirConfiguracion() {
        showAlert("Configuración", "Abriendo configuración...");
        // Aquí iría la lógica para abrir la configuración
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}