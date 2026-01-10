package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.service.CommunityForumService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class CommunityForumController {

    // Forum Posts Tab Components
    @FXML private TextField postTitleField;
    @FXML private TextArea postContentArea;
    @FXML private CheckBox anonymousCheckBox;
    @FXML private Button createPostButton;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private TextField searchField;
    @FXML private VBox postsContainer;
    @FXML private Button backButton;
    @FXML private ProgressIndicator loadingIndicator;

    // Group Chat Tab Components
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextArea chatMessageInput;
    @FXML private CheckBox chatAnonymousCheckBox;
    @FXML private Button sendMessageButton;
    @FXML private Label onlineUsersLabel;

    private final CommunityForumService forumService = new CommunityForumService();
    private Timer chatRefreshTimer;

    @FXML
    public void initialize() {
        System.out.println(" [CommunityForum] Initializing Community Forum...");
        sortComboBox.setValue("Latest");
        loadPosts();

        // Initialize Group Chat
        initializeGroupChat();

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void initializeGroupChat() {
        System.out.println("💬 [GroupChat] Initializing Group Chat...");
        loadChatMessages();
        updateOnlineUsers();

        // Auto-refresh chat every 3 seconds
        chatRefreshTimer = new Timer(true);
        chatRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                loadChatMessages();
                updateOnlineUsers();
            }
        }, 3000, 3000); // Start after 3 seconds, repeat every 3 seconds
    }

    private void setupKeyboardShortcuts() {
        if (chatMessageInput != null) {
            chatMessageInput.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER") && event.isControlDown()) {
                    handleSendChatMessage();
                    event.consume();
                }
            });
        }
    }

    @FXML
    private void handleCreatePost() {
        String title = postTitleField.getText().trim();
        String content = postContentArea.getText().trim();

        System.out.println("=== CREATE POST DEBUG ===");
        System.out.println("Title: " + title);
        System.out.println("Content length: " + content.length());
        System.out.println("User ID: " + Dataholder.userId);
        System.out.println("Anonymous: " + anonymousCheckBox.isSelected());
        System.out.println("========================");

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Validation Error", "Please fill in both title and content", Alert.AlertType.ERROR);
            return;
        }

        if (Dataholder.userId == null) {
            System.err.println(" ERROR: User ID is NULL! User not logged in properly.");
            showAlert("Error", "User not logged in", Alert.AlertType.ERROR);
            return;
        }

        createPostButton.setDisable(true);

        new Thread(() -> {
            try {
                System.out.println(" [CommunityForum] Creating post: " + title);
                System.out.println("[CommunityForum] Sending request to backend with userId: " + Dataholder.userId);
                forumService.createPost(title, content, Dataholder.userId, anonymousCheckBox.isSelected());

                Platform.runLater(() -> {
                    postTitleField.clear();
                    postContentArea.clear();
                    anonymousCheckBox.setSelected(false);
                    createPostButton.setDisable(false);
                    showAlert("Success", "Post created successfully!", Alert.AlertType.INFORMATION);
                    loadPosts();
                });
            } catch (Exception e) {
                System.err.println(" [CommunityForum] Failed to create post!");
                System.err.println("Error message: " + e.getMessage());
                Platform.runLater(() -> {
                    createPostButton.setDisable(false);
                    showAlert("Error", "Failed to create post: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void loadPosts() {
        loadingIndicator.setVisible(true);
        postsContainer.getChildren().clear();

        String sortType = sortComboBox.getValue() != null ? sortComboBox.getValue() : "Latest";

        new Thread(() -> {
            try {
                System.out.println(" [CommunityForum] Loading posts with sort: " + sortType);
                Long currentUserId = Dataholder.userId != null ? Dataholder.userId : 0L;
                JsonArray posts = forumService.getPosts(sortType, currentUserId);

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    if (posts != null) {
                        displayPosts(posts);
                        System.out.println("[CommunityForum] Successfully loaded " + posts.size() + " posts");
                    } else {
                        displayEmptyPosts();
                    }
                });
            } catch (Exception e) {
                System.err.println(" [CommunityForum] Failed to load posts: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    displayEmptyPosts();

                });
            }
        }).start();
    }

    private void displayEmptyPosts() {
        postsContainer.getChildren().clear();
        Label emptyLabel = new Label("No posts yet. Be the first to share! 📝");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 50; -fx-alignment: center;");
        postsContainer.getChildren().add(emptyLabel);
    }

    private void displayPosts(JsonArray posts) {
        postsContainer.getChildren().clear();

        if (posts.isEmpty()) {
            Label emptyLabel = new Label("No posts yet. Be the first to share!");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-padding: 50;");
            postsContainer.getChildren().add(emptyLabel);
            return;
        }

        System.out.println(" [CommunityForum] Displaying " + posts.size() + " posts");

        for (int i = 0; i < posts.size(); i++) {
            JsonObject post = posts.get(i).getAsJsonObject();
            VBox postCard = createPostCard(post);
            postsContainer.getChildren().add(postCard);
        }
    }

    private VBox createPostCard(JsonObject post) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(1100);

        // Header with author and date
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String authorName = post.get("anonymous").getAsBoolean() ?
                           "👤 Anonymous User" :
                           "👤 " + post.get("authorName").getAsString();
        Label authorLabel = new Label(authorName);
        authorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        authorLabel.setStyle("-fx-text-fill: #2C3E50;");

        String dateStr = post.has("createdAt") ? post.get("createdAt").getAsString() : "";
        Label dateLabel = new Label("🕐 " + formatDate(dateStr));
        dateLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Delete button if own post
        HBox headerRight = new HBox(10);
        headerRight.setAlignment(Pos.CENTER_RIGHT);

        if (post.has("authorId") && post.get("authorId").getAsLong() == Dataholder.userId) {
            Button deleteButton = new Button("🗑️ Delete");
            deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; " +
                                "-fx-padding: 5 10; -fx-background-radius: 4; -fx-cursor: hand;");
            deleteButton.setOnAction(e -> handleDeletePost(post.get("id").getAsLong()));
            headerRight.getChildren().add(deleteButton);
        }

        header.getChildren().addAll(authorLabel, spacer, dateLabel, headerRight);

        // Title
        Label titleLabel = new Label(post.get("title").getAsString());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        // Content
        Label contentLabel = new Label(post.get("content").getAsString());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e; -fx-padding: 10 0;");

        // Separator
        Separator separator = new Separator();

        // Actions (Like, Comment)
        HBox actions = new HBox(20);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(5, 0, 0, 0));

        int likeCount = post.get("likes").getAsInt();
        boolean isLiked = post.has("isLikedByCurrentUser") && post.get("isLikedByCurrentUser").getAsBoolean();

        Button likeButton = new Button((isLiked ? "❤️" : "🤍") + " " + likeCount + " Likes");
        likeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (isLiked ? "#e74c3c" : "#7f8c8d") +
                           "; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 5 15; -fx-border-color: #bdc3c7; " +
                           "-fx-border-radius: 5; -fx-background-radius: 5;");
        likeButton.setOnAction(e -> handleLikePost(post.get("id").getAsLong(), likeButton));

        // ⭐ Handle both 'commentCount' and 'commentsCount' field names from backend
        int commentCount = 0;
        if (post.has("commentCount")) {
            commentCount = post.get("commentCount").getAsInt();
        } else if (post.has("commentsCount")) {
            commentCount = post.get("commentsCount").getAsInt();
        }

        Button commentButton = new Button("💬 " + commentCount + " Comments");
        commentButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 13px; " +
                              "-fx-cursor: hand; -fx-padding: 5 15; -fx-border-color: #bdc3c7; -fx-border-radius: 5; " +
                              "-fx-background-radius: 5;");
        commentButton.setOnAction(e -> showComments(post));

        actions.getChildren().addAll(likeButton, commentButton);

        card.getChildren().addAll(header, titleLabel, contentLabel, separator, actions);
        return card;
    }

    private void handleLikePost(Long postId, Button likeButton) {
        likeButton.setDisable(true);

        new Thread(() -> {
            try {
                System.out.println("❤️ [CommunityForum] Toggling like for post: " + postId);
                forumService.toggleLike(postId, Dataholder.userId);

                Platform.runLater(() -> {
                    likeButton.setDisable(false);
                    loadPosts(); // Refresh to show updated like count
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    likeButton.setDisable(false);
                    showAlert("Error", "Failed to like post", Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void handleDeletePost(Long postId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Post");
        confirm.setContentText("Are you sure you want to delete this post?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        System.out.println("🗑️ [CommunityForum] Deleting post: " + postId);
                        forumService.deletePost(postId);

                        Platform.runLater(() -> {
                            showAlert("Success", "Post deleted successfully", Alert.AlertType.INFORMATION);
                            loadPosts();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() ->
                            showAlert("Error", "Failed to delete post", Alert.AlertType.ERROR)
                        );
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private void showComments(JsonObject post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mentalhealthdesktop/PostComments.fxml"));
            Parent root = loader.load();

            PostCommentsController controller = loader.getController();
            controller.setPost(post);

            // Set callback to refresh posts when comment count changes
            controller.setOnCommentCountChanged(() -> {
                System.out.println(" [CommunityForum] Comment count changed, refreshing posts...");
                loadPosts();
            });

            Stage stage = new Stage();
            stage.setTitle("Comments - " + post.get("title").getAsString());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setWidth(700);
            stage.setHeight(600);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open comments", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSortChange() {
        loadPosts();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showAlert("Search", "Please enter a search term", Alert.AlertType.WARNING);
            return;
        }

        loadingIndicator.setVisible(true);
        postsContainer.getChildren().clear();

        new Thread(() -> {
            try {
                System.out.println("🔍 [CommunityForum] Searching for: " + query);
                JsonArray results = forumService.searchPosts(query);

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    displayPosts(results);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showAlert("Error", "Search failed: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        loadPosts();
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mentalhealthdesktop/User_dash.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            System.out.println("🔙 [CommunityForum] Returning to User Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to dashboard", Alert.AlertType.ERROR);
        }
    }

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "Just now";

        try {
            LocalDateTime dt = LocalDateTime.parse(dateTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            return dt.format(formatter);
        } catch (Exception e) {
            return dateTime;
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==================== GROUP CHAT METHODS ====================

    @FXML
    private void handleSendChatMessage() {
        String message = chatMessageInput.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        if (Dataholder.userId == null) {
            showAlert("Error", "User not logged in", Alert.AlertType.ERROR);
            return;
        }

        sendMessageButton.setDisable(true);

        new Thread(() -> {
            try {
                System.out.println("[GroupChat] Sending message: " + message.substring(0, Math.min(20, message.length())) + "...");
                forumService.sendChatMessage(message, Dataholder.userId, chatAnonymousCheckBox.isSelected());

                Platform.runLater(() -> {
                    chatMessageInput.clear();
                    chatAnonymousCheckBox.setSelected(false);
                    sendMessageButton.setDisable(false);
                    loadChatMessages();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    sendMessageButton.setDisable(false);
                    showAlert("Error", "Failed to send message: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void loadChatMessages() {
        new Thread(() -> {
            try {
                JsonArray messages = forumService.getChatMessages();

                Platform.runLater(() -> {
                    displayChatMessages(messages);
                    scrollChatToBottom();
                });
            } catch (Exception e) {
                // Silent fail for auto-refresh
                System.err.println("Failed to load chat messages: " + e.getMessage());
            }
        }).start();
    }

    private void displayChatMessages(JsonArray messages) {
        chatMessagesContainer.getChildren().clear();

        if (messages.isEmpty()) {
            Label emptyLabel = new Label("No messages yet. Be the first to say hi! 👋");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; -fx-padding: 50; -fx-alignment: center;");
            chatMessagesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (int i = 0; i < messages.size(); i++) {
            JsonObject message = messages.get(i).getAsJsonObject();
            VBox messageCard = createChatMessageCard(message);
            chatMessagesContainer.getChildren().add(messageCard);
        }
    }

    private VBox createChatMessageCard(JsonObject message) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 1);");
        card.setPrefWidth(1050);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        boolean isAnonymous = message.get("anonymous").getAsBoolean();
        boolean isOwnMessage = message.has("userId") && message.get("userId").getAsLong() == Dataholder.userId;

        String authorName = isAnonymous ? "👤 Anonymous" : "👤 " + message.get("userName").getAsString();
        Label authorLabel = new Label(authorName);
        authorLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        authorLabel.setStyle("-fx-text-fill: " + (isOwnMessage ? "#3498db" : "#34495e") + ";");

        if (isOwnMessage && !isAnonymous) {
            Label youBadge = new Label("(You)");
            youBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #3498db; -fx-font-style: italic;");
            header.getChildren().add(youBadge);
        }

        String timeStr = message.has("createdAt") ? message.get("createdAt").getAsString() : "";
        Label timeLabel = new Label("🕐 " + formatChatTime(timeStr));
        timeLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(authorLabel, spacer, timeLabel);

        // Message content
        Label contentLabel = new Label(message.get("message").getAsString());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-padding: 5 0 0 0;");

        card.getChildren().addAll(header, contentLabel);
        return card;
    }

    private void scrollChatToBottom() {
        if (chatScrollPane != null) {
            chatScrollPane.setVvalue(1.0);
        }
    }

    private void updateOnlineUsers() {
        new Thread(() -> {
            try {
                int count = forumService.getOnlineUsersCount();

                Platform.runLater(() -> {
                    if (onlineUsersLabel != null) {
                        onlineUsersLabel.setText("👥 " + count + " users online");
                    }
                });
            } catch (Exception e) {
                // Silent fail
            }
        }).start();
    }

    private String formatChatTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "Just now";

        try {
            LocalDateTime dt = LocalDateTime.parse(dateTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return dt.format(formatter);
        } catch (Exception e) {
            return "Now";
        }
    }

    // Cleanup timer when controller is destroyed
    public void cleanup() {
        if (chatRefreshTimer != null) {
            chatRefreshTimer.cancel();
        }
    }
}

