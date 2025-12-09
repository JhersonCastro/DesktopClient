package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import org.example.fronted.api.ProyectoApi;
import org.example.fronted.dto.ProjectCardDTO;
import org.example.fronted.models.ProyectoGrado;
import org.example.fronted.util.SessionManager;

public class DashboardCoordinadorController extends UIBase{

    @FXML private Label pendientesCount;
    @FXML private Label aprobadosCount;
    @FXML private Label rechazadosCount;
    ProyectoApi proyectoApi = new ProyectoApi();
    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        loadStatistics();
    }

    private void loadStatistics() {

        proyectoApi.obtenerProyectosPendientes()
                .subscribe(
                        lista -> Platform.runLater(() -> {
                            pendientesCount.setText("" + lista.size());
                        }),
                        error -> {
                            error.printStackTrace();
                            Platform.runLater(() ->
                                    System.out.println("Error al cargar proyectos pendientes: " + error.getMessage())
                            );
                        }
                );

        proyectoApi.obtenerProyectosAprobados()
                .subscribe(
                        lista -> Platform.runLater(() -> {
                            aprobadosCount.setText("" + lista.size());
                        }),
                        error -> {
                            error.printStackTrace();
                            Platform.runLater(() ->
                                    System.out.println("Error al cargar proyectos aprobados: " + error.getMessage())
                            );
                        }
                );

        proyectoApi.obtenerProyectosRechazados()
                .subscribe(
                        lista -> Platform.runLater(() -> {
                            rechazadosCount.setText("" + lista.size());
                        }),
                        error -> {
                            error.printStackTrace();
                            Platform.runLater(() ->
                                    System.out.println("Error al cargar proyectos rechazados: " + error.getMessage())
                            );
                        }
                );
    }


    // ============ HANDLERS DE BOTONES ============

    @FXML
    private void verProyectosPendientes() {
        //showAlert("Navegación", "Mostrando proyectos pendientes...");
        System.out.println("Mostrando proyectos pendientes...");
        // Aquí iría la navegación a la vista detallada de proyectos pendientes
        loadView("/views/coordinator/pendientes_list.fxml");
    }

    @FXML
    private void verProyectosAprobados() {
        //showAlert("Navegación", "Mostrando proyectos aprobados...");
        // Aquí iría la navegación a la vista detallada de proyectos aprobados
        loadView("/views/coordinator/aprobados_list.fxml");
    }

    @FXML
    private void verProyectosRechazados() {
        //showAlert("Navegación", "Mostrando proyectos rechazados...");
        // Aquí iría la navegación a la vista detallada de proyectos rechazados
        loadView("/views/coordinator/rechazados_list.fxml");
    }

    @FXML
    private void nuevoProyecto() {
        showAlert("Nuevo Proyecto", "Creando nuevo proyecto...");
        // Aquí iría la lógica para crear un nuevo proyecto
        loadView("/views/coordinator/project_new.fxml");
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