package com.example.mentalhealthdesktop.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TherapyNote {
    private Long id;

    @SerializedName("instructorId")
    private Long instructorId;

    @SerializedName("clientId")
    private Long clientId;

    private String clientName;

    @SerializedName("sessionId")
    private Long sessionId;

    @SerializedName("sessionDate")
    private LocalDate sessionDate;

    @SerializedName("sessionType")
    private String sessionType;

    @SerializedName("notes")  // Backend expects "notes" field
    private String notes;

    @SerializedName("createdAt")
    private LocalDateTime createdAt;

    @SerializedName("updatedAt")
    private LocalDateTime updatedAt;

    public TherapyNote() {
    }

    public TherapyNote(Long id, Long instructorId, Long clientId, String clientName,
                      Long sessionId, LocalDate sessionDate, String sessionType,
                      String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.instructorId = instructorId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.sessionId = sessionId;
        this.sessionDate = sessionDate;
        this.sessionType = sessionType;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
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

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Convenience methods for noteText alias
    public String getNoteText() {
        return notes;
    }

    public void setNoteText(String noteText) {
        this.notes = noteText;
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
}

