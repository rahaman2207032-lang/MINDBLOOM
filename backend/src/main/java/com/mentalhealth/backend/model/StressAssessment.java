package com.mentalhealth.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "stress_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StressAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate = LocalDate.now();

    @Column(name = "workload_level", nullable = false)
    private Integer workloadLevel;

    @Column(name = "sleep_quality_level", nullable = false)
    private Integer sleepQualityLevel;

    @Column(name = "anxiety_level", nullable = false)
    private Integer anxietyLevel;

    @Column(name = "mood_level", nullable = false)
    private Integer moodLevel;

    @Column(name = "physical_symptoms_level", nullable = false)
    private Integer physicalSymptomsLevel;

    @Column(name = "concentration_level", nullable = false)
    private Integer concentrationLevel;

    @Column(name = "social_connection_level", nullable = false)
    private Integer socialConnectionLevel;

    @Column(name = "stress_score", nullable = false)
    private Integer stressScore;

    @Column(name = "stress_level", nullable = false)
    private String stressLevel;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        if (assessmentDate == null) {
            assessmentDate = LocalDate.now();
        }
        calculateStressScore();
    }

    public void calculateStressScore() {
        this.stressScore = workloadLevel + sleepQualityLevel + anxietyLevel +
                moodLevel + physicalSymptomsLevel + concentrationLevel +
                socialConnectionLevel;

        if (stressScore <= 14) {
            this.stressLevel = "LOW";
        } else if (stressScore <= 24) {
            this.stressLevel = "MODERATE";
        } else {
            this.stressLevel = "HIGH";
        }
    }
}