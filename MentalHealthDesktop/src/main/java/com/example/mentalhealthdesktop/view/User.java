package com.example.mentalhealthdesktop.view;

public class User {
    private Long id;      // match the type from backend
    private String username;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
