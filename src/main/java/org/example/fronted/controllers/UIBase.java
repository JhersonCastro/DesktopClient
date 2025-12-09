package org.example.fronted.controllers;

import lombok.Setter;

@Setter
public abstract class UIBase {
    protected MainController mainController;
    Object[] args;
    protected void loadView(String fxmlPath, Object... args) {
        if (mainController != null) {
            mainController.loadView(fxmlPath, args);
        } else {
            System.err.println("Error: MainController no configurado en UIBase");
        }
    }

    protected void showError(String message) {
        // Podrías agregar un método en MainController para mostrar errores
        System.err.println("ERROR: " + message);
    }

}