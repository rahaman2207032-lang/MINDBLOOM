package com.mentalhealth.backend.dto;

public class ForumPostRequest {
    private String title;
    private String content;
    private Long userId;
    private boolean anonymous;

    // Constructors
    public ForumPostRequest() {}

    public ForumPostRequest(String title, String content, Long userId, boolean anonymous) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.anonymous = anonymous;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
}

