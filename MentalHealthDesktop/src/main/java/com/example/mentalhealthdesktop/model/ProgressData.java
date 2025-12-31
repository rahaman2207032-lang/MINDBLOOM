package com.example.mentalhealthdesktop.model;

import java.time.LocalDate;
import java.util.List;

/**
 * This class represents aggregated progress data for a user
 * combining metrics from different features
 */
public class ProgressData {
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;

    // Aggregated metrics
    private double averageMoodRating;
    private double averageStressScore;
    private int totalHabitsCompleted;
    private double habitCompletionRate;
    private double averageSleepHours;

    // Trend indicators
    private String moodTrend; // IMPROVING, STABLE, DECLINING
    private String stressTrend; // IMPROVING, STABLE, WORSENING

    // Milestone tracking
    private int consecutiveDaysOfMoodLogging;
    private int consecutiveDaysOfStressManagement;
    private List<String> achievedMilestones;

    // Correlations
    private String sleepMoodCorrelation; // e.g., "Better sleep correlates with improved mood"

    // Constructors
    public ProgressData() {
    }

    public ProgressData(Long userId) {
        this.userId = userId;
        this.startDate = LocalDate.now().minusDays(30); // Default to last 30 days
        this.endDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getAverageMoodRating() {
        return averageMoodRating;
    }

    public void setAverageMoodRating(double averageMoodRating) {
        this.averageMoodRating = averageMoodRating;
    }

    public double getAverageStressScore() {
        return averageStressScore;
    }

    public void setAverageStressScore(double averageStressScore) {
        this.averageStressScore = averageStressScore;
    }

    public int getTotalHabitsCompleted() {
        return totalHabitsCompleted;
    }

    public void setTotalHabitsCompleted(int totalHabitsCompleted) {
        this.totalHabitsCompleted = totalHabitsCompleted;
    }

    public double getHabitCompletionRate() {
        return habitCompletionRate;
    }

    public void setHabitCompletionRate(double habitCompletionRate) {
        this.habitCompletionRate = habitCompletionRate;
    }

    public double getAverageSleepHours() {
        return averageSleepHours;
    }

    public void setAverageSleepHours(double averageSleepHours) {
        this.averageSleepHours = averageSleepHours;
    }

    public String getMoodTrend() {
        return moodTrend;
    }

    public void setMoodTrend(String moodTrend) {
        this.moodTrend = moodTrend;
    }

    public String getStressTrend() {
        return stressTrend;
    }

    public void setStressTrend(String stressTrend) {
        this.stressTrend = stressTrend;
    }

    public int getConsecutiveDaysOfMoodLogging() {
        return consecutiveDaysOfMoodLogging;
    }

    public void setConsecutiveDaysOfMoodLogging(int consecutiveDaysOfMoodLogging) {
        this.consecutiveDaysOfMoodLogging = consecutiveDaysOfMoodLogging;
    }

    public int getConsecutiveDaysOfStressManagement() {
        return consecutiveDaysOfStressManagement;
    }

    public void setConsecutiveDaysOfStressManagement(int consecutiveDaysOfStressManagement) {
        this.consecutiveDaysOfStressManagement = consecutiveDaysOfStressManagement;
    }

    public List<String> getAchievedMilestones() {
        return achievedMilestones;
    }

    public void setAchievedMilestones(List<String> achievedMilestones) {
        this.achievedMilestones = achievedMilestones;
    }

    public String getSleepMoodCorrelation() {
        return sleepMoodCorrelation;
    }

    public void setSleepMoodCorrelation(String sleepMoodCorrelation) {
        this.sleepMoodCorrelation = sleepMoodCorrelation;
    }
}

