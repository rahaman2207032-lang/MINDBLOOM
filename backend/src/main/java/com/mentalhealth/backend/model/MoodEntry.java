package com.mentalhealth.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mood_entries")
@Data
public class MoodEntry {
    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate date;
    private String mood; // HAPPY, SAD, ANXIOUS, CALM, etc.
    private Integer moodLevel; // 1-10
    private String notes;
    private LocalDateTime createdAt;
}