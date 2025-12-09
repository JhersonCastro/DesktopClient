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
    requires static lombok;
    requires javafx.web;
    requires org.apache.pdfbox;
    requires java.desktop;
    requires javafx.swing;
    requires com.fasterxml.jackson.annotation;
    requires spring.context;
    requires javafx.graphics;
    opens org.example.fronted.config to spring.core;


    opens org.example.fronted to javafx.fxml;
    opens org.example.fronted.controllers to javafx.fxml;
    opens org.example.fronted.api to spring.web;

    exports org.example.fronted.controllers;
    exports org.example.fronted;
}
