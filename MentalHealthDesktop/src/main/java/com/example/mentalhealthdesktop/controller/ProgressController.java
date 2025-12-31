package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.model.ProgressData;
import com.example.mentalhealthdesktop.service.ProgressService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ProgressController {

    @FXML private Label averageMoodLabel;
    @FXML private Label averageStressLabel;
    @FXML private Label habitCompletionLabel;
    @FXML private Label averageSleepLabel;

    @FXML private Label moodTrendLabel;
    @FXML private Label stressTrendLabel;

    @FXML private TextArea milestonesArea;
    @FXML private TextArea correlationsArea;

    @FXML private AreaChart<String, Number> progressChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button loadButton;
    @FXML private Button backButton;

    private ProgressService progressService;

    @FXML
    public void initialize() {
        progressService = new ProgressService();

        // Set default dates (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        // Load progress data
        loadProgressData();

        System.out.println("Progress Dashboard initialized successfully!");
    }

    @FXML
    private void handleLoad() {
        loadProgressData();
    }

    private void loadProgressData() {
        new Thread(() -> {
            try {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();

                if (startDate == null || endDate == null) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Warning");
                        alert.setHeaderText(null);
                        alert.setContentText("Please select both start and end dates!");
                        alert.showAndWait();
                    });
                    return;
                }

                ProgressData progressData = progressService.getProgressData(startDate, endDate);

                Platform.runLater(() -> {
                    updateUI(progressData);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to load progress data: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void updateUI(ProgressData data) {
        // Update metrics
        averageMoodLabel.setText(String.format("%.1f", data.getAverageMoodRating()));
        averageStressLabel.setText(String.format("%.1f", data.getAverageStressScore()));
        habitCompletionLabel.setText(String.format("%.0f%%", data.getHabitCompletionRate()));
        averageSleepLabel.setText(String.format("%.1f hrs", data.getAverageSleepHours()));

        // Update trends with colors
        updateTrendLabel(moodTrendLabel, data.getMoodTrend());
        updateTrendLabel(stressTrendLabel, data.getStressTrend());

        // Update milestones
        if (data.getAchievedMilestones() != null && !data.getAchievedMilestones().isEmpty()) {
            StringBuilder milestones = new StringBuilder("🎉 Achievements:\n\n");
            for (String milestone : data.getAchievedMilestones()) {
                milestones.append("✓ ").append(milestone).append("\n");
            }
            milestonesArea.setText(milestones.toString());
        } else {
            milestonesArea.setText("🎯 Keep tracking to unlock achievements!");
        }

        // Update correlations
        if (data.getSleepMoodCorrelation() != null) {
            correlationsArea.setText("📊 Insights:\n\n" + data.getSleepMoodCorrelation() +
                "\n\n• Mood logging streak: " + data.getConsecutiveDaysOfMoodLogging() + " days" +
                "\n• Stress management streak: " + data.getConsecutiveDaysOfStressManagement() + " days");
        } else {
            correlationsArea.setText("📊 Insights:\n\nKeep logging your activities to see patterns and correlations!");
        }

        // Load chart
        loadProgressChart(data);
    }

    private void updateTrendLabel(Label label, String trend) {
        if (trend == null) {
            label.setText("N/A");
            label.setStyle("-fx-text-fill: gray;");
            return;
        }

        label.setText(trend);
        switch (trend) {
            case "IMPROVING":
                label.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                break;
            case "STABLE":
                label.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                break;
            case "DECLINING":
            case "WORSENING":
                label.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                break;
            default:
                label.setStyle("-fx-text-fill: gray;");
        }
    }

    private void loadProgressChart(ProgressData data) {
        progressChart.getData().clear();

        // Create sample data for visualization
        XYChart.Series<String, Number> moodSeries = new XYChart.Series<>();
        moodSeries.setName("Average Mood");

        XYChart.Series<String, Number> stressSeries = new XYChart.Series<>();
        stressSeries.setName("Stress Level");

        // Add week data points (this is simplified - backend should provide actual weekly data)
        String[] weeks = {"Week 1", "Week 2", "Week 3", "Week 4"};
        for (String week : weeks) {
            moodSeries.getData().add(new XYChart.Data<>(week, data.getAverageMoodRating()));
            stressSeries.getData().add(new XYChart.Data<>(week, data.getAverageStressScore() / 7)); // Normalize
        }

        progressChart.getData().addAll(moodSeries, stressSeries);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/mentalhealthdesktop/User_dash.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 800, 1000);
            stage.setScene(scene);
            stage.setTitle("User Dashboard");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not navigate back to dashboard.");
            alert.showAndWait();
        }
    }
}

