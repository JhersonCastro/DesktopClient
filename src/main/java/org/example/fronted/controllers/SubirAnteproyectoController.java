package org.example.fronted.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.example.fronted.api.ProjectApi;
import org.example.fronted.dto.SubirAnteproyectoDTO;
import org.example.fronted.util.SessionManager;

import java.io.File;

public class SubirAnteproyectoController extends UIBase {

    @FXML private Label proyectoTitleLabel;
    @FXML private Label estudianteLabel;
    @FXML private Label directorLabel;
    @FXML private TextField archivoPrincipalField;
    @FXML private TextField anexosField;
    @FXML private TextArea observacionesArea;

    private File archivoPrincipal;
    private File archivoAnexos;
    private Long proyectoId; // ID del proyecto al que pertenece
    private ProjectApi projectApi;

    // Este método debe llamarse desde el controlador que abre esta vista
    public void setProyectoData(Long proyectoId, String titulo, String estudiante, String director) {
        this.proyectoId = proyectoId;
        this.projectApi = new ProjectApi();

        proyectoTitleLabel.setText(titulo);
        estudianteLabel.setText("Estudiante: " + estudiante);
        directorLabel.setText("Director: " + director);
    }

    @FXML
    public void seleccionarArchivoPrincipal(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Anteproyecto Principal");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );

        File archivo = fileChooser.showOpenDialog(archivoPrincipalField.getScene().getWindow());
        if (archivo != null) {
            archivoPrincipalField.setText(archivo.getAbsolutePath());
            archivoPrincipal = archivo;
        }
    }

    @FXML
    public void seleccionarAnexos(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Anexos (opcional)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Archivos ZIP", "*.zip"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File archivo = fileChooser.showOpenDialog(anexosField.getScene().getWindow());
        if (archivo != null) {
            anexosField.setText(archivo.getAbsolutePath());
            archivoAnexos = archivo;
        }
    }

    @FXML
    public void cancelar(ActionEvent actionEvent) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cancelación");
        confirmacion.setHeaderText("¿Desea cancelar la subida del anteproyecto?");
        confirmacion.setContentText("Se perderán los archivos seleccionados.");

        if (confirmacion.showAndWait().orElse(null) == ButtonType.OK) {
            regresarAlDashboard(actionEvent);
        }
    }

    @FXML
    public void guardarBorrador(ActionEvent actionEvent) {
        // Aquí podrías guardar localmente o en una base temporal
        mostrarAlerta("Borrador guardado",
                "Los archivos se han guardado como borrador. Puede continuar más tarde.",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    public void enviarAnteproyecto(ActionEvent actionEvent) {
        // Validaciones
        if (proyectoId == null) {
            mostrarAlerta("Error", "No se ha identificado el proyecto", Alert.AlertType.ERROR);
            return;
        }

        if (archivoPrincipal == null) {
            mostrarAlerta("Error", "Debe seleccionar el archivo principal del anteproyecto",
                    Alert.AlertType.ERROR);
            return;
        }

        // Confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar envío");
        confirmacion.setHeaderText("¿Está seguro de enviar el anteproyecto?");
        confirmacion.setContentText("El anteproyecto será enviado al jefe de departamento para evaluación.");

        if (confirmacion.showAndWait().orElse(null) != ButtonType.OK) {
            return;
        }

        // Enviar al microservicio
        new Thread(() -> {
            boolean exito = enviarAnteproyectoAlServidor();

            javafx.application.Platform.runLater(() -> {
                if (exito) {
                    mostrarAlerta("Éxito",
                            "Anteproyecto enviado correctamente. Se notificará al jefe de departamento.",
                            Alert.AlertType.INFORMATION);
                    regresarAlDashboard(actionEvent);
                } else {
                    mostrarAlerta("Error",
                            "No se pudo enviar el anteproyecto. Intente nuevamente.",
                            Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    private boolean enviarAnteproyectoAlServidor() {
        try {
            SubirAnteproyectoDTO dto = new SubirAnteproyectoDTO();
            dto.setIdProyecto(proyectoId);
            dto.setAnteproyectoPdf(archivoPrincipal);

            // Obtener email del jefe de departamento (aquí necesitas lógica específica)
            // Por ahora, obtén el email del usuario logueado si es jefe
            String jefeEmail = obtenerEmailJefeDepartamento();
            dto.setJefeDepartamentoEmail(jefeEmail);

            // Llamada al API
            return projectApi.subirAnteproyecto(dto).block(); // block() porque estamos en hilo aparte

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String obtenerEmailJefeDepartamento() {
        // Aquí necesitas lógica para obtener el email del jefe de departamento
        // Opciones:
        // 1. Del usuario logueado si tiene rol JEFE_DEPARTAMENTO
        // 2. De una lista predefinida
        // 3. De una llamada a otro servicio

        // Por ahora, retorna el email del usuario logueado
        if (SessionManager.getInstance().getCurrentUser() != null) {
            return SessionManager.getInstance().getCurrentUser().getEmail();
        }
        return "jefe.departamento@unicauca.edu.co"; // Fallback
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    public void regresarAlDashboard(ActionEvent actionEvent) {
        loadView("/views/professor/dashboard_professor.fxml");
    }
}