package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import org.example.fronted.api.MessagingApi;
import org.example.fronted.api.ProyectoApi;
import org.example.fronted.models.MensajeInterno;
import org.example.fronted.models.ProyectoGrado;
import org.example.fronted.models.User;
import org.example.fronted.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class JefeDashboardController extends UIBase {

    @FXML private TableView<ProyectoGrado> anteproyectosTable;
    @FXML private VBox notificacionesContainer;
    @FXML private VBox notificacionesList;
    @FXML private Label lblTotalProyectos;
    @FXML private Label lblPendientes;
    @FXML private Label lblEnRevision;

    private SessionManager sessionManager;
    private ProyectoApi proyectoApi;
    private MessagingApi messagingApi;
    private ObservableList<ProyectoGrado> anteproyectosData;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        proyectoApi = new ProyectoApi(); // Instancia directa, no Singleton
        messagingApi = new MessagingApi(); // Instancia directa
        anteproyectosData = FXCollections.observableArrayList();

        configurarColumnasTabla();
        cargarDatos();
        cargarNotificaciones();
    }

    private void cargarDatos() {

        new Thread(() -> {
            try {
                String emailJefe = "";
                if (sessionManager.getCurrentUser() != null) {
                    emailJefe = sessionManager.getCurrentUser().getEmail();
                }
                //TODO remove that
                ProyectoApi ps = new ProyectoApi();

                if (!emailJefe.isEmpty()) {
                    List<ProyectoGrado> proyectos = new ArrayList<>();
                            proyectoApi.obtenerProyectosPorJefe(emailJefe)
                            .subscribe(DTO ->{
                                for(int i = 0; i<DTO.size(); i++){
                                    ProyectoGrado pg = new ProyectoGrado();
                                    pg.setId(DTO.get(i).getId());
                                    pg.setTitulo(DTO.get(i).getTitulo());
                                    pg.setEstadoActual(DTO.get(i).getEstado());
                                    pg.setEstudiante1Email(DTO.get(i).getEstudiante());
                                    proyectos.add(pg);
                                }
                                Platform.runLater(() -> {
                                    if (proyectos != null) {
                                        anteproyectosData.setAll(proyectos);
                                        anteproyectosTable.setItems(anteproyectosData);
                                        cargarEstadisticas(proyectos);
                                    }
                                });
                                    }

                            );


                } else {
                    Platform.runLater(() -> System.err.println("No se pudo identificar el email del jefe de departamento."));
                }
            } catch (Exception e) {
                Platform.runLater(() -> System.err.println("Error cargando proyectos del jefe: " + e.getMessage()));
            }
        }).start();
    }

    private void cargarEstadisticas(List<ProyectoGrado> proyectos) {
        if (proyectos == null) return;

        long total = proyectos.size();
        long pendientes = proyectos.stream()
                .filter(p -> p.getEstadoActual() != null && p.getEstadoActual().toUpperCase().contains("PENDIENTE"))
                .count();
        long revision = proyectos.stream()
                .filter(p -> p.getEstadoActual() != null &&
                        (p.getEstadoActual().toUpperCase().contains("REVISION") ||
                                p.getEstadoActual().toUpperCase().contains("EVALUACION")))
                .count();

        if (lblTotalProyectos != null) lblTotalProyectos.setText(String.valueOf(total));
        if (lblPendientes != null) lblPendientes.setText(String.valueOf(pendientes));
        if (lblEnRevision != null) lblEnRevision.setText(String.valueOf(revision));
    }

    private void configurarColumnasTabla() {
        TableColumn<ProyectoGrado, String> colTitulo = new TableColumn<>("Título");
        TableColumn<ProyectoGrado, String> colEstudiante = new TableColumn<>("Estudiante");
        TableColumn<ProyectoGrado, String> colEstado = new TableColumn<>("Estado");
        TableColumn<ProyectoGrado, Void> colAcciones = new TableColumn<>("Acciones");

        colTitulo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitulo() != null ? cellData.getValue().getTitulo() : "Sin Título"));

        // En ProyectoGrado tienes estudiante1Email, no getIdEstudiante(). Ajustamos.
        colEstudiante.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstudiante1Email() != null ? cellData.getValue().getEstudiante1Email() : "N/A"));

        // Usamos fechaAnteproyecto o fechaFormatoA según corresponda

        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstadoActual() != null ? cellData.getValue().getEstadoActual() : "DESCONOCIDO"));

        colAcciones.setCellFactory(param -> new TableCell<ProyectoGrado, Void>() {
            private final Button btnAsignar = new Button("Asignar");
            private final Button btnVer = new Button("Ver");

            {
                btnAsignar.getStyleClass().add("table-button");
                btnVer.getStyleClass().add("table-button-ver");

                btnAsignar.setOnAction(event -> {
                    ProyectoGrado proyecto = getTableView().getItems().get(getIndex());
                    asignarEvaluadoresAnteproyecto(proyecto);
                });

                btnVer.setOnAction(event -> {
                    ProyectoGrado proyecto = getTableView().getItems().get(getIndex());
                    verDetalleAnteproyecto(proyecto);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox botones = new HBox(5);
                    botones.getChildren().addAll(btnAsignar, btnVer);
                    setGraphic(botones);
                }
            }
        });

        anteproyectosTable.getColumns().clear();
        anteproyectosTable.getColumns().addAll(colTitulo, colEstudiante, colEstado, colAcciones);
    }

    private void cargarNotificaciones() {
        if (notificacionesList == null || notificacionesContainer == null) return;
        notificacionesList.getChildren().clear();

        new Thread(() -> {
            try {
                String email = null;
                User u = sessionManager.getCurrentUser();
                if (u != null) {
                    email = u.getEmail();
                }

                if (email != null) {
                    List<MensajeInterno> mensajes = messagingApi.getMensajesRecibidos(email).block();

                    Platform.runLater(() -> {
                        if (mensajes != null && !mensajes.isEmpty()) {
                            notificacionesContainer.setVisible(true);
                            for (MensajeInterno msg : mensajes) {
                                Label msgLabel = new Label("• " + (msg.getAsunto() != null ? msg.getAsunto() : "Mensaje nuevo"));
                                msgLabel.getStyleClass().add("notification-item");
                                notificacionesList.getChildren().add(msgLabel);
                            }
                        } else {
                            notificacionesContainer.setVisible(false);
                        }
                    });
                } else {
                    Platform.runLater(() -> notificacionesContainer.setVisible(false));
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Error en notificaciones: " + e.getMessage());
                    notificacionesContainer.setVisible(false);
                });
            }
        }).start();
    }

    // ============ HANDLERS DE BOTONES ============

    @FXML
    private void verAnteproyectosPendientes() {
        // Ejemplo: Cargar solo pendientes usando el método existente en API
        new Thread(() -> {
            try {
                List<ProyectoGrado> pendientes = proyectoApi.obtenerProyectosPendientes().block();
                Platform.runLater(() -> {
                    if(pendientes != null) {
                        anteproyectosData.setAll(pendientes);
                    }
                });
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void verTodosAnteproyectos() {
        cargarDatos();
    }

    @FXML
    private void asignarEvaluadores() {
        loadView("/views/DepartmentHead/evaluators_Assignment.fxml");
    }

    @FXML
    private void verEvaluacionesEnCurso() {
        // Implementar navegación
    }

    @FXML
    private void verDocentesDisponibles() {
        // Implementar navegación
    }

    @FXML
    private void generarReportes() {
        mostrarAlerta("Reportes", "Funcionalidad en construcción.");
    }

    @FXML
    private void abrirConfiguracion() {
        mostrarAlerta("Configuración", "Panel de configuración.");
    }

    // ============ MÉTODOS AUXILIARES ============

    private void asignarEvaluadoresAnteproyecto(ProyectoGrado proyecto) {
        if(proyecto == null) return;
        System.out.println("Asignando evaluadores a proyecto: " + proyecto.getId());

        loadView("/views/DepartmentHead/evaluators_Assignment.fxml", proyecto.getId(), SessionManager.getInstance().getCurrentUser().getEmail());
    }

    private void verDetalleAnteproyecto(ProyectoGrado proyecto) {
        if(proyecto == null) return;

        loadView("/views/utils/visor-pdf.fxml", proyecto);
    }




    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}