package com.example.mentalhealthdesktop.model;

import java.time.LocalDateTime;

public class Habit {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String frequency; // "DAILY" or "WEEKLY"
    private String targetDays; // For weekly: "MON,WED,FRI" or for daily: "ALL"
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDateTime createdAt;
    private LocalDateTime lastCompletedAt;
    private Boolean isActive;

    public Habit() {
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.isActive = true;
    }

    public Habit(Long id, Long userId, String name, String description, String frequency,
                 String targetDays, Integer currentStreak, Integer longestStreak,
                 LocalDateTime createdAt, LocalDateTime lastCompletedAt, Boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.targetDays = targetDays;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.createdAt = createdAt;
        this.lastCompletedAt = lastCompletedAt;
        this.isActive = isActive;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getTargetDays() {
        return targetDays;
    }

    public void setTargetDays(String targetDays) {
        this.targetDays = targetDays;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastCompletedAt() {
        return lastCompletedAt;
    }

    public void setLastCompletedAt(LocalDateTime lastCompletedAt) {
        this.lastCompletedAt = lastCompletedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "Habit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", frequency='" + frequency + '\'' +
                ", currentStreak=" + currentStreak +
                ", longestStreak=" + longestStreak +
                '}';
    }
}

