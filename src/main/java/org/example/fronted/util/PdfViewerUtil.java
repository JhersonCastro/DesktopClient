package org.example.fronted.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.fronted.controllers.VisorPdfController;

import java.io.File;
import java.net.URL;

public class PdfViewerUtil {

    /**
     * Muestra un PDF desde un archivo local
     */
    public static void mostrarPDF(String pdfPath, String titulo) {
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
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            VisorPdfController controller = loader.getController();
            controller.setPdfPath(pdfPath, titulo);

            // Configurar ventana
            crearVentanaVisor(root, titulo, controller);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo cargar el visor PDF: " + e.getMessage());
        }
    }

    /**
     * Muestra un PDF descargado desde el servidor
     */
    public static void mostrarPDFDesdeServidor(Long documentoId, String titulo) {
        try {
            // Cargar FXML
            URL fxmlUrl = PdfViewerUtil.class.getResource("/views/utils/visor-pdf.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            VisorPdfController controller = loader.getController();

            // Usar el nuevo método para documentos del servidor
            controller.setDocumentoDesdeServidor(documentoId, titulo);

            // Configurar ventana
            crearVentanaVisor(root, titulo, controller);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo cargar el documento desde el servidor: " + e.getMessage());
        }
    }

    /**
     * Crea y configura la ventana del visor
     */
    private static void crearVentanaVisor(Parent root, String titulo, VisorPdfController controller) {
        Stage stage = new Stage();
        stage.setTitle("Visor PDF - " + titulo);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);

        // Manejar cierre de ventana
        stage.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.limpiarRecursos();
            }
        });

        stage.show();
    }

    private static void mostrarError(String mensaje) {
        System.err.println("Error PDF Viewer: " + mensaje);
        // Puedes agregar un diálogo de error aquí si lo necesitas
    }
}