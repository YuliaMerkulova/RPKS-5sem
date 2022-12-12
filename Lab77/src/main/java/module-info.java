module com.example.lab77 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires javafx.web;
    requires java.desktop;
    requires com.google.gson;

    opens com.example.lab77 to javafx.fxml;
    exports com.example.lab77;
}