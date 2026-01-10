package com.example.mentalhealthdesktop.controller;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.service.CommunityForumService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PostCommentsController {

    @FXML private VBox postInfoBox;
    @FXML private TextArea commentContentArea;
    @FXML private CheckBox commentAnonymousCheckBox;
    @FXML private Button addCommentButton;
    @FXML private VBox commentsContainer;
    @FXML private Button closeButton;
    @FXML private ProgressIndicator loadingIndicator;

    private final CommunityForumService forumService = new CommunityForumService();
    private JsonObject currentPost;
    private Runnable onCommentCountChanged;

    public void setPost(JsonObject post) {
        this.currentPost = post;
        displayPostInfo();
        loadComments();
    }


    public void setOnCommentCountChanged(Runnable callback) {
        this.onCommentCountChanged = callback;
    }

    private void displayPostInfo() {
        postInfoBox.getChildren().clear();

        // Post title
        Label titleLabel = new Label(currentPost.get("title").getAsString());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        // Post content preview
        String content = currentPost.get("content").getAsString();
        if (content.length() > 200) {
            content = content.substring(0, 200) + "...";
        }
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        postInfoBox.getChildren().addAll(titleLabel, contentLabel);
    }

    private void loadComments() {
        loadingIndicator.setVisible(true);
        commentsContainer.getChildren().clear();

        Long postId = currentPost.get("id").getAsLong();

        new Thread(() -> {
            try {
                System.out.println("📥 [PostComments] Loading comments for post: " + postId);
                JsonArray comments = forumService.getComments(postId);

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    displayComments(comments);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    showAlert("Error", "Failed to load comments: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void displayComments(JsonArray comments) {
        commentsContainer.getChildren().clear();

        if (comments.size() == 0) {
            Label emptyLabel = new Label("No comments yet. Be the first to comment!");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; -fx-padding: 20;");
            commentsContainer.getChildren().add(emptyLabel);
            return;
        }

        System.out.println(" [PostComments] Displaying " + comments.size() + " comments");

        for (int i = 0; i < comments.size(); i++) {
            JsonObject comment = comments.get(i).getAsJsonObject();
            VBox commentCard = createCommentCard(comment);
            commentsContainer.getChildren().add(commentCard);
        }
    }

    private VBox createCommentCard(JsonObject comment) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 5; " +
                     "-fx-border-color: #dee2e6; -fx-border-radius: 5;");
        card.setPrefWidth(640);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String authorName = comment.get("anonymous").getAsBoolean() ?
                           "👤 Anonymous" :
                           "👤 " + comment.get("authorName").getAsString();
        Label authorLabel = new Label(authorName);
        authorLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        authorLabel.setStyle("-fx-text-fill: #495057;");

        String dateStr = comment.has("createdAt") ? comment.get("createdAt").getAsString() : "";
        Label dateLabel = new Label("🕐 " + formatDate(dateStr));
        dateLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Delete button if own comment
        HBox headerRight = new HBox(5);
        headerRight.setAlignment(Pos.CENTER_RIGHT);

        if (comment.has("authorId") && comment.get("authorId").getAsLong() == Dataholder.userId) {
            Button deleteButton = new Button("🗑️");
            deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px; " +
                                "-fx-padding: 3 8; -fx-background-radius: 3; -fx-cursor: hand;");
            deleteButton.setOnAction(e -> handleDeleteComment(comment.get("id").getAsLong()));
            headerRight.getChildren().add(deleteButton);
        }

        header.getChildren().addAll(authorLabel, spacer, dateLabel, headerRight);

        // Content
        Label contentLabel = new Label(comment.get("content").getAsString());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #212529;");

        card.getChildren().addAll(header, contentLabel);
        return card;
    }

    @FXML
    private void handleAddComment() {
        String content = commentContentArea.getText().trim();

        if (content.isEmpty()) {
            showAlert("Validation Error", "Please enter a comment", Alert.AlertType.ERROR);
            return;
        }

        if (Dataholder.userId == null) {
            showAlert("Error", "User not logged in", Alert.AlertType.ERROR);
            return;
        }

        addCommentButton.setDisable(true);
        Long postId = currentPost.get("id").getAsLong();

        new Thread(() -> {
            try {
                System.out.println("💬 [PostComments] Adding comment to post: " + postId);
                CommunityForumService.CommentResult result = forumService.addComment(
                    postId, content, Dataholder.userId, commentAnonymousCheckBox.isSelected()
                );

                Platform.runLater(() -> {
                    commentContentArea.clear();
                    commentAnonymousCheckBox.setSelected(false);
                    addCommentButton.setDisable(false);

                    // Update the current post's comment count if backend provided it
                    if (result.updatedCommentCount >= 0) {
                        currentPost.addProperty("commentCount", result.updatedCommentCount);
                        System.out.println("📊 [PostComments] Updated post comment count to: " + result.updatedCommentCount);

                        // Notify parent controller to refresh the post list
                        if (onCommentCountChanged != null) {
                            onCommentCountChanged.run();
                        }
                    }

                    showAlert("Success", "Comment added successfully!", Alert.AlertType.INFORMATION);
                    loadComments();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addCommentButton.setDisable(false);
                    showAlert("Error", "Failed to add comment: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void handleDeleteComment(Long commentId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Comment");
        confirm.setContentText("Are you sure you want to delete this comment?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        System.out.println("🗑️ [PostComments] Deleting comment: " + commentId);


                        CommunityForumService.DeleteResult result = forumService.deleteComment(commentId, Dataholder.userId);

                        Platform.runLater(() -> {

                            if (result.updatedCommentCount >= 0 && result.postId != null) {
                                currentPost.addProperty("commentCount", result.updatedCommentCount);
                                System.out.println("📊 [PostComments] Updated post " + result.postId +
                                                 " comment count to: " + result.updatedCommentCount);

                                // Notify parent controller to refresh the post list
                                if (onCommentCountChanged != null) {
                                    onCommentCountChanged.run();
                                }
                            }

                            showAlert("Success", "Comment deleted successfully", Alert.AlertType.INFORMATION);
                            loadComments();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() ->
                            showAlert("Error", "Failed to delete comment: " + e.getMessage(), Alert.AlertType.ERROR)
                        );
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private String formatDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "Just now";

        try {
            LocalDateTime dt = LocalDateTime.parse(dateTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
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
}

