package org.example.fronted;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el layout principal (16:9)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainLayout.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);

        // Cargar CSS principal
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

        // Configurar ventana
        primaryStage.setTitle("Universidad del Cauca - Sistema Académico");
        primaryStage.setScene(scene);

        // Mantener relación 16:9
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(576); // 1024x576 mantiene 16:9

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}