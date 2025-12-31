package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.SessionManager;
import com.example.mentalhealthdesktop.service.MessageService;
import com.example.mentalhealthdesktop.service.NotificationService;
import javafx.application.HostServices;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ✅ UPDATED: Controller for displaying notifications with action buttons
 * - Shows "Join Meeting" button for SESSION_ACCEPTED notifications
 * - Shows "Reply" button for MESSAGE notifications
 * - Uses SessionManager for dynamic user handling
 * - Integrates with backend /with-details endpoint
 */
public class NotificationController {

    @FXML private VBox notificationsContainer;
    @FXML private Button allNotificationsBtn;
    @FXML private Button messagesBtn;
    @FXML private Button sessionsBtn;
    @FXML private Button systemBtn;

    private final NotificationService notificationService = new NotificationService();
    private final MessageService messageService = new MessageService();

    private List<Map<String, Object>> allNotificationsWithDetails;
    private String currentFilter = "ALL";
    private HostServices hostServices;

    @FXML
    public void initialize() {
        System.out.println("✅ [NotificationController] Initializing...");
        loadNotifications();
    }

    /**
     * Set host services for opening URLs in browser
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
        System.out.println("✅ [NotificationController] HostServices set - can open URLs");
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 [NotificationController] Refreshing notifications...");
        loadNotifications();
    }

    @FXML
    private void handleMarkAllRead() {
        new Thread(() -> {
            try {
                Long userId = getUserId();
                notificationService.markAllAsRead(userId);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "All notifications marked as read");
                    loadNotifications();
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to mark all as read: " + e.getMessage())
                );
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void showAllNotifications() {
        currentFilter = "ALL";
        updateFilterButtons();
        if (allNotificationsWithDetails != null) {
            displayNotifications(allNotificationsWithDetails);
        }
    }

    @FXML
    private void showMessages() {
        currentFilter = "MESSAGE";
        updateFilterButtons();
        if (allNotificationsWithDetails != null) {
            List<Map<String, Object>> filtered = allNotificationsWithDetails.stream()
                    .filter(n -> "MESSAGE".equals(n.get("notificationType")))
                    .collect(Collectors.toList());
            displayNotifications(filtered);
        }
    }

    @FXML
    private void showSessions() {
        currentFilter = "SESSION";
        updateFilterButtons();
        if (allNotificationsWithDetails != null) {
            List<Map<String, Object>> filtered = allNotificationsWithDetails.stream()
                    .filter(n -> {
                        String type = (String) n.get("notificationType");
                        return "SESSION".equals(type) || "SESSION_ACCEPTED".equals(type);
                    })
                    .collect(Collectors.toList());
            displayNotifications(filtered);
        }
    }

    @FXML
    private void showSystem() {
        currentFilter = "SYSTEM";
        updateFilterButtons();
        if (allNotificationsWithDetails != null) {
            List<Map<String, Object>> filtered = allNotificationsWithDetails.stream()
                    .filter(n -> "SYSTEM".equals(n.get("notificationType")))
                    .collect(Collectors.toList());
            displayNotifications(filtered);
        }
    }

    /**
     * Load notifications with details from backend
     */
    private void loadNotifications() {
        new Thread(() -> {
            try {
                Long userId = getUserId();

                if (userId == null) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.WARNING, "Not Logged In", "Please log in to view notifications");
                    });
                    return;
                }

                System.out.println("🔍 [NotificationController] Fetching notifications for user: " + userId);

                // ✅ Use new endpoint with details
                allNotificationsWithDetails = notificationService.getUserNotificationsWithDetails(userId);

                System.out.println("📥 [NotificationController] Received " + allNotificationsWithDetails.size() + " notifications");

                Platform.runLater(() -> {
                    if (currentFilter.equals("ALL")) {
                        displayNotifications(allNotificationsWithDetails);
                    } else {
                        // Apply current filter
                        switch (currentFilter) {
                            case "MESSAGE" -> showMessages();
                            case "SESSION" -> showSessions();
                            case "SYSTEM" -> showSystem();
                        }
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ [NotificationController] Error loading notifications: " + e.getMessage());
                Platform.runLater(() -> {
                    notificationsContainer.getChildren().clear();
                    Label errorLabel = new Label("Failed to load notifications: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14; -fx-padding: 20;");
                    notificationsContainer.getChildren().add(errorLabel);
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Display notifications with action buttons
     */
    private void displayNotifications(List<Map<String, Object>> notifications) {
        notificationsContainer.getChildren().clear();

        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("📭 No notifications to display");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16; -fx-padding: 40;");
            notificationsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Map<String, Object> notification : notifications) {
            notificationsContainer.getChildren().add(createNotificationCard(notification));
        }
    }

    /**
     * ✅ NEW: Create notification card with action buttons
     */
    private VBox createNotificationCard(Map<String, Object> notification) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));

        Boolean isRead = (Boolean) notification.get("isRead");
        String bgColor = (isRead != null && isRead) ? "#ffffff" : "#e3f2fd";
        card.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #dfe6e9; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Header with icon and title
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String type = (String) notification.get("notificationType");
        Label iconLabel = new Label(getTypeIcon(type));
        iconLabel.setStyle("-fx-font-size: 24;");

        Label titleLabel = new Label((String) notification.get("title"));
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(iconLabel, titleLabel);

        // Message content
        Label messageLabel = new Label((String) notification.get("message"));
        messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #34495e; -fx-padding: 8 0;");
        messageLabel.setWrapText(true);

        card.getChildren().addAll(header, new Separator(), messageLabel);

        // ✅ Add action buttons based on notification type
        if ("SESSION_ACCEPTED".equals(type)) {
            addJoinMeetingButton(card, notification);
        } else if ("MESSAGE".equals(type)) {
            addReplyButton(card, notification);
        }

        // Footer with timestamp and mark as read
        HBox footer = new HBox(10);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String timestamp = (String) notification.get("createdAt");
        Label timeLabel = new Label("🕒 " + (timestamp != null ? timestamp : ""));
        timeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        if (isRead == null || !isRead) {
            Button markReadBtn = new Button("Mark as Read");
            markReadBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                    "-fx-padding: 5 10; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11;");
            markReadBtn.setOnAction(e -> markAsRead(notification));
            footer.getChildren().addAll(timeLabel, spacer, markReadBtn);
        } else {
            footer.getChildren().add(timeLabel);
        }

        card.getChildren().add(footer);
        return card;
    }

    /**
     * ✅ Add "Join Meeting" button for session notifications
     */
    private void addJoinMeetingButton(VBox card, Map<String, Object> notification) {
        Boolean canJoin = (Boolean) notification.get("canJoin");
        String zoomLink = (String) notification.get("zoomLink");

        if (canJoin != null && canJoin && zoomLink != null && !zoomLink.isEmpty()) {
            // Session details
            String sessionDate = (String) notification.get("sessionDate");
            String instructorName = (String) notification.get("instructorName");

            if (sessionDate != null || instructorName != null) {
                Label detailsLabel = new Label(
                        (sessionDate != null ? "📅 " + sessionDate : "") +
                                (instructorName != null ? " with " + instructorName : "")
                );
                detailsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d; -fx-padding: 5 0;");
                card.getChildren().add(detailsLabel);
            }

            // Join button
            Button joinButton = new Button("🎥 Join Meeting");
            joinButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                    "-fx-padding: 12 24; -fx-background-radius: 5; -fx-cursor: hand; " +
                    "-fx-font-size: 14; -fx-font-weight: bold;");
            joinButton.setOnAction(e -> {
                System.out.println("🎥 Opening Zoom link: " + zoomLink);
                openZoomLink(zoomLink);
            });

            card.getChildren().add(joinButton);
        }
    }

    /**
     * ✅ Add "Reply" button for message notifications
     */
    private void addReplyButton(VBox card, Map<String, Object> notification) {
        Boolean canReply = (Boolean) notification.get("canReply");
        Object senderIdObj = notification.get("senderId");
        String senderName = (String) notification.get("senderName");

        if (canReply != null && canReply && senderIdObj != null) {
            Long senderId = ((Number) senderIdObj).longValue();

            Button replyButton = new Button("💬 Reply to " + (senderName != null ? senderName : "Sender"));
            replyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                    "-fx-padding: 12 24; -fx-background-radius: 5; -fx-cursor: hand; " +
                    "-fx-font-size: 14; -fx-font-weight: bold;");
            replyButton.setOnAction(e -> {
                System.out.println("💬 Opening reply dialog for: " + senderName + " (ID: " + senderId + ")");
                openReplyDialog(senderId, senderName);
            });

            card.getChildren().add(replyButton);
        }
    }

    /**
     * Open Zoom link in browser
     */
    private void openZoomLink(String zoomLink) {
        if (hostServices != null) {
            hostServices.showDocument(zoomLink);
            showAlert(Alert.AlertType.INFORMATION, "Opening Zoom", "Opening meeting in your browser...");
        } else {
            // Fallback: Copy to clipboard
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(zoomLink);
            clipboard.setContent(content);
            showAlert(Alert.AlertType.INFORMATION, "Zoom Link Copied",
                    "Zoom link copied to clipboard:\n\n" + zoomLink + "\n\nPaste it in your browser to join.");
        }
    }

    /**
     * Open reply dialog
     */
    private void openReplyDialog(Long recipientId, String recipientName) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reply to " + recipientName);
        dialog.setHeaderText("Send a reply to " + recipientName);
        dialog.setContentText("Your message:");

        dialog.showAndWait().ifPresent(messageText -> {
            if (!messageText.trim().isEmpty()) {
                sendReply(recipientId, recipientName, messageText);
            }
        });
    }

    /**
     * Send reply message
     */
    private void sendReply(Long recipientId, String recipientName, String messageText) {
        new Thread(() -> {
            try {
                Long currentUserId = getUserId();

                System.out.println("📤 Sending reply from " + currentUserId + " to " + recipientId);
                messageService.sendMessageToInstructor(currentUserId, recipientId, messageText);

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Reply Sent",
                            "Your reply to " + recipientName + " has been sent successfully!");
                });
            } catch (Exception e) {
                System.err.println("❌ Error sending reply: " + e.getMessage());
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to send reply: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Mark notification as read
     */
    private void markAsRead(Map<String, Object> notification) {
        new Thread(() -> {
            try {
                Long notificationId = ((Number) notification.get("id")).longValue();
                notificationService.markAsRead(notificationId);

                Platform.runLater(this::loadNotifications);
            } catch (Exception e) {
                System.err.println("❌ Error marking as read: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Update filter button styles
     */
    private void updateFilterButtons() {
        String inactiveStyle = "-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-padding: 8 20; -fx-background-radius: 5; -fx-cursor: hand;";

        allNotificationsBtn.setStyle(inactiveStyle);
        messagesBtn.setStyle(inactiveStyle);
        sessionsBtn.setStyle(inactiveStyle);
        systemBtn.setStyle(inactiveStyle);

        switch (currentFilter) {
            case "ALL" -> allNotificationsBtn.setStyle(activeStyle);
            case "MESSAGE" -> messagesBtn.setStyle(activeStyle);
            case "SESSION" -> sessionsBtn.setStyle(activeStyle);
            case "SYSTEM" -> systemBtn.setStyle(activeStyle);
        }
    }

    /**
     * Get type icon for notification
     */
    private String getTypeIcon(String type) {
        if (type == null || type.isEmpty()) {
            return "❓";
        }

        return switch (type.toUpperCase()) {
            case "MESSAGE" -> "💬";
            case "SESSION", "SESSION_ACCEPTED" -> "📅";
            case "SYSTEM" -> "🔔";
            default -> "📌";
        };
    }

    /**
     * Get current user ID (with SessionManager support)
     */
    private Long getUserId() {
        Long userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null) {
            userId = Dataholder.userId;
            System.out.println("⚠️ [NotificationController] Using Dataholder (SessionManager not set)");
        }
        return userId;
    }

    /**
     * Navigate back to dashboard
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mentalhealthdesktop/User_dash.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) notificationsContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to go back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

