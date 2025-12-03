package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.application.Platform;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PendientesListaController extends UIBase implements Initializable {

    @FXML private FlowPane proyectosContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Datos simulados
        List<ProyectoPendiente> proyectos = new ArrayList<>();
        proyectos.add(new ProyectoPendiente("Sistema de Inventarios", "Juan Pérez", "Investigación", "Dr. Gómez"));
        proyectos.add(new ProyectoPendiente("App Móvil Educativa", "María Gómez", "Práctica Profesional", "Mg. Martínez"));
        proyectos.add(new ProyectoPendiente("Control de Asistencias", "Carlos Ramírez", "Investigación", "Dra. Fernández"));
        proyectos.add(new ProyectoPendiente("Página Web de la Empresa", "Ana Torres", "Práctica Profesional", "Dr. Hernández"));

        // Agregar cada proyecto como tarjeta
        Platform.runLater(() -> proyectos.forEach(this::agregarTarjetaProyecto));
    }

    private void agregarTarjetaProyecto(ProyectoPendiente proyecto) {
        VBox card = new VBox();
        card.getStyleClass().add("project-card");
        card.setPadding(new Insets(15));
        card.setSpacing(8);

        Label title = new Label(proyecto.titulo);
        title.getStyleClass().add("project-title");

        Label estudiante = new Label("Estudiante: " + proyecto.estudiante);
        estudiante.getStyleClass().add("project-info");

        Label modalidad = new Label("Modalidad: " + proyecto.modalidad);
        modalidad.getStyleClass().add("project-info");

        Label director = new Label("Director: " + proyecto.director);
        director.getStyleClass().add("project-info");

        Button evaluarBtn = new Button("Evaluar");
        evaluarBtn.getStyleClass().add("action-button");
        evaluarBtn.setOnAction(e -> abrirEvaluacion(proyecto));

        card.getChildren().addAll(title, estudiante, modalidad, director, evaluarBtn);
        proyectosContainer.getChildren().add(card);
    }

    private void abrirEvaluacion(ProyectoPendiente proyecto) {
        // Aquí cargamos EvaluarFormatoA.fxml y le pasamos datos simulados
        System.out.println("Evaluando proyecto: " + proyecto.titulo);
        // UIBase.loadView("/views/coordinator/evaluar_formato_a.fxml");

        // Ejemplo: pasar datos a EvaluarFormatoAController
        // EvaluarFormatoAController.setProyectoActual(proyecto);
    }

    public static class ProyectoPendiente {
        public String titulo;
        public String estudiante;
        public String modalidad;
        public String director;

        public ProyectoPendiente(String titulo, String estudiante, String modalidad, String director) {
            this.titulo = titulo;
            this.estudiante = estudiante;
            this.modalidad = modalidad;
            this.director = director;
        }
    }
}
