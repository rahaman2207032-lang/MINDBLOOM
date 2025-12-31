package com.mentalhealth.backend.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "mood_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate = LocalDate.now();

    @Column(name = "mood_rating", nullable = false)
    private Integer moodRating;

    @Column(name = "mood_emoji")
    private String moodEmoji;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "activities", columnDefinition = "TEXT")
    private String activities;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        if (logDate == null) {
            logDate = LocalDate.now();
        }
        setMoodEmojiFromRating();
    }

    public void setMoodEmojiFromRating() {
        switch (moodRating) {
            case 1: this.moodEmoji = "😢"; break;
            case 2: this.moodEmoji = "😟"; break;
            case 3: this.moodEmoji = "😐"; break;
            case 4: this.moodEmoji = "🙂"; break;
            case 5: this.moodEmoji = "😊"; break;
            default: this.moodEmoji = "😐";
        }
    }
}