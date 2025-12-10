package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;

import org.example.fronted.api.UserApi;
import org.example.fronted.api.ProyectoApi;

import java.util.Map;
import java.util.Optional;

public class EvaluatorAssignmentController extends UIBase {

    @FXML
    private ScrollPane scrollPane;



    @FXML
    private TextField evaluador1TextField;

    @FXML
    private TextField evaluador2TextField;

    @FXML
    private VBox resultadosEvaluador1;

    @FXML
    private VBox resultadosEvaluador2;

    // Variables para almacenar selecciones
    private String anteproyectoSeleccionadoId;
    private String evaluador1SeleccionadoEmail;
    private String evaluador2SeleccionadoEmail;

    private UserApi userApi;
    private ProyectoApi proyectoApi;


    Long ProyectoId;
    String JefeEmail;
    @Override
    public void setArgs(Object... args){
        ProyectoId =(Long)args[0];
        JefeEmail = (String)args[1];
        userApi = new UserApi();
        proyectoApi = new ProyectoApi();

        // Configurar búsquedas con ENTER
        evaluador1TextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                buscarEvaluadores(evaluador1TextField.getText(), resultadosEvaluador1, 1);
            }
        });

        evaluador2TextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                buscarEvaluadores(evaluador2TextField.getText(), resultadosEvaluador2, 2);
            }
        });


        // Ocultar resultados cuando se hace clic fuera
        setupClickOutsideListener();
    }
    /* --------- AUTOCOMPLETADO EVALUADORES ---------- */

    private void buscarEvaluadores(String query, VBox contenedorResultados, int numeroEvaluador) {
        if (query == null || query.trim().isEmpty()) {
            ocultarResultados(contenedorResultados);
            return;
        }

        mostrarCargando(contenedorResultados, "Buscando evaluadores...");

        userApi.buscarUsuarios(query).subscribe(usuarios -> Platform.runLater(() -> {
            contenedorResultados.getChildren().clear();

            boolean encontrados = false;
            for (Map<String, Object> u : usuarios) {
                String nombre = (String) u.get("nombreCompleto");
                String email = (String) u.get("email");
                String rol = (String) u.getOrDefault("rol", "DOCENTE");


                encontrados = true;
                String texto = nombre + " (" + email + ")";
                Label item = new Label(texto);
                item.getStyleClass().add("ev-result-item");
                item.setOnMouseClicked(ev -> seleccionarEvaluador(
                        contenedorResultados, texto, email, numeroEvaluador
                ));

                contenedorResultados.getChildren().add(item);
            }

            if (!encontrados) {
                Label noResults = new Label("No se encontraron docentes");
                noResults.getStyleClass().add("ev-result-item");
                contenedorResultados.getChildren().add(noResults);
            }

            mostrarResultados(contenedorResultados);
        }), err -> Platform.runLater(() -> {
            contenedorResultados.getChildren().clear();
            Label error = new Label("Error al buscar evaluadores");
            error.getStyleClass().add("ev-result-item");
            contenedorResultados.getChildren().add(error);
            mostrarResultados(contenedorResultados);
        }));
    }

    private void seleccionarEvaluador(VBox contenedor, String texto, String email, int numeroEvaluador) {
        switch (numeroEvaluador) {
            case 1:
                evaluador1TextField.setText(texto);
                evaluador1SeleccionadoEmail = email;
                break;
            case 2:
                evaluador2TextField.setText(texto);
                evaluador2SeleccionadoEmail = email;
                break;
        }

        ocultarResultados(contenedor);
    }

    /* --------- BÚSQUEDA DE ANTEPROYECTOS ---------- */

    private void buscarAnteproyectos(String query) {
        if (query == null || query.trim().isEmpty()) return;

        // Aquí implementarías la búsqueda de anteproyectos
        // Por ahora, mostraremos un ejemplo
        mostrarAlertaInfo("Búsqueda de anteproyectos",
                "Buscaría anteproyectos con: " + query + "\n" +
                        "Implementa esta función según tu API");
    }

    /* --------- MÉTODOS AUXILIARES ---------- */

    private void mostrarCargando(VBox contenedor, String mensaje) {
        contenedor.getChildren().clear();
        Label loading = new Label(mensaje);
        loading.getStyleClass().add("ev-result-item");
        contenedor.getChildren().add(loading);
        mostrarResultados(contenedor);
    }

    private void mostrarResultados(VBox contenedor) {
        contenedor.setVisible(true);
        contenedor.setManaged(true);
    }

    private void ocultarResultados(VBox contenedor) {
        contenedor.setVisible(false);
        contenedor.setManaged(false);
    }

    private void setupClickOutsideListener() {
        // Ocultar resultados cuando se hace clic en cualquier lugar
        scrollPane.setOnMouseClicked(e -> {
            ocultarResultados(resultadosEvaluador1);
            ocultarResultados(resultadosEvaluador2);
        });
    }

    /* --------- MÉTODOS EXISTENTES ---------- */

    @FXML
    private void regresarAlDashboard() {
        loadView("/views/DepartmentHead/dashboard_DepartmentHead.fxml");
    }

    @FXML
    private void confirmarSeleccion() {
        if (!validarSeleccion()) return;

        mostrarAlertaInfo("Selección confirmada",
                "Anteproyecto: " + ProyectoId + "\n" +
                        "Evaluador 1: " + evaluador1TextField.getText() + "\n" +
                        "Evaluador 2: " + evaluador2TextField.getText() + "\n\n" +
                        "Puede proceder a asignar los evaluadores.");
    }
    // En EvaluatorAssignmentController.java
    @FXML
    private void asignarEvaluadores() {
        if (!validarSeleccion()) return;

        Alert loading = new Alert(Alert.AlertType.INFORMATION);
        loading.setTitle("Asignando evaluadores");
        loading.setHeaderText(null);
        loading.setContentText("Por favor espere...");
        loading.show();

        proyectoApi.asignarEvaluadores(ProyectoId, JefeEmail, evaluador1SeleccionadoEmail, evaluador2SeleccionadoEmail)
                .subscribe(
                        response -> Platform.runLater(() -> {
                            loading.close();
                            mostrarAlertaInfo("Asignación exitosa",
                                    "✅ Evaluadores asignados correctamente al proyecto:\n\n" +
                                            "• Id: " + ProyectoId + "\n" +
                                            "• Evaluador 1: " + evaluador1TextField.getText() + "\n" +
                                            "• Evaluador 2: " + evaluador2TextField.getText() + "\n\n" +
                                            "Se enviarán notificaciones a los docentes seleccionados.");

                            limpiarFormulario();
                            loadView("/views/DepartmentHead/dashboard_DepartmentHead.fxml");
                        }),
                        error -> Platform.runLater(() -> {
                            loading.close();
                            mostrarAlertaError("Error en asignación",
                                    "❌ No se pudieron asignar los evaluadores:\n\n" +
                                            error.getMessage());
                        })
                );
    }
    @FXML
    private void cancelar() {
        Optional<ButtonType> result = mostrarAlertaConfirmacion(
                "Cancelar asignación",
                "¿Desea cancelar la operación? Se perderán los datos ingresados."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            limpiarFormulario();
            loadView("/views/DepartmentHead/dashboard_DepartmentHead.fxml");
        }
    }


    private boolean validarSeleccion() {


        // Validar evaluador 1
        if (evaluador1TextField.getText() == null || evaluador1TextField.getText().trim().isEmpty()) {
            mostrarAlertaError("Campo requerido", "Debe seleccionar el Evaluador 1");
            evaluador1TextField.requestFocus();
            return false;
        }

        // Validar evaluador 2
        if (evaluador2TextField.getText() == null || evaluador2TextField.getText().trim().isEmpty()) {
            mostrarAlertaError("Campo requerido", "Debe seleccionar el Evaluador 2");
            evaluador2TextField.requestFocus();
            return false;
        }

        // Validar que sean diferentes
        if (evaluador1SeleccionadoEmail != null &&
                evaluador2SeleccionadoEmail != null &&
                evaluador1SeleccionadoEmail.equals(evaluador2SeleccionadoEmail)) {
            mostrarAlertaError("Validación", "Los dos evaluadores deben ser diferentes");
            evaluador1TextField.requestFocus();
            return false;
        }

        return true;
    }

    private void limpiarFormulario() {
        evaluador1TextField.clear();
        evaluador2TextField.clear();

        anteproyectoSeleccionadoId = null;
        evaluador1SeleccionadoEmail = null;
        evaluador2SeleccionadoEmail = null;

        ocultarResultados(resultadosEvaluador1);
        ocultarResultados(resultadosEvaluador2);
    }

    /* --------- MÉTODOS DE ALERTA MEJORADOS ---------- */

    private void mostrarAlertaInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private Optional<ButtonType> mostrarAlertaConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait();
    }
}