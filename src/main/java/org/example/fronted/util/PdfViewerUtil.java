package org.example.fronted.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.fronted.controllers.VisorPdfController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PdfViewerUtil {

    private static final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    // Método existente para archivos locales
    public static void mostrarPDF(String pdfPath, String titulo) {
        Platform.runLater(() -> {
            try {
                // Verificar que el archivo existe
                File file = new File(pdfPath);
                if (!file.exists()) {
                    mostrarError("El archivo no existe: " + pdfPath);
                    return;
                }

                // Verificar que es un PDF
                if (!pdfPath.toLowerCase().endsWith(".pdf")) {
                    mostrarError("El archivo no es un PDF válido: " + pdfPath);
                    return;
                }

                // Cargar FXML
                URL fxmlUrl = PdfViewerUtil.class.getResource("/views/utils/visor-pdf.fxml");
                if (fxmlUrl == null) {
                    mostrarError("Archivo FXML no encontrado: /views/utils/visor-pdf.fxml");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                VisorPdfController controller = loader.getController();

                // Verificar que el controlador existe
                if (controller == null) {
                    mostrarError("Error al cargar el controlador del visor PDF");
                    return;
                }

                controller.setPdfPath(pdfPath, titulo);

                // Configurar ventana
                Stage stage = new Stage();
                stage.setTitle("Visor PDF - " + titulo);

                Scene scene = new Scene(root, 1000, 700);
                stage.setScene(scene);

                // Manejar cierre de ventana
                stage.setOnCloseRequest(event -> {
                    controller.limpiarRecursos();
                });

                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                mostrarError("No se pudo cargar el visor PDF: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                mostrarError("Error inesperado: " + e.getMessage());
            }
        });
    }

    /**
     * FUNC: Descarga bytes, guarda en archivo temporal y muestra el PDF
     */
    public static void mostrarPDFDesdeBytes(byte[] pdfBytes, String tituloVentana) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            mostrarError("El documento PDF está vacío");
            return;
        }

        try {
            // Crear directorio temporal específico para la aplicación
            String userHome = System.getProperty("user.home");
            Path appTempDir = Paths.get(userHome, ".proyectos_grado", "temp_pdfs");

            if (!Files.exists(appTempDir)) {
                Files.createDirectories(appTempDir);
            }

            // Generar nombre de archivo único y descriptivo
            String nombreArchivo = generarNombreArchivo(tituloVentana);
            Path tempFilePath = appTempDir.resolve(nombreArchivo);

            // Guardar bytes en archivo
            Files.write(tempFilePath, pdfBytes);

            System.out.println("[DEBUG] PDF guardado en: " + tempFilePath.toAbsolutePath());

            // Usar el método existente para mostrar el PDF
            mostrarPDF(tempFilePath.toAbsolutePath().toString(), tituloVentana);

            // Programar limpieza después de 30 minutos
            programarLimpieza(tempFilePath.toFile());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo mostrar el PDF: " + e.getMessage());
        }
    }

    /**
     * Método sobrecargado con nombre de archivo sugerido
     */
    public static void mostrarPDFDesdeBytes(byte[] pdfBytes, String tituloVentana, String nombreSugerido) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            mostrarError("El documento PDF está vacío");
            return;
        }

        try {
            // Crear directorio temporal específico para la aplicación
            String userHome = System.getProperty("user.home");
            Path appTempDir = Paths.get(userHome, ".proyectos_grado", "temp_pdfs");

            if (!Files.exists(appTempDir)) {
                Files.createDirectories(appTempDir);
            }

            // Usar nombre sugerido o generar uno
            String nombreArchivo = (nombreSugerido != null && !nombreSugerido.isEmpty())
                    ? sanitizeFileName(nombreSugerido)
                    : generarNombreArchivo(tituloVentana);

            // Asegurar extensión .pdf
            if (!nombreArchivo.toLowerCase().endsWith(".pdf")) {
                nombreArchivo += ".pdf";
            }

            Path tempFilePath = appTempDir.resolve(nombreArchivo);

            // Si el archivo ya existe, agregar sufijo único
            int counter = 1;
            while (Files.exists(tempFilePath)) {
                String nombreBase = nombreArchivo.replace(".pdf", "");
                nombreArchivo = nombreBase + "_" + counter + ".pdf";
                tempFilePath = appTempDir.resolve(nombreArchivo);
                counter++;
            }

            // Guardar bytes en archivo
            Files.write(tempFilePath, pdfBytes);

            System.out.println("[DEBUG] PDF guardado en: " + tempFilePath.toAbsolutePath());

            // Usar el método existente para mostrar el PDF
            mostrarPDF(tempFilePath.toAbsolutePath().toString(), tituloVentana);

            // Programar limpieza después de 30 minutos
            programarLimpieza(tempFilePath.toFile());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo mostrar el PDF: " + e.getMessage());
        }
    }

    /**
     * Genera un nombre de archivo descriptivo
     */
    private static String generarNombreArchivo(String titulo) {
        // Limpiar título para nombre de archivo
        String nombreLimpio = titulo
                .replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]", "_") // Mantener letras, números y espacios
                .replaceAll("\\s+", "_") // Reemplazar espacios con _
                .trim();

        // Limitar longitud
        if (nombreLimpio.length() > 50) {
            nombreLimpio = nombreLimpio.substring(0, 50);
        }

        // Agregar timestamp único
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomId = UUID.randomUUID().toString().substring(0, 8);

        return nombreLimpio + "_" + timestamp + "_" + randomId + ".pdf";
    }

    /**
     * Sanitiza el nombre de archivo
     */
    private static String sanitizeFileName(String fileName) {
        // Eliminar caracteres no permitidos
        String sanitized = fileName
                .replaceAll("[\\\\/:*?\"<>|]", "_") // Caracteres prohibidos en Windows
                .replaceAll("\\s+", "_") // Espacios a guiones bajos
                .trim();

        // Limitar longitud
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }

        return sanitized;
    }

    /**
     * Programa la limpieza automática del archivo temporal
     */
    private static void programarLimpieza(File tempFile) {
        cleanupExecutor.schedule(() -> {
            try {
                if (tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    if (deleted) {
                        System.out.println("[DEBUG] Archivo temporal eliminado: " + tempFile.getAbsolutePath());
                    } else {
                        System.err.println("[WARN] No se pudo eliminar archivo temporal: " + tempFile.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Error al eliminar archivo temporal: " + e.getMessage());
            }
        }, 30, TimeUnit.MINUTES); // Limpiar después de 30 minutos
    }

    /**
     * Limpia todos los archivos temporales antiguos
     */
    public static void limpiarArchivosTemporales() {
        try {
            String userHome = System.getProperty("user.home");
            Path appTempDir = Paths.get(userHome, ".proyectos_grado", "temp_pdfs");

            if (!Files.exists(appTempDir)) {
                return;
            }

            long tiempoActual = System.currentTimeMillis();
            long tiempoLimite = 24 * 60 * 60 * 1000; // 24 horas

            Files.list(appTempDir)
                    .filter(path -> {
                        try {
                            return Files.isRegularFile(path) &&
                                    path.toString().toLowerCase().endsWith(".pdf") &&
                                    (tiempoActual - Files.getLastModifiedTime(path).toMillis()) > tiempoLimite;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("[DEBUG] Archivo antiguo eliminado: " + path);
                        } catch (IOException e) {
                            System.err.println("[WARN] No se pudo eliminar archivo antiguo: " + path);
                        }
                    });

        } catch (Exception e) {
            System.err.println("[ERROR] Error al limpiar archivos temporales: " + e.getMessage());
        }
    }

    /**
     * Detiene el ejecutor de limpieza al cerrar la aplicación
     */
    public static void shutdown() {
        try {
            cleanupExecutor.shutdown();
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void mostrarError(String mensaje) {
        System.err.println("Error PDF Viewer: " + mensaje);

        // Mostrar alerta en el hilo de JavaFX
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error PDF Viewer");
                alert.setHeaderText("Error al abrir PDF");
                alert.setContentText(mensaje);
                alert.show();
            } catch (Exception e) {
                // Si no podemos mostrar la alerta, al menos loguear
                System.err.println("No se pudo mostrar alerta: " + e.getMessage());
            }
        });
    }
}