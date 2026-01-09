package com.mentalhealth.backend.dto;

import com.mentalhealth.backend.model.ChatMessage;
import java.time.LocalDateTime;

public class ChatMessageResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String message;
    private boolean anonymous;
    private LocalDateTime createdAt;

    // Default constructor
    public ChatMessageResponse() {}

    // Constructor from ChatMessage entity
    public ChatMessageResponse(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.userId = chatMessage.getUser().getId();
        this.userName = chatMessage.isAnonymous() ? "Anonymous" : chatMessage.getUser().getUsername();
        this.message = chatMessage.getMessage();
        this.anonymous = chatMessage.isAnonymous();
        this.createdAt = chatMessage.getCreatedAt();
    }

    // Full constructor
    public ChatMessageResponse(Long id, Long userId, String userName, String message,
                              boolean anonymous, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.message = message;
        this.anonymous = anonymous;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

