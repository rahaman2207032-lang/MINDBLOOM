package com.example.mentalhealthdesktop;

/**
 * Singleton class to manage user session across the application
 * Replaces hardcoded user IDs with dynamic session management
 */
public class SessionManager {
    private static SessionManager instance;
    private Long currentUserId;
    private String currentUsername;
    private String currentUserRole;

    private SessionManager() {
        // Private constructor for singleton
    }

    /**
     * Get singleton instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set current user session (call after successful login)
     */
    public void setCurrentUser(Long userId, String username, String role) {
        this.currentUserId = userId;
        this.currentUsername = username;
        this.currentUserRole = role;
        System.out.println("✅ [SessionManager] Session set for user: " + username + " (ID: " + userId + ", Role: " + role + ")");
    }

    /**
     * Get current logged-in user ID
     */
    public Long getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Get current logged-in username
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Get current user role (USER or INSTRUCTOR)
     */
    public String getCurrentUserRole() {
        return currentUserRole;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUserId != null;
    }

    /**
     * Check if current user is instructor
     */
    public boolean isInstructor() {
        return "INSTRUCTOR".equalsIgnoreCase(currentUserRole);
    }

    /**
     * Check if current user is regular user
     */
    public boolean isUser() {
        return "USER".equalsIgnoreCase(currentUserRole);
    }

    /**
     * Clear session (call on logout)
     */
    public void clearSession() {
        System.out.println("✅ [SessionManager] Session cleared for user: " + currentUsername);
        this.currentUserId = null;
        this.currentUsername = null;
        this.currentUserRole = null;
    }

    /**
     * Get user info as string for debugging
     */
    @Override
    public String toString() {
        if (isLoggedIn()) {
            return "User{id=" + currentUserId + ", username=" + currentUsername + ", role=" + currentUserRole + "}";
        }
        return "User{not logged in}";
    }
}

