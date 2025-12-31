package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.Habit;
import com.example.mentalhealthdesktop.model.HabitCompletion;
import com.example.mentalhealthdesktop.service.HabitService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class HabitTrackerController {

    @FXML private VBox habitsContainer;
    @FXML private Button addHabitButton;
    @FXML private Button backButton;
    @FXML private Label totalHabitsLabel;
    @FXML private Label activeStreaksLabel;

    private HabitService habitService;

    @FXML
    public void initialize() {
        habitService = new HabitService();
        loadHabits();
        System.out.println("Habit Tracker initialized successfully!");
    }

    @FXML
    private void handleAddHabit() {
        Dialog<Habit> dialog = new Dialog<>();
        dialog.setTitle("Create New Habit");
        dialog.setHeaderText("Add a new habit to track");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Habit name");

        TextArea descField = new TextArea();
        descField.setPromptText("Description (optional)");
        descField.setPrefRowCount(3);

        ComboBox<String> frequencyBox = new ComboBox<>();
        frequencyBox.getItems().addAll("DAILY", "WEEKLY");
        frequencyBox.setValue("DAILY");

        TextField targetDaysField = new TextField();
        targetDaysField.setPromptText("e.g., MON,WED,FRI or ALL");
        targetDaysField.setText("ALL");

        grid.add(new Label("Habit Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Frequency:"), 0, 2);
        grid.add(frequencyBox, 1, 2);
        grid.add(new Label("Target Days:"), 0, 3);
        grid.add(targetDaysField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Habit habit = new Habit();
                habit.setName(nameField.getText());
                habit.setDescription(descField.getText());
                habit.setFrequency(frequencyBox.getValue());
                habit.setTargetDays(targetDaysField.getText());
                return habit;
            }
            return null;
        });

        Optional<Habit> result = dialog.showAndWait();
        result.ifPresent(habit -> {
            if (habit.getName() == null || habit.getName().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input",
                        "Habit name cannot be empty.");
                return;
            }

            new Thread(() -> {
                try {
                    habitService.createHabit(
                        habit.getName(),
                        habit.getDescription(),
                        habit.getFrequency(),
                        habit.getTargetDays()
                    );

                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Habit created successfully!");
                        loadHabits();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to create habit: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            }).start();
        });
    }

    private void loadHabits() {
        new Thread(() -> {
            try {
                List<Habit> habits = habitService.getAllHabits();

                Platform.runLater(() -> {
                    habitsContainer.getChildren().clear();

                    if (habits.isEmpty()) {
                        Label emptyLabel = new Label("No habits yet. Click 'Add New Habit' to start!");
                        emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-padding: 20;");
                        habitsContainer.getChildren().add(emptyLabel);

                        totalHabitsLabel.setText("0");
                        activeStreaksLabel.setText("0");
                    } else {
                        int activeStreaks = 0;
                        for (Habit habit : habits) {
                            habitsContainer.getChildren().add(createHabitCard(habit));
                            if (habit.getCurrentStreak() > 0) {
                                activeStreaks++;
                            }
                        }

                        totalHabitsLabel.setText(String.valueOf(habits.size()));
                        activeStreaksLabel.setText(String.valueOf(activeStreaks));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Error loading habits: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private VBox createHabitCard(Habit habit) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; " +
                     "-fx-background-radius: 15; -fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 15; -fx-border-width: 2; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(900);

        // Header with name and streak
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(habit.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label streakLabel = new Label("🔥 " + habit.getCurrentStreak() + " day streak");
        streakLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");

        header.getChildren().addAll(nameLabel, streakLabel);

        // Description
        VBox detailsBox = new VBox(5);
        if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
            Label descLabel = new Label(habit.getDescription());
            descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
            descLabel.setWrapText(true);
            detailsBox.getChildren().add(descLabel);
        }

        // Frequency info
        Label freqLabel = new Label("📅 " + habit.getFrequency() +
                (habit.getTargetDays() != null ? " (" + habit.getTargetDays() + ")" : ""));
        freqLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        detailsBox.getChildren().add(freqLabel);

        // Longest streak
        Label longestLabel = new Label("🏆 Longest streak: " + habit.getLongestStreak() + " days");
        longestLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
        detailsBox.getChildren().add(longestLabel);

        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button completeBtn = new Button("✅ Mark Complete");
        completeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                           "-fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 8;");
        completeBtn.setOnAction(e -> markHabitComplete(habit));

        Button viewBtn = new Button("📊 View History");
        viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                        "-fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 8;");
        viewBtn.setOnAction(e -> viewHabitHistory(habit));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                          "-fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 8;");
        deleteBtn.setOnAction(e -> deleteHabit(habit));

        actionBox.getChildren().addAll(completeBtn, viewBtn, deleteBtn);

        card.getChildren().addAll(header, detailsBox, actionBox);

        return card;
    }

    private void markHabitComplete(Habit habit) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Complete Habit");
        dialog.setHeaderText("Mark '" + habit.getName() + "' as complete for today");
        dialog.setContentText("Notes (optional):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(notes -> {
            new Thread(() -> {
                try {
                    habitService.completeHabit(habit.getId(), notes);

                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Habit marked as complete! Keep up the great work! 🎉");
                        loadHabits();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to complete habit: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            }).start();
        });
    }

    private void viewHabitHistory(Habit habit) {
        new Thread(() -> {
            try {
                List<HabitCompletion> completions = habitService.getHabitCompletions(habit.getId());

                Platform.runLater(() -> {
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("Habit History");
                    dialog.setHeaderText(habit.getName() + " - Completion History");

                    VBox content = new VBox(10);
                    content.setPadding(new Insets(20));
                    content.setStyle("-fx-background-color: white;");

                    if (completions.isEmpty()) {
                        Label emptyLabel = new Label("No completions yet. Start today!");
                        emptyLabel.setStyle("-fx-text-fill: #666;");
                        content.getChildren().add(emptyLabel);
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

                        for (HabitCompletion completion : completions) {
                            HBox row = new HBox(15);
                            row.setAlignment(Pos.CENTER_LEFT);
                            row.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; " +
                                       "-fx-background-radius: 8;");

                            Label dateLabel = new Label("✅ " + completion.getCompletionDate().format(formatter));
                            dateLabel.setStyle("-fx-font-size: 14px;");

                            if (completion.getNotes() != null && !completion.getNotes().isEmpty()) {
                                Label notesLabel = new Label("- " + completion.getNotes());
                                notesLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                                row.getChildren().addAll(dateLabel, notesLabel);
                            } else {
                                row.getChildren().add(dateLabel);
                            }

                            content.getChildren().add(row);
                        }
                    }

                    ScrollPane scrollPane = new ScrollPane(content);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setPrefHeight(400);
                    scrollPane.setPrefWidth(500);

                    dialog.getDialogPane().setContent(scrollPane);
                    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                    dialog.showAndWait();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to load habit history: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void deleteHabit(Habit habit) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Habit");
        confirmation.setHeaderText("Are you sure you want to delete '" + habit.getName() + "'?");
        confirmation.setContentText("This will also delete all completion history. This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        habitService.deleteHabit(habit.getId());
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Deleted",
                                    "Habit deleted successfully!");
                            loadHabits();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Failed to delete habit: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
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

