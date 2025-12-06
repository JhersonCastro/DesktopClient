package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import org.example.fronted.util.SessionManager;

public class JefeDashboardController extends UIBase{

    @FXML private TableView<Anteproyecto> anteproyectosTable;
    @FXML private VBox notificacionesContainer;
    @FXML private VBox notificacionesList;

    private SessionManager sessionManager;
    private ObservableList<Anteproyecto> anteproyectosData;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        anteproyectosData = FXCollections.observableArrayList();

        cargarEstadisticas();
        cargarAnteproyectosRecientes();
        cargarNotificaciones();
    }

    private void cargarEstadisticas() {
        // Aquí se cargarían las estadísticas reales desde el backend

    }

    private void cargarAnteproyectosRecientes() {
        // Datos de ejemplo - en producción vendrían del backend
        anteproyectosData.add(new Anteproyecto(
                "Sistema de Gestión de Proyectos",
                "Dr. Carlos Mendoza",
                "15 Nov 2025",
                "Pendiente"
        ));

        anteproyectosData.add(new Anteproyecto(
                "IA para Diagnóstico Médico",
                "Dra. Ana López",
                "14 Nov 2025",
                "Asignado"
        ));

        anteproyectosData.add(new Anteproyecto(
                "App Móvil Educativa",
                "Mg. Pedro Gómez",
                "12 Nov 2025",
                "En Evaluación"
        ));

        // Configurar la tabla
        configurarColumnasTabla();
        anteproyectosTable.setItems(anteproyectosData);
    }

    private void configurarColumnasTabla() {
        // Configurar las columnas de la tabla
        TableColumn<Anteproyecto, String> colTitulo = new TableColumn<>("Título");
        TableColumn<Anteproyecto, String> colDocente = new TableColumn<>("Docente");
        TableColumn<Anteproyecto, String> colFecha = new TableColumn<>("Fecha");
        TableColumn<Anteproyecto, String> colEstado = new TableColumn<>("Estado");
        TableColumn<Anteproyecto, Void> colAcciones = new TableColumn<>("Acciones");

        // Configurar cell value factories
        colTitulo.setCellValueFactory(cellData -> cellData.getValue().tituloProperty());
        colDocente.setCellValueFactory(cellData -> cellData.getValue().docenteProperty());
        colFecha.setCellValueFactory(cellData -> cellData.getValue().fechaProperty());
        colEstado.setCellValueFactory(cellData -> cellData.getValue().estadoProperty());

        // Configurar columna de acciones con botones
        colAcciones.setCellFactory(param -> new TableCell<Anteproyecto, Void>() {
            private final Button btnAsignar = new Button("Asignar");
            private final Button btnVer = new Button("Ver");

            {
                btnAsignar.getStyleClass().add("table-button");
                btnVer.getStyleClass().add("table-button");

                btnAsignar.setOnAction(event -> {
                    Anteproyecto anteproyecto = getTableView().getItems().get(getIndex());
                    asignarEvaluadoresAnteproyecto(anteproyecto);
                });

                btnVer.setOnAction(event -> {
                    Anteproyecto anteproyecto = getTableView().getItems().get(getIndex());
                    verDetalleAnteproyecto(anteproyecto);
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

        // Limpiar y agregar columnas
        anteproyectosTable.getColumns().clear();
        anteproyectosTable.getColumns().addAll(colTitulo, colDocente, colFecha, colEstado, colAcciones);
    }

    private void cargarNotificaciones() {
        // Cargar notificaciones importantes
        boolean tieneNotificaciones = true; // Ejemplo
        notificacionesContainer.setVisible(tieneNotificaciones);
    }

    // ============ HANDLERS DE BOTONES ============

    @FXML
    private void verAnteproyectosPendientes() {
        System.out.println("Navegando a anteproyectos pendientes...");
        // loadView("/views/jefe/lista_anteproyectos.fxml");
    }

    @FXML
    private void verEvaluacionesEnCurso() {
        System.out.println("Navegando a evaluaciones en curso...");
        // loadView("/views/jefe/seguimiento_evaluaciones.fxml");
    }

    @FXML
    private void verDocentesDisponibles() {
        System.out.println("Navegando a docentes disponibles...");
        // loadView("/views/jefe/docentes_disponibles.fxml");
    }

    @FXML
    private void verTodosAnteproyectos() {
        System.out.println("Navegando a todos los anteproyectos...");
        verAnteproyectosPendientes();
    }

    @FXML
    private void asignarEvaluadores() {
        System.out.println("Navegando a asignar evaluadores...");
        // loadView("/views/jefe/asignar_evaluadores.fxml");
    }

    @FXML
    private void generarReportes() {
        System.out.println("Generando reportes...");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reportes");
        alert.setHeaderText("Generando reportes de evaluación");
        alert.setContentText("Los reportes se generarán en segundo plano.");
        alert.showAndWait();
    }

    @FXML
    private void abrirConfiguracion() {
        System.out.println("Abriendo configuración...");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración");
        alert.setHeaderText("Configuración del Sistema");
        alert.setContentText("Aquí se configurarían los parámetros del sistema.");
        alert.showAndWait();
    }

    // ============ MÉTODOS AUXILIARES ============

    private void asignarEvaluadoresAnteproyecto(Anteproyecto anteproyecto) {
        System.out.println("Asignando evaluadores para: " + anteproyecto.getTitulo());
        // Aquí se abriría un diálogo para asignar evaluadores
        loadView("/views/DepartmentHead/evaluators_Assignment.fxml");
    }

    private void verDetalleAnteproyecto(Anteproyecto anteproyecto) {
        System.out.println("Viendo detalle de: " + anteproyecto.getTitulo());
        // Aquí se cargaría la vista de detalle
    }

    // ============ CLASE MODELO PARA ANTEPROYECTO ============

    public static class Anteproyecto {
        private final javafx.beans.property.SimpleStringProperty titulo;
        private final javafx.beans.property.SimpleStringProperty docente;
        private final javafx.beans.property.SimpleStringProperty fecha;
        private final javafx.beans.property.SimpleStringProperty estado;

        public Anteproyecto(String titulo, String docente, String fecha, String estado) {
            this.titulo = new javafx.beans.property.SimpleStringProperty(titulo);
            this.docente = new javafx.beans.property.SimpleStringProperty(docente);
            this.fecha = new javafx.beans.property.SimpleStringProperty(fecha);
            this.estado = new javafx.beans.property.SimpleStringProperty(estado);
        }

        public String getTitulo() { return titulo.get(); }
        public javafx.beans.property.StringProperty tituloProperty() { return titulo; }

        public String getDocente() { return docente.get(); }
        public javafx.beans.property.StringProperty docenteProperty() { return docente; }

        public String getFecha() { return fecha.get(); }
        public javafx.beans.property.StringProperty fechaProperty() { return fecha; }

        public String getEstado() { return estado.get(); }
        public javafx.beans.property.StringProperty estadoProperty() { return estado; }
    }
}