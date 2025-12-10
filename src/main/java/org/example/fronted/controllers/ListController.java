package org.example.fronted.controllers;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.fronted.dto.ProjectCardDTO;
import org.example.fronted.util.SessionManager;


public interface ListController {

    public default void agregarTarjetaProyecto(ProjectCardDTO proyecto) {
        VBox card = new VBox();
        card.getStyleClass().add("project-card");
        card.setPadding(new Insets(15));
        card.setSpacing(8);

        Label title = new Label(proyecto.getTitulo());
        title.getStyleClass().add("card-title");

        Label estudiante = new Label("Estudiante: " + proyecto.getEstudiante());
        estudiante.getStyleClass().add("card-info");

        Label modalidad = new Label("Modalidad: " + proyecto.getModalidad());
        modalidad.getStyleClass().add("card-info");

        Label director = new Label("Director: " + proyecto.getDirector());
        director.getStyleClass().add("card-info");

        Button evaluarBtn = getButton();
        evaluarBtn.getStyleClass().add("card-button");
        evaluarBtn.setOnAction(e -> btnAction(proyecto));

        addCard(card, title, estudiante, modalidad, director, evaluarBtn);
    }

    public default String obtenerCorreoActual(){
        return SessionManager.getInstance().getCurrentUser().getEmail();
    }

    public abstract void btnAction(ProjectCardDTO proyecto);
    public abstract void addCard(VBox card, Label title, Label estudiante, Label modalidad, Label director, Button evaluarBtn);
    public abstract Button getButton();
}
