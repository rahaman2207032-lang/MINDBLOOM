package com.example.mentalhealthdesktop;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx. scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Login_Signup implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML private Label login,message;
    @FXML private TextField username, pass;
    @FXML private PasswordField password;
    @FXML private Button loginButton;
    @FXML private ToggleButton Toggle;

    @FXML private Hyperlink Signup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== IMAGE DEBUG TEST ===");


        URL imageUrl = getClass().getResource("/images/background.jpg");
        if (imageUrl == null) {
            System.out.println("❌ ERROR: Image NOT FOUND at /images/background.jpg");
            System.out.println("Check that your image is in:  src/main/resources/images/");
        } else {
            System.out.println("✅ Image found at: " + imageUrl);
        }


        try {
            Image image = new Image(getClass().getResourceAsStream("/images/background.jpg"));
            if (image.isError()) {
                System.out.println("❌ ERROR: Image could not be loaded!");
            } else {
                System.out.println("✅ Image loaded successfully!");
                System.out.println("   Size: " + image.getWidth() + " x " + image.getHeight());

                BackgroundImage bgImage = new BackgroundImage(
                        image,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(400, 600, true, true, true, true)
                );
                rootPane.setBackground(new Background(bgImage));
                System.out.println("✅ Background set programmatically!");

            }
        } catch (Exception e) {
            System.out.println("❌ EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
        password.setVisible(false);
        password.setManaged(false);
        pass.textProperty().bindBidirectional(password.textProperty());

        Toggle.setOnAction(event ->{togglePasswordVisibility();});

    }

    public void togglePasswordVisibility() {
        boolean visible = Toggle.isSelected();
        password.setVisible(!visible);
        password.setManaged(!visible);
        pass.setVisible(visible);
        pass.setManaged(visible);
        if (visible) {
            pass.requestFocus();
            pass.positionCaret(pass.getText().length());
           Toggle.setText("\uD83D\uDC41");
        }
        else {
            password.requestFocus();
            password.positionCaret(password.getText().length());
            Toggle.setText("\uD83D\uDC41");
        }
    }
}