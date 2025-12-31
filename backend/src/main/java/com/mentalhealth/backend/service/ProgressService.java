package com.mentalhealth.backend.service;

import com.mentalhealth.backend.dto.ProgressData;
import com.mentalhealth.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProgressService {

    @Autowired
    private MoodLogService moodLogService;

    @Autowired
    private StressAssessmentService stressAssessmentService;

    @Autowired
    private SleepRepository sleepEntryRepository;

    @Autowired
    private HabitCompletionRepository habitCompletionRepository;

    @Autowired
    private HabitRepository habitRepository;

    public ProgressData getProgressData(Long userId, LocalDate startDate, LocalDate endDate) {
        ProgressData progressData = new ProgressData();
        progressData.setUserId(userId);
        progressData.setStartDate(startDate);
        progressData.setEndDate(endDate);

        // Mood metrics
        Double avgMood = moodLogService.getAverageMoodRating(userId, startDate, endDate);
        progressData.setAverageMoodRating(avgMood != null ? avgMood : 0.0);
        progressData.setMoodTrend(moodLogService.determineMoodTrend(userId));
        progressData.setConsecutiveDaysOfMoodLogging(
                moodLogService.calculateConsecutiveLoggingDays(userId)
        );

        // Stress metrics
        Double avgStress = stressAssessmentService.getAverageStressScore(userId, startDate, endDate);
        progressData.setAverageStressScore(avgStress != null ? avgStress : 0.0);
        progressData.setStressTrend(stressAssessmentService.determineStressTrend(userId));

        // Sleep metrics
        Double avgSleep = sleepEntryRepository.getAverageSleepHours(userId, startDate, endDate);
        progressData.setAverageSleepHours(avgSleep != null ? avgSleep : 0.0);

        // Habit metrics
        Long completedHabits = habitCompletionRepository.countCompletedHabits(userId, startDate, endDate);
        progressData.setTotalHabitsCompleted(completedHabits != null ? completedHabits.intValue() : 0);

        // Calculate habit completion rate
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long activeHabits = habitRepository.findByUserIdAndIsActiveTrue(userId).size();
        double totalPossible = totalDays * activeHabits;
        double completionRate = totalPossible > 0 ? (completedHabits / totalPossible) * 100 : 0;
        progressData.setHabitCompletionRate(completionRate);

        // Generate milestones
        progressData.setAchievedMilestones(generateMilestones(progressData));

        // Generate correlations
        progressData.setSleepMoodCorrelation(generateSleepMoodCorrelation(progressData));

        return progressData;
    }

    private List<String> generateMilestones(ProgressData data) {
        List<String> milestones = new ArrayList<>();

        if (data.getConsecutiveDaysOfMoodLogging() >= 7) {
            milestones.add("7-day mood logging streak!");
        }
        if (data.getConsecutiveDaysOfMoodLogging() >= 30) {
            milestones.add("30-day mood logging streak!");
        }
        if (data.getAverageMoodRating() >= 4.0) {
            milestones.add("Maintaining positive mood!");
        }
        if (data.getAverageStressScore() <= 14.0) {
            milestones.add("Successfully managing stress!");
        }
        if (data.getHabitCompletionRate() >= 80.0) {
            milestones.add("80%+ habit completion rate!");
        }
        if (data.getAverageSleepHours() >= 7.0) {
            milestones.add("Getting adequate sleep!");
        }

        return milestones;
    }

    private String generateSleepMoodCorrelation(ProgressData data) {
        StringBuilder correlation = new StringBuilder();

        if (data.getAverageSleepHours() >= 7.0 && data.getAverageMoodRating() >= 3.5) {
            correlation.append("Good sleep appears to correlate with better mood! ");
        } else if (data.getAverageSleepHours() < 6.0 && data.getAverageMoodRating() < 3.0) {
            correlation.append("Consider improving sleep to boost mood. ");
        }

        if (data.getAverageStressScore() <= 14.0 && data.getAverageMoodRating() >= 4.0) {
            correlation.append("Low stress levels are associated with positive mood!");
        }

        return correlation.toString().isEmpty() ?
                "Keep tracking to discover patterns!" : correlation.toString();
    }
}