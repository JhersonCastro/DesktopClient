module org.example.fronted {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires spring.webflux;

    opens org.example.fronted to javafx.fxml;

    // ⭐ Necesario para que JavaFX pueda crear la instancia del controlador
    opens org.example.fronted.controllers to javafx.fxml;

    // ⭐ Opcional: exporta tu paquete de controladores por si otro módulo lo usa
    exports org.example.fronted.controllers;

    exports org.example.fronted;
}
