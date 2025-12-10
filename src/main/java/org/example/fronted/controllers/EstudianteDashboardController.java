package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.example.fronted.api.FormatoAApi;
import org.example.fronted.api.MessagingApi;
import org.example.fronted.api.ProyectoApi;
import org.example.fronted.models.MensajeInterno;
import org.example.fronted.models.ProyectoGrado;
import org.example.fronted.util.SessionManager;

import java.util.Objects;

public class EstudianteDashboardController extends UIBase {

    // --- Etiquetas de Información General ---
    @FXML private Label estadoActualLabel;
    @FXML private Label proyectoTituloLabel;
    @FXML private Label directorLabel;
    @FXML private Label modalidadLabel;
    @FXML private Label fechaActualizacionLabel;
    @FXML private Label proximoPasoLabel;
    @FXML private Label directorContactLabel;

    // --- Contenedores Dinámicos ---
    @FXML private VBox observacionesList;
    @FXML private VBox noObservationsBox;
    @FXML private VBox notificationsList;

    // --- Elementos del Timeline (Asegúrate de tener estos ID en el FXML) ---
    @FXML private VBox step1Box; // Formato A
    @FXML private VBox step2Box; // Evaluación 1
    @FXML private VBox step3Box; // Evaluación 2
    @FXML private VBox step4Box; // Anteproyecto
    @FXML private VBox step5Box; // Final

    @FXML private Region line1;
    @FXML private Region line2;
    @FXML private Region line3;
    @FXML private Region line4;

    @FXML private Label fechaEvaluacion1Label;

    // --- Puntos de Intento ---
    @FXML private Circle attemptDot1;
    @FXML private Circle attemptDot2;
    @FXML private Circle attemptDot3;

    // --- APIs y Sesión ---
    private SessionManager sessionManager;
    private final ProyectoApi proyectoApi = new ProyectoApi();
    private final FormatoAApi formatoAApi = new FormatoAApi();
    private final MessagingApi messagingApi = new MessagingApi(); // Instancia de tu API

    private Long currentProyectoId;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();

        // Validación de sesión
        if (!sessionManager.isLoggedIn() || sessionManager.getCurrentUser() == null) {
            System.err.println("No hay usuario logueado.");
            return;
        }

        cargarDatosEstudiante();
        cargarNotificaciones(); // Ahora sí cargamos las notificaciones reales
    }

    private void cargarDatosEstudiante() {
        String email = sessionManager.getCurrentUser().getEmail(); // CORREGIDO

        proyectoApi.obtenerPorEstudiante(email)
                .subscribe(proyectos -> {
                    if (proyectos != null && !proyectos.isEmpty()) {
                        ProyectoGrado p = proyectos.get(0);
                        this.currentProyectoId = p.getId();

                        Platform.runLater(() -> {
                            actualizarInfoGeneral(p);
                            actualizarTimeline(p.getEstadoActual());
                            actualizarIntentos(p.getIntentos() != null ? p.getIntentos() : 1);
                            cargarObservaciones(p);
                        });
                    } else {
                        Platform.runLater(this::mostrarEstadoSinProyecto);
                    }
                }, error -> {
                    Platform.runLater(() -> System.err.println("Error cargando proyecto: " + error.getMessage()));
                });
    }

    // ==========================================
    // INTEGRACIÓN DE NOTIFICACIONES (MESSAGING API)
    // ==========================================
    private void cargarNotificaciones() {
        String email = sessionManager.getCurrentUser().getEmail();

        messagingApi.getMensajesRecibidos(email)
                .subscribe(mensajes -> {
                    Platform.runLater(() -> {
                        notificationsList.getChildren().clear();

                        if (mensajes != null && !mensajes.isEmpty()) {
                            // Mostrar solo las 5 más recientes (o todas si prefieres quitar el limit)
                            mensajes.stream()
                                    .limit(5)
                                    .forEach(msg -> notificationsList.getChildren().add(crearItemNotificacion(msg)));
                        } else {
                            Label empty = new Label("No tienes notificaciones recientes.");
                            empty.setStyle("-fx-text-fill: #6c757d; -fx-font-style: italic;");
                            notificationsList.getChildren().add(empty);
                        }
                    });
                }, error -> {
                    Platform.runLater(() -> System.err.println("Error cargando notificaciones: " + error.getMessage()));
                });
    }

    private HBox crearItemNotificacion(MensajeInterno msg) {
        HBox item = new HBox(15);
        item.getStyleClass().add("notification-item"); // Clase CSS del FXML
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Icono
        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("notification-icon-container");

        try {
            // Intentamos cargar la imagen, si falla usamos un placeholder
            ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icons/notification-info.png"))));
            icon.setFitWidth(20);
            icon.setFitHeight(20);
            icon.setPreserveRatio(true);
            iconContainer.getChildren().add(icon);
        } catch (Exception e) {
            // Fallback si no encuentra la imagen
            Label iconFallback = new Label("ℹ️");
            iconContainer.getChildren().add(iconFallback);
        }

        // Contenido de texto
        VBox textContainer = new VBox(3);

        Label title = new Label(msg.getAsunto());
        title.getStyleClass().add("notification-title");

        Label body = new Label(msg.getCuerpo());
        body.getStyleClass().add("notification-text");
        body.setWrapText(true);

        Label time = new Label(msg.getFechaEnvio() != null ? msg.getFechaEnvio() : "Reciente");
        time.getStyleClass().add("notification-time");

        textContainer.getChildren().addAll(title, body, time);
        item.getChildren().addAll(iconContainer, textContainer);

        return item;
    }

    // ==========================================
    // MÉTODOS DE LA VISTA (TIMELINE, UI, ALERTAS)
    // ==========================================

    private void actualizarInfoGeneral(ProyectoGrado p) {
        estadoActualLabel.setText(p.getEstadoActual());
        proyectoTituloLabel.setText(p.getTitulo());

        String director = p.getDirectorEmail() != null ? p.getDirectorEmail() : "Sin asignar";
        directorLabel.setText("Director: " + director);
        directorContactLabel.setText(director);

        modalidadLabel.setText("Modalidad: " + (p.getModalidad() != null ? p.getModalidad() : "No definida"));
        fechaActualizacionLabel.setText(p.getFechaFormatoA() != null ? p.getFechaFormatoA() : "Reciente");

        configurarProximoPaso(p);
    }

    private void configurarProximoPaso(ProyectoGrado p) {
        String estado = p.getEstadoActual();
        String paso = "Esperar indicaciones";

        if (estado != null) {
            String e = estado.toLowerCase();
            if (e.contains("aprobado")) {
                paso = "Iniciar desarrollo del anteproyecto";
            } else if (e.contains("rechazado")) {
                paso = (p.getIntentos() != null && p.getIntentos() < 3)
                        ? "Corregir y reintentar Formato A"
                        : "Contactar a coordinación (Intentos agotados)";
            } else if (e.contains("evaluación")) {
                paso = "Estar atento a resultados de evaluación";
            }
        }
        proximoPasoLabel.setText(paso);
    }

    private void actualizarTimeline(String estadoActual) {
        int paso = 1;
        if (estadoActual != null) {
            String e = estadoActual.toLowerCase();
            if (e.contains("evaluación") || e.contains("asignado")) paso = 2;
            else if (e.contains("evaluación 2")) paso = 3;
            else if (e.contains("anteproyecto")) paso = 4;
            else if (e.contains("final") || e.contains("sustentación")) paso = 5;
        }

        if(step1Box != null) actualizarEstiloPaso(step1Box, line1, paso >= 1, paso > 1);
        if(step2Box != null) actualizarEstiloPaso(step2Box, line2, paso >= 2, paso > 2);
        if(step3Box != null) actualizarEstiloPaso(step3Box, line3, paso >= 3, paso > 3);
        if(step4Box != null) actualizarEstiloPaso(step4Box, line4, paso >= 4, paso > 4);
        if(step5Box != null) actualizarEstiloPaso(step5Box, null, paso >= 5, false);
    }

    private void actualizarEstiloPaso(VBox stepBox, Region line, boolean active, boolean completed) {
        stepBox.getStyleClass().removeAll("step-active", "step-current", "step-pending");
        StackPane circle = (StackPane) stepBox.getChildren().get(0);
        circle.getStyleClass().removeAll("circle-active", "circle-current", "circle-pending");

        if (completed) {
            stepBox.getStyleClass().add("step-active");
            circle.getStyleClass().add("circle-active");
            if (line != null) {
                line.getStyleClass().removeAll("line-active", "line-pending");
                line.getStyleClass().add("line-active");
            }
        } else if (active) {
            stepBox.getStyleClass().add("step-current");
            circle.getStyleClass().add("circle-current");
            if (line != null) {
                line.getStyleClass().removeAll("line-active", "line-pending");
                line.getStyleClass().add("line-pending");
            }
        } else {
            stepBox.getStyleClass().add("step-pending");
            circle.getStyleClass().add("circle-pending");
            if (line != null) {
                line.getStyleClass().removeAll("line-active", "line-pending");
                line.getStyleClass().add("line-pending");
            }
        }
    }

    private void actualizarIntentos(int intento) {
        attemptDot1.getStyleClass().remove("dot-active");
        attemptDot2.getStyleClass().remove("dot-active");
        attemptDot3.getStyleClass().remove("dot-active");

        if (intento >= 1) attemptDot1.getStyleClass().add("dot-active");
        if (intento >= 2) attemptDot2.getStyleClass().add("dot-active");
        if (intento >= 3) attemptDot3.getStyleClass().add("dot-active");
    }

    private void cargarObservaciones(ProyectoGrado p) {
        observacionesList.getChildren().clear();
        String observaciones = p.getObservacionesEvaluacion();

        if (observaciones != null && !observaciones.trim().isEmpty()) {
            noObservationsBox.setVisible(false);
            noObservationsBox.setManaged(false);

            VBox card = new VBox();
            card.getStyleClass().add("observation-item");
            card.setStyle("-fx-background-color: #fff3cd; -fx-padding: 10; -fx-border-color: #ffeeba; -fx-border-radius: 4;");

            HBox header = new HBox(10);
            Label icon = new Label("📝");
            Label author = new Label("Comité / Coordinador");
            author.setStyle("-fx-font-weight: bold; -fx-text-fill: #856404;");
            header.getChildren().addAll(icon, author);

            Label content = new Label(observaciones);
            content.setWrapText(true);
            content.setStyle("-fx-text-fill: #333; -fx-padding: 5 0 0 0;");

            card.getChildren().addAll(header, content);

            // Botón reintentar
            if (p.getEstadoActual() != null &&
                    p.getEstadoActual().toLowerCase().contains("rechazado") &&
                    (p.getIntentos() == null || p.getIntentos() < 3)) {

                Region spacer = new Region();
                spacer.setPrefHeight(10);

                Button btnReintentar = new Button("Corregir y Reintentar");
                btnReintentar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                btnReintentar.setOnAction(e -> confirmarReintento());

                card.getChildren().addAll(spacer, btnReintentar);
            }

            observacionesList.getChildren().add(card);
        } else {
            noObservationsBox.setVisible(true);
            noObservationsBox.setManaged(true);
        }
    }

    // ==========================================
    // ACCIONES DE REINTENTO Y HELPERS
    // ==========================================

    private void confirmarReintento() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nuevo Intento");
        alert.setHeaderText("¿Deseas crear una nueva versión?");
        alert.setContentText("Se generará un nuevo borrador basado en tu información actual para que apliques las correcciones.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ejecutarReintento();
            }
        });
    }

    private void ejecutarReintento() {
        if (currentProyectoId == null) return;

        formatoAApi.reintentarProyecto(currentProyectoId)
                .subscribe(unused -> {
                    Platform.runLater(() -> {
                        mostrarAlerta("Éxito", "Nuevo intento generado.", Alert.AlertType.INFORMATION);
                        cargarDatosEstudiante();
                    });
                }, error -> {
                    Platform.runLater(() -> mostrarAlerta("Error", "No se pudo crear el reintento: " + error.getMessage(), Alert.AlertType.ERROR));
                });
    }

    private void mostrarEstadoSinProyecto() {
        proyectoTituloLabel.setText("Sin proyecto activo");
        estadoActualLabel.setText("-");
    }

    // Helper para alertas (ya que no está en UIBase o no es accesible)
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    @FXML
    private void verTodasNotificaciones() {
        loadView("/views/student/notificaciones_list.fxml");
        System.out.println("Navegar a lista completa de notificaciones...");
    }
}