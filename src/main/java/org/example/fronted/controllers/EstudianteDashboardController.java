package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import org.example.fronted.util.SessionManager;

public class EstudianteDashboardController extends UIBase{

    @FXML private Label estadoActualLabel;
    @FXML private Label proyectoTituloLabel;
    @FXML private Label directorLabel;
    @FXML private Label modalidadLabel;
    @FXML private Label fechaActualizacionLabel;
    @FXML private Label proximoPasoLabel;
    @FXML private Label fechaEvaluacion1Label;
    @FXML private Label directorContactLabel;

    @FXML private VBox observacionesList;
    @FXML private VBox noObservationsBox;
    @FXML private VBox notificationsList;

    @FXML private Line timelineLine2;
    @FXML private Line timelineLine3;
    @FXML private Line timelineLine4;

    @FXML private Circle attemptDot2;
    @FXML private Circle attemptDot3;

    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        cargarDatosEstudiante();
        configurarTimeline();
        cargarObservaciones();
        cargarNotificaciones();
    }

    private void cargarDatosEstudiante() {
        // Aquí se cargarían los datos reales desde el backend
        estadoActualLabel.setText("Formato A en evaluación");
        proyectoTituloLabel.setText("Sistema de Gestión de Proyectos de Grado");
        directorLabel.setText("Director: Dr. Carlos Mendoza");
        modalidadLabel.setText("Modalidad: Investigación");
        fechaActualizacionLabel.setText("15 Nov 2025");
        proximoPasoLabel.setText("Esperar evaluación del coordinador");
        fechaEvaluacion1Label.setText("En evaluación");
        directorContactLabel.setText("Dr. Carlos Mendoza - cmendoza@unicauca.edu.co");
    }

    private void configurarTimeline() {
        // Aquí se configuraría el timeline según el estado real del proyecto
        // Por ejemplo, si el proyecto está en evaluación 2, marcar etapas anteriores como completadas
    }

    private void cargarObservaciones() {
        // Cargar observaciones desde el backend
        // Si no hay observaciones, mostrar el mensaje de "no hay observaciones"
        boolean tieneObservaciones = false; // Cambiar según datos reales

        if (tieneObservaciones) {
            noObservationsBox.setVisible(false);
            noObservationsBox.setManaged(false);
        } else {
            noObservationsBox.setVisible(true);
            noObservationsBox.setManaged(true);
        }
    }

    private void cargarNotificaciones() {
        // Cargar notificaciones desde el backend
        // Por ahora se muestran notificaciones de ejemplo
    }

    @FXML
    private void verTodasNotificaciones() {
        System.out.println("Navegando a todas las notificaciones...");
        // loadView("/views/estudiante/todas_notificaciones.fxml");
    }

    // Método para actualizar intentos de Formato A
    private void actualizarIntentos(int intentoActual) {
        // Reiniciar todos los puntos
        attemptDot2.getStyleClass().remove("dot-active");
        attemptDot3.getStyleClass().remove("dot-active");

        // Activar puntos según el intento actual
        if (intentoActual >= 2) {
            attemptDot2.getStyleClass().add("dot-active");
        }
        if (intentoActual >= 3) {
            attemptDot3.getStyleClass().add("dot-active");
        }
    }

    // Método para actualizar timeline según estado
    private void actualizarTimeline(String estado) {
        // Lógica para actualizar colores y estados del timeline
        // basado en el estado actual del proyecto
    }
}