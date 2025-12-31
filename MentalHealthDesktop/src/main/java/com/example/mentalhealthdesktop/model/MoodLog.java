package com.example.mentalhealthdesktop.model;

import java.time.LocalDate;

public class MoodLog {
    private Long id;
    private Long userId;
    private LocalDate logDate;

    // Mood rating (1-5 scale: 1=Very Low, 2=Low, 3=Neutral, 4=Good, 5=Very Good)
    private int moodRating;

    // Emoji representation
    private String moodEmoji; // 😢, 😟, 😐, 🙂, 😊

    // Optional notes about the day
    private String notes;

    // Activities that influenced mood (optional)
    private String activities;

    // Constructors
    public MoodLog() {
    }

    public MoodLog(Long userId, int moodRating, String notes, String activities) {
        this.userId = userId;
        this.logDate = LocalDate.now();
        this.moodRating = moodRating;
        this.notes = notes;
        this.activities = activities;
        setMoodEmojiFromRating();
    }

    // Set emoji based on rating
    public void setMoodEmojiFromRating() {
        switch (moodRating) {
            case 1:
                this.moodEmoji = "😢";
                break;
            case 2:
                this.moodEmoji = "😟";
                break;
            case 3:
                this.moodEmoji = "😐";
                break;
            case 4:
                this.moodEmoji = "🙂";
                break;
            case 5:
                this.moodEmoji = "😊";
                break;
            default:
                this.moodEmoji = "😐";
        }
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

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public int getMoodRating() {
        return moodRating;
    }

    public void setMoodRating(int moodRating) {
        this.moodRating = moodRating;
        setMoodEmojiFromRating();
    }

    public String getMoodEmoji() {
        return moodEmoji;
    }

    public void setMoodEmoji(String moodEmoji) {
        this.moodEmoji = moodEmoji;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getActivities() {
        return activities;
    }

    public void setActivities(String activities) {
        this.activities = activities;
    }
}

