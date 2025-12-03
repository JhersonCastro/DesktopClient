package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
    private List<VBox> paginaBoxes = new ArrayList<>(); // NUEVA: lista para almacenar los VBox de cada página
    private int paginaActual = 0;
    private double zoomLevel = 1.0;
    private final double ZOOM_STEP = 0.1;
    private ExecutorService executorService;
    private long fileSize;
    private int totalPaginas = 0;

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
        progressBar.setProgress(-1); // Progreso indeterminado

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
                    double targetY = IntStream.range(0, numeroPagina).filter(i -> i < paginaBoxes.size()).mapToObj(i -> paginaBoxes.get(i)).mapToDouble(box -> box.getHeight() + imageContainer.getSpacing()).sum();

                    // Usar scrollTo para desplazar suavemente
                    scrollPane.setVvalue(0); // Primero ir al inicio

                    // Desplazar después de un pequeño delay para asegurar que se haya renderizado
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            Platform.runLater(() -> {
                                // Calcular el valor V basado en la posición
                                double totalHeight = imageContainer.getHeight();
                                if (totalHeight > 0) {
                                    double vvalue = targetY / totalHeight;
                                    scrollPane.setVvalue(vvalue);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

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

    // Métodos de navegación CORREGIDOS
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

    // Métodos de zoom CORREGIDOS
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

                File destFile = new File(downloadDir, nombreArchivo +".pdf");

                // Copiar archivo
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                Platform.runLater(() -> {
                    estadoLabel.setText("Descargado exitosamente");

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
                // Guardar PDF temporalmente
                File tempFile = File.createTempFile("imprimir_", ".pdf");
                document.save(tempFile);

                // Abrir con navegador predeterminado
                boolean exito = abrirConNavegadorPredeterminado(tempFile);

                if (exito) {
                    estadoLabel.setText("PDF abierto en navegador - Ctrl+P para imprimir");

                    // Mostrar mensaje informativo
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Listo para imprimir");
                        alert.setHeaderText(null);
                        alert.setContentText("PDF abierto en su navegador\nPresione Ctrl+P para imprimir");
                        alert.show();
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Error al imprimir");
                        alert.setHeaderText(null);
                        alert.setContentText("No se pudo imprimir, intenta descargarlo para posteriormente imprimir");
                        alert.show();
                    });
                }

                // Eliminar después de 5 minutos
                programarEliminacion(tempFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            estadoLabel.setText("Error al imprimir");
            mostrarError("Error: " + e.getMessage());
        }
    }
    private void programarEliminacion(File file) {
        // Eliminar después de 5 minutos
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (file.exists()) {
                    file.delete();
                }
            }
        }, 300000); // 5 minutos = 300,000 ms
    }
    private boolean abrirConNavegadorPredeterminado(File pdfFile) {
        try {
            // Método 1: Usar Desktop (abre con aplicación predeterminada)
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(pdfFile);
                return true;
            }
            return false;
        } catch (Exception e) {
            // Si Desktop falla, intentar métodos alternativos
            return abrirConNavegadorAlternativo(pdfFile);
        }
    }
    private boolean abrirConNavegadorAlternativo(File pdfFile) {
        try {
            // Método 2: Usar comando start de Windows (abre con aplicación predeterminada)
            ProcessBuilder pb = new ProcessBuilder(
                    "cmd", "/c", "start", "", pdfFile.getAbsolutePath()
            );
            Process process = pb.start();
            return process.isAlive() || process.exitValue() == 0;
        } catch (Exception e1) {
            try {
                // Método 3: Usar Runtime.exec
                Runtime.getRuntime().exec(
                        "cmd /c start \"\" \"" + pdfFile.getAbsolutePath() + "\""
                );
                return true;
            } catch (Exception e2) {
                try {
                    // Método 4: Usar explorer
                    ProcessBuilder pb = new ProcessBuilder(
                            "explorer", pdfFile.getAbsolutePath()
                    );
                    pb.start();
                    return true;
                } catch (Exception e3) {
                    return false;
                }
            }
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
            alert.setHeaderText("Error al cargar PDF");
            alert.setContentText(mensaje);
            alert.show();
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