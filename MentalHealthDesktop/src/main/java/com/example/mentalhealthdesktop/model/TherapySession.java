package com.example.mentalhealthdesktop.model;

import java.time.LocalDateTime;

public class TherapySession {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long instructorId;
    private LocalDateTime sessionDate;
    private String sessionType;
    private String zoomLink;
    private String status; // scheduled, completed, cancelled
    private LocalDateTime createdAt;

    public TherapySession() {
    }

    public TherapySession(Long id, Long clientId, String clientName, Long instructorId,
                         LocalDateTime sessionDate, String sessionType, String zoomLink,
                         String status, LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.instructorId = instructorId;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.zoomLink = zoomLink;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
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

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

