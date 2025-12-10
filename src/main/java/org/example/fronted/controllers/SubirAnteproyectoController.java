package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.example.fronted.api.ProyectoApi;
import org.example.fronted.models.ProyectoGrado;
import org.example.fronted.util.SessionManager;
import org.springframework.core.io.ByteArrayResource;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SubirAnteproyectoController extends UIBase implements Initializable {

    // ====== FXML (coinciden con tu subir_anteproyecto.fxml) ======

    @FXML
    private ComboBox<ProyectoGrado> proyectoComboBox;

    @FXML
    private TextField tituloTextField;

    @FXML
    private TextField estudianteTextField;

    @FXML
    private TextField modalidadTextField;

    @FXML
    private TextField tituloAnteproyectoTextField;

    @FXML
    private TextArea resumenTextArea;

    @FXML
    private TextField palabrasClaveTextField;

    @FXML
    private TextField archivoPrincipalTextField;

    @FXML
    private TextField anexosTextField;

    @FXML
    private DatePicker fechaEstimadaDatePicker;

    // ====== Estado interno ======

    private final ProyectoApi proyectoApi = new ProyectoApi();
    private final SessionManager sessionManager = SessionManager.getInstance();

    private File archivoPrincipal;
    // (los anexos de momento solo se muestran, no se envían al backend)
    private List<File> anexosSeleccionados;

    // ====== Ciclo de vida ======

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarProyectosAprobados();
        configurarListenerProyecto();
    }

    /**
     * Carga proyectos del ESTUDIANTE actual y filtra solo los que tienen
     * Formato A aprobado.
     */
    private void cargarProyectosAprobados() {
        String emailEstudiante = sessionManager.getCurrentUser().getEmail();

        Mono<List<ProyectoGrado>> mono = proyectoApi.obtenerPorEstudiante(emailEstudiante);

        mono.subscribe(proyectos -> {
            List<ProyectoGrado> aprobados = proyectos.stream()
                    .filter(this::tieneFormatoAAprobado)
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                proyectoComboBox.getItems().clear();
                proyectoComboBox.getItems().addAll(aprobados);
            });

        }, error -> {
            error.printStackTrace();
            Platform.runLater(() ->
                    mostrarAlerta("Error",
                            "Error al cargar proyectos aprobados: " + error.getMessage(),
                            Alert.AlertType.ERROR)
            );
        });
    }

    /**
     * Regla simple: consideramos Formato A aprobado si el estadoActual
     * es exactamente "FORMATO_A_APROBADO" (ajusta al valor real que maneje tu backend).
     */
    private boolean tieneFormatoAAprobado(ProyectoGrado p) {
        return p.getEstadoActual() != null
                && p.getEstadoActual().equalsIgnoreCase("FORMATO_A_APROBADO");
    }

    /**
     * Cuando se selecciona un proyecto en el ComboBox, rellenamos los campos
     * de título, estudiante y modalidad (solo lectura).
     */
    private void configurarListenerProyecto() {
        proyectoComboBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, nuevo) -> {
                    if (nuevo != null) {
                        tituloTextField.setText(nuevo.getTitulo());
                        estudianteTextField.setText(
                                nuevo.getEstudiante1Email() != null ? nuevo.getEstudiante1Email() : ""
                        );
                        modalidadTextField.setText(nuevo.getModalidad());
                    } else {
                        tituloTextField.clear();
                        estudianteTextField.clear();
                        modalidadTextField.clear();
                    }
                });
    }

    // ====== Métodos llamados desde el FXML ======

    @FXML
    public void seleccionarArchivoPrincipal(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar anteproyecto (PDF)");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            archivoPrincipal = file;
            archivoPrincipalTextField.setText(file.getName());
        }
    }

    @FXML
    public void seleccionarAnexos(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar anexos");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("ZIP", "*.zip"),
                new FileChooser.ExtensionFilter("DOCX", "*.docx")
        );
        List<File> files = chooser.showOpenMultipleDialog(null);
        if (files != null && !files.isEmpty()) {
            anexosSeleccionados = files;
            String nombres = files.stream()
                    .map(File::getName)
                    .collect(Collectors.joining(", "));
            anexosTextField.setText(nombres);
        }
    }

    @FXML
    public void cancelar(ActionEvent actionEvent) {
        limpiarFormulario();
        loadView("/views/professor/dashboard_professor.fxml");
    }

    @FXML
    public void guardarBorrador(ActionEvent actionEvent) {
        // Por ahora no hay endpoint para borradores → solo mostramos un mensaje
        mostrarAlerta("Borrador",
                "La opción de guardar borrador aún no está implementada en el backend.",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    public void enviarAnteproyecto(ActionEvent actionEvent) {
        if (!validarFormulario()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar envío");
        confirm.setHeaderText("¿Desea enviar el anteproyecto?");
        confirm.setContentText("Una vez enviado, no podrá modificarlo hasta la evaluación.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        new Thread(() -> {
            boolean ok = procesarEnvioAnteproyecto();

            Platform.runLater(() -> {
                if (ok) {
                    mostrarAlerta("Éxito",
                            "Anteproyecto enviado correctamente. Se ha notificado al jefe de departamento.",
                            Alert.AlertType.INFORMATION);
                    limpiarFormulario();
                    loadView("/views/professor/dashboard_professor.fxml");
                } else {
                    mostrarAlerta("Error",
                            "No se pudo enviar el anteproyecto. Intente nuevamente.",
                            Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    @FXML
    public void regresarAlDashboard(ActionEvent actionEvent) {
        loadView("/views/professor/dashboard_professor.fxml");
    }

    // ====== Lógica interna ======

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (proyectoComboBox.getValue() == null) {
            errores.append("- Debe seleccionar un proyecto.\n");
        }
        if (tituloAnteproyectoTextField.getText() == null ||
                tituloAnteproyectoTextField.getText().isBlank()) {
            errores.append("- El título del anteproyecto es obligatorio.\n");
        }
        if (resumenTextArea.getText() == null || resumenTextArea.getText().isBlank()) {
            errores.append("- El resumen ejecutivo es obligatorio.\n");
        }
        if (palabrasClaveTextField.getText() == null ||
                palabrasClaveTextField.getText().isBlank()) {
            errores.append("- Las palabras clave son obligatorias.\n");
        }
        if (archivoPrincipal == null) {
            errores.append("- Debe seleccionar el documento principal (PDF).\n");
        }
        if (fechaEstimadaDatePicker.getValue() == null) {
            errores.append("- Debe seleccionar una fecha estimada de finalización.\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Formulario incompleto",
                    errores.toString(),
                    Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    /**
     * Llama a ProyectoApi.subirAnteproyecto(...)
     */
    private boolean procesarEnvioAnteproyecto() {
        try {
            ProyectoGrado seleccionado = proyectoComboBox.getValue();
            if (seleccionado == null) return false;

            Long idProyecto = seleccionado.getId();

            byte[] bytes = Files.readAllBytes(archivoPrincipal.toPath());
            ByteArrayResource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return archivoPrincipal.getName();
                }
            };

            // TODO: aquí deberías poner el email REAL del jefe de departamento.
            // De momento uso el email del usuario actual como placeholder.
            String jefeEmail = sessionManager.getCurrentUser().getEmail();


            Mono<Void> mono = proyectoApi.subirAnteproyecto(idProyecto, jefeEmail, resource);

            // bloquea en el hilo secundario (no en el hilo de JavaFX)
            mono.block();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void limpiarFormulario() {
        proyectoComboBox.getSelectionModel().clearSelection();
        tituloTextField.clear();
        estudianteTextField.clear();
        modalidadTextField.clear();
        tituloAnteproyectoTextField.clear();
        resumenTextArea.clear();
        palabrasClaveTextField.clear();
        archivoPrincipalTextField.clear();
        anexosTextField.clear();
        fechaEstimadaDatePicker.setValue(null);
        archivoPrincipal = null;
        anexosSeleccionados = null;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
