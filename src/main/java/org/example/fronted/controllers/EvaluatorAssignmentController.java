package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class EvaluatorAssignmentController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField anteproyectoTexField;

    @FXML
    private TextField evaluador1TextField;

    @FXML
    private TextField evaluador2TextField;


    @FXML
    public void initialize() {
        //cargarAnteproyectos();
        //cargarEvaluadores();
    }


    /*private void cargarAnteproyectos() {
        anteproyectoComboBox.getItems().addAll(
                "Anteproyecto A",
                "Anteproyecto B",
                "Anteproyecto C"
        );
    }*/


    /*private void cargarEvaluadores() {
        evaluador1TextField.getItems().addAll(
                "Evaluador 1",
                "Evaluador 2",
                "Evaluador 3"
        );

        evaluador2ComboBox.getItems().addAll(
                "Evaluador 1",
                "Evaluador 2",
                "Evaluador 3"
        );
    }*/


    @FXML
    private void regresarAlDashboard() {
        mostrarAlertaInfo("Volviendo al dashboard...");
    }


    @FXML
    private void confirmarSeleccion() {
        String anteproyecto = anteproyectoTexField.getText();
        String eval1 = evaluador1TextField.getText();
        String eval2 = evaluador2TextField.getText();

        if (!validarSeleccion(anteproyecto, eval1, eval2)) return;

        mostrarAlertaInfo("Selección confirmada. Puede continuar.");
    }


    @FXML
    private void cancelar() {
        Optional<ButtonType> result = mostrarAlertaConfirmacion("¿Desea cancelar la operación?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            limpiarFormulario();
        }
    }


    @FXML
    private void asignarEvaluadores() {
        String anteproyecto = anteproyectoTexField.getText();
        String eval1 = evaluador1TextField.getText();
        String eval2 = evaluador2TextField.getText();

        if (!validarSeleccion(anteproyecto, eval1, eval2)) return;

        mostrarAlertaInfo("Evaluadores asignados correctamente.");
        limpiarFormulario();
    }


    private boolean validarSeleccion(String anteproyecto, String eval1, String eval2) {
        if (anteproyecto == null || eval1 == null || eval2 == null) {
            mostrarAlertaError("Debe completar todos los campos obligatorios.");
            return false;
        }

        if (eval1.equals(eval2)) {
            mostrarAlertaError("Los dos evaluadores deben ser diferentes.");
            return false;
        }

        return true;
    }


    private void limpiarFormulario() {
        anteproyectoTexField.clear();
        evaluador1TextField.clear();
        evaluador2TextField.clear();
    }


    private void mostrarAlertaInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlertaError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private Optional<ButtonType> mostrarAlertaConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait();
    }
}