package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.fronted.api.ProyectoApi;
import org.example.fronted.dto.ProjectCardDTO;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MisProyectosDocenteController extends UIBase implements ListController, Initializable {

    @FXML
    private FlowPane proyectosContainer;

    ProyectoApi proyectoApi = new ProyectoApi();

    // SECCION DE CARGA TARJETAS

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarProyectosDocente();
    }

    private void cargarProyectosDocente() {
        String emailDocente = obtenerCorreoActual();

        proyectoApi.obtenerProyectosPorDocente(emailDocente, null)
                .subscribe(this::pintarProyectos, this::manejarError);
    }

    private void pintarProyectos(List<ProjectCardDTO> proyectos) {
        Platform.runLater(() ->
                proyectos.forEach(this::agregarTarjetaProyecto)
        );
    }

    private void manejarError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void btnAction(ProjectCardDTO proyecto) {
        abrirProyecto(proyecto);
    }

    @Override
    public void addCard(VBox card, Label title, Label estudiante, Label modalidad, Label director, Button evaluarBtn) {
        card.getChildren().addAll(title, estudiante, modalidad, director, evaluarBtn);
        proyectosContainer.getChildren().add(card);
    }

    @Override
    public Button getButton() {
        return new Button("Ver Proyecto");
    }

    public void regresar(ActionEvent actionEvent) {
        loadView("/views/professor/dashboard_professor.fxml");
    }

    private void abrirProyecto(ProjectCardDTO proyecto) {
        // Aquí cargamos EvaluarFormatoA.fxml y le pasamos datos simulados
        System.out.println("Abriendo Proyecto: " + proyecto.titulo);

        // Ejemplo: pasar datos a EvaluarFormatoAController
        // EvaluarFormatoAController.setProyectoActual(proyecto);
    }
}

