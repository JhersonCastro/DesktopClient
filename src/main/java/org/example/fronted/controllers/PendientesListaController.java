package org.example.fronted.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.application.Platform;
import org.example.fronted.api.ProyectoApi;
import org.example.fronted.dto.ProjectCardDTO;
import org.example.fronted.models.ProyectoGrado;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PendientesListaController extends UIBase implements Initializable, ListController {

    @FXML private FlowPane proyectosContainer;
    private ProyectoApi proyectoApi = new ProyectoApi();
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        proyectoApi.obtenerProyectosPendientes()
                .subscribe(
                        lista -> {
                            Platform.runLater(() -> {
                                for (ProyectoGrado p : lista) {
                                    Long id = p.getId();

                                    String titulo = p.getTitulo();
                                    String estudiante = p.getEstudiante1Email(); // o nombre si lo tienes
                                    String modalidad = p.getModalidad();
                                    String director = p.getDirectorEmail();

                                    ProjectCardDTO dto = new ProjectCardDTO(
                                            id,
                                            titulo != null ? titulo : "Sin título",
                                            estudiante != null ? estudiante : "Sin estudiante",
                                            modalidad != null ? modalidad : "Sin modalidad",
                                            director != null ? director : "Sin director"
                                    );

                                    agregarTarjetaProyecto(dto);
                                }
                            });
                        },
                        error -> {
                            error.printStackTrace();
                            Platform.runLater(() ->
                                    System.out.println("Error al cargar proyectos: " + error.getMessage())
                            );
                        }
                );
    }




    private void abrirEvaluacion(ProjectCardDTO proyecto) {
        // Aquí cargamos EvaluarFormatoA.fxml y le pasamos datos simulados

        System.out.println("Evaluando proyecto: " + proyecto.titulo);
        // UIBase.loadView("/views/coordinator/evaluar_formato_a.fxml");
        showError("Mira el id " + proyecto.id);

        loadView("/views/coordinator/evaluar_formatoA.fxml", proyecto.id);
        // Ejemplo: pasar datos a EvaluarFormatoAController
        // EvaluarFormatoAController.setProyectoActual(proyecto);
    }

    public void regresar() {
        loadView("/views/coordinator/dashboard_coordinator.fxml");
    }

    @Override
    public void btnAction(ProjectCardDTO proyecto) {
        abrirEvaluacion(proyecto);

    }

    @Override
    public void addCard(VBox card, Label title, Label estudiante, Label modalidad, Label director, Button evaluarBtn) {
        card.getChildren().addAll(title, estudiante, modalidad, director, evaluarBtn);
        proyectosContainer.getChildren().add(card);
    }

    @Override
    public Button getButton() {
        return new Button("Evaluar");
    }
}
