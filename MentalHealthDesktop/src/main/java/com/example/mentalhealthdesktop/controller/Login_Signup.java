package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.Dataholder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx. scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class Login_Signup implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML private Label login;
    @FXML private TextField username, passtext;
    @FXML private PasswordField password;
    @FXML private Button loginButton,back;
    @FXML private ToggleButton Toggle;
    @FXML private Text message;  // Changed from Label to Text to match FXML
    @FXML private Hyperlink Signup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("img debug test");


        URL imageUrl = getClass().getResource("/images/background.jpg");
        if (imageUrl == null) {
            System.out.println("ERROR: Image NOT FOUND at /images/background.jpg");
            System.out.println("Check that your image is in:  src/main/resources/images/");
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
        passtext.textProperty().bindBidirectional(password.textProperty());

        Toggle.setOnAction(event ->{togglePasswordVisibility();});

        back.setOnAction(event -> {
         switchToScene1(back, "Scene1.fxml", "Role Selection");
        });

        Signup.setOnAction(event -> {
            switchTo_account_creation(Signup,"Create_account.fxml", "Create Account");
        });

        loginButton.setOnAction(event -> {

            String user = username.getText();
            String pass = password.getText();


            System.out.println("Attempting login with Username: " + user + " Password: " + pass);


            if(user.isEmpty() || pass.isEmpty()) {
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
            } else {

                if(Objects.equals(Dataholder.selectedOption, "User")){
                    String json = """
{
  "username": "%s",
  "password": "%s",
  "role": "USER"
}
""".formatted(user, pass);

                    HttpURLConnection con =
                            null;
                    try {
                        con = (HttpURLConnection) new URL("http://localhost:8080/auth/login").openConnection();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        con.setRequestMethod("POST");
                    } catch (ProtocolException e) {
                        throw new RuntimeException(e);
                    }
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);

                    try (OutputStream os = con.getOutputStream()) {
                        os.write(json.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    int responseCode = 0;
                    try {
                        responseCode = con.getResponseCode();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (responseCode == 200) {
                        // Store logged-in user info and parse userId from response
                        try {
                            java.io.BufferedReader in = new java.io.BufferedReader(
                                new java.io.InputStreamReader(con.getInputStream())
                            );
                            StringBuilder responseStr = new StringBuilder();
                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                responseStr.append(inputLine);
                            }
                            in.close();

                            // Parse JSON response to get userId
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            com.google.gson.JsonObject jsonResponse = gson.fromJson(
                                responseStr.toString(),
                                com.google.gson.JsonObject.class
                            );

                            // Store user information
                            Dataholder.loggedInUsername = user;
                            Dataholder.userId = jsonResponse.get("userId").getAsLong();

                            System.out.println("✅ Logged in successfully!");
                            System.out.println("   Username: " + Dataholder.loggedInUsername);
                            System.out.println("   User ID: " + Dataholder.userId);

                        } catch (Exception e) {
                            System.err.println("Error parsing login response: " + e.getMessage());
                            e.printStackTrace();
                        }

                        swtichtouserdashboard(loginButton, "User_dash.fxml", "User Dashboard");
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Login Failed");
                        alert.setHeaderText(null);
                        alert.setContentText("Invalid username or password.");
                        alert.showAndWait();
                    }


                }
                else {
                    String json = """
{
  "username": "%s",
  "password": "%s",
  "role": "INSTRUCTOR"
}
""".formatted(user, pass);

                    HttpURLConnection con =
                            null;
                    try {
                        con = (HttpURLConnection) new URL("http://localhost:8080/auth/login").openConnection();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        con.setRequestMethod("POST");
                    } catch (ProtocolException e) {
                        throw new RuntimeException(e);
                    }
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setDoOutput(true);

                    try (OutputStream os = con.getOutputStream()) {
                        os.write(json.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    int responseCode = 0;
                    try {
                        responseCode = con.getResponseCode();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (responseCode == 200) {
                        // Store logged-in instructor info and parse userId from response
                        try {
                            java.io.BufferedReader in = new java.io.BufferedReader(
                                new java.io.InputStreamReader(con.getInputStream())
                            );
                            StringBuilder responseStr = new StringBuilder();
                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                responseStr.append(inputLine);
                            }
                            in.close();

                            // Parse JSON response to get userId
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            com.google.gson.JsonObject jsonResponse = gson.fromJson(
                                responseStr.toString(),
                                com.google.gson.JsonObject.class
                            );

                            // Store instructor information
                            Dataholder.loggedInUsername = user;
                            Dataholder.userId = jsonResponse.get("userId").getAsLong();

                            System.out.println("✅ Instructor logged in successfully!");
                            System.out.println("   Username: " + Dataholder.loggedInUsername);
                            System.out.println("   Instructor ID: " + Dataholder.userId);

                        } catch (Exception e) {
                            System.err.println("Error parsing instructor login response: " + e.getMessage());
                            e.printStackTrace();
                        }

                        swtichtoinstructordashboard(loginButton, "Instructor_dash.fxml", "Instructor Dashboard");
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Login Failed");
                        alert.setHeaderText(null);
                        alert.setContentText("Invalid username or password.");
                        alert.showAndWait();
                    }


                }
            }

        });
    }

    public void switchToScene1(Button button, String fxmlFile, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/mentalhealthdesktop/" + fxmlFile)
            );
            Parent root = loader.load();

            Stage stage = (Stage) button.getScene().getWindow();
             Scene scene = new Scene(root, 800, 1000);
            stage.setScene(scene);
            stage.setTitle(title);
         //   stage.setResizable(false);
            stage.show();

            System.out.println("Switched to " + fxmlFile + " successfully!");

        } catch (Exception e) {
            System.out.println(" ERROR loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void switchTo_account_creation(Hyperlink button, String fxmlFile, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/mentalhealthdesktop/" + fxmlFile)
            );
            Parent root = loader.load();

           Stage stage = (Stage) button.getScene().getWindow();
           Scene scene = new Scene(root, 800, 1000);
            stage.setScene(scene);
            stage.setTitle(title);
         //   stage.setResizable(false);
            stage.show();

            System.out.println("Switched to " + fxmlFile + " successfully!");

        } catch (Exception e) {
            System.out.println(" ERROR loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void togglePasswordVisibility() {
        boolean visible =Toggle.isSelected();
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
    public void swtichtouserdashboard(Button button, String fxmlFile, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/mentalhealthdesktop/" + fxmlFile)
            );
           Parent root = loader.load();

            Stage stage =(Stage)button.getScene().getWindow();
           Scene scene = new Scene(root, 1000, 600);


            stage.setScene(scene);
            stage.setTitle(title);
            stage.setScene(scene);
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
              Parent root = loader.load();

           Stage stage = (Stage) button.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
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

}
