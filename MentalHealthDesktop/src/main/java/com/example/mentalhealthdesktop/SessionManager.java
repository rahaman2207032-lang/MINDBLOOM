package com.example.mentalhealthdesktop;


public class SessionManager {
    private static SessionManager instance;
    private Long currentUserId;
    private String currentUsername;
    private String currentUserRole;

    private SessionManager() {
        // Private constructor for singleton
    }


    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }


    public void setCurrentUser(Long userId, String username, String role) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentUserRole = role;
        System.out.println(" [SessionManager] Session set for user: " + username + " (ID: " + userId + ", Role: " + role + ")");
    }


    public Long getCurrentUserId() {
        return currentUserId;
    }


    public String getCurrentUsername() {
        return currentUsername;
    }


    public String getCurrentUserRole() {
        return currentUserRole;
    }


    public boolean isLoggedIn() {
        return currentUserId != null;
    }


    public boolean isInstructor() {
        return "INSTRUCTOR".equalsIgnoreCase(currentUserRole);
    }

    public boolean isUser() {
        return "USER".equalsIgnoreCase(currentUserRole);
    }


    public void clearSession() {
        System.out.println(" [SessionManager] Session cleared for user: " + currentUsername);
        this.currentUserId = null;
        this.currentUsername = null;
        this.currentUserRole = null;
    }


    @Override
    public String toString() {
        if (isLoggedIn()) {
            return "User{id=" + currentUserId + ", username=" + currentUsername + ", role=" + currentUserRole + "}";
        }
        return "User{not logged in}";
    }
}

