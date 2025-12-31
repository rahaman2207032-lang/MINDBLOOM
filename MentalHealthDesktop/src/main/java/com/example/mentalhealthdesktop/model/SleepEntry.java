package com.example.mentalhealthdesktop.model;

import java.time.LocalDateTime;
import java.time.Duration;

public class SleepEntry {
    private Long id;
    private Long userId;
    private LocalDateTime sleepStartTime;
    private LocalDateTime sleepEndTime;
    private Integer sleepQuality; // 1-5 rating
    private String notes;
    private LocalDateTime createdAt;

    public SleepEntry() {
    }

    public SleepEntry(Long id, Long userId, LocalDateTime sleepStartTime, LocalDateTime sleepEndTime,
                      Integer sleepQuality, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
        this.sleepQuality = sleepQuality;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Calculate sleep duration in hours
    public double getSleepDurationHours() {
        if (sleepStartTime != null && sleepEndTime != null) {
            Duration duration = Duration.between(sleepStartTime, sleepEndTime);
            return duration.toMinutes() / 60.0;
        }
        return 0.0;
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

    public LocalDateTime getSleepStartTime() {
        return sleepStartTime;
    }

    public void setSleepStartTime(LocalDateTime sleepStartTime) {
        this.sleepStartTime = sleepStartTime;
    }

    public LocalDateTime getSleepEndTime() {
        return sleepEndTime;
    }

    public void setSleepEndTime(LocalDateTime sleepEndTime) {
        this.sleepEndTime = sleepEndTime;
    }

    public Integer getSleepQuality() {
        return sleepQuality;
    }

    public void setSleepQuality(Integer sleepQuality) {
        this.sleepQuality = sleepQuality;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SleepEntry{" +
                "id=" + id +
                ", userId=" + userId +
                ", sleepStartTime=" + sleepStartTime +
                ", sleepEndTime=" + sleepEndTime +
                ", sleepQuality=" + sleepQuality +
                ", duration=" + String.format("%.1f", getSleepDurationHours()) + " hours" +
                '}';
    }
}

