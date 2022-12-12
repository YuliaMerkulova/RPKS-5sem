package com.example.lab8;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;


public class ClientTwo extends Application {

    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientOne.class.getResource("hello-view.fxml"));
        Client client = new Client("localhost", 8843, "Jhon");
        Scene scene = new Scene(fxmlLoader.load(), 879, 586);
        stage.setResizable(false);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        ClientGUI controller = fxmlLoader.getController();
        controller.setClient(client); //проставляете значение в котроллер
        client.start();
        stage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Application Closed by click to Close Button(X)");
                        System.exit(0);
                    }
                });
            }
        });
    }


    public static void main(String[] args) {
        launch();
    }
}
