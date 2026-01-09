package com.mentalhealth.backend.dto;

public class ForumCommentRequest {
    private String content;
    private boolean anonymous;

    // Constructors
    public ForumCommentRequest() {}

    public ForumCommentRequest(String content, boolean anonymous) {
        this.content = content;
        this.anonymous = anonymous;
    }

    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }
}

