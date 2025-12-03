package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class EvaluarFormatoAController extends UIBase {

    @FXML private Label tituloProyectoLabel;
    @FXML private Label modalidadLabel;
    @FXML private Label estudianteLabel;
    @FXML private Label directorLabel;

    @FXML private TextArea observacionesTextArea;

    @FXML
    public void initialize() {
        cargarDatosSimulados();
    }

    private void cargarDatosSimulados() {
        tituloProyectoLabel.setText("Sistema de Gestión Académica");
        modalidadLabel.setText("Trabajo de Grado");
        estudianteLabel.setText("Juan Pérez");
        directorLabel.setText("Dra. Marcela López");
    }

    // ========================== ACCIONES =============================


    @FXML
    private void aprobarFormatoA() {
        if(observacionesTextArea.getText().isEmpty()){
            showAlert("Error", "Falta indicar las observaciones");
        }
        showAlert("Formato A Aprobado", "El Formato A ha sido aprobado exitosamente.");
    }

    @FXML
    private void rechazarFormatoA() {
        if(observacionesTextArea.getText().isEmpty()){
            showAlert("Error", "Falta indicar las observaciones");
        }
        showAlert("Formato A Rechazado", "El Formato A ha sido rechazado.");
    }

    @FXML
    private void verPDF() {
        showAlert("PDF", "Simulando apertura de PDF del Formato A...");
    }

    // ========================== ALERTA =============================

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
