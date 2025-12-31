package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.model.JournalEntry;
import com.example.mentalhealthdesktop.service.JournalService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class JournalController {

    @FXML
    private TextArea journalTextArea;

    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button backButton;

    private final JournalService journalService = new JournalService();
    private JournalEntry currentJournal;

    @FXML
    public void initialize() {
        loadJournalFromDatabase();

        // Auto-save indicator when user types
        journalTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            statusLabel.setText("Unsaved changes");
            statusLabel.setStyle("-fx-text-fill: #ffc107;");
        });
    }

    /**
     * Load existing journal from database
     */
    private void loadJournalFromDatabase() {
        // Load in background thread to avoid UI freeze
        new Thread(() -> {
            try {
                currentJournal = journalService.getJournalEntry();

                Platform.runLater(() -> {
                    if (currentJournal != null) {
                        journalTextArea.setText(currentJournal.getContent());
                        updateLastUpdatedLabel();
                        statusLabel.setText("Loaded from database");
                        statusLabel.setStyle("-fx-text-fill: #28a745;");
                    } else {
                        statusLabel.setText("Start your first journal entry!");
                        statusLabel.setStyle("-fx-text-fill: #6c757d;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Failed to load journal");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    System.err.println("Error loading journal: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    /**
     * Save journal to database
     */
    @FXML
    private void handleSave() {
        String content = journalTextArea.getText();

        if (content == null || content.trim().isEmpty()) {
            showAlert("Empty Journal", "Please write something before saving.", Alert.AlertType.WARNING);
            return;
        }

        saveButton.setDisable(true);
        statusLabel.setText("Saving...");
        statusLabel.setStyle("-fx-text-fill: #007bff;");

        // Save in background thread
        new Thread(() -> {
            try {
                if (currentJournal == null) {
                    // Create new journal entry
                    currentJournal = journalService.saveJournalEntry(content);
                } else {
                    // Update existing journal entry
                    currentJournal = journalService.updateJournalEntry(currentJournal.getId(), content);
                }

                Platform.runLater(() -> {
                    statusLabel.setText("✓ Saved successfully!");
                    statusLabel.setStyle("-fx-text-fill: #28a745;");
                    updateLastUpdatedLabel();
                    saveButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Failed to save");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    saveButton.setDisable(false);
                    showAlert("Save Failed", "Could not save journal: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    /**
     * Update the last updated label
     */
    private void updateLastUpdatedLabel() {
        if (currentJournal != null && currentJournal.getUpdatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
            lastUpdatedLabel.setText("Last updated: " + currentJournal.getUpdatedAt().format(formatter));
        }
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

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

