package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class NewProjectController extends UIBase{

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

    private final List<String> estudiantes = new ArrayList<>();


    @FXML
    public void initialize() {
        estudianteField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                buscarEstudiantes(estudianteField.getText());
        });

        directorField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                buscarDocentes(directorField.getText(), resultadosDirectores);
        });

        codirectorField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                buscarDocentes(codirectorField.getText(), resultadosCodirectores);
        });
    }

    /* --------- AUTOCOMPLETADO ---------- */

    private void buscarEstudiantes(String query) {
        resultadosEstudiantes.getChildren().clear();

        // Aquí iría la consulta a BD
        for (int i = 0; i < 5; i++) {
            String r = query + " Estudiante " + (i + 1);
            Label item = new Label(r);
            item.getStyleClass().add("cnp-result-item");
            item.setOnMouseClicked(ev -> agregarEstudiante(r));
            resultadosEstudiantes.getChildren().add(item);
        }

        resultadosEstudiantes.setVisible(true);
        resultadosEstudiantes.setManaged(true);
    }

    private void buscarDocentes(String q, VBox cont) {
        cont.getChildren().clear();

        for (int i = 0; i < 5; i++) {
            String r = q + " Docente " + (i + 1);
            Label item = new Label(r);
            item.setOnMouseClicked(ev -> seleccionarCampo(q, r));
            cont.getChildren().add(item);
        }

        cont.setVisible(true);
        cont.setManaged(true);
    }

    private void seleccionarCampo(String tipo, String valor) {
        if (tipo.equals(directorField.getText())) directorField.setText(valor);
        else codirectorField.setText(valor);
    }

    /* --------- ESTUDIANTES (TAGS) ---------- */

    private void agregarEstudiante(String nombre) {
        if (estudiantes.size() >= 2) return;

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

    @FXML private void regresar() {
        loadView("/views/coordinator/dashboard_coordinator.fxml");
    }

    @FXML private void cancelar() { /* Cerrar ventana */ }

    @FXML private void crearProyecto() {
        // Validar y enviar
        System.out.println("Proyecto creado.");
    }

    /* ----- RECIBIR DATOS PREV-CARGADOS ----- */
    public void precargar(String titulo, List<String> ests, String director) {
        if (titulo != null) tituloField.setText(titulo);
        if (director != null) directorField.setText(director);
        if (ests != null) ests.forEach(this::agregarEstudiante);
    }
}

