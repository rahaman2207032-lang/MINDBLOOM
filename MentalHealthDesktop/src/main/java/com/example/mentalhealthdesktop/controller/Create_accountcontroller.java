package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.JournalEntry;
import com.example.mentalhealthdesktop.service.JournalService;
import com.example.mentalhealthdesktop.view.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx. scene.layout.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

public class Create_accountcontroller implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML private Label create;
    @FXML private TextField username, passtext,passtext1;
    @FXML private PasswordField password,password1;
    @FXML private Button Signup,back;
    @FXML private ToggleButton Toggle,Toggle1;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("IMG");


        URL imageUrl = getClass().getResource("/images/background.jpg");
        if (imageUrl == null) {
            System.out.println("ERROR: Image NOT FOUND at /images/background.jpg");

        } else {
            System.out.println(" Image found at: " + imageUrl);
        }


        try {
            Image image = new Image(getClass().getResourceAsStream("/images/background.jpg"));
            if (image.isError()) {
                System.out.println(" ERROR: Image could not be loaded!");
            } else {
                System.out.println(" Image loaded successfully!");
                System.out.println("   Size: " + image.getWidth() + " x " + image.getHeight());

                BackgroundImage bgImage = new BackgroundImage(
                        image,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(800, 1000, true, true, true, true)
                );
                rootPane.setBackground(new Background(bgImage));
                System.out.println(" Background set programmatically!");

            }
        } catch (Exception e) {
            System.out.println(" EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
        password.setVisible(true);
        password.setManaged(true);
        password1.setVisible(true);
        password1.setManaged(true);
        passtext.textProperty().bindBidirectional(password.textProperty());
        passtext1.textProperty().bindBidirectional(password1.textProperty());
        Toggle.setOnAction(event ->{togglePasswordVisibility();});
        Toggle1.setOnAction(event ->{togglePasswordVisibility1();});

        back.setOnAction(event -> {
            switchTologin(back, "Login_Signup.fxml", "Role Selection");
        });

        Signup.setOnAction(event -> {
            if(username.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Username cannot be empty!");
                alert.showAndWait();
            }
            else if(password.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Password cannot be empty!");
                alert.showAndWait();
            }
            else if(!password.getText().equals(password1.getText())){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Passwords do not match!");
                alert.showAndWait();
            }
            else{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Account created successfully!");
                alert.showAndWait();

                if(Objects.equals(Dataholder.selectedOption, "User")) {
                    try {
                        URL url = new URL("http://localhost:8080/api/users/register");
                        User createdUser = handleCreateAccount(url);
                        Dataholder.userId = createdUser.getId();
                        System.out.println("Logged in as userId: " + Dataholder.userId);

                        // Create first journal entry
                        JournalService journalService = new JournalService();
                        JournalEntry entry = new JournalEntry();
                        entry.setUserId(Dataholder.userId);
                        entry.setContent("First journal entry");
                        journalService.saveJournalEntry(entry.getContent());
                        swtichtouserdashboard(Signup, "User_dash.fxml", "User Dashboard");


                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                }
                else {
                    try {
                        URL url = new URL("http://localhost:8080/api/instructors/register");
                        User createdUser = handleCreateAccount(url);
                        Dataholder.userId = createdUser.getId();
                        System.out.println("Logged in as userId: " + Dataholder.userId);

                        // Create first journal entry
                        JournalService journalService = new JournalService();
                        JournalEntry entry = new JournalEntry();
                        entry.setUserId(Dataholder.userId);
                        entry.setContent("First journal entry");
                        journalService.saveJournalEntry(entry.getContent());
                        swtichtoinstructordashboard(Signup, "Instructor_dash.fxml", "Instructor Dashboard");

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                }

            }
        });

    }

    public void switchTologin(Button button, String fxmlFile, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/mentalhealthdesktop/" + fxmlFile)
            );
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) button.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 1000);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(false);
            stage.show();

            System.out.println("Switched to " + fxmlFile + " successfully!");

        } catch (Exception e) {
            System.out.println(" ERROR loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void togglePasswordVisibility() {
        boolean visible = Toggle.isSelected();
        password.setVisible(!visible);
        password.setManaged(!visible);
        passtext.setVisible(visible);
        passtext.setManaged(visible);
        if (visible) {
            passtext.requestFocus();
            passtext.positionCaret(passtext.getText().length());
            Toggle.setText("🙈");
        }
        else {
            password.requestFocus();
            password.positionCaret(password.getText().length());
            Toggle.setText("👁");
        }
    }


    public void togglePasswordVisibility1() {
        boolean visible = Toggle1.isSelected();
        password1.setVisible(!visible);
        password1.setManaged(!visible);
        passtext1.setVisible(visible);
        passtext1.setManaged(visible);
        if (visible) {
            passtext1.requestFocus();
            passtext1.positionCaret(passtext1.getText().length());
            Toggle1.setText("🙈");
        }
        else {
            password1.requestFocus();
            password1.positionCaret(password1.getText().length());
            Toggle1.setText("👁");
        }
    }



    public void swtichtouserdashboard(Button button, String fxmlFile, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/mentalhealthdesktop/" + fxmlFile)
            );
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) button.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 600);
            stage.setScene(scene);
            stage.setTitle(title);
         //   stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            System.out.println("Switched to " + fxmlFile + " successfully!");

        } catch (Exception e) {
            System.out.println(" ERROR loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void swtichtoinstructordashboard(Button button, String fxmlFile, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/mentalhealthdesktop/" + fxmlFile)
            );
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) button.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1400, 900);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(true);
            stage.setMinWidth(1200);
            stage.setMinHeight(700);
            stage.show();

            System.out.println("Switched to " + fxmlFile + " successfully!");

        } catch (Exception e) {
            System.out.println(" ERROR loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML

    private User handleCreateAccount(URL url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String json = """
        {
          "username": "%s",
          "password": "%s"
        }
        """.formatted(username.getText(), password.getText());

        try (OutputStream os = con.getOutputStream()) {
            os.write(json.getBytes());
        }

        int responseCode = con.getResponseCode();
        if (responseCode == 200 || responseCode == 201) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                // Gson with LocalDateTime support
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class,
                                (JsonDeserializer<LocalDateTime>) (jsonElement, type, context) ->
                                        LocalDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_DATE_TIME))
                        .registerTypeAdapter(LocalDateTime.class,
                                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)))
                        .create();
                return gson.fromJson(response.toString(), User.class);  // <-- return user
            }
        } else {
            throw new RuntimeException("Failed to create account. Response code: " + responseCode);
        }
    }



}