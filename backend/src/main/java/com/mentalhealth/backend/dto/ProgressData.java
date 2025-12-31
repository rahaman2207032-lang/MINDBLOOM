package com.mentalhealth.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressData {
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;

    private Double averageMoodRating;
    private Double averageStressScore;
    private Integer totalHabitsCompleted;
    private Double habitCompletionRate;
    private Double averageSleepHours;

    private String moodTrend;
    private String stressTrend;

    private Integer consecutiveDaysOfMoodLogging;
    private Integer consecutiveDaysOfStressManagement;
    private List<String> achievedMilestones;

    private String sleepMoodCorrelation;
}