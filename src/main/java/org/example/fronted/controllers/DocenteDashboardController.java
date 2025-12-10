package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.fronted.api.ProyectoApi;
import org.example.fronted.dto.StatsDocenteDTO;
import org.example.fronted.util.SessionManager;

public class DocenteDashboardController  extends UIBase{

    @FXML private Label pendientesCount;
    @FXML private Label aprobadosCount;
    @FXML private Label evaluacionesCount;
    @FXML private TableView<Proyecto> proyectosTable;
    @FXML private VBox notificacionesContainer;
    @FXML private VBox notificacionesList;

    private SessionManager sessionManager;
    private ProyectoApi proyectoApi;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        cargarEstadisticas();
        cargarProyectosRecientes();
        cargarNotificaciones();
    }

    private void cargarEstadisticas() {
        // Aquí se cargarían las estadísticas reales desde el backend
        StatsDocenteDTO stats = proyectoApi.obtenerEstadisticasDocente(sessionManager.getCurrentUser().getEmail()).block();
        assert stats != null;
        pendientesCount.setText(stats.formatoAPendiente + "");
        aprobadosCount.setText(stats.formatoAAprobado + "");
        evaluacionesCount.setText(stats.pendientesEvaluar + "");
    }

    private void cargarProyectosRecientes() {
        // Configurar tabla de proyectos
        // Aquí se cargarían los proyectos del docente desde el backend
    }

    private void cargarNotificaciones() {
        // Mostrar notificaciones importantes
        // Si hay notificaciones, mostrar el contenedor
        notificacionesContainer.setVisible(false);
    }

    // ============ HANDLERS DE BOTONES ============

    @FXML
    private void verProyectosPendientes() {
        System.out.println("Navegando a proyectos pendientes...");
        loadView("/views/professor/pendientes_list.fxml");
    }

    @FXML
    private void subirAnteproyecto() {
        System.out.println("Navegando a subir anteproyecto...");
        loadView("/views/professor/aprobados_list.fxml");
    }

    @FXML
    private void verEvaluacionesAsignadas() {
        System.out.println("Navegando a evaluaciones asignadas...");
        loadView("/views/professor/evaluaciones_list.fxml") ;
    }

    @FXML
    private void nuevoFormatoA() {
        System.out.println("Navegando a nuevo formato A...");
        loadView("/views/professor/formatA_new.fxml");
    }

    @FXML
    private void verMisProyectos() {
        System.out.println("Navegando a mis proyectos...");
        loadView("/views/professor/proyectos_list.fxml");
    }


    @FXML
    private void verTodosProyectos() {
        System.out.println("Navegando a todos los proyectos...");
        verMisProyectos();
    }

    // ============ CLASE MODELO PARA PROYECTO ============

    public static class Proyecto {
        private String titulo;
        private String estado;
        private String fecha;

        public Proyecto(String titulo, String estado, String fecha) {
            this.titulo = titulo;
            this.estado = estado;
            this.fecha = fecha;
        }

        public String getTitulo() { return titulo; }
        public String getEstado() { return estado; }
        public String getFecha() { return fecha; }
    }
}