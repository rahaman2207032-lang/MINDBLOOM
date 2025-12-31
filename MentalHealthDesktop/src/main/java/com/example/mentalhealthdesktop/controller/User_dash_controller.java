package com.example.mentalhealthdesktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class User_dash_controller {

    @FXML
    private Label dashboardTitle;

    @FXML
    public void initialize() {
        System.out.println("User Dashboard loaded successfully!");
    }

    // Navigation methods for each card button
    @FXML
    private void handleJournal() {
        navigateToScene("Journal.fxml", "My Journal", 800, 600);
    }

    @FXML
    private void handleMeditation() {
        navigateToScene("Meditation.fxml", "Meditation & Mindfulness", 1000, 700);
    }

    @FXML
    private void handleSleepTracker() {
        navigateToScene("SleepTracker.fxml", "Sleep Tracker", 1000, 700);
    }

    @FXML
    private void handleHabits() {
        navigateToScene("HabitTracker.fxml", "Habit Tracker", 1000, 700);
    }

    @FXML
    private void handleStressMonitor() {
        navigateToScene("StressMonitor.fxml", "Stress Monitor", 1000, 700);
    }

    @FXML
    private void handleNotification() {
        navigateToScene("Notification.fxml", "Notifications", 800, 700);
    }

    @FXML
    private void handleMoodAnalytics() {
        navigateToScene("MoodAnalytics.fxml", "Mood Analytics", 1000, 700);
    }

    @FXML
    private void handleProgress() {
        navigateToScene("Progress.fxml", "Progress Dashboard", 1000, 700);
    }

    @FXML
    private void handleTherapyNotes() {
        // Not implemented yet
    }

    @FXML
    private void handleTherapistZoom() {
        navigateToScene("TherapistRequest.fxml", "Therapist Sessions", 800, 700);
    }

    @FXML
    private void handleCommunityForum() {

    }

    /**
     * Helper method to navigate to different scenes
     */
    private void navigateToScene(String fxmlFile, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/mentalhealthdesktop/" + fxmlFile)
            );
            Parent root = loader.load();

            Stage stage = (Stage) dashboardTitle.getScene().getWindow();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

            System.out.println("Navigated to " + fxmlFile + " successfully!");
        } catch (Exception e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load " + title + ". Please try again.");
            alert.showAndWait();
        }
    }

}

