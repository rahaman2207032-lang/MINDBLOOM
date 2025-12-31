package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.model.ClientOverview;
import com.example.mentalhealthdesktop.service.ClientProgressService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ClientProgressViewController {

    @FXML private Label clientNameLabel;
    @FXML private Label avgMoodLabel;
    @FXML private Label stressLevelLabel;
    @FXML private Label habitCompletionLabel;
    @FXML private Label avgSleepLabel;
    @FXML private VBox moodLogsContainer;
    @FXML private VBox stressAssessmentsContainer;
    @FXML private GridPane habitsGrid;
    @FXML private VBox sleepDataContainer;
    @FXML private Label statusLabel;

    private ClientOverview client;
    private final ClientProgressService progressService = new ClientProgressService();

    public void setClient(ClientOverview client) {
        this.client = client;
        clientNameLabel.setText(client.getClientName() + "'s Progress");


        loadAllData();
    }

    @FXML
    private void initialize() {
        System.out.println("Client Progress View initialized");
    }

    private void loadAllData() {
        updateStatus("Loading client data...", true);

        new Thread(() -> {
            try {
                // Load summary
                Map<String, Object> summary = progressService.getClientProgressSummary(client.getClientId());

                Platform.runLater(() -> {
                    // Update summary cards
                    Object avgMood = summary.get("averageMood");
                    avgMoodLabel.setText(avgMood != null ? String.format("%.1f/5", ((Number) avgMood).doubleValue()) : "N/A");

                    Object stressLevel = summary.get("currentStressLevel");
                    stressLevelLabel.setText(stressLevel != null ? stressLevel.toString() : "N/A");

                    Object habitCompletion = summary.get("habitCompletionRate");
                    habitCompletionLabel.setText(habitCompletion != null ? String.format("%.0f%%", ((Number) habitCompletion).doubleValue()) : "N/A");

                    Object avgSleep = summary.get("averageSleepHours");
                    avgSleepLabel.setText(avgSleep != null ? String.format("%.1fh", ((Number) avgSleep).doubleValue()) : "N/A");
                });

                // Load detailed data
                loadMoodLogs();
                loadStressAssessments();
                loadHabits();
                loadSleepData();

                Platform.runLater(() -> updateStatus("Data loaded successfully", true));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    updateStatus("Error loading data: " + e.getMessage(), false);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load client progress data.\n\nError: " + e.getMessage());
                });
            }
        }).start();
    }

    private void loadMoodLogs() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> moodLogs = progressService.getClientMoodLogs(client.getClientId());

                Platform.runLater(() -> {
                    moodLogsContainer.getChildren().clear();

                    if (moodLogs.isEmpty()) {
                        Label noData = new Label("No mood logs available");
                        noData.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13;");
                        moodLogsContainer.getChildren().add(noData);
                    } else {
                        for (Map<String, Object> log : moodLogs) {
                            HBox logCard = createMoodLogCard(log);
                            moodLogsContainer.getChildren().add(logCard);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Failed to load mood logs");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                    moodLogsContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private HBox createMoodLogCard(Map<String, Object> log) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

        // Date
        String dateStr = log.get("date") != null ? log.get("date").toString() : "Unknown date";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        dateLabel.setPrefWidth(120);

        // Mood score
        Object moodScore = log.get("moodScore");
        String moodEmoji = getMoodEmoji(moodScore);
        Label moodLabel = new Label(moodEmoji + " " + (moodScore != null ? String.format("%.1f/5", ((Number) moodScore).doubleValue()) : "N/A"));
        moodLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        moodLabel.setPrefWidth(100);

        // Notes
        String notes = log.get("notes") != null ? log.get("notes").toString() : "";
        Label notesLabel = new Label(notes);
        notesLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        notesLabel.setWrapText(true);
        HBox.setHgrow(notesLabel, Priority.ALWAYS);

        card.getChildren().addAll(dateLabel, moodLabel, notesLabel);
        return card;
    }

    private String getMoodEmoji(Object moodScore) {
        if (moodScore == null) return "😐";
        double score = ((Number) moodScore).doubleValue();
        if (score >= 4.5) return "😄";
        if (score >= 3.5) return "🙂";
        if (score >= 2.5) return "😐";
        if (score >= 1.5) return "😔";
        return "😢";
    }

    private void loadStressAssessments() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> assessments = progressService.getClientStressAssessments(client.getClientId());

                Platform.runLater(() -> {
                    stressAssessmentsContainer.getChildren().clear();

                    if (assessments.isEmpty()) {
                        Label noData = new Label("No stress assessments available");
                        noData.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13;");
                        stressAssessmentsContainer.getChildren().add(noData);
                    } else {
                        for (Map<String, Object> assessment : assessments) {
                            HBox assessmentCard = createStressAssessmentCard(assessment);
                            stressAssessmentsContainer.getChildren().add(assessmentCard);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Failed to load stress assessments");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                    stressAssessmentsContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private HBox createStressAssessmentCard(Map<String, Object> assessment) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #fff5e6; -fx-padding: 15; -fx-background-radius: 8;");

        // Date
        String dateStr = assessment.get("assessmentDate") != null ? assessment.get("assessmentDate").toString() : "Unknown date";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        dateLabel.setPrefWidth(120);

        // Stress level
        String stressLevel = assessment.get("stressLevel") != null ? assessment.get("stressLevel").toString() : "N/A";
        String color = getStressColor(stressLevel);
        Label stressLabel = new Label(stressLevel);
        stressLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        stressLabel.setPrefWidth(100);

        // Score
        Object stressScore = assessment.get("stressScore");
        Label scoreLabel = new Label("Score: " + (stressScore != null ? stressScore.toString() : "N/A"));
        scoreLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(dateLabel, stressLabel, scoreLabel);
        return card;
    }

    private String getStressColor(String stressLevel) {
        return switch (stressLevel.toUpperCase()) {
            case "LOW" -> "#2ecc71";
            case "MODERATE" -> "#f39c12";
            case "HIGH" -> "#e74c3c";
            default -> "#95a5a6";
        };
    }

    private void loadHabits() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> habits = progressService.getClientHabits(client.getClientId());

                Platform.runLater(() -> {
                    habitsGrid.getChildren().clear();

                    if (habits.isEmpty()) {
                        Label noData = new Label("No habits tracked");
                        noData.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13;");
                        habitsGrid.add(noData, 0, 0);
                    } else {
                        int col = 0, row = 0;
                        for (Map<String, Object> habit : habits) {
                            VBox habitCard = createHabitCard(habit);
                            habitsGrid.add(habitCard, col, row);

                            col++;
                            if (col > 2) {
                                col = 0;
                                row++;
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Failed to load habits");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                    habitsGrid.add(errorLabel, 0, 0);
                });
            }
        }).start();
    }

    private VBox createHabitCard(Map<String, Object> habit) {
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: #e8f8f5; -fx-padding: 15; -fx-background-radius: 8;");

        // Habit name
        String habitName = habit.get("habitName") != null ? habit.get("habitName").toString() : "Unnamed Habit";
        Label nameLabel = new Label(habitName);
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        nameLabel.setWrapText(true);

        // Streak
        Object streak = habit.get("currentStreak");
        Label streakLabel = new Label("🔥 Streak: " + (streak != null ? streak.toString() + " days" : "0 days"));
        streakLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #e67e22;");

        // Completion rate
        Object completionRate = habit.get("completionRate");
        ProgressBar progressBar = new ProgressBar(completionRate != null ? ((Number) completionRate).doubleValue() / 100.0 : 0);
        progressBar.setPrefWidth(220);
        progressBar.setStyle("-fx-accent: #2ecc71;");

        Label rateLabel = new Label(completionRate != null ? String.format("%.0f%% completed", ((Number) completionRate).doubleValue()) : "0% completed");
        rateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(nameLabel, streakLabel, progressBar, rateLabel);
        return card;
    }

    private void loadSleepData() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> sleepData = progressService.getClientSleepData(client.getClientId());

                Platform.runLater(() -> {
                    sleepDataContainer.getChildren().clear();

                    if (sleepData.isEmpty()) {
                        Label noData = new Label("No sleep data available");
                        noData.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13;");
                        sleepDataContainer.getChildren().add(noData);
                    } else {
                        for (Map<String, Object> sleep : sleepData) {
                            HBox sleepCard = createSleepCard(sleep);
                            sleepDataContainer.getChildren().add(sleepCard);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Failed to load sleep data");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                    sleepDataContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private HBox createSleepCard(Map<String, Object> sleep) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #e8f4f8; -fx-padding: 15; -fx-background-radius: 8;");

        // Date
        String dateStr = sleep.get("sleepDate") != null ? sleep.get("sleepDate").toString() : "Unknown date";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        dateLabel.setPrefWidth(120);

        // Duration
        Object duration = sleep.get("durationHours");
        Label durationLabel = new Label("⏱ " + (duration != null ? String.format("%.1f hours", ((Number) duration).doubleValue()) : "N/A"));
        durationLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        durationLabel.setPrefWidth(120);

        // Quality
        Object quality = sleep.get("quality");
        String qualityStr = quality != null ? quality.toString() : "N/A";
        Label qualityLabel = new Label("Quality: " + qualityStr);
        qualityLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(dateLabel, durationLabel, qualityLabel);
        return card;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mentalhealthdesktop/Instructor_dash.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) clientNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 900);
            stage.setScene(scene);
            stage.setTitle("Instructor Dashboard");
            stage.setResizable(true);
            stage.setMinWidth(1200);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to go back: " + e.getMessage());
        }
    }

    @FXML
    private void refreshData() {
        loadAllData();
    }

    private void updateStatus(String status, boolean success) {
        statusLabel.setText(status);
        statusLabel.setTextFill(success ? Color.web("#2ecc71") : Color.web("#e74c3c"));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

