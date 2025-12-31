package com.example.mentalhealthdesktop.model;

import java.time.LocalDate;

public class StressAssessment {
    private Long id;
    private Long userId;
    private LocalDate assessmentDate;

    // Questionnaire answers (scale 1-5)
    private int workloadLevel;
    private int sleepQualityLevel;
    private int anxietyLevel;
    private int moodLevel;
    private int physicalSymptomsLevel;
    private int concentrationLevel;
    private int socialConnectionLevel;

    // Calculated stress score (7-35 range)
    private int stressScore;

    // Stress level category
    private String stressLevel; // LOW, MODERATE, HIGH

    // Optional notes
    private String notes;

    // Constructors
    public StressAssessment() {
    }

    public StressAssessment(Long userId, int workloadLevel, int sleepQualityLevel,
                          int anxietyLevel, int moodLevel, int physicalSymptomsLevel,
                          int concentrationLevel, int socialConnectionLevel) {
        this.userId = userId;
        this.assessmentDate = LocalDate.now();
        this.workloadLevel = workloadLevel;
        this.sleepQualityLevel = sleepQualityLevel;
        this.anxietyLevel = anxietyLevel;
        this.moodLevel = moodLevel;
        this.physicalSymptomsLevel = physicalSymptomsLevel;
        this.concentrationLevel = concentrationLevel;
        this.socialConnectionLevel = socialConnectionLevel;
        calculateStressScore();
    }

    // Calculate total stress score and determine level
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

    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public int getWorkloadLevel() {
        return workloadLevel;
    }

    public void setWorkloadLevel(int workloadLevel) {
        this.workloadLevel = workloadLevel;
    }

    public int getSleepQualityLevel() {
        return sleepQualityLevel;
    }

    public void setSleepQualityLevel(int sleepQualityLevel) {
        this.sleepQualityLevel = sleepQualityLevel;
    }

    public int getAnxietyLevel() {
        return anxietyLevel;
    }

    public void setAnxietyLevel(int anxietyLevel) {
        this.anxietyLevel = anxietyLevel;
    }

    public int getMoodLevel() {
        return moodLevel;
    }

    public void setMoodLevel(int moodLevel) {
        this.moodLevel = moodLevel;
    }

    public int getPhysicalSymptomsLevel() {
        return physicalSymptomsLevel;
    }

    public void setPhysicalSymptomsLevel(int physicalSymptomsLevel) {
        this.physicalSymptomsLevel = physicalSymptomsLevel;
    }

    public int getConcentrationLevel() {
        return concentrationLevel;
    }

    public void setConcentrationLevel(int concentrationLevel) {
        this.concentrationLevel = concentrationLevel;
    }

    public int getSocialConnectionLevel() {
        return socialConnectionLevel;
    }

    public void setSocialConnectionLevel(int socialConnectionLevel) {
        this.socialConnectionLevel = socialConnectionLevel;
    }

    public int getStressScore() {
        return stressScore;
    }

    public void setStressScore(int stressScore) {
        this.stressScore = stressScore;
    }

    public String getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(String stressLevel) {
        this.stressLevel = stressLevel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

