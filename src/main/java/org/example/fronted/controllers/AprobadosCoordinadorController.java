package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.fronted.dto.ProjectCardDTO;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AprobadosCoordinadorController extends UIBase implements ListController, Initializable {

    @FXML
    private FlowPane proyectosContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Datos simulados
        List<ProjectCardDTO> proyectos = new ArrayList<>();
        proyectos.add(new ProjectCardDTO("Sistema de Inventarios", "Juan Pérez", "Investigación", "Dr. Gómez"));
        proyectos.add(new ProjectCardDTO("App Móvil Educativa", "María Gómez", "Práctica Profesional", "Mg. Martínez"));
        proyectos.add(new ProjectCardDTO("Control de Asistencias", "Carlos Ramírez", "Investigación", "Dra. Fernández"));
        proyectos.add(new ProjectCardDTO("Página Web de la Empresa", "Ana Torres", "Práctica Profesional", "Dr. Hernández"));

        // Agregar cada proyecto como tarjeta
        Platform.runLater(() -> proyectos.forEach(this::agregarTarjetaProyecto));
    }

    @Override
    public void btnAction(ProjectCardDTO proyecto) {
        verProyecto(proyecto);
    }

    @Override
    public void addCard(VBox card, Label title, Label estudiante, Label modalidad, Label director, Button evaluarBtn) {
        card.getChildren().addAll(title, estudiante, modalidad, director, evaluarBtn);
        proyectosContainer.getChildren().add(card);
    }

    @Override
    public Button getButton() {
        return new Button("Ver");
    }

    public void regresar(ActionEvent actionEvent) {
        loadView("/views/coordinator/dashboard_coordinator.fxml");
    }

    private void verProyecto(ProjectCardDTO proyecto) {
        // Aquí cargamos EvaluarFormatoA.fxml y le pasamos datos simulados
        System.out.println("Viendo proyecto: " + proyecto.titulo);
        // UIBase.loadView("/views/coordinator/evaluar_formato_a.fxml");

        // Ejemplo: pasar datos a EvaluarFormatoAController
        // EvaluarFormatoAController.setProyectoActual(proyecto);
    }
}
