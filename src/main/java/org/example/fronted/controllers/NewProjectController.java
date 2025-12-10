package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.KeyCode;

import org.example.fronted.api.UserApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewProjectController extends UIBase {

    // ========== FXML ==========
    @FXML private TextField tituloField;
    @FXML private TextField estudianteField;
    @FXML private VBox estudiantesSeleccionados;
    @FXML private VBox resultadosEstudiantes;

    @FXML private TextField directorField;
    @FXML private VBox resultadosDirectores;

    @FXML private TextField codirectorField;
    @FXML private VBox resultadosCodirectores;

    @FXML private ComboBox<String> programaCombo;
    @FXML private TextArea objetivoGeneralArea;
    @FXML private VBox objetivosEspecificosContainer;

    @FXML private RadioButton radioInvestigacion;
    @FXML private RadioButton radioPractica;

    // ========== VARIABLES ==========
    private final List<String> estudiantes = new ArrayList<>();
    private UserApi userApi;

    // ========== INIT ==========
    @FXML
    public void initialize() {
        userApi = new UserApi();

        estudianteField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                buscarEstudiantes(estudianteField.getText());
            }
        });

        directorField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                buscarDocentes(directorField.getText(), resultadosDirectores);
            }
        });

        codirectorField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                buscarDocentes(codirectorField.getText(), resultadosCodirectores);
            }
        });
    }

    /* --------- AUTOCOMPLETADO REAL ---------- */

    private void buscarEstudiantes(String query) {
        resultadosEstudiantes.getChildren().clear();
        resultadosEstudiantes.setVisible(true);
        resultadosEstudiantes.setManaged(true);

        Label loading = new Label("Buscando estudiantes...");
        resultadosEstudiantes.getChildren().add(loading);

        userApi.buscarUsuarios(query).subscribe(usuarios -> {
            Platform.runLater(() -> {
                resultadosEstudiantes.getChildren().clear();

                for (Map<String, Object> u : usuarios) {
                    String nombre = (String) u.get("nombreCompleto");
                    String email = (String) u.get("email");

                    // Filtrar solo estudiantes
                    String rol = (String) u.getOrDefault("rol", "ESTUDIANTE");
                    if (!rol.equalsIgnoreCase("ESTUDIANTE")) continue;

                    String texto = nombre + " (" + email + ")";
                    Label item = new Label(texto);
                    item.getStyleClass().add("cnp-result-item");
                    item.setOnMouseClicked(ev -> agregarEstudiante(texto));

                    resultadosEstudiantes.getChildren().add(item);
                }

                if (resultadosEstudiantes.getChildren().isEmpty()) {
                    resultadosEstudiantes.getChildren().add(
                            new Label("No se encontraron estudiantes")
                    );
                }
            });
        }, err -> {
            Platform.runLater(() -> {
                resultadosEstudiantes.getChildren().clear();
                resultadosEstudiantes.getChildren().add(
                        new Label("Error al buscar estudiantes")
                );
            });
        });
    }

    private void buscarDocentes(String query, VBox cont) {
        cont.getChildren().clear();
        cont.setVisible(true);
        cont.setManaged(true);

        Label loading = new Label("Buscando docentes...");
        cont.getChildren().add(loading);

        userApi.buscarUsuarios(query).subscribe(usuarios -> {
            Platform.runLater(() -> {
                cont.getChildren().clear();

                for (Map<String, Object> u : usuarios) {
                    String nombre = (String) u.get("nombreCompleto");
                    String email = (String) u.get("email");

                    // Filtrar solo docentes
                    String rol = (String) u.getOrDefault("rol", "DOCENTE");
                    if (!rol.equalsIgnoreCase("DOCENTE")) continue;

                    String texto = nombre + " (" + email + ")";
                    Label item = new Label(texto);
                    item.getStyleClass().add("cnp-result-item");
                    item.setOnMouseClicked(ev -> seleccionarCampo(cont, texto));

                    cont.getChildren().add(item);
                }

                if (cont.getChildren().isEmpty()) {
                    cont.getChildren().add(
                            new Label("No se encontraron docentes")
                    );
                }
            });
        }, err -> {
            Platform.runLater(() -> {
                cont.getChildren().clear();
                cont.getChildren().add(
                        new Label("Error al buscar docentes")
                );
            });
        });
    }

    private void seleccionarCampo(VBox cont, String valor) {
        if (cont == resultadosDirectores) {
            directorField.setText(valor);
        } else {
            codirectorField.setText(valor);
        }

        cont.setVisible(false);
        cont.setManaged(false);
    }

    /* --------- ESTUDIANTES (TAGS) ---------- */

    private void agregarEstudiante(String nombre) {
        if (estudiantes.size() >= 2) return;

        if (estudiantes.contains(nombre)) return;

        estudiantes.add(nombre);

        HBox tag = new HBox();
        tag.getStyleClass().add("cnp-tag");

        Label label = new Label(nombre);
        Button remove = new Button("x");
        remove.setOnAction(e -> {
            estudiantes.remove(nombre);
            estudiantesSeleccionados.getChildren().remove(tag);
        });

        tag.getChildren().addAll(label, remove);
        estudiantesSeleccionados.getChildren().add(tag);

        resultadosEstudiantes.setVisible(false);
        resultadosEstudiantes.setManaged(false);
        estudianteField.clear();
    }

    /* --------- OBJETIVOS ---------- */

    @FXML
    private void agregarObjetivo() {
        TextArea area = new TextArea();
        area.setPromptText("Objetivo específico...");
        area.getStyleClass().add("cnp-textarea-small");
        objetivosEspecificosContainer.getChildren().add(area);
    }

    /* --------- NAVEGACIÓN / ACCIONES ---------- */

    @FXML
    private void regresar() {
        loadView("/views/coordinator/dashboard_coordinator.fxml");
    }

    @FXML
    private void cancelar() {
        // Podrías cerrar modal o limpiar formulario
    }

    @FXML
    private void crearProyecto() {
        // Ejemplo de validación simple
        if (tituloField.getText().isEmpty()) {
            mostrarAlerta("Error", "Debe ingresar el título del proyecto", Alert.AlertType.WARNING);
            return;
        }

        if (estudiantes.isEmpty()) {
            mostrarAlerta("Error", "Debe seleccionar al menos un estudiante", Alert.AlertType.WARNING);
            return;
        }

        System.out.println("Proyecto creado:");
        System.out.println("Titulo: " + tituloField.getText());
        System.out.println("Estudiantes: " + estudiantes);
        System.out.println("Director: " + directorField.getText());
    }

    /* ----- PRECARGA ----- */

    public void precargar(String titulo, List<String> ests, String director) {
        if (titulo != null) tituloField.setText(titulo);
        if (director != null) directorField.setText(director);
        if (ests != null) ests.forEach(this::agregarEstudiante);
    }

    /* --------- UTILIDAD ALERTA ---------- */

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
