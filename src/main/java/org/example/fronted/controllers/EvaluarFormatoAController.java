package org.example.fronted.controllers;

import org.example.fronted.api.DocumentoApi;
import org.example.fronted.api.FormatoAApi;
import org.example.fronted.api.ProyectoApi;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.example.fronted.models.Documento;
import org.example.fronted.util.PdfViewerUtil;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EvaluarFormatoAController extends UIBase{

    @FXML
    private Label tituloProyectoLabel;

    @FXML
    private Label modalidadLabel;

    @FXML
    private Label estudianteLabel;

    @FXML
    private Label directorLabel;

    @FXML
    private Button verPDFButton;

    @FXML
    private Button aprobarButton;

    @FXML
    private Button rechazarButton;

    @FXML
    private TextArea observacionesTextArea;

    private Long formatoAId; // ID del documento Formato A
    private ProyectoApi proyectoApi = new ProyectoApi();
    private FormatoAApi formatoAApi = new FormatoAApi();
    private DocumentoApi documentoApi = new DocumentoApi();

    @Override
    public void setArgs(Object[] args) {
        super.setArgs(args);
        this.formatoAId = ((Integer) args[0]).longValue();

        cargarDatosFormatoA();
        cargarDocumentosProyecto();
    }

    private void cargarDatosFormatoA() {
        if (formatoAId == null) return;

        proyectoApi.obtenerPorId(formatoAId)
                .subscribe(
                        proyecto -> {
                            Platform.runLater(() -> {
                                tituloProyectoLabel.setText(proyecto.getTitulo());
                                modalidadLabel.setText(proyecto.getModalidad());

                                // Mostrar estudiantes
                                String estudiantes = proyecto.getEstudiante1Email();
                                if (proyecto.getEstudiante2Email() != null && !proyecto.getEstudiante2Email().isEmpty()) {
                                    estudiantes += "\n" + proyecto.getEstudiante2Email();
                                }
                                estudianteLabel.setText(estudiantes);

                                // Mostrar director y codirector
                                String director = proyecto.getDirectorEmail();
                                if (proyecto.getCodirectorEmail() != null && !proyecto.getCodirectorEmail().isEmpty()) {
                                    director += "\nCodirector: " + proyecto.getCodirectorEmail();
                                }
                                directorLabel.setText(director);

                                // Deshabilitar botones si ya fue evaluado
                                if ("APROBADO".equals(proyecto.getEstadoActual()) || "RECHAZADO".equals(proyecto.getEstadoActual())) {
                                    aprobarButton.setDisable(true);
                                    rechazarButton.setDisable(true);
                                    observacionesTextArea.setDisable(true);
                                }
                            });
                        },
                        error -> {
                            Platform.runLater(() -> {
                                showAlert("Error", "No se pudo cargar el proyecto: " + error.getMessage());
                                System.out.println(error.getMessage());
                            });
                        }
                );
    }
    Long docID;
    private void cargarDocumentosProyecto() {
        if (formatoAId == null) return;

        documentoApi.obtenerDocumentosPorProyecto(formatoAId)
                .subscribe(
                        documentos -> {
                            Platform.runLater(() -> {
                                // Buscar el documento de tipo FORMATO_A
                                for (Object docObj : documentos) {
                                    if (docObj instanceof Map) {
                                        Map<String, Object> doc = (Map<String, Object>) docObj;

                                        System.out.println("----- DOCUMENTO -----");
                                        for (Map.Entry<String, Object> entry : doc.entrySet()) {
                                            System.out.println(entry.getKey() + " = " + entry.getValue());
                                        }
                                        docID = ((Integer) doc.get("id")).longValue();
                                    }
                                }


                                if (formatoAId == null) {
                                    verPDFButton.setDisable(true);
                                    verPDFButton.setText("No hay Formato A disponible");
                                }
                            });
                        },
                        error -> {
                            Platform.runLater(() -> {
                                verPDFButton.setDisable(true);
                                verPDFButton.setText("Error al cargar documentos");
                            });
                        }
                );
    }

    @FXML
    private void verPDF() {
        if (formatoAId == null) {
            showAlert("Error", "No hay Formato A disponible para este proyecto");
            return;
        }

        // Mostrar indicador de carga
        verPDFButton.setText("Cargando...");
        verPDFButton.setDisable(true);

        documentoApi.descargarDocumento(docID)
                .subscribe(
                        pdfBytes -> {
                            Platform.runLater(() -> {
                                verPDFButton.setText("Ver PDF");
                                verPDFButton.setDisable(false);

                                // Mostrar el PDF en una ventana
                                String tituloPDF = "Formato A - " + tituloProyectoLabel.getText();
                                PdfViewerUtil.mostrarPDFDesdeBytes(pdfBytes, tituloPDF);
                            });
                        },
                        error -> {
                            Platform.runLater(() -> {
                                verPDFButton.setText("Ver PDF");
                                verPDFButton.setDisable(false);
                                showAlert("Error", "No se pudo cargar el PDF: " + error.getMessage());
                            });
                        }
                );
    }

    @FXML
    private void aprobarFormatoA() {
        String observaciones = observacionesTextArea.getText();

        if (observaciones.isEmpty()) {
            showAlert("Error", "Falta indicar las observaciones");
            return;
        }

        if (formatoAId == null) {
            showAlert("Error", "No se ha seleccionado un proyecto");
            return;
        }

        aprobarButton.setDisable(true);
        rechazarButton.setDisable(true);

        formatoAApi.evaluarFormatoA(formatoAId, true, observaciones)
                .subscribe(
                        response -> {
                            Platform.runLater(() -> {
                                showAlert("Formato A Aprobado",
                                        "El Formato A ha sido aprobado exitosamente.\n" +
                                                "Estado: " + response.get("estadoActual") + "\n" +
                                                "Intento: " + response.get("numeroIntento"));

                            });
                        },
                        error -> {
                            Platform.runLater(() -> {
                                aprobarButton.setDisable(false);
                                rechazarButton.setDisable(false);
                                showAlert("Error",
                                        "No se pudo aprobar el Formato A: " + error.getMessage());
                            });
                        }
                );
    }

    @FXML
    private void rechazarFormatoA() {
        String observaciones = observacionesTextArea.getText();

        if (observaciones.isEmpty()) {
            showAlert("Error", "Falta indicar las observaciones");
            return;
        }

        if (formatoAId == null) {
            showAlert("Error", "No se ha seleccionado un proyecto");
            return;
        }

        aprobarButton.setDisable(true);
        rechazarButton.setDisable(true);

        formatoAApi.evaluarFormatoA(formatoAId, false, observaciones)
                .subscribe(
                        response -> {
                            Platform.runLater(() -> {
                                showAlert("Formato A Rechazado",
                                        "El Formato A ha sido rechazado.\n" +
                                                "Observaciones: " + observaciones + "\n" +
                                                "El docente puede reintentar el proyecto.");

                            });
                        },
                        error -> {
                            Platform.runLater(() -> {
                                aprobarButton.setDisable(false);
                                rechazarButton.setDisable(false);
                                showAlert("Error",
                                        "No se pudo rechazar el Formato A: " + error.getMessage());
                            });
                        }
                );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}