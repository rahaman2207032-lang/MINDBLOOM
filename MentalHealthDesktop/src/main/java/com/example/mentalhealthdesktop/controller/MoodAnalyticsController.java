package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.model.MoodLog;
import com.example.mentalhealthdesktop.service.MoodService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MoodAnalyticsController {

    @FXML private ToggleGroup moodGroup;
    @FXML private RadioButton mood1Radio;
    @FXML private RadioButton mood2Radio;
    @FXML private RadioButton mood3Radio;
    @FXML private RadioButton mood4Radio;
    @FXML private RadioButton mood5Radio;

    @FXML private TextArea notesArea;
    @FXML private TextField activitiesField;
    @FXML private Button submitButton;
    @FXML private Button backButton;

    @FXML private Label averageMoodLabel;
    @FXML private Label currentStreakLabel;
    @FXML private Label totalLogsLabel;

    @FXML private BarChart<String, Number> moodChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private TextArea correlationArea;

    private MoodService moodService;

    @FXML
    public void initialize() {
        moodService = new MoodService();

        // Set emojis for radio buttons
        mood1Radio.setText("😢 Very Low");
        mood2Radio.setText("😟 Low");
        mood3Radio.setText("😐 Neutral");
        mood4Radio.setText("🙂 Good");
        mood5Radio.setText("😊 Very Good");

        // Load mood analytics
        loadMoodAnalytics();

        System.out.println("Mood Analytics initialized successfully!");
    }

    @FXML
    private void handleSubmit() {
        try {
            RadioButton selectedMood = (RadioButton) moodGroup.getSelectedToggle();
            if (selectedMood == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("Please select a mood rating!");
                alert.showAndWait();
                return;
            }

            int moodRating = 0;
            if (selectedMood == mood1Radio) moodRating = 1;
            else if (selectedMood == mood2Radio) moodRating = 2;
            else if (selectedMood == mood3Radio) moodRating = 3;
            else if (selectedMood == mood4Radio) moodRating = 4;
            else if (selectedMood == mood5Radio) moodRating = 5;

            MoodLog moodLog = new MoodLog(
                null,
                moodRating,
                notesArea.getText(),
                activitiesField.getText()
            );

            moodService.saveMoodLog(moodLog);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Mood log saved successfully!");
            alert.showAndWait();

            // Reload analytics
            loadMoodAnalytics();

            // Clear form
            resetForm();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save mood log: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void resetForm() {
        moodGroup.selectToggle(null);
        notesArea.clear();
        activitiesField.clear();
    }

    private void loadMoodAnalytics() {
        new Thread(() -> {
            try {
                List<MoodLog> moodLogs = moodService.getUserMoodLogs();

                Platform.runLater(() -> {
                    // Update statistics
                    totalLogsLabel.setText(String.valueOf(moodLogs.size()));

                    if (!moodLogs.isEmpty()) {
                        double avgMood = moodService.calculateAverageMood(moodLogs);
                        averageMoodLabel.setText(String.format("%.1f", avgMood));

                        // Calculate streak (consecutive days)
                        int streak = calculateStreak(moodLogs);
                        currentStreakLabel.setText(String.valueOf(streak) + " days");

                        // Load chart with last 7 days
                        loadMoodChart(moodLogs);

                        // Show correlations
                        showCorrelations();
                    } else {
                        averageMoodLabel.setText("N/A");
                        currentStreakLabel.setText("0 days");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    System.err.println("Error loading mood analytics: " + e.getMessage());
                });
            }
        }).start();
    }

    private int calculateStreak(List<MoodLog> moodLogs) {
        if (moodLogs.isEmpty()) return 0;

        // Sort by date descending
        moodLogs.sort((a, b) -> b.getLogDate().compareTo(a.getLogDate()));

        int streak = 0;
        LocalDate expectedDate = LocalDate.now();

        for (MoodLog log : moodLogs) {
            if (log.getLogDate().equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    private void loadMoodChart(List<MoodLog> allLogs) {
        moodChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Mood Ratings (Last 7 Days)");

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("MM/dd"));

            // Find mood for this date
            int moodRating = 0;
            for (MoodLog log : allLogs) {
                if (log.getLogDate().equals(date)) {
                    moodRating = log.getMoodRating();
                    break;
                }
            }

            series.getData().add(new XYChart.Data<>(dateStr, moodRating));
        }

        moodChart.getData().add(series);
    }

    private void showCorrelations() {
        correlationArea.setText(
            "📊 Mood Insights:\n\n" +
            "• Track your mood daily to identify patterns\n" +
            "• Better sleep often correlates with improved mood\n" +
            "• Regular exercise and social connections boost mood\n" +
            "• Notice activities that make you feel better\n\n" +
            "Tip: Review your journal entries to see what affects your mood!"
        );
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

