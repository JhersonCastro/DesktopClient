package org.example.fronted.controllers;

public abstract class UIBase {
    protected MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    protected void loadView(String fxmlPath) {
        if (mainController != null) {
            mainController.loadView(fxmlPath);
        } else {
            System.err.println("Error: MainController no configurado en UIBase");
        }
    }

    protected void showError(String message) {
        // Podrías agregar un método en MainController para mostrar errores
        System.err.println("ERROR: " + message);
    }
}