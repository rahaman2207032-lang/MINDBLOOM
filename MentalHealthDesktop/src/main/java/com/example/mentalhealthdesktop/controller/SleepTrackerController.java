package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.SleepEntry;
import com.example.mentalhealthdesktop.service.SleepService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SleepTrackerController {

    @FXML private DatePicker sleepDatePicker;
    @FXML private TextField sleepStartHour;
    @FXML private TextField sleepStartMinute;
    @FXML private TextField sleepEndHour;
    @FXML private TextField sleepEndMinute;
    @FXML private ComboBox<Integer> qualityRating;
    @FXML private TextArea sleepNotes;
    @FXML private Button saveSleepButton;
    @FXML private Button backButton;
    @FXML private VBox sleepHistoryBox;
    @FXML private BarChart<String, Number> sleepChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label avgSleepLabel;
    @FXML private Label totalEntriesLabel;

    private SleepService sleepService;

    @FXML
    public void initialize() {
        sleepService = new SleepService();

        // Initialize date picker to today
        sleepDatePicker.setValue(LocalDate.now().minusDays(1));

        // Initialize quality rating dropdown
        qualityRating.getItems().addAll(1, 2, 3, 4, 5);
        qualityRating.setValue(3);

        // Set default time values
        sleepStartHour.setText("22");
        sleepStartMinute.setText("00");
        sleepEndHour.setText("07");
        sleepEndMinute.setText("00");

        // Load sleep data
        loadSleepHistory();
        loadWeeklyChart();

        System.out.println("Sleep Tracker initialized successfully!");
    }

    @FXML
    private void handleSaveSleep() {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            LocalDate sleepDate = sleepDatePicker.getValue();
            int startHour = Integer.parseInt(sleepStartHour.getText());
            int startMin = Integer.parseInt(sleepStartMinute.getText());
            int endHour = Integer.parseInt(sleepEndHour.getText());
            int endMin = Integer.parseInt(sleepEndMinute.getText());

            LocalDateTime startTime = LocalDateTime.of(sleepDate, LocalTime.of(startHour, startMin));
            LocalDateTime endTime = LocalDateTime.of(sleepDate.plusDays(1), LocalTime.of(endHour, endMin));

            // If end time is before start time, it's the same day
            if (endHour > startHour) {
                endTime = LocalDateTime.of(sleepDate, LocalTime.of(endHour, endMin));
            }

            final LocalDateTime finalStartTime = startTime;
            final LocalDateTime finalEndTime = endTime;
            final Integer quality = qualityRating.getValue();
            final String notes = sleepNotes.getText();

            // Save to backend in a separate thread
            new Thread(() -> {
                try {
                    sleepService.saveSleepEntry(finalStartTime, finalEndTime, quality, notes);

                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Sleep entry saved successfully!");
                        clearForm();
                        loadSleepHistory();
                        loadWeeklyChart();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to save sleep entry: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input",
                    "Please enter valid numbers for hours and minutes.");
        }
    }

    private boolean validateInputs() {
        if (sleepDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Date", "Please select a date.");
            return false;
        }

        try {
            int startHour = Integer.parseInt(sleepStartHour.getText());
            int startMin = Integer.parseInt(sleepStartMinute.getText());
            int endHour = Integer.parseInt(sleepEndHour.getText());
            int endMin = Integer.parseInt(sleepEndMinute.getText());

            if (startHour < 0 || startHour > 23 || endHour < 0 || endHour > 23) {
                showAlert(Alert.AlertType.WARNING, "Invalid Time",
                        "Hours must be between 0 and 23.");
                return false;
            }

            if (startMin < 0 || startMin > 59 || endMin < 0 || endMin > 59) {
                showAlert(Alert.AlertType.WARNING, "Invalid Time",
                        "Minutes must be between 0 and 59.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input",
                    "Please enter valid numbers for time.");
            return false;
        }

        if (qualityRating.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Rating",
                    "Please select a sleep quality rating.");
            return false;
        }

        return true;
    }

    private void loadSleepHistory() {
        new Thread(() -> {
            try {
                List<SleepEntry> entries = sleepService.getAllSleepEntries();

                Platform.runLater(() -> {
                    sleepHistoryBox.getChildren().clear();

                    if (entries.isEmpty()) {
                        Label emptyLabel = new Label("No sleep entries yet. Start tracking your sleep!");
                        emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
                        sleepHistoryBox.getChildren().add(emptyLabel);
                    } else {
                        double totalHours = 0;
                        for (SleepEntry entry : entries) {
                            sleepHistoryBox.getChildren().add(createSleepEntryCard(entry));
                            totalHours += entry.getSleepDurationHours();
                        }

                        // Update statistics
                        double avgHours = totalHours / entries.size();
                        avgSleepLabel.setText(String.format("%.1f hours", avgHours));
                        totalEntriesLabel.setText(String.valueOf(entries.size()));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Error loading sleep history: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private VBox createSleepEntryCard(SleepEntry entry) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #f0f4f8; -fx-padding: 15; " +
                     "-fx-background-radius: 10; -fx-border-color: #d0d0d0; " +
                     "-fx-border-radius: 10; -fx-border-width: 1;");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        String dateStr = entry.getSleepStartTime().format(dateFormatter);
        String startStr = entry.getSleepStartTime().format(timeFormatter);
        String endStr = entry.getSleepEndTime().format(timeFormatter);

        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label timeLabel = new Label(String.format("🛌 %s → 🌅 %s", startStr, endStr));
        timeLabel.setStyle("-fx-font-size: 13px;");

        Label durationLabel = new Label(String.format("Duration: %.1f hours",
                entry.getSleepDurationHours()));
        durationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2196F3;");

        String stars = "⭐".repeat(entry.getSleepQuality());
        Label qualityLabel = new Label("Quality: " + stars);
        qualityLabel.setStyle("-fx-font-size: 13px;");

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                          "-fx-cursor: hand; -fx-padding: 5 10;");
        deleteBtn.setOnAction(e -> deleteSleepEntry(entry.getId()));

        actionBox.getChildren().add(deleteBtn);

        card.getChildren().addAll(dateLabel, timeLabel, durationLabel, qualityLabel, actionBox);

        if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
            Label notesLabel = new Label("Notes: " + entry.getNotes());
            notesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-wrap-text: true;");
            card.getChildren().add(3, notesLabel);
        }

        return card;
    }

    private void deleteSleepEntry(Long sleepId) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Sleep Entry");
        confirmation.setHeaderText("Are you sure you want to delete this sleep entry?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        sleepService.deleteSleepEntry(sleepId);
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Deleted",
                                    "Sleep entry deleted successfully!");
                            loadSleepHistory();
                            loadWeeklyChart();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Failed to delete sleep entry: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }

    private void loadWeeklyChart() {
        new Thread(() -> {
            try {
                List<SleepEntry> entries = sleepService.getWeeklySleepEntries();

                Platform.runLater(() -> {
                    sleepChart.getData().clear();

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Sleep Hours");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

                    for (SleepEntry entry : entries) {
                        String date = entry.getSleepStartTime().format(formatter);
                        series.getData().add(new XYChart.Data<>(date, entry.getSleepDurationHours()));
                    }

                    sleepChart.getData().add(series);
                });
            } catch (Exception e) {
                System.err.println("Error loading weekly chart: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void clearForm() {
        sleepDatePicker.setValue(LocalDate.now().minusDays(1));
        sleepStartHour.setText("22");
        sleepStartMinute.setText("00");
        sleepEndHour.setText("07");
        sleepEndMinute.setText("00");
        qualityRating.setValue(3);
        sleepNotes.clear();
    }

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
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not return to dashboard.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

