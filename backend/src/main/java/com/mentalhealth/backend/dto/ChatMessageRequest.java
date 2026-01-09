package com.mentalhealth.backend.dto;

public class ChatMessageRequest {
    private String message;
    private Long userId;
    private boolean anonymous;

    // Constructors
    public ChatMessageRequest() {}

    public ChatMessageRequest(String message, Long userId, boolean anonymous) {
        this.message = message;
        this.userId = userId;
        this.anonymous = anonymous;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
}

