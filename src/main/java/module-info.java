module org.example.fronted {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires spring.webflux;
    requires spring.web;
    requires spring.core;
    requires reactor.core;
    requires com.fasterxml.jackson.databind;
    requires static lombok;
    requires javafx.web;
    requires org.apache.pdfbox;
    requires java.desktop;
    requires javafx.swing; // <-- AÑADE ESTA LÍNEA

    opens org.example.fronted to javafx.fxml;
    opens org.example.fronted.controllers to javafx.fxml;
    opens org.example.fronted.api to spring.web;
    opens org.example.fronted.dto to com.fasterxml.jackson.databind; // <-- CAMBIA ESTA

    exports org.example.fronted.controllers;
    exports org.example.fronted;
}