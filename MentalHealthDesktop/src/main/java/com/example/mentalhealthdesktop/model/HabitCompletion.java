package com.example.mentalhealthdesktop.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HabitCompletion {
    private Long id;
    private Long habitId;
    private Long userId;
    private LocalDate completionDate;
    private LocalDateTime completedAt;
    private String notes;

    public HabitCompletion() {
    }

    public HabitCompletion(Long id, Long habitId, Long userId, LocalDate completionDate,
                          LocalDateTime completedAt, String notes) {
        this.id = id;
        this.habitId = habitId;
        this.userId = userId;
        this.completionDate = completionDate;
        this.completedAt = completedAt;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHabitId() {
        return habitId;
    }

    public void setHabitId(Long habitId) {
        this.habitId = habitId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "HabitCompletion{" +
                "id=" + id +
                ", habitId=" + habitId +
                ", completionDate=" + completionDate +
                '}';
    }
}

