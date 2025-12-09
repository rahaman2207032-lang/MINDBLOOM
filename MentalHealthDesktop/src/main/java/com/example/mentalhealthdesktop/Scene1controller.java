package com.example.mentalhealthdesktop;

import javafx.fxml. FXML;
import javafx. scene.control.Label;
import javafx.scene.control.RadioButton;

import java.awt.event.ActionEvent;

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
        });

        chk2.setOnAction(event -> {
            System.out. println("Second radio button selected");
        });
    }

    public void onRadioButtonClicked(ActionEvent event) {

    }
}