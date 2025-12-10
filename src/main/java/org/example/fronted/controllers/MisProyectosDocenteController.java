package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.fronted.api.ProjectApi;
import org.example.fronted.dto.ProjectCardDTO;
import org.example.fronted.dto.ProyectoDTO;
import org.example.fronted.util.SessionManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MisProyectosDocenteController extends UIBase implements ListController, Initializable {

    @FXML
    private FlowPane proyectosContainer;

    private ProjectApi projectApi;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectApi = new ProjectApi();
        cargarProyectos();
    }

    private void cargarProyectos() {
        // Obtener email del usuario logueado
        String userEmail = obtenerEmailUsuario();

        System.out.println("Cargando proyectos para: " + userEmail);

        // Intentar cargar proyectos reales
        projectApi.obtenerProyectosDeEstudiante(userEmail)
                .subscribe(
                        proyectos -> Platform.runLater(() -> mostrarProyectos(proyectos)),
                        error -> {
                            System.err.println("Error cargando proyectos reales: " + error.getMessage());
                            Platform.runLater(this::mostrarDatosSimulados);
                        }
                );
    }

    private String obtenerEmailUsuario() {
        if (SessionManager.getInstance().getCurrentUser() != null &&
                SessionManager.getInstance().getCurrentUser().getEmail() != null) {
            return SessionManager.getInstance().getCurrentUser().getEmail();
        }
        return "estudiante@unicauca.edu.co"; // Email de prueba
    }

    private void mostrarProyectos(List<ProyectoDTO> proyectos) {
        proyectosContainer.getChildren().clear();

        if (proyectos == null || proyectos.isEmpty()) {
            System.out.println("No hay proyectos reales para mostrar");
            mostrarDatosSimulados();
            return;
        }

        System.out.println("Mostrando " + proyectos.size() + " proyectos reales");

        for (ProyectoDTO proyecto : proyectos) {
            try {
                ProjectCardDTO card = crearCardDesdeProyecto(proyecto);
                agregarTarjetaProyecto(card);
            } catch (Exception e) {
                System.err.println("Error procesando proyecto: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private ProjectCardDTO crearCardDesdeProyecto(ProyectoDTO proyecto) {
        ProjectCardDTO card = new ProjectCardDTO();

        // Usar la estructura REAL de ProyectoDTO
        card.setTitulo(proyecto.getTitulo() != null ? proyecto.getTitulo() : "Sin título");
        card.setId(proyecto.getId());
        card.setEstado(proyecto.getEstado() != null ? proyecto.getEstado() : "DESCONOCIDO");

        // En tu ProyectoDTO, 'modalidad' es como 'tipo'
        card.setTipo(proyecto.getModalidad() != null ? proyecto.getModalidad() : "N/A");

        // Estudiante - usando email en lugar de objeto
        String estudianteInfo = "Estudiante: ";
        if (proyecto.getEstudiante1Email() != null) {
            estudianteInfo += proyecto.getEstudiante1Email();
            if (proyecto.getEstudiante2Email() != null) {
                estudianteInfo += ", " + proyecto.getEstudiante2Email();
            }
        } else {
            estudianteInfo += "No asignado";
        }
        card.setEstudiante(estudianteInfo);

        // Director - usando email en lugar de objeto
        String directorInfo = "Director: ";
        if (proyecto.getDirectorEmail() != null) {
            directorInfo += proyecto.getDirectorEmail();
            if (proyecto.getCodirectorEmail() != null) {
                directorInfo += " (Co-dir: " + proyecto.getCodirectorEmail() + ")";
            }
        } else {
            directorInfo += "No asignado";
        }
        card.setDirector(directorInfo);

        return card;
    }

    private void mostrarDatosSimulados() {
        System.out.println("Mostrando datos simulados (fallback)");

        List<ProjectCardDTO> proyectos = new ArrayList<>();

        // Proyecto 1
        ProjectCardDTO p1 = new ProjectCardDTO(
                "Sistema de Inventarios",
                "Estudiante: juan.perez@unicauca.edu.co",
                "Investigación",
                "Director: dr.gomez@unicauca.edu.co"
        );
        p1.setId(1L);
        p1.setEstado("EN_REVISION");
        proyectos.add(p1);

        // Proyecto 2
        ProjectCardDTO p2 = new ProjectCardDTO(
                "App Móvil Educativa",
                "Estudiante: maria.gomez@unicauca.edu.co",
                "Práctica Profesional",
                "Director: mg.martinez@unicauca.edu.co"
        );
        p2.setId(2L);
        p2.setEstado("APROBADO");
        proyectos.add(p2);

        proyectosContainer.getChildren().clear();
        proyectos.forEach(this::agregarTarjetaProyecto);
    }

    @Override
    public void btnAction(ProjectCardDTO proyecto) {
        System.out.println("Abriendo Proyecto: " + proyecto.getTitulo());
        System.out.println("ID del proyecto: " + proyecto.getId());
        // Aquí puedes navegar a la pantalla de detalle del proyecto
    }

    @Override
    public void addCard(VBox card, Label title, Label estudiante, Label modalidad,
                        Label director, Button evaluarBtn) {
        card.getChildren().addAll(title, estudiante, modalidad, director, evaluarBtn);
        proyectosContainer.getChildren().add(card);
    }

    @Override
    public Button getButton() {
        Button btn = new Button("Ver Proyecto");
        btn.getStyleClass().add("btn-primary");
        return btn;
    }

    public void regresar(ActionEvent actionEvent) {
        loadView("/views/professor/dashboard_professor.fxml");
    }
}