module com.example.lab8 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;

    opens com.example.lab8 to javafx.fxml;
    exports com.example.lab8;
}