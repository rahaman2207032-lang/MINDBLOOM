package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.SessionManager;
import com.example.mentalhealthdesktop.model.Instructor;
import com.example.mentalhealthdesktop.model.SessionRequest;
import com.example.mentalhealthdesktop.service.InstructorService;
import com.example.mentalhealthdesktop.service.SessionRequestService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * ✅ UPDATED: Controller for therapist session requests
 * - Uses SessionManager for dynamic user IDs
 * - Loads instructors dynamically from backend
 * - Separates pending requests and upcoming sessions
 * - Follows REACT_FRONTEND_GUIDE.md patterns
 */
public class TherapistRequestController {

    @FXML private ComboBox<Instructor> instructorComboBox;  // ✅ NEW
    @FXML private DatePicker sessionDatePicker;
    @FXML private ComboBox<String> sessionTimeCombo;
    @FXML private ComboBox<String> sessionTypeCombo;
    @FXML private TextArea reasonTextArea;
    @FXML private Button submitRequestBtn;
    @FXML private VBox requestsContainer;
    @FXML private VBox upcomingSessionsContainer;

    private final SessionRequestService sessionRequestService = new SessionRequestService();
    private final InstructorService instructorService = new InstructorService();  // ✅ NEW
    private final com.example.mentalhealthdesktop.service.TherapySessionService therapySessionService = new com.example.mentalhealthdesktop.service.TherapySessionService();  // ✅ NEW WORKFLOW

    // ✅ FIX: Keep Timeline as field to prevent garbage collection
    private javafx.animation.Timeline autoRefreshTimeline;

    @FXML
    public void initialize() {
        setupTimeSlots();
        setupSessionTypes();
        loadInstructors();  // ✅ NEW
        loadUserRequests();
        loadUpcomingSessions();

        // ✅ NEW: Auto-refresh confirmed sessions every 10 seconds (FRONTEND_CHANGES_REQUIRED.md)
        startAutoRefresh();
    }

    /**
     * ✅ NEW: Auto-refresh confirmed sessions to catch newly accepted sessions with zoom links
     */
    private void startAutoRefresh() {
        System.out.println("🚀 [TherapistRequest] Starting auto-refresh timer...");

        autoRefreshTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10), event -> {
                System.out.println("🔄 [TherapistRequest] Auto-refreshing confirmed sessions...");
                System.out.println("   Time: " + java.time.LocalDateTime.now());
                loadUpcomingSessions();
            })
        );
        autoRefreshTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoRefreshTimeline.play();

        System.out.println("✅ [TherapistRequest] Auto-refresh started (every 10 seconds)");
        System.out.println("   Timeline status: " + autoRefreshTimeline.getStatus());
        System.out.println("   Current rate: " + autoRefreshTimeline.getRate());
    }

    /**
     * ✅ NEW: Get current user ID dynamically (SessionManager + Dataholder fallback)
     * Follows REACT_FRONTEND_GUIDE.md pattern for dynamic user IDs
     */
    private Long getCurrentUserId() {
        Long userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null) {
            userId = Dataholder.userId;
            System.out.println("⚠️ [TherapistRequest] Using Dataholder (SessionManager not set)");
        }
        return userId;
    }

    private void setupTimeSlots() {
        sessionTimeCombo.getItems().addAll(
                "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
                "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM",
                "05:00 PM", "06:00 PM", "07:00 PM", "08:00 PM"
        );
    }

    private void setupSessionTypes() {
        sessionTypeCombo.getItems().addAll(
                "Individual Therapy",
                "Stress Management",
                "Anxiety Support",
                "Depression Support",
                "General Consultation",
                "Follow-up Session"
        );
    }

    /**
     * ✅ NEW: Load instructors from backend dynamically
     */
    private void loadInstructors() {
        new Thread(() -> {
            try {
                System.out.println("📥 [TherapistRequest] Loading instructors from backend...");
                List<Instructor> instructors = instructorService.getAllInstructors();

                Platform.runLater(() -> {
                    instructorComboBox.getItems().clear();
                    instructorComboBox.getItems().addAll(instructors);

                    if (!instructors.isEmpty()) {
                        instructorComboBox.getSelectionModel().selectFirst();
                        System.out.println("✅ [TherapistRequest] Loaded " + instructors.size() + " instructors");
                    } else {
                        System.out.println("⚠️ [TherapistRequest] No instructors found");

                        // Fallback: Show warning but allow submission (will use default)
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Warning");
                        alert.setHeaderText("No Instructors Available");
                        alert.setContentText("Could not load instructor list. The system will assign a default instructor.");
                        alert.showAndWait();
                    }
                });
            } catch (Exception e) {
                System.err.println("❌ [TherapistRequest] Error loading instructors: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Connection Issue");
                    alert.setHeaderText("Could not load instructors");
                    alert.setContentText("Backend may not be running. A default instructor will be assigned.");
                    alert.showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void handleSubmitRequest() {
        // ✅ Validate instructor selection FIRST
        Instructor selectedInstructor = instructorComboBox.getValue();
        if (selectedInstructor == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                     "Please select an instructor for your session.");
            return;
        }

        // Validate inputs
        if (sessionDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a date for the session.");
            return;
        }

        if (sessionTimeCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a time for the session.");
            return;
        }

        if (sessionTypeCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a session type.");
            return;
        }

        // Check if date is in the future
        LocalDate selectedDate = sessionDatePicker.getValue();
        if (selectedDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date", "Please select a future date.");
            return;
        }

        submitRequestBtn.setDisable(true);

        new Thread(() -> {
            try {
                // Parse time
                String timeStr = sessionTimeCombo.getValue();
                LocalTime time = parseTime(timeStr);
                LocalDateTime requestedDateTime = LocalDateTime.of(selectedDate, time);

                // ✅ FIXED: Create session request with SELECTED instructor (NOT hardcoded!)
                SessionRequest request = new SessionRequest();
                request.setUserId(getCurrentUserId());  // ✅ UPDATED: Use dynamic method
                request.setInstructorId(selectedInstructor.getId());  // ✅ Dynamic instructor selection!
                request.setRequestedDateTime(requestedDateTime);
                request.setSessionType(sessionTypeCombo.getValue());
                request.setReason(reasonTextArea.getText().trim().isEmpty() ? null : reasonTextArea.getText().trim());
                request.setStatus("PENDING");

                // Log what we're sending for debugging
                System.out.println("🔍 Creating session request:");
                System.out.println("   userId: " + request.getUserId());
                System.out.println("   instructorId: " + request.getInstructorId() +
                                 " (" + selectedInstructor.getUsername() + ")");  // ✅ Show instructor name
                System.out.println("   requestedDateTime: " + request.getRequestedDateTime());
                System.out.println("   sessionType: " + request.getSessionType());

                // Submit to backend
                SessionRequest createdRequest = sessionRequestService.createSessionRequest(request);

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Your session request has been submitted to " + selectedInstructor.getUsername() + "!\n" +
                            "You will be notified when they respond.");

                    // Clear form
                    instructorComboBox.getSelectionModel().clearSelection();  // ✅ Also clear instructor
                    sessionDatePicker.setValue(null);
                    sessionTimeCombo.setValue(null);
                    sessionTypeCombo.setValue(null);
                    reasonTextArea.clear();

                    // Reload requests
                    loadUserRequests();

                    submitRequestBtn.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to submit session request: " + e.getMessage());
                    submitRequestBtn.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleRefreshRequests() {
        loadUserRequests();
        loadUpcomingSessions();
    }

    private void loadUserRequests() {
        new Thread(() -> {
            try {
                Long userId = getCurrentUserId();  // ✅ UPDATED: Use dynamic method
                System.out.println("📥 [TherapistRequest] Loading pending requests for user: " + userId);

                List<SessionRequest> requests = sessionRequestService.getUserSessionRequests(userId);

                Platform.runLater(() -> {
                    requestsContainer.getChildren().clear();

                    if (requests.isEmpty()) {
                        Label emptyLabel = new Label("No session requests yet. Submit your first request above!");
                        emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14; -fx-padding: 20;");
                        requestsContainer.getChildren().add(emptyLabel);
                    } else {
                        System.out.println("✅ [TherapistRequest] Loaded " + requests.size() + " pending requests");
                        for (SessionRequest request : requests) {
                            requestsContainer.getChildren().add(createRequestCard(request));
                        }
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ [TherapistRequest] Error loading requests: " + e.getMessage());
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Failed to load requests: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                    requestsContainer.getChildren().add(errorLabel);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void loadUpcomingSessions() {
        new Thread(() -> {
            try {
                Long userId = getCurrentUserId();
                System.out.println("📅 [TherapistRequest] Loading scheduled sessions for user: " + userId);
                System.out.println("   🆕 NEW WORKFLOW: Fetching from therapy_sessions table");

                // ✅ NEW WORKFLOW: Use TherapySessionService to fetch from therapy_sessions table
                List<com.example.mentalhealthdesktop.model.TherapySession> scheduledSessions =
                    therapySessionService.getScheduledSessionsForUser(userId);

                Platform.runLater(() -> {
                    upcomingSessionsContainer.getChildren().clear();

                    if (scheduledSessions.isEmpty()) {
                        System.out.println("   ℹ️ No scheduled sessions found");
                        Label emptyLabel = new Label("No upcoming sessions scheduled.");
                        emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14; -fx-padding: 20;");
                        upcomingSessionsContainer.getChildren().add(emptyLabel);
                    } else {
                        System.out.println("✅ [TherapistRequest] Loaded " + scheduledSessions.size() + " scheduled sessions");
                        for (com.example.mentalhealthdesktop.model.TherapySession session : scheduledSessions) {
                            // Create session card from TherapySession object
                            upcomingSessionsContainer.getChildren().add(createTherapySessionCard(session));
                        }
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ [TherapistRequest] Error loading sessions: " + e.getMessage());
                Platform.runLater(() -> {
                    upcomingSessionsContainer.getChildren().clear();
                    Label errorLabel = new Label("Failed to load sessions: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                    upcomingSessionsContainer.getChildren().add(errorLabel);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private VBox createRequestCard(SessionRequest request) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: " + getStatusColor(request.getStatus()) + "; " +
                "-fx-background-radius: 8; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label typeLabel = new Label(request.getSessionType());
        typeLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label statusLabel = new Label(request.getStatus());
        statusLabel.setStyle("-fx-background-color: " + getStatusBadgeColor(request.getStatus()) + "; " +
                "-fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; " +
                "-fx-font-size: 11; -fx-font-weight: bold;");

        header.getChildren().addAll(typeLabel, spacer, statusLabel);

        // Date/Time
        Label dateTimeLabel = new Label("📅 " + formatDateTime(request.getRequestedDateTime()));
        dateTimeLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #34495e;");

        // Reason
        if (request.getReason() != null && !request.getReason().isEmpty()) {
            Label reasonLabel = new Label("💬 " + request.getReason());
            reasonLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
            reasonLabel.setWrapText(true);
            card.getChildren().addAll(header, dateTimeLabel, reasonLabel);
        } else {
            card.getChildren().addAll(header, dateTimeLabel);
        }

        // Add zoom link if confirmed
        if ("CONFIRMED".equals(request.getStatus()) && request.getZoomLink() != null && !request.getZoomLink().isEmpty()) {
            HBox actionBox = new HBox(10);
            actionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            actionBox.setStyle("-fx-padding: 8 0 0 0;");

            Button joinButton = new Button("🔗 Join Session");
            joinButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                    "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 6 15; " +
                    "-fx-background-radius: 5; -fx-cursor: hand;");
            joinButton.setOnAction(e -> openZoomLink(request.getZoomLink()));

            actionBox.getChildren().add(joinButton);
            card.getChildren().add(actionBox);
        }

        return card;
    }

    /**
     * ✅ NEW WORKFLOW: Create session card for TherapySession (from therapy_sessions table)
     * TherapySession objects ALWAYS have zoom links (created in one transaction)
     */
    private VBox createTherapySessionCard(com.example.mentalhealthdesktop.model.TherapySession session) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #e8f8f5; -fx-background-radius: 8; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Header - Session Type
        Label typeLabel = new Label("✅ " + (session.getSessionType() != null ? session.getSessionType() : "Therapy Session"));
        typeLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        // Date/Time
        Label dateTimeLabel = new Label("📅 " + formatDateTime(session.getSessionDate()));
        dateTimeLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #34495e;");

        // Status Badge
        Label statusLabel = new Label(session.getStatus() != null ? session.getStatus() : "SCHEDULED");
        statusLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-padding: 5 10; -fx-background-radius: 4; -fx-font-size: 12; -fx-font-weight: bold;");

        card.getChildren().addAll(typeLabel, dateTimeLabel, statusLabel);

        // ✅ NEW WORKFLOW: Zoom link should ALWAYS be present
        String zoomLink = session.getZoomLink();

        if (zoomLink != null && !zoomLink.isEmpty()) {
            System.out.println("   ✅ [NEW WORKFLOW] Zoom link present: " + zoomLink);

            HBox actionBox = new HBox(10);
            actionBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Button joinButton = new Button("🎥 Join Meeting");
            joinButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                    "-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 20; " +
                    "-fx-background-radius: 5; -fx-cursor: hand;");

            joinButton.setOnAction(e -> {
                try {
                    System.out.println("✅ [TherapistRequest] Opening Zoom link: " + zoomLink);
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(zoomLink));
                } catch (Exception ex) {
                    System.err.println("❌ [TherapistRequest] Failed to open Zoom: " + ex.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Zoom link: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            // Hover effect
            joinButton.setOnMouseEntered(e ->
                joinButton.setStyle("-fx-background-color: #229954; -fx-text-fill: white; " +
                        "-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 20; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;")
            );
            joinButton.setOnMouseExited(e ->
                joinButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-font-size: 14; -fx-font-weight: bold; -fx-padding: 10 20; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;")
            );

            actionBox.getChildren().add(joinButton);
            card.getChildren().add(actionBox);
        } else {
            // ⚠️ Should NOT happen with new workflow, but handle gracefully
            System.err.println("   ⚠️ [NEW WORKFLOW] WARNING: Zoom link missing for therapy session ID: " + session.getId());

            Label errorLabel = new Label("⚠️ Zoom link unavailable - Contact support");
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic; -fx-font-size: 13;");
            card.getChildren().add(errorLabel);
        }

        return card;
    }

    private void openZoomLink(String zoomLink) {
        if (zoomLink == null || zoomLink.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Link", "Zoom link is not available yet.");
            return;
        }

        try {
            // Open in default browser
            java.awt.Desktop.getDesktop().browse(new java.net.URI(zoomLink));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Zoom link: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "PENDING" -> "#fff3cd";
            case "CONFIRMED" -> "#d4edda";
            case "REJECTED" -> "#f8d7da";
            case "COMPLETED" -> "#d1ecf1";
            default -> "#ffffff";
        };
    }

    private String getStatusBadgeColor(String status) {
        return switch (status) {
            case "PENDING" -> "#f39c12";
            case "CONFIRMED" -> "#27ae60";
            case "REJECTED" -> "#e74c3c";
            case "COMPLETED" -> "#3498db";
            default -> "#95a5a6";
        };
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.toLocalDate().toString() + " at " +
               dateTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
    }

    private LocalTime parseTime(String timeStr) {
        // Parse "09:00 AM" format
        timeStr = timeStr.trim().toUpperCase();
        String[] parts = timeStr.split(" ");
        String[] timeParts = parts[0].split(":");

        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        if (parts[1].equals("PM") && hour != 12) {
            hour += 12;
        } else if (parts[1].equals("AM") && hour == 12) {
            hour = 0;
        }

        return LocalTime.of(hour, minute);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mentalhealthdesktop/User_dash.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) sessionDatePicker.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("User Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to go back: " + e.getMessage());
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



