package com.mentalhealth.backend.dto;

public class LoginResponse {
    private Long userId;
    private String username;
    private String role;
    private String message;
    public LoginResponse(Long userId, String username, String role, String message) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.message = message;
    }
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
