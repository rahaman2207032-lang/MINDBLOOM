package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.ClientOverview;
import com.example.mentalhealthdesktop.model.Message;
import com.example.mentalhealthdesktop.model.SessionRequest;
import com.example.mentalhealthdesktop.model.TherapyNote;
import com.example.mentalhealthdesktop.model.TherapySession;
import com.example.mentalhealthdesktop.service.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Instructor_dash_controller {

    // Top Bar
    @FXML private Label instructorTitle;
    @FXML private Label pendingRequestsLabel;
    @FXML private Label todaySessionsLabel;
    @FXML private Label totalClientsLabel;
    @FXML private Label availableSlotsLabel;

    // Left Panel
    @FXML private VBox requestsContainer;
    @FXML private VBox calendarContainer;
    @FXML private Label weekLabel;

    // Tab Pane
    @FXML private TabPane mainTabPane;

    // Clients Tab
    @FXML private VBox clientListContainer;

    // Therapy Notes Tab
    @FXML private ComboBox<String> clientComboBox;
    @FXML private DatePicker sessionDatePicker;
    @FXML private ComboBox<String> sessionTypeComboBox;
    @FXML private TextArea therapyNotesArea;
    @FXML private VBox previousNotesContainer;

    // Messages Tab
    @FXML private VBox conversationsContainer;
    @FXML private Label selectedClientLabel;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageTextArea;

    // Analytics Tab
    @FXML private ComboBox<String> analyticsTimeRangeCombo;
    @FXML private Label totalSessionsLabel;
    @FXML private Label completedSessionsLabel;
    @FXML private Label avgRatingLabel;
    @FXML private VBox analyticsChartsContainer;

    // Bottom Status Bar
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;

    private LocalDate currentWeekStart;
    private String selectedClientForNotes;
    private String selectedConversationClient;
    private Long selectedConversationClientId;

    // Services
    private final InstructorService instructorService = new InstructorService();
    private final SessionRequestService sessionRequestService = new SessionRequestService();
    private final TherapySessionService therapySessionService = new TherapySessionService();
    private final TherapyNoteService therapyNoteService = new TherapyNoteService();
    private final MessageService messageService = new MessageService();

    // Data cache
    private List<SessionRequest> pendingRequests = new ArrayList<>();
    private List<TherapySession> weeklySessions = new ArrayList<>();
    private List<ClientOverview> allClients = new ArrayList<>();

    // ✅ FIX #2: Auto-refresh for conversations (polling)
    private java.util.Timer autoRefreshTimer;

    @FXML
    public void initialize() {
        System.out.println("Instructor Dashboard loaded successfully!");

        // Make stage resizable after scene is loaded
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) instructorTitle.getScene().getWindow();
                stage.setResizable(true);
                stage.setMinWidth(1200);
                stage.setMinHeight(700);
                stage.setWidth(1400);
                stage.setHeight(900);
                System.out.println("✅ Stage configured: resizable and sized to 1400x900");

                // ✅ FIX #2: Start auto-refresh for conversations (5 seconds)
                startAutoRefresh();
            } catch (Exception e) {
                System.err.println("⚠️ Could not configure stage: " + e.getMessage());
            }
        });

        // Initialize current week
        currentWeekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        updateWeekLabel();

        // Initialize combo boxes
        initializeComboBoxes();

        // Load initial data
        loadDashboardData();

        System.out.println("Instructor Dashboard initialized successfully!");
    }

    private void initializeComboBoxes() {
        // Session types
        if (sessionTypeComboBox != null) {
            sessionTypeComboBox.getItems().addAll(
                "Initial Consultation",
                "Follow-up Session",
                "Crisis Intervention",
                "Group Therapy",
                "Family Session",
                "Progress Review"
            );
        }


        // Analytics time range
        if (analyticsTimeRangeCombo != null) {
            analyticsTimeRangeCombo.getItems().addAll(
                "Last 7 Days",
                "Last 30 Days",
                "Last 3 Months",
                "Last 6 Months",
                "Last Year"
            );
            analyticsTimeRangeCombo.setValue("Last 30 Days");
        }
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                // Load dashboard stats from API
                Map<String, Integer> stats = instructorService.getDashboardStats();

                Platform.runLater(() -> {
                    // Update stats with real data
                    pendingRequestsLabel.setText(String.valueOf(stats.getOrDefault("pendingRequests", 0)));
                    todaySessionsLabel.setText(String.valueOf(stats.getOrDefault("todaySessions", 0)));
                    totalClientsLabel.setText(String.valueOf(stats.getOrDefault("totalClients", 0)));
                    availableSlotsLabel.setText(String.valueOf(stats.getOrDefault("availableSlots", 0)));

                    // Load sections
                    loadSessionRequests();
                    loadCalendar();
                    loadClientList(); // This will also load conversations after clients are ready
                    updateAnalytics();

                    updateStatus("Ready", true);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    // Set default values
                    pendingRequestsLabel.setText("0");
                    todaySessionsLabel.setText("0");
                    totalClientsLabel.setText("0");
                    availableSlotsLabel.setText("0");

                    updateStatus("Backend not available", false);

                    // Show error but allow user to see the UI
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Backend Connection Issue");
                    alert.setHeaderText("Cannot connect to backend server");
                    alert.setContentText("The instructor dashboard requires backend API endpoints.\n\n" +
                            "Error: " + e.getMessage() + "\n\n" +
                            "Please ensure:\n" +
                            "1. Backend server is running on http://localhost:8080\n" +
                            "2. All required API endpoints are implemented\n" +
                            "3. Database tables are created\n\n" +
                            "See BACKEND_API_SPECIFICATION.md for details.");
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void loadSessionRequests() {
        new Thread(() -> {
            try {
                // Fetch real session requests from API
                pendingRequests = sessionRequestService.getPendingRequests();

                Platform.runLater(() -> {
                    requestsContainer.getChildren().clear();

                    if (pendingRequests.isEmpty()) {
                        Label noRequests = new Label("No pending requests");
                        noRequests.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13;");
                        requestsContainer.getChildren().add(noRequests);
                    } else {
                        for (SessionRequest request : pendingRequests) {
                            VBox requestCard = createRequestCard(request);
                            requestsContainer.getChildren().add(requestCard);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Failed to load requests");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13;");
                    requestsContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private VBox createRequestCard(SessionRequest request) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 6; -fx-padding: 12; -fx-border-color: #3498db; -fx-border-width: 1; -fx-border-radius: 6;");

        Label nameLabel = new Label("👤 " + request.getClientName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        Label reasonLabel = new Label("Reason: " + request.getReason());
        reasonLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #34495e;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");
        String dateTimeStr = request.getRequestedDate().format(formatter);
        Label dateLabel = new Label("🕒 " + dateTimeStr);
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button acceptBtn = new Button("✓ Accept");
        acceptBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
        acceptBtn.setOnAction(e -> acceptRequest(request));

        Button declineBtn = new Button("✗ Decline");
        declineBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
        declineBtn.setOnAction(e -> declineRequest(request));

        Button detailsBtn = new Button("Details");
        detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
        detailsBtn.setOnAction(e -> viewRequestDetails(request));

        buttonBox.getChildren().addAll(acceptBtn, declineBtn, detailsBtn);

        card.getChildren().addAll(nameLabel, reasonLabel, dateLabel, buttonBox);
        return card;
    }

    private void loadCalendar() {
        new Thread(() -> {
            try {
                // Fetch weekly sessions from API
                weeklySessions = therapySessionService.getWeeklySessions();

                Platform.runLater(() -> {
                    calendarContainer.getChildren().clear();

                    for (int i = 0; i < 7; i++) {
                        LocalDate date = currentWeekStart.plusDays(i);

                        // Find sessions for this date
                        List<TherapySession> sessionsForDay = new ArrayList<>();
                        for (TherapySession session : weeklySessions) {
                            if (session.getSessionDate().toLocalDate().equals(date)) {
                                sessionsForDay.add(session);
                            }
                        }

                        VBox dayCard = createDayCard(date, sessionsForDay);
                        calendarContainer.getChildren().add(dayCard);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    calendarContainer.getChildren().clear();
                    Label errorLabel = new Label("Failed to load calendar");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                    calendarContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private VBox createDayCard(LocalDate date, List<TherapySession> sessions) {
        VBox card = new VBox(6);
        boolean hasSession = !sessions.isEmpty();
        String bgColor = hasSession ? "#dff9fb" : "#f8f9fa";
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 5; -fx-padding: 10; -fx-border-color: " + (hasSession ? "#26de81" : "#dfe6e9") + "; -fx-border-width: 1; -fx-border-radius: 5;");

        Label dayLabel = new Label(date.format(DateTimeFormatter.ofPattern("EEE, MMM d")));
        dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

        card.getChildren().add(dayLabel);

        if (hasSession) {
            for (TherapySession session : sessions) {
                String timeStr = session.getSessionDate().format(DateTimeFormatter.ofPattern("h:mm a"));
                Label sessionLabel = new Label("📅 " + timeStr + " - " + session.getClientName());
                sessionLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #2d3436;");
                sessionLabel.setWrapText(true);
                card.getChildren().add(sessionLabel);
            }
        } else {
            Label noSession = new Label("No sessions");
            noSession.setStyle("-fx-font-size: 11; -fx-text-fill: #b2bec3;");
            card.getChildren().add(noSession);
        }

        return card;
    }

    private void updateWeekLabel() {
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        String weekStr = currentWeekStart.format(DateTimeFormatter.ofPattern("MMM d")) +
                         " - " + weekEnd.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        weekLabel.setText("Week of " + weekStr);
    }

    private void loadClientList() {
        new Thread(() -> {
            try {
                // Fetch real clients from API
                allClients = instructorService.getAllClients();

                Platform.runLater(() -> {
                    clientListContainer.getChildren().clear();
                    clientComboBox.getItems().clear();

                    if (allClients.isEmpty()) {
                        Label noClients = new Label("No clients yet");
                        noClients.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 13;");
                        clientListContainer.getChildren().add(noClients);
                    } else {
                        for (ClientOverview client : allClients) {
                            VBox clientCard = createClientCard(client);
                            clientListContainer.getChildren().add(clientCard);
                            clientComboBox.getItems().add(client.getClientName());
                        }

                        System.out.println("✅ Loaded " + allClients.size() + " clients");
                    }

                    // Load conversations AFTER clients are loaded
                    loadConversations();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Failed to load clients");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                    clientListContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private VBox createClientCard(ClientOverview client) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #dfe6e9; -fx-border-width: 1; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("👤 " + client.getClientName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewBtn = new Button("View Profile");
        viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
        viewBtn.setOnAction(e -> viewClientProfile(client));

        headerBox.getChildren().addAll(nameLabel, spacer, viewBtn);

        // Stats (consent-based data)
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(8);

        String avgMoodStr = client.getAverageMood() != null ?
            String.format("Avg Mood: %.1f/5", client.getAverageMood()) : "Avg Mood: N/A";
        Label avgMoodLabel = new Label(avgMoodStr);
        avgMoodLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #34495e;");

        String stressStr = client.getStressLevel() != null ?
            "Stress: " + client.getStressLevel() : "Stress: N/A";
        Label stressLabel = new Label(stressStr);
        stressLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #e67e22;");

        String lastSessionStr = client.getLastSessionDate() != null ?
            "Last Session: " + client.getLastSessionDate() : "Last Session: N/A";
        Label lastSessionLabel = new Label(lastSessionStr);
        lastSessionLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        statsGrid.add(avgMoodLabel, 0, 0);
        statsGrid.add(stressLabel, 1, 0);
        statsGrid.add(lastSessionLabel, 0, 1, 2, 1);

        card.getChildren().addAll(headerBox, new Separator(), statsGrid);
        return card;
    }

    private void loadConversations() {
        new Thread(() -> {
            try {
                // ✅ FIXED: Show ALL clients (not just those with messages) so instructor can message anyone
                System.out.println("📥 [Instructor] Loading conversations from API...");
                List<Map<String, Object>> conversations = messageService.getInstructorConversations(Dataholder.userId);

                Platform.runLater(() -> {
                    conversationsContainer.getChildren().clear();

                    // Backend returns ALL clients (even without messages), so we show them all
                    if (conversations.isEmpty()) {
                        Label noClients = new Label("No clients available to message.\n\nClients will appear here after they register.");
                        noClients.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12; -fx-padding: 20; -fx-text-alignment: CENTER;");
                        noClients.setWrapText(true);
                        conversationsContainer.getChildren().add(noClients);
                    } else {
                        System.out.println("✅ [Instructor] Loaded " + conversations.size() + " clients (including those without messages)");

                        // Show all clients - backend returns everyone with role='USER'
                        for (Map<String, Object> conv : conversations) {
                            VBox convCard = createConversationCard(conv);
                            conversationsContainer.getChildren().add(convCard);
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("❌ [Instructor] Error loading conversations: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    conversationsContainer.getChildren().clear();
                    Label errorLabel = new Label("Failed to load clients: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-padding: 10;");
                    conversationsContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    /**
     * ✅ FIXED: Create conversation card - shows ALL clients (with/without messages)
     * - Shows unread badge if client has sent messages
     * - Shows "No messages yet" if no conversation history
     */
    private VBox createConversationCard(Map<String, Object> conv) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");

        // Extract data from conversation map
        Long clientId = ((Number) conv.get("clientId")).longValue();
        String clientName = (String) conv.get("clientName");
        String lastMessage = (String) conv.get("lastMessage");
        Object unreadCountObj = conv.get("unreadCount");
        int unreadCount = unreadCountObj != null ? ((Number) unreadCountObj).intValue() : 0;

        // Header row with name and unread badge
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("💬 " + clientName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #2c3e50;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Unread badge (only show if > 0)
        if (unreadCount > 0) {
            Label unreadBadge = new Label(String.valueOf(unreadCount));
            unreadBadge.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;");
            headerRow.getChildren().addAll(nameLabel, unreadBadge);
        } else {
            headerRow.getChildren().add(nameLabel);
        }

        // Last message preview OR "No messages yet" if no conversation
        if (lastMessage != null && !lastMessage.isEmpty()) {
            Label msgPreview = new Label(lastMessage);
            msgPreview.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
            msgPreview.setMaxWidth(Double.MAX_VALUE);
            msgPreview.setWrapText(false);
            // Truncate long messages
            if (lastMessage.length() > 50) {
                msgPreview.setText(lastMessage.substring(0, 47) + "...");
            }
            card.getChildren().addAll(headerRow, msgPreview);
        } else {
            // ✅ FIXED: Show "No messages yet" so instructor knows they can start a conversation
            Label noMsgLabel = new Label("No messages yet - Click to start conversation");
            noMsgLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11; -fx-font-style: italic;");
            card.getChildren().addAll(headerRow, noMsgLabel);
        }

        // Make clickable
        card.setOnMouseClicked(e -> {
            selectConversation(clientName, clientId);
            // Highlight selected
            conversationsContainer.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    node.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
                }
            });
            card.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196F3; -fx-border-width: 0 0 2 0; -fx-cursor: hand;");
        });

        // Hover effect
        card.setOnMouseEntered(e -> {
            if (!card.getStyle().contains("#e3f2fd")) {
                card.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
            }
        });
        card.setOnMouseExited(e -> {
            if (!card.getStyle().contains("#e3f2fd")) {
                card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
            }
        });

        return card;
    }

    // Action Handlers
    @FXML
    private void handleNotifications() {
        showAlert(Alert.AlertType.INFORMATION, "Notifications", "No new notifications");
    }

    @FXML
    private void handleProfile() {
        showAlert(Alert.AlertType.INFORMATION, "Profile", "Profile settings coming soon");
    }

    @FXML
    private void handleLogout() {
        try {
            Dataholder.userId = null;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mentalhealthdesktop/Login_Signup.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) instructorTitle.getScene().getWindow();
            Scene scene = new Scene(root, 800, 1000);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }

    @FXML
    private void refreshRequests() {
        updateStatus("Refreshing...", true);
        loadSessionRequests();
        updateStatus("Refreshed", true);
    }

    @FXML
    private void handleAvailability() {
        showAlert(Alert.AlertType.INFORMATION, "Availability", "Availability management coming soon");
    }

    @FXML
    private void previousWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
        updateWeekLabel();
        loadCalendar();
    }

    @FXML
    private void nextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
        updateWeekLabel();
        loadCalendar();
    }

    @FXML
    private void refreshClientList() {
        updateStatus("Refreshing clients...", true);
        loadClientList();
        updateStatus("Clients refreshed", true);
    }

    @FXML
    private void selectClientForNotes() {
        selectedClientForNotes = clientComboBox.getValue();
        if (selectedClientForNotes != null) {
            // Find client ID from name
            for (ClientOverview client : allClients) {
                if (client.getClientName().equals(selectedClientForNotes)) {
                    loadPreviousNotes(client.getClientId());
                    break;
                }
            }
        }
    }

    @FXML
    private void createNewNote() {
        therapyNotesArea.clear();
        sessionDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void saveTherapyNote() {
        if (selectedClientForNotes == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a client first");
            return;
        }

        String noteText = therapyNotesArea.getText();
        if (noteText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a note");
            return;
        }

        // Find client ID from name
        Long clientId = null;
        for (ClientOverview client : allClients) {
            if (client.getClientName().equals(selectedClientForNotes)) {
                clientId = client.getClientId();
                break;
            }
        }

        if (clientId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Client not found");
            return;
        }

        final Long finalClientId = clientId;
        new Thread(() -> {
            try {
                TherapyNote note = new TherapyNote();
                note.setClientId(finalClientId);
                note.setInstructorId(Dataholder.userId);
                note.setNoteText(noteText);

                // Set session date from picker (or use today if not set)
                if (sessionDatePicker.getValue() != null) {
                    note.setSessionDate(sessionDatePicker.getValue());
                } else {
                    note.setSessionDate(java.time.LocalDate.now());
                }

                // Set session type from combo box (or use default)
                if (sessionTypeComboBox.getValue() != null) {
                    note.setSessionType(sessionTypeComboBox.getValue());
                } else {
                    note.setSessionType("Progress Note");
                }

                therapyNoteService.createNote(note);

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Therapy note saved successfully");
                    therapyNotesArea.clear();
                    sessionDatePicker.setValue(null);
                    sessionTypeComboBox.setValue(null);
                    loadPreviousNotes(finalClientId);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to save note: " + e.getMessage())
                );
            }
        }).start();
    }

    @FXML
    private void clearNote() {
        therapyNotesArea.clear();
    }

    private void loadPreviousNotes(Long clientId) {
        new Thread(() -> {
            try {
                List<TherapyNote> notes = therapyNoteService.getNotesByClient(clientId);

                Platform.runLater(() -> {
                    previousNotesContainer.getChildren().clear();

                    if (notes.isEmpty()) {
                        Label noNotes = new Label("No previous notes");
                        noNotes.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12;");
                        previousNotesContainer.getChildren().add(noNotes);
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                        for (TherapyNote note : notes) {
                            String dateStr = note.getCreatedAt().format(formatter);
                            Label noteLabel = new Label(dateStr + " - " + note.getNoteText());
                            noteLabel.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5; -fx-font-size: 12;");
                            noteLabel.setWrapText(true);
                            noteLabel.setMaxWidth(Double.MAX_VALUE);
                            previousNotesContainer.getChildren().add(noteLabel);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    previousNotesContainer.getChildren().clear();
                    Label errorLabel = new Label("Failed to load previous notes");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                    previousNotesContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    private void selectConversation(String clientName, Long clientId) {
        selectedConversationClient = clientName;
        selectedConversationClientId = clientId;
        selectedClientLabel.setText("Messaging: " + clientName);
        loadMessages(clientId);

        // ✅ FIX: Mark all messages in this conversation as read
        // This will remove notifications and update unread counts
        new Thread(() -> {
            try {
                int markedCount = messageService.markConversationAsRead(Dataholder.userId, clientId);
                System.out.println("✅ [Instructor] Marked " + markedCount + " messages as read with client: " + clientName);

                // Refresh conversations list to update unread badges
                Platform.runLater(() -> {
                    loadConversations();
                });
            } catch (Exception e) {
                System.err.println("⚠️ [Instructor] Failed to mark messages as read: " + e.getMessage());
                // Don't show error to user - this is a background operation
            }
        }).start();
    }

    private void loadMessages(Long clientId) {
        new Thread(() -> {
            try {
                List<Message> messages = messageService.getConversationMessages(clientId);

                Platform.runLater(() -> {
                    messagesContainer.getChildren().clear();

                    if (messages.isEmpty()) {
                        Label noMessages = new Label("No messages yet. Start a conversation!");
                        noMessages.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12;");
                        messagesContainer.getChildren().add(noMessages);
                    } else {
                        for (Message message : messages) {
                            Label msgLabel = new Label(message.getMessageText());

                            // Style based on sender
                            boolean isSentByMe = message.getSenderId().equals(Dataholder.userId);
                            if (isSentByMe) {
                                msgLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 10; -fx-font-size: 12;");
                                HBox msgBox = new HBox(msgLabel);
                                msgBox.setAlignment(Pos.CENTER_RIGHT);
                                msgBox.setPadding(new Insets(5));
                                messagesContainer.getChildren().add(msgBox);
                            } else {
                                msgLabel.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-padding: 10; -fx-background-radius: 10; -fx-font-size: 12;");
                                HBox msgBox = new HBox(msgLabel);
                                msgBox.setAlignment(Pos.CENTER_LEFT);
                                msgBox.setPadding(new Insets(5));
                                messagesContainer.getChildren().add(msgBox);
                            }

                            msgLabel.setMaxWidth(300);
                            msgLabel.setWrapText(true);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    messagesContainer.getChildren().clear();
                    Label errorLabel = new Label("Failed to load messages");
                    errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                    messagesContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }

    @FXML
    private void sendMessage() {
        String messageText = messageTextArea.getText();
        if (messageText.isEmpty() || selectedConversationClientId == null) {
            return;
        }

        new Thread(() -> {
            try {
                messageService.sendMessage(selectedConversationClientId, messageText);

                // Create notification for the user
                try {
                    com.example.mentalhealthdesktop.model.Notification notification =
                        new com.example.mentalhealthdesktop.model.Notification();
                    notification.setUserId(selectedConversationClientId);
                    notification.setType("MESSAGE");
                    notification.setTitle("New Message from Your Therapist 💬");
                    notification.setMessage(messageText);

                    NotificationService notificationService = new NotificationService();
                    notificationService.createNotification(notification);

                    System.out.println("✅ Message notification sent to user ID: " + selectedConversationClientId);
                } catch (Exception notifEx) {
                    System.err.println("⚠️ Failed to send message notification: " + notifEx.getMessage());
                }

                Platform.runLater(() -> {
                    // Add message to UI immediately
                    Label newMsg = new Label(messageText);
                    newMsg.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 10; -fx-font-size: 12;");
                    newMsg.setMaxWidth(300);
                    newMsg.setWrapText(true);

                    HBox msgBox = new HBox(newMsg);
                    msgBox.setAlignment(Pos.CENTER_RIGHT);
                    msgBox.setPadding(new Insets(5));

                    messagesContainer.getChildren().add(msgBox);
                    messageTextArea.clear();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to send message: " + e.getMessage())
                );
            }
        }).start();
    }


    @FXML
    private void updateAnalytics() {
        new Thread(() -> {
            try {
                String timeRange = analyticsTimeRangeCombo.getValue();
                if (timeRange == null) {
                    timeRange = "Last 30 Days";
                }

                Map<String, Object> analytics = instructorService.getAnalytics(timeRange);

                Platform.runLater(() -> {
                    totalSessionsLabel.setText(String.valueOf(analytics.getOrDefault("totalSessions", 0)));
                    completedSessionsLabel.setText(String.valueOf(analytics.getOrDefault("completedSessions", 0)));

                    Object avgRating = analytics.get("avgRating");
                    if (avgRating != null) {
                        avgRatingLabel.setText(String.format("%.1f", ((Number) avgRating).doubleValue()));
                    } else {
                        avgRatingLabel.setText("N/A");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    totalSessionsLabel.setText("--");
                    completedSessionsLabel.setText("--");
                    avgRatingLabel.setText("--");
                });
            }
        }).start();
    }

    private void acceptRequest(SessionRequest request) {
        // ✅ UPDATED: No manual zoom link prompt needed - backend creates automatically!

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Accept Session Request");
        confirmAlert.setHeaderText("Accept session request from " + request.getClientName() + "?");
        confirmAlert.setContentText("A Zoom meeting will be created automatically and sent to the client.\n\n" +
            "📅 Date: " + request.getRequestedDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")) + "\n" +
            "⏱️ Duration: 60 minutes");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        System.out.println("🎥 Accepting session request - Zoom meeting will be created automatically...");

                        // Call backend - it will create Zoom meeting automatically!
                        sessionRequestService.acceptRequest(request.getId(), null);  // null = automatic creation

                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Request Accepted ✅",
                                "Session request from " + request.getClientName() + " has been accepted!\n\n" +
                                "✅ Zoom meeting created automatically\n" +
                                "✅ Client notified with join link\n\n" +
                                "The meeting link has been sent to the client's notifications.");
                            loadSessionRequests();
                            loadCalendar();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to accept request: " + e.getMessage() + "\n\n" +
                                "Make sure Zoom API is configured in the backend.")
                        );
                    }
                }).start();
            }
        });
    }

    private void declineRequest(SessionRequest request) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Decline Request");
        confirmAlert.setHeaderText("Decline session request from " + request.getClientName() + "?");
        confirmAlert.setContentText("This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        sessionRequestService.declineRequest(request.getId());

                        // Create notification for the user
                        try {
                            com.example.mentalhealthdesktop.model.Notification notification =
                                new com.example.mentalhealthdesktop.model.Notification();
                            notification.setUserId(request.getClientId());
                            notification.setType("SESSION");
                            notification.setTitle("Session Request Update");
                            notification.setMessage("We're sorry, but your therapy session request for " +
                                request.getRequestedDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")) +
                                " could not be confirmed at this time.\n\n" +
                                "Please try requesting a different time slot or contact us for more information.");
                            notification.setRelatedEntityId(request.getId());

                            NotificationService notificationService = new NotificationService();
                            notificationService.createNotification(notification);

                            System.out.println("✅ Notification sent to user ID: " + request.getClientId());
                        } catch (Exception notifEx) {
                            System.err.println("⚠️ Failed to send notification: " + notifEx.getMessage());
                        }

                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Request Declined",
                                "Session request from " + request.getClientName() + " has been declined.");
                            loadSessionRequests();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to decline request: " + e.getMessage())
                        );
                    }
                }).start();
            }
        });
    }

    private void viewRequestDetails(SessionRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        String details = String.format(
            "Client: %s\n\nReason: %s\n\nRequested Date: %s\n\nStatus: %s",
            request.getClientName(),
            request.getReason(),
            request.getRequestedDate().format(formatter),
            request.getStatus()
        );

        showAlert(Alert.AlertType.INFORMATION, "Request Details", details);
    }

    private void viewClientProfile(ClientOverview client) {

        // Navigate to detailed progress view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mentalhealthdesktop/ClientProgressView.fxml"));
            Parent root = loader.load();

            ClientProgressViewController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) instructorTitle.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("Client Progress - " + client.getClientName());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open client progress view: " + e.getMessage());
        }
    }



    private void updateStatus(String status, boolean success) {
        statusLabel.setText(status);
        statusLabel.setTextFill(success ? Color.web("#2ecc71") : Color.web("#e74c3c"));
        lastUpdateLabel.setText("Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm")));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ✅ FIX #2: Start auto-refresh timer for conversations
    private void startAutoRefresh() {
        autoRefreshTimer = new java.util.Timer();
        autoRefreshTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        // Refresh conversations only
                        loadConversations();
                    } catch (Exception e) {
                        System.err.println("❌ Error during auto-refresh: " + e.getMessage());
                    }
                });
            }
        }, 0, 5000); // Every 5 seconds
    }

    // ✅ FIX #2: Stop auto-refresh timer
    private void stopAutoRefresh() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer = null;
        }
    }
}

