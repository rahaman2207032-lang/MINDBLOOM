package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.model.StressAssessment;
import com.example.mentalhealthdesktop.service.StressService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class StressMonitorController {

    @FXML private Slider workloadSlider;
    @FXML private Slider sleepQualitySlider;
    @FXML private Slider anxietySlider;
    @FXML private Slider moodSlider;
    @FXML private Slider physicalSymptomsSlider;
    @FXML private Slider concentrationSlider;
    @FXML private Slider socialConnectionSlider;

    @FXML private Label workloadLabel;
    @FXML private Label sleepQualityLabel;
    @FXML private Label anxietyLabel;
    @FXML private Label moodLabel;
    @FXML private Label physicalSymptomsLabel;
    @FXML private Label concentrationLabel;
    @FXML private Label socialConnectionLabel;

    @FXML private Label stressScoreLabel;
    @FXML private Label stressLevelLabel;
    @FXML private TextArea copingSuggestionsArea;
    @FXML private TextArea notesArea;
    @FXML private Button submitButton;
    @FXML private Button backButton;
    @FXML private LineChart<Number, Number> stressTrendChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    private StressService stressService;

    @FXML
    public void initialize() {
        stressService = new StressService();

        setupSliderListener(workloadSlider, workloadLabel);
        setupSliderListener(sleepQualitySlider, sleepQualityLabel);
        setupSliderListener(anxietySlider, anxietyLabel);
        setupSliderListener(moodSlider, moodLabel);
        setupSliderListener(physicalSymptomsSlider, physicalSymptomsLabel);
        setupSliderListener(concentrationSlider, concentrationLabel);
        setupSliderListener(socialConnectionSlider, socialConnectionLabel);

        loadStressTrend();

        System.out.println("Stress Monitor initialized successfully!");
    }

    private void setupSliderListener(Slider slider, Label label) {
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            label.setText(String.valueOf(newValue.intValue()));
            calculateCurrentStress();
        });
    }

    private void calculateCurrentStress() {
        int workload = (int) workloadSlider.getValue();
        int sleepQuality = (int) sleepQualitySlider.getValue();
        int anxiety = (int) anxietySlider.getValue();
        int mood = (int) moodSlider.getValue();
        int physicalSymptoms = (int) physicalSymptomsSlider.getValue();
        int concentration = (int) concentrationSlider.getValue();
        int socialConnection = (int) socialConnectionSlider.getValue();

        int totalScore = workload + sleepQuality + anxiety + mood +
                physicalSymptoms + concentration + socialConnection;

        stressScoreLabel.setText(String.valueOf(totalScore));

        String level;
        String color;
        if (totalScore <= 14) {
            level = "LOW";
            color = "#4CAF50";
        } else if (totalScore <= 24) {
            level = "MODERATE";
            color = "#FF9800";
        } else {
            level = "HIGH";
            color = "#F44336";
        }

        stressLevelLabel.setText(level);
        stressLevelLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 18px;");

        copingSuggestionsArea.setText(stressService.getCopingSuggestion(level));
    }

    @FXML
    private void handleSubmit() {
        try {
            StressAssessment assessment = new StressAssessment(
                    null,
                    (int) workloadSlider.getValue(),
                    (int) sleepQualitySlider.getValue(),
                    (int) anxietySlider.getValue(),
                    (int) moodSlider.getValue(),
                    (int) physicalSymptomsSlider.getValue(),
                    (int) concentrationSlider.getValue(),
                    (int) socialConnectionSlider.getValue()
            );
            assessment.setNotes(notesArea.getText());

            stressService.saveStressAssessment(assessment);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Stress assessment saved successfully!");
            alert.showAndWait();

            loadStressTrend();
            resetForm();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save stress assessment: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void resetForm() {
        workloadSlider.setValue(3);
        sleepQualitySlider.setValue(3);
        anxietySlider.setValue(3);
        moodSlider.setValue(3);
        physicalSymptomsSlider.setValue(3);
        concentrationSlider.setValue(3);
        socialConnectionSlider.setValue(3);
        notesArea.clear();
    }

    private void loadStressTrend() {
        new Thread(() -> {
            try {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(30);

                List<StressAssessment> assessments = stressService.getStressAssessmentsByDateRange(startDate, endDate);

                Platform.runLater(() -> {
                    stressTrendChart.getData().clear();

                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName("Stress Score Trend");

                    for (int i = 0; i < assessments.size(); i++) {
                        StressAssessment assessment = assessments.get(i);
                        series.getData().add(new XYChart.Data<>(i + 1, assessment.getStressScore()));
                    }

                    stressTrendChart.getData().add(series);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    System.err.println("Error loading stress trend: " + e.getMessage());
                });
            }
        }).start();
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
