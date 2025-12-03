package org.example.fronted.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.fronted.controllers.VisorPdfController;

import java.io.File;
import java.io.IOException;

public class PdfViewerUtil {

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
            FXMLLoader loader = new FXMLLoader(
                    PdfViewerUtil.class.getClassLoader().getResource("/views/utils/visor-pdf.fxml")
            );

            Parent root = loader.load();
            VisorPdfController controller = loader.getController();
            controller.setPdfPath(pdfPath, titulo);

            // Configurar ventana
            Stage stage = new Stage();
            stage.setTitle("Visor PDF - " + titulo);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);

            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);

            // Manejar cierre de ventana
            stage.setOnCloseRequest(event -> {
                controller.limpiarRecursos();
            });

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("No se pudo cargar el visor PDF: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            mostrarError("Archivo FXML no encontrado");
        }
    }

    private static void mostrarError(String mensaje) {
        System.err.println("Error PDF Viewer: " + mensaje);

        // Opcional: mostrar diálogo de error
        /*
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al abrir PDF");
            alert.setContentText(mensaje);
            alert.show();
        });
        */
    }
}