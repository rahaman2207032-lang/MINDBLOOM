package com.example.mentalhealthdesktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MeditationController {

    @FXML
    private WebView videoWebView;

    @FXML
    private Label currentVideoTitle;

    @FXML
    private Label videoDescription;

    @FXML
    private Button backButton;

    private WebEngine webEngine;

    // YouTube video IDs for different meditation types
    private static final String BREATHING_EXERCISE = "inpok4MKVLM"; // 5-Minute Breathing Exercise
    private static final String GUIDED_MEDITATION = "ZToicYcHIOU"; // 10-Minute Guided Meditation
    private static final String SLEEP_MEDITATION = "aEqlQvczMJQ"; // 15-Minute Sleep Meditation
    private static final String BODY_SCAN = "15q-N-_kkrU"; // 8-Minute Body Scan
    private static final String STRESS_RELIEF = "z6X5oEIg6Ak"; // 7-Minute Stress Relief
    private static final String MORNING_MEDITATION = "tEmt1l9DP4c"; // 10-Minute Morning Meditation

    @FXML
    public void initialize() {
        webEngine = videoWebView.getEngine();

        // Load default video (Breathing Exercise)
        loadVideo(BREATHING_EXERCISE, "5 Minute Breathing Exercise",
                 "A simple breathing exercise to help you relax and center yourself.");
    }

    /**
     * Load a YouTube video in the WebView
     */
    private void loadVideo(String videoId, String title, String description) {
        currentVideoTitle.setText(title);
        videoDescription.setText(description);

        // Embed YouTube video using iframe
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        background-color: #000;
                    }
                    .video-container {
                        position: relative;
                        width: 100%%;
                        padding-bottom: 56.25%%;
                        height: 0;
                    }
                    .video-container iframe {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%%;
                        height: 100%%;
                        border: 0;
                    }
                </style>
            </head>
            <body>
                <div class="video-container">
                    <iframe 
                        src="https://www.youtube.com/embed/%s?autoplay=0&rel=0&modestbranding=1" 
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                        allowfullscreen>
                    </iframe>
                </div>
            </body>
            </html>
            """.formatted(videoId);

        webEngine.loadContent(html);
    }

    @FXML
    private void loadBreathingExercise() {
        loadVideo(BREATHING_EXERCISE, "5 Minute Breathing Exercise",
                 "A calming breathing practice to reduce anxiety and increase mindfulness.");
    }

    @FXML
    private void loadGuidedMeditation() {
        loadVideo(GUIDED_MEDITATION, "10 Minute Guided Meditation",
                 "A gentle guided meditation for relaxation and inner peace.");
    }

    @FXML
    private void loadSleepMeditation() {
        loadVideo(SLEEP_MEDITATION, "15 Minute Sleep Meditation",
                 "A soothing meditation to help you fall asleep peacefully.");
    }

    @FXML
    private void loadBodyScan() {
        loadVideo(BODY_SCAN, "8 Minute Body Scan Meditation",
                 "Progressive body scan to release tension and promote relaxation.");
    }

    @FXML
    private void loadStressRelief() {
        loadVideo(STRESS_RELIEF, "7 Minute Stress Relief Meditation",
                 "Quick and effective meditation to relieve stress and anxiety.");
    }

    @FXML
    private void loadMorningMeditation() {
        loadVideo(MORNING_MEDITATION, "10 Minute Morning Meditation",
                 "Start your day with positive energy and mindful intention.");
    }

    /**
     * Go back to user dashboard
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/mentalhealthdesktop/User_dash.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);
            stage.setScene(scene);
            stage.setTitle("User Dashboard");
            stage.show();
        } catch (Exception e) {
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

