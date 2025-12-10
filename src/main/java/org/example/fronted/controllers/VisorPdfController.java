package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.example.fronted.api.DocumentApi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VisorPdfController implements Initializable {

    // Componentes FXML
    @FXML private Label nombreArchivoLabel;
    @FXML private Label estadoLabel;
    @FXML private Label paginaActualLabel;
    @FXML private Label zoomLabel;
    @FXML private Label infoLabel;
    @FXML private ProgressBar progressBar;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox imageContainer;

    // Variables de estado
    private String nombreArchivo;
    private PDDocument document;
    private PDFRenderer renderer;
    private List<Image> paginas = new ArrayList<>();
    private List<ImageView> imageViews = new ArrayList<>();
    private List<VBox> paginaBoxes = new ArrayList<>();
    private int paginaActual = 0;
    private double zoomLevel = 1.0;
    private final double ZOOM_STEP = 0.1;
    private ExecutorService executorService;
    private long fileSize;
    private int totalPaginas = 0;

    // API para documentos
    private DocumentApi documentApi;
    private Long documentoId;
    private File archivoDescargado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        executorService = Executors.newSingleThreadExecutor();
        documentApi = new DocumentApi();

        // Configurar el scroll pane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Inicializar etiquetas
        zoomLabel.setText("100%");
        paginaActualLabel.setText("Página 1");
        estadoLabel.setText("Esperando documento...");

        // Configurar el contenedor de imágenes
        imageContainer.setAlignment(Pos.TOP_CENTER);
        imageContainer.setSpacing(20);
        imageContainer.setStyle("-fx-padding: 20;");
    }

    /**
     * Método para establecer el documento desde el servidor
     */
    public void setDocumentoDesdeServidor(Long documentoId, String nombre) {
        this.documentoId = documentoId;
        this.nombreArchivo = nombre;
        this.nombreArchivoLabel.setText(nombre != null ? nombre : "Documento #" + documentoId);

        // Descargar y cargar el PDF
        descargarYCargarPDF();
    }

    /**
     * Método para establecer documento desde archivo local (mantener compatibilidad)
     */
    public void setPdfPath(String path, String nombre) {
        this.nombreArchivo = nombre;
        this.nombreArchivoLabel.setText(nombre);

        // Cargar desde archivo local
        cargarPDFLocal(path);
    }

    private void descargarYCargarPDF() {
        if (documentoId == null) {
            mostrarError("No se ha especificado un documento para visualizar");
            return;
        }

        estadoLabel.setText("Descargando documento...");
        progressBar.setVisible(true);
        progressBar.setProgress(-1);

        documentApi.descargarDocumento(documentoId)
                .subscribe(
                        archivo -> {
                            archivoDescargado = archivo;
                            Platform.runLater(() -> {
                                estadoLabel.setText("Documento descargado, cargando...");
                                cargarPDFLocal(archivo.getAbsolutePath());
                            });
                        },
                        error -> {
                            Platform.runLater(() -> {
                                estadoLabel.setText("Error descargando documento");
                                mostrarError("No se pudo descargar el documento: " + error.getMessage());
                                progressBar.setVisible(false);
                            });
                            System.err.println("Error descargando documento: " + error.getMessage());
                        }
                );
    }

    private void cargarPDFLocal(String path) {
        estadoLabel.setText("Cargando PDF...");
        progressBar.setVisible(true);
        progressBar.setProgress(-1);

        executorService.submit(() -> {
            try {
                // Verificar que el archivo existe
                File file = new File(path);
                if (!file.exists()) {
                    Platform.runLater(() -> {
                        estadoLabel.setText("Error: Archivo no encontrado");
                        mostrarError("El archivo no existe: " + path);
                    });
                    return;
                }

                fileSize = file.length();

                // Cargar documento PDF
                document = PDDocument.load(file);
                renderer = new PDFRenderer(document);

                totalPaginas = document.getNumberOfPages();

                Platform.runLater(() -> {
                    infoLabel.setText(String.format("%d páginas | %.1f MB",
                            totalPaginas, fileSize / (1024.0 * 1024.0)));
                });

                // Limpiar listas y contenedor antes de cargar
                Platform.runLater(() -> {
                    paginas.clear();
                    imageViews.clear();
                    paginaBoxes.clear();
                    imageContainer.getChildren().clear();
                });

                // Cargar todas las páginas
                for (int i = 0; i < totalPaginas; i++) {
                    final int paginaIndex = i;

                    // Actualizar estado para cada página
                    Platform.runLater(() -> {
                        estadoLabel.setText(String.format("Cargando página %d de %d...",
                                paginaIndex + 1, totalPaginas));
                    });

                    // Renderizar página
                    renderizarPagina(paginaIndex);

                    // Actualizar progreso
                    final double progreso = (double) (paginaIndex + 1) / totalPaginas;
                    Platform.runLater(() -> {
                        progressBar.setProgress(progreso);
                    });

                    // Pequeña pausa para no sobrecargar
                    Thread.sleep(100);
                }

                Platform.runLater(() -> {
                    estadoLabel.setText("PDF cargado completamente");
                    progressBar.setVisible(false);

                    // Solo llamar a mostrarPagina si hay páginas cargadas
                    if (!paginaBoxes.isEmpty()) {
                        mostrarPagina(0);
                    } else {
                        estadoLabel.setText("Error: No se cargaron páginas");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    estadoLabel.setText("Error al cargar PDF");
                    mostrarError("Error: " + e.getMessage());
                });
            }
        });
    }

    private void renderizarPagina(int numeroPagina) throws IOException {
        // Renderizar la página con calidad decente
        BufferedImage awtImage = renderer.renderImageWithDPI(numeroPagina, 150);
        Image fxImage = SwingFXUtils.toFXImage(awtImage, null);

        Platform.runLater(() -> {
            paginas.add(fxImage);

            // Crear ImageView para esta página
            ImageView imageView = new ImageView(fxImage);
            imageView.setPreserveRatio(true);

            // Obtener el ancho disponible (usar valor por defecto si aún no está disponible)
            double availableWidth = scrollPane.getWidth() > 0 ? scrollPane.getWidth() - 40 : 800;
            imageView.setFitWidth(availableWidth);

            // Agregar número de página
            Label numeroLabel = new Label("Página " + (numeroPagina + 1));
            numeroLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #555;");

            VBox paginaBox = new VBox(numeroLabel, imageView);
            paginaBox.setSpacing(5);
            paginaBox.setStyle("-fx-background-color: white; -fx-padding: 10; " +
                    "-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");

            imageViews.add(imageView);
            paginaBoxes.add(paginaBox); // Agregar a la lista
            imageContainer.getChildren().add(paginaBox);
        });
    }

    private void mostrarPagina(int numeroPagina) {
        // Verificar límites usando paginaBoxes en lugar de paginas
        if (numeroPagina >= 0 && numeroPagina < paginaBoxes.size()) {
            paginaActual = numeroPagina;

            // Actualizar etiqueta
            paginaActualLabel.setText(String.format("Página %d de %d",
                    numeroPagina + 1, paginaBoxes.size()));

            // Aplicar zoom actual
            aplicarZoom();

            // Desplazar a la página usando scrollTo
            Platform.runLater(() -> {
                try {
                    // Calcular posición aproximada para desplazar
                    double targetY = 0;
                    for (int i = 0; i < numeroPagina && i < paginaBoxes.size(); i++) {
                        VBox box = paginaBoxes.get(i);
                        if (box != null) {
                            targetY += box.getHeight() + imageContainer.getSpacing();
                        }
                    }

                    // Calcular el valor V basado en la posición
                    double totalHeight = imageContainer.getHeight();
                    if (totalHeight > 0) {
                        double vvalue = targetY / totalHeight;
                        scrollPane.setVvalue(vvalue);
                    }

                } catch (Exception e) {
                    System.err.println("Error al desplazar a la página: " + e.getMessage());
                }
            });
        } else {
            System.err.println("Índice de página fuera de rango: " + numeroPagina +
                    ", total páginas: " + paginaBoxes.size());
        }
    }

    private void aplicarZoom() {
        Platform.runLater(() -> {
            // Aplicar zoom solo si hay imageViews
            if (!imageViews.isEmpty()) {
                double baseWidth = scrollPane.getWidth() - 40;
                if (baseWidth <= 0) baseWidth = 800; // Ancho por defecto

                for (ImageView imageView : imageViews) {
                    imageView.setFitWidth(baseWidth * zoomLevel);
                }
                zoomLabel.setText(String.format("%d%%", (int)(zoomLevel * 100)));
            }
        });
    }

    // Métodos de navegación
    @FXML
    private void primeraPagina() {
        mostrarPagina(0);
    }

    @FXML
    private void paginaAnterior() {
        if (paginaActual > 0) {
            mostrarPagina(paginaActual - 1);
        }
    }

    @FXML
    private void paginaSiguiente() {
        if (paginaActual < paginaBoxes.size() - 1) {
            mostrarPagina(paginaActual + 1);
        }
    }

    @FXML
    private void ultimaPagina() {
        if (!paginaBoxes.isEmpty()) {
            mostrarPagina(paginaBoxes.size() - 1);
        }
    }

    // Métodos de zoom
    @FXML
    private void zoomIn() {
        if (zoomLevel < 3.0) {
            zoomLevel += ZOOM_STEP;
            aplicarZoom();
        }
    }

    @FXML
    private void zoomOut() {
        if (zoomLevel > 0.5) {
            zoomLevel -= ZOOM_STEP;
            aplicarZoom();
        }
    }

    @FXML
    private void descargarPDF() {
        if (archivoDescargado == null || !archivoDescargado.exists()) {
            mostrarError("No hay documento descargado para guardar");
            return;
        }

        estadoLabel.setText("Guardando copia...");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar documento como...");
        fileChooser.setInitialFileName(archivoDescargado.getName());
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );

        File destino = fileChooser.showSaveDialog(nombreArchivoLabel.getScene().getWindow());
        if (destino != null) {
            executorService.submit(() -> {
                try {
                    Files.copy(
                            archivoDescargado.toPath(),
                            destino.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    Platform.runLater(() -> {
                        estadoLabel.setText("Documento guardado");
                        mostrarAlerta("Éxito", "Documento guardado en: " + destino.getAbsolutePath(),
                                Alert.AlertType.INFORMATION);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        estadoLabel.setText("Error al guardar");
                        mostrarError("Error guardando documento: " + e.getMessage());
                    });
                }
            });
        } else {
            estadoLabel.setText("Operación cancelada");
        }
    }

    @FXML
    private void imprimirPDF() {
        if (archivoDescargado == null || !archivoDescargado.exists()) {
            mostrarError("No hay documento para imprimir");
            return;
        }

        estadoLabel.setText("Preparando para imprimir...");

        try {
            // Abrir con aplicación predeterminada
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(archivoDescargado);
                    estadoLabel.setText("PDF abierto - Ctrl+P para imprimir");

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Listo para imprimir");
                        alert.setHeaderText(null);
                        alert.setContentText("PDF abierto en su visor predeterminado\nPresione Ctrl+P para imprimir");
                        alert.show();
                    });
                    return;
                }
            }

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No se puede imprimir");
                alert.setHeaderText(null);
                alert.setContentText("No se pudo abrir el PDF para imprimir.\nDescárguelo e imprímalo manualmente.");
                alert.show();
                estadoLabel.setText("No se pudo abrir para imprimir");
            });

        } catch (Exception e) {
            estadoLabel.setText("Error al imprimir");
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void cerrarVisor() {
        // Cerrar documento PDF
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Cerrar executor
        if (executorService != null) {
            executorService.shutdown();
        }

        // Cerrar ventana
        Stage stage = (Stage) nombreArchivoLabel.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.show();
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(tipo);
            alerta.setTitle(titulo);
            alerta.setHeaderText(null);
            alerta.setContentText(mensaje);
            alerta.showAndWait();
        });
    }

    // Método para limpiar recursos
    public void limpiarRecursos() {
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (executorService != null) {
            executorService.shutdownNow();
        }

        paginas.clear();
        imageViews.clear();
        paginaBoxes.clear();
    }
}