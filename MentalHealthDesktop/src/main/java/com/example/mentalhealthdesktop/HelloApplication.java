package com.example.mentalhealthdesktop;

import javafx.application.Application;
import javafx.fxml. FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass(). getResource("/com/example/mentalhealthdesktop/Scene1.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 1000);
            stage.setTitle("Mental Health Desktop App");
            stage. setScene(scene);
           // stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            System.out.println("Scene1.fxml loaded successfully!");

        } catch (Exception e) {
            System.err. println(" Error loading FXML file!");
            e. printStackTrace();
        }
    }
}