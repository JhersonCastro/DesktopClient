package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import org.example.fronted.api.NotificationApi;
import org.example.fronted.dto.NotificacionDTO;
import org.example.fronted.util.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MisNotificacionesController extends UIBase implements Initializable {

    @FXML private VBox notificacionesContainer;
    @FXML private Label tituloLabel;
    @FXML private Label contadorLabel;
    @FXML private Button marcarTodasBtn;
    @FXML private Button recargarBtn;

    private NotificationApi notificationApi;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        notificationApi = new NotificationApi();

        // Configurar título con nombre de usuario
        String nombreUsuario = SessionManager.getInstance().getUserFullName();
        tituloLabel.setText("Notificaciones de " + nombreUsuario);

        cargarNotificaciones();
        cargarContadorNoLeidas();

        // Configurar botones
        marcarTodasBtn.setOnAction(e -> marcarTodasComoLeidas());
        recargarBtn.setOnAction(e -> recargar());
    }

    private void cargarNotificaciones() {
        notificacionesContainer.getChildren().clear();

        notificationApi.obtenerNotificaciones()
                .subscribe(
                        notificaciones -> {
                            javafx.application.Platform.runLater(() -> {
                                if (notificaciones.isEmpty()) {
                                    mostrarSinNotificaciones();
                                } else {
                                    mostrarNotificaciones(notificaciones);
                                }
                            });
                        },
                        error -> {
                            System.err.println("Error cargando notificaciones: " + error.getMessage());
                            javafx.application.Platform.runLater(() -> {
                                mostrarErrorCarga();
                            });
                        }
                );
    }

    private void mostrarNotificaciones(List<NotificacionDTO> notificaciones) {
        for (NotificacionDTO notificacion : notificaciones) {
            notificacionesContainer.getChildren().add(crearTarjetaNotificacion(notificacion));
        }
    }

    private VBox crearTarjetaNotificacion(NotificacionDTO notificacion) {
        VBox tarjeta = new VBox(8);
        tarjeta.setPadding(new Insets(12));
        tarjeta.setStyle(
                "-fx-background-color: " + (notificacion.isLeida() ? "#f8f9fa" : "#e3f2fd") + ";" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        // Header con título y fecha
        HBox header = new HBox();
        header.setSpacing(10);

        Label tituloLabel = new Label(notificacion.getTitulo());
        tituloLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label fechaLabel = new Label(notificacion.getTiempoRelativo());
        fechaLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        header.getChildren().addAll(tituloLabel, fechaLabel);

        // Mensaje
        Text mensajeText = new Text(notificacion.getMensaje());
        mensajeText.setWrappingWidth(600);

        // Footer con acciones
        HBox footer = new HBox(10);

        if (!notificacion.isLeida()) {
            Button marcarLeidaBtn = new Button("Marcar como leída");
            marcarLeidaBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 8;");
            marcarLeidaBtn.setOnAction(e -> marcarComoLeida(notificacion.getId(), tarjeta));
            footer.getChildren().add(marcarLeidaBtn);
        }

        if (notificacion.getAccionUrl() != null && !notificacion.getAccionUrl().isEmpty()) {
            Button accionBtn = new Button("Ver más");
            accionBtn.setStyle("-fx-font-size: 12px; -fx-padding: 4 8;");
            accionBtn.setOnAction(e -> ejecutarAccion(notificacion));
            footer.getChildren().add(accionBtn);
        }

        // Badge de tipo
        Label tipoBadge = new Label(notificacion.getTipo());
        tipoBadge.setStyle(
                "-fx-background-color: " + obtenerColorTipo(notificacion.getTipo()) + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 2 6;" +
                        "-fx-font-size: 10px;" +
                        "-fx-background-radius: 10;"
        );

        tarjeta.getChildren().addAll(header, mensajeText, footer, tipoBadge);

        return tarjeta;
    }

    private String obtenerColorTipo(String tipo) {
        switch (tipo.toUpperCase()) {
            case "INFO": return "#17a2b8";
            case "ALERTA": return "#ffc107";
            case "EXITO": return "#28a745";
            case "ERROR": return "#dc3545";
            case "PROYECTO": return "#007bff";
            default: return "#6c757d";
        }
    }

    private void marcarComoLeida(Long notificacionId, VBox tarjeta) {
        notificationApi.marcarComoLeida(notificacionId)
                .subscribe(
                        success -> {
                            if (success) {
                                javafx.application.Platform.runLater(() -> {
                                    // Cambiar estilo de la tarjeta
                                    tarjeta.setStyle(
                                            "-fx-background-color: #f8f9fa;" +
                                                    "-fx-border-color: #dee2e6;" +
                                                    "-fx-border-width: 1;" +
                                                    "-fx-border-radius: 8;" +
                                                    "-fx-background-radius: 8;"
                                    );
                                    cargarContadorNoLeidas();
                                });
                            }
                        },
                        error -> System.err.println("Error marcando como leída: " + error.getMessage())
                );
    }

    private void marcarTodasComoLeidas() {
        // Nota: Necesitarías un endpoint en el backend para esto
        mostrarAlerta("Función en desarrollo",
                "Marcar todas como leídas requiere endpoint adicional en el backend.",
                Alert.AlertType.INFORMATION);
    }

    private void ejecutarAccion(NotificacionDTO notificacion) {
        // Navegar según la acción
        if (notificacion.getProyectoId() != null) {
            // Navegar al proyecto
            System.out.println("Navegando al proyecto: " + notificacion.getProyectoId());
        } else if (notificacion.getAccionUrl() != null) {
            // Abrir URL
            System.out.println("Abriendo: " + notificacion.getAccionUrl());
        }
    }

    private void cargarContadorNoLeidas() {
        notificationApi.obtenerConteoNoLeidas()
                .subscribe(
                        count -> javafx.application.Platform.runLater(() ->
                                contadorLabel.setText("Tienes " + count + " notificaciones no leídas")
                        ),
                        error -> javafx.application.Platform.runLater(() ->
                                contadorLabel.setText("Error cargando contador")
                        )
                );
    }

    private void mostrarSinNotificaciones() {
        Label mensaje = new Label("🎉 ¡No tienes notificaciones!");
        mensaje.setStyle("-fx-font-size: 16px; -fx-text-fill: #6c757d; -fx-padding: 20;");
        notificacionesContainer.getChildren().add(mensaje);
    }

    private void mostrarErrorCarga() {
        Label mensaje = new Label("⚠️ Error cargando notificaciones");
        mensaje.setStyle("-fx-font-size: 16px; -fx-text-fill: #dc3545; -fx-padding: 20;");
        notificacionesContainer.getChildren().add(mensaje);
    }

    private void recargar() {
        cargarNotificaciones();
        cargarContadorNoLeidas();
    }

    @FXML
    private void regresar() {
        // Navegar al dashboard según rol
        // loadView("/views/dashboard.fxml");
        System.out.println("Regresando al dashboard...");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}