package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.example.fronted.api.ProjectApi;
import org.example.fronted.dto.EvaluarFormatoADTO;
import org.example.fronted.util.PdfViewerUtil;
import org.example.fronted.util.SessionManager;

public class EvaluarFormatoAController extends UIBase {

    @FXML private Label tituloProyectoLabel;
    @FXML private Label modalidadLabel;
    @FXML private Label estudianteLabel;
    @FXML private Label directorLabel;
    @FXML private TextArea observacionesTextArea;

    private ProjectApi projectApi;
    private Long proyectoId;

    @FXML
    public void initialize() {
        projectApi = new ProjectApi();
        cargarDatosSimulados();
    }

    /**
     * Método para establecer los datos reales del proyecto
     */
    public void setProyectoData(Long proyectoId, String titulo, String modalidad,
                                String estudiante, String director) {
        this.proyectoId = proyectoId;

        tituloProyectoLabel.setText(titulo);
        modalidadLabel.setText(modalidad);
        estudianteLabel.setText(estudiante);
        directorLabel.setText(director);
    }

    private void cargarDatosSimulados() {
        // Datos de ejemplo (deberían venir del controlador anterior)
        this.proyectoId = 1L;
        tituloProyectoLabel.setText("Sistema de Gestión Académica");
        modalidadLabel.setText("Trabajo de Grado");
        estudianteLabel.setText("Juan Pérez");
        directorLabel.setText("Dra. Marcela López");
    }

    // ========================== ACCIONES =============================

    @FXML
    private void aprobarFormatoA() {
        if (observacionesTextArea.getText().trim().isEmpty()) {
            showAlert("Error", "Debe ingresar observaciones antes de aprobar", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar aprobación");
        confirmacion.setHeaderText("¿Está seguro de aprobar este Formato A?");
        confirmacion.setContentText("Esta acción notificará al estudiante y docente.");

        if (confirmacion.showAndWait().orElse(null) != javafx.scene.control.ButtonType.OK) {
            return;
        }

        evaluarFormatoA(true);
    }

    @FXML
    private void rechazarFormatoA() {
        if (observacionesTextArea.getText().trim().isEmpty()) {
            showAlert("Error", "Debe ingresar observaciones antes de rechazar", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar rechazo");
        confirmacion.setHeaderText("¿Está seguro de rechazar este Formato A?");
        confirmacion.setContentText("Esta acción notificará al estudiante y docente.");

        if (confirmacion.showAndWait().orElse(null) != javafx.scene.control.ButtonType.OK) {
            return;
        }

        evaluarFormatoA(false);
    }

    private void evaluarFormatoA(boolean aprobado) {
        if (proyectoId == null) {
            showAlert("Error", "No se ha identificado el proyecto a evaluar", Alert.AlertType.ERROR);
            return;
        }

        EvaluarFormatoADTO dto = new EvaluarFormatoADTO();
        dto.setIdProyecto(proyectoId);
        dto.setAprobado(aprobado);
        dto.setObservaciones(observacionesTextArea.getText().trim());

        // Obtener email del evaluador (usuario actual)
        String evaluadorEmail = SessionManager.getInstance().getCurrentUser() != null ?
                SessionManager.getInstance().getCurrentUser().getEmail() :
                "coordinador@unicauca.edu.co";
        dto.setEvaluadorEmail(evaluadorEmail);

        // Enviar al servidor
        projectApi.evaluarFormatoA(dto)
                .subscribe(
                        success -> {
                            javafx.application.Platform.runLater(() -> {
                                if (success) {
                                    String mensaje = aprobado ?
                                            "Formato A aprobado exitosamente. Se ha notificado a los involucrados." :
                                            "Formato A rechazado. Se ha notificado a los involucrados.";

                                    showAlert(aprobado ? "Aprobado" : "Rechazado",
                                            mensaje, Alert.AlertType.INFORMATION);

                                    // Regresar a la pantalla anterior
                                    regresar();
                                } else {
                                    showAlert("Error",
                                            "No se pudo enviar la evaluación. Intente nuevamente.",
                                            Alert.AlertType.ERROR);
                                }
                            });
                        },
                        error -> {
                            javafx.application.Platform.runLater(() -> {
                                showAlert("Error",
                                        "Error al evaluar: " + error.getMessage(),
                                        Alert.AlertType.ERROR);
                            });
                        }
                );
    }

    @FXML
    private void verPDF() {
        // Aquí necesitarías obtener el documentoId del proyecto
        // Por ahora usamos un ejemplo
        Long documentoIdEjemplo = 123L;

        // Usar el nuevo método que se conecta al servidor
        String nombreArchivo = "FormatoA_" + tituloProyectoLabel.getText() + ".pdf";
        PdfViewerUtil.mostrarPDFDesdeServidor(documentoIdEjemplo, nombreArchivo);
    }

    @FXML
    private void regresar() {
        loadView("/views/coordinator/dashboard_coordinator.fxml");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}