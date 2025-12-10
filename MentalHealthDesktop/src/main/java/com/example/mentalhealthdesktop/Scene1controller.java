package com.example.mentalhealthdesktop;

import javafx.fxml. FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class Scene1controller {

    @FXML
    private Label title;

    @FXML
    private Label query;

    @FXML
    private RadioButton chk;

    @FXML
    private RadioButton chk2;

    @FXML
    public void initialize() {


        System.out.println("✅ Scene1controller loaded!");


        if (title != null) {
            title.setText("MINDBLOOM");
        }

        if (query != null) {
            query.setText("What is your role?");
        }


        chk.setOnAction(event -> {
            System.out.println("First radio button selected");
            navigateToLogin("User");
        });

        chk2.setOnAction(event -> {
            System.out. println("Second radio button selected");
            navigateToLogin("Instructor");
        });
    }
    @FXML
    private void navigateToLogin(String role) {
        try {
            System.out.println("Selected role: " + role);


            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/mentalhealthdesktop/Login_Signup.fxml")
            );
            Parent root = loader.load();


            Login_Signup loginController = loader.getController();

            //loginController.setRole(role);


            Stage stage = (Stage)chk.getScene().getWindow();

            Scene scene = new Scene(root, 800, 1000);

            stage.setScene(scene);
            stage.setTitle("Login - " + role);
            stage.setResizable(false);
            stage.show();

            System.out.println("✅ Navigated to Login page!");

        } catch (IOException e) {
            System.out.println("❌ Error loading Login_Signup. fxml: " + e.getMessage());
            e.printStackTrace();


            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert. setTitle("Navigation Error");
            alert.setHeaderText("Could not load login page");
            alert.setContentText(e.getMessage());
            alert. showAndWait();
        }
    }
}
