package com.mentalhealth.backend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sleep_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SleepEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sleep_start_time", nullable = false)
    private LocalDateTime sleepStartTime;

    @Column(name = "sleep_end_time", nullable = false)
    private LocalDateTime sleepEndTime;

    @Column(name = "sleep_quality")
    private Integer sleepQuality; // 1-5 rating

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Calculated field - not stored in database
    @Transient
    public double getSleepDurationHours() {
        if (sleepStartTime != null && sleepEndTime != null) {
            long minutes = java.time.Duration.between(sleepStartTime, sleepEndTime).toMinutes();
            return minutes / 60.0;
        }
        return 0.0;
    }
}
