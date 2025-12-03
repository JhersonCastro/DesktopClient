package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
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
    private String pdfPath;
    private String nombreArchivo;
    private PDDocument document;
    private PDFRenderer renderer;
    private List<Image> paginas = new ArrayList<>();
    private List<ImageView> imageViews = new ArrayList<>();
    private int paginaActual = 0;
    private double zoomLevel = 1.0;
    private final double ZOOM_STEP = 0.1;
    private ExecutorService executorService;
    private long fileSize;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        executorService = Executors.newSingleThreadExecutor();

        // Configurar el scroll pane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Inicializar etiquetas
        zoomLabel.setText("100%");
        paginaActualLabel.setText("Página 1");
        estadoLabel.setText("Listo");

        // Configurar el contenedor de imágenes
        imageContainer.setAlignment(Pos.TOP_CENTER);
        imageContainer.setSpacing(20);
        imageContainer.setStyle("-fx-padding: 20;");
    }

    public void setPdfPath(String path, String nombre) {
        this.pdfPath = path;
        this.nombreArchivo = nombre;
        this.nombreArchivoLabel.setText(nombre);

        // Cargar el PDF en un hilo separado
        cargarPDF();
    }

    private void cargarPDF() {
        estadoLabel.setText("Cargando PDF...");
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Progresso indeterminado

        executorService.submit(() -> {
            try {
                // Verificar que el archivo existe
                File file = new File(pdfPath);
                if (!file.exists()) {
                    Platform.runLater(() -> {
                        estadoLabel.setText("Error: Archivo no encontrado");
                        mostrarError("El archivo no existe: " + pdfPath);
                    });
                    return;
                }

                fileSize = file.length();

                // Cargar documento PDF
                document = PDDocument.load(file);
                renderer = new PDFRenderer(document);

                int totalPaginas = document.getNumberOfPages();

                Platform.runLater(() -> {
                    infoLabel.setText(String.format("%d páginas | %.1f MB",
                            totalPaginas, fileSize / (1024.0 * 1024.0)));
                });

                // Renderizar primera página inmediatamente
                renderizarPagina(0);

                // Cargar el resto de páginas en segundo plano
                for (int i = 1; i < totalPaginas; i++) {
                    final int paginaIndex = i;
                    Platform.runLater(() -> {
                        estadoLabel.setText(String.format("Cargando página %d de %d...",
                                paginaIndex + 1, totalPaginas));
                    });
                    renderizarPagina(paginaIndex);
                    Thread.sleep(100); // Pequeña pausa para no sobrecargar
                }

                Platform.runLater(() -> {
                    estadoLabel.setText("PDF cargado completamente");
                    progressBar.setVisible(false);
                    mostrarPagina(0);
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
            imageView.setFitWidth(scrollPane.getWidth() - 40); // Margen

            // Agregar número de página
            Label numeroLabel = new Label("Página " + (numeroPagina + 1));
            numeroLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #555;");

            VBox paginaBox = new VBox(numeroLabel, imageView);
            paginaBox.setSpacing(5);
            paginaBox.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");

            imageViews.add(imageView);
            imageContainer.getChildren().add(paginaBox);

            // Actualizar progreso
            double progreso = (double) (numeroPagina + 1) / document.getNumberOfPages();
            progressBar.setProgress(progreso);
        });
    }

    private void mostrarPagina(int numeroPagina) {
        if (numeroPagina >= 0 && numeroPagina < paginas.size()) {
            paginaActual = numeroPagina;

            // Desplazar al viewport a esta página
            VBox paginaBox = (VBox) imageContainer.getChildren().get(numeroPagina);
            scrollPane.setContent(paginaBox);

            // Actualizar etiqueta
            paginaActualLabel.setText(String.format("Página %d de %d",
                    numeroPagina + 1, paginas.size()));

            // Aplicar zoom actual
            aplicarZoom();
        }
    }

    private void aplicarZoom() {
        for (int i = 0; i < imageViews.size(); i++) {
            ImageView imageView = imageViews.get(i);
            imageView.setFitWidth((scrollPane.getWidth() - 40) * zoomLevel);
        }
        zoomLabel.setText(String.format("%d%%", (int)(zoomLevel * 100)));

        // Volver a mostrar la página actual después del zoom
        mostrarPagina(paginaActual);
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
        if (paginaActual < paginas.size() - 1) {
            mostrarPagina(paginaActual + 1);
        }
    }

    @FXML
    private void ultimaPagina() {
        mostrarPagina(paginas.size() - 1);
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
        estadoLabel.setText("Descargando...");

        executorService.submit(() -> {
            try {
                File sourceFile = new File(pdfPath);
                if (!sourceFile.exists()) {
                    Platform.runLater(() -> {
                        estadoLabel.setText("Archivo no encontrado");
                    });
                    return;
                }

                String userHome = System.getProperty("user.home");
                File downloadDir = new File(userHome + "/Downloads");
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs();
                }

                File destFile = new File(downloadDir, nombreArchivo);

                // Copiar archivo
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                Platform.runLater(() -> {
                    estadoLabel.setText("Descargado en: " + destFile.getPath());

                    // Mostrar confirmación
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Descarga completada");
                    alert.setHeaderText("PDF descargado exitosamente");
                    alert.setContentText("El archivo se ha guardado en:\n" + destFile.getPath());
                    alert.show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    estadoLabel.setText("Error en descarga");
                    mostrarError("Error al descargar: " + e.getMessage());
                });
            }
        });
    }

    @FXML
    private void imprimirPDF() {
        estadoLabel.setText("Preparando para imprimir...");

        try {
            if (document != null) {
                // Guardar PDF temporalmente para imprimir
                File tempFile = File.createTempFile("pdf_print_", ".pdf");
                document.save(tempFile);

                // Abrir diálogo de impresión del sistema
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().print(tempFile);
                    estadoLabel.setText("Enviado a impresora");
                } else {
                    estadoLabel.setText("Impresión no soportada");
                }

                // Eliminar archivo temporal después de un tiempo
                tempFile.deleteOnExit();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al cargar PDF");
        alert.setContentText(mensaje);
        alert.show();
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
    }
}