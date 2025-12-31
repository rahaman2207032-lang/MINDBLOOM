package com.example.mentalhealthdesktop.model;

import java.time.LocalDateTime;

public class SessionRequest {
    private Long id;
    private Long userId;
    private Long clientId; // Alias for userId for compatibility
    private String clientName; // Client's name for display
    private Long instructorId;
    private LocalDateTime requestedDateTime;
    private LocalDateTime requestedDate; // Alias for requestedDateTime for compatibility
    private String sessionType;
    private String reason;
    private String status; // PENDING, CONFIRMED, REJECTED, COMPLETED
    private String zoomLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public SessionRequest() {
    }

    public SessionRequest(Long userId, LocalDateTime requestedDateTime, String sessionType, String reason) {
        this.userId = userId;
        this.requestedDateTime = requestedDateTime;
        this.sessionType = sessionType;
        this.reason = reason;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getClientId() {
        return clientId != null ? clientId : userId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
        if (this.userId == null) {
            this.userId = clientId;
        }
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public LocalDateTime getRequestedDateTime() {
        return requestedDateTime;
    }

    public void setRequestedDateTime(LocalDateTime requestedDateTime) {
        this.requestedDateTime = requestedDateTime;
        this.requestedDate = requestedDateTime;
    }

    public LocalDateTime getRequestedDate() {
        return requestedDate != null ? requestedDate : requestedDateTime;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
        if (this.requestedDateTime == null) {
            this.requestedDateTime = requestedDate;
        }
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "SessionRequest{" +
                "id=" + id +
                ", userId=" + userId +
                ", instructorId=" + instructorId +
                ", requestedDateTime=" + requestedDateTime +
                ", sessionType='" + sessionType + '\'' +
                ", status='" + status + '\'' +
                ", zoomLink='" + zoomLink + '\'' +
                '}';
    }
}

