package com.mentalhealth.backend.service;

import com.mentalhealth.backend.model.*;
import com.mentalhealth.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class ClientProgressService {

    @Autowired
    private MoodLogRepository moodLogRepository;

    @Autowired
    private StressAssessmentRepository stressAssessmentRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitCompletionRepository habitCompletionRepository;

    @Autowired
    private SleepRepository sleepRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public Map<String, Object> getClientSummary(Long clientId, Long instructorId) {

        Map<String, Object> summary = new HashMap<>();

        try {

            Double avgMood = calculateAverageMood(clientId, 30);
            summary.put("averageMood", avgMood != null ? Math.round(avgMood * 10.0) / 10.0 : null);


            String stressLevel = getCurrentStressLevel(clientId);
            summary.put("currentStressLevel", stressLevel);


            Double habitRate = calculateHabitCompletionRate(clientId);
            summary.put("habitCompletionRate", habitRate != null ? Math.round(habitRate * 10.0) / 10.0 : null);


            Double avgSleep = calculateAverageSleep(clientId, 30);
            summary.put("averageSleepHours", avgSleep != null ? Math.round(avgSleep * 10.0) / 10.0 : null);

        } catch (Exception e) {
            e.printStackTrace();
            // Return null values on error
            summary.put("averageMood", null);
            summary.put("currentStressLevel", "N/A");
            summary.put("habitCompletionRate", null);
            summary.put("averageSleepHours", null);
        }

        return summary;
    }


    public List<Map<String, Object>> getMoodLogs(Long clientId, Long instructorId, Integer days) {

        List<Map<String, Object>> moodLogs = new ArrayList<>();

        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days != null ? days : 30);
            List<MoodLog> logs = moodLogRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(clientId, startDate);

            for (MoodLog log : logs) {
                Map<String, Object> moodData = new HashMap<>();
                moodData.put("date", log.getCreatedAt().format(DATE_FORMATTER));
                moodData.put("moodScore", log.getMoodRating());
                moodData.put("notes", log.getNotes());
                moodData.put("timestamp", log.getCreatedAt().format(DATETIME_FORMATTER));
                moodLogs.add(moodData);
            }
        } catch (Exception e) {
            // Log error silently
        }

        return moodLogs;
    }

    public List<Map<String, Object>> getStressAssessments(Long clientId, Long instructorId, Integer days) {

        List<Map<String, Object>> assessments = new ArrayList<>();

        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days != null ? days : 30);
            List<StressAssessment> stressData = stressAssessmentRepository
                    .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(clientId, startDate);

            for (StressAssessment assessment : stressData) {
                Map<String, Object> data = new HashMap<>();
                data.put("assessmentDate", assessment.getCreatedAt().format(DATE_FORMATTER));
                data.put("stressLevel", assessment.getStressLevel());
                data.put("stressScore", assessment.getStressScore());
                data.put("timestamp", assessment.getCreatedAt().format(DATETIME_FORMATTER));
                assessments.add(data);
            }
        } catch (Exception e) {
            // Log error silently
        }

        return assessments;
    }


    public List<Map<String, Object>> getHabits(Long clientId, Long instructorId) {
        List<Map<String, Object>> habitsList = new ArrayList<>();

        try {
            List<Habit> habits = habitRepository.findByUserId(clientId);

            for (Habit habit : habits) {
                Map<String, Object> data = new HashMap<>();
                data.put("habitName", habit.getName());
                data.put("currentStreak", habit.getCurrentStreak());

                // Calculate completion rate
                Double completionRate = calculateHabitCompletionRateForHabit(habit.getId());
                data.put("completionRate", completionRate != null ? Math.round(completionRate * 10.0) / 10.0 : 0.0);

                data.put("frequency", habit.getFrequency());
                habitsList.add(data);
            }
        } catch (Exception e) {
            // Log error silently
        }

        return habitsList;
    }


    public List<Map<String, Object>> getSleepData(Long clientId, Long instructorId, Integer days) {

        List<Map<String, Object>> sleepData = new ArrayList<>();

        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days != null ? days : 30);
            List<SleepEntry> entries = sleepRepository.findByUserIdAndSleepStartTimeAfterOrderBySleepStartTimeDesc(clientId, startDate);

            for (SleepEntry entry : entries) {
                Map<String, Object> data = new HashMap<>();
                data.put("sleepDate", entry.getSleepStartTime().format(DATE_FORMATTER));

                // Calculate duration in hours
                if (entry.getSleepEndTime() != null) {
                    long minutes = java.time.Duration.between(entry.getSleepStartTime(), entry.getSleepEndTime()).toMinutes();
                    double hours = minutes / 60.0;
                    data.put("durationHours", Math.round(hours * 10.0) / 10.0);
                } else {
                    data.put("durationHours", null);
                }

                data.put("quality", entry.getSleepQuality());
                data.put("notes", entry.getNotes());
                sleepData.add(data);
            }
        } catch (Exception e) {
            // Log error silently
        }

        return sleepData;
    }


    private Double calculateAverageMood(Long clientId, int days) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            List<MoodLog> logs = moodLogRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(clientId, startDate);

            if (logs.isEmpty()) {
                return null;
            }

            return logs.stream()
                    .mapToDouble(MoodLog::getMoodRating)
                    .average()
                    .orElse(0.0);
        } catch (Exception e) {
            return null;
        }
    }


    private String getCurrentStressLevel(Long clientId) {
        try {
            List<StressAssessment> assessments = stressAssessmentRepository
                    .findTop1ByUserIdOrderByCreatedAtDesc(clientId);

            if (assessments.isEmpty()) {
                return "N/A";
            }

            return assessments.get(0).getStressLevel();
        } catch (Exception e) {
            return "N/A";
        }
    }


    private Double calculateHabitCompletionRate(Long clientId) {
        try {
            List<Habit> habits = habitRepository.findByUserId(clientId);

            if (habits.isEmpty()) {
                return null;
            }


            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            List<HabitCompletion> completions = habitCompletionRepository
                    .findByUserIdAndCompletionDateAfter(clientId, thirtyDaysAgo);


            int expectedCompletions = habits.size() * 30;
            int actualCompletions = completions.size();

            if (expectedCompletions == 0) {
                return null;
            }

            return (actualCompletions * 100.0) / expectedCompletions;
        } catch (Exception e) {
            return null;
        }
    }


    private Double calculateHabitCompletionRateForHabit(Long habitId) {
        try {
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            List<HabitCompletion> completions = habitCompletionRepository
                    .findByHabitIdAndCompletionDateAfter(habitId, thirtyDaysAgo);


            int actualCompletions = completions.size();
            return (actualCompletions * 100.0) / 30.0;
        } catch (Exception e) {
            return 0.0;
        }
    }


    private Double calculateAverageSleep(Long clientId, int days) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            List<SleepEntry> entries = sleepRepository.findByUserIdAndSleepStartTimeAfterOrderBySleepStartTimeDesc(clientId, startDate);

            if (entries.isEmpty()) {
                return null;
            }

            return entries.stream()
                    .filter(e -> e.getSleepEndTime() != null)
                    .mapToDouble(e -> {
                        long minutes = java.time.Duration.between(e.getSleepStartTime(), e.getSleepEndTime()).toMinutes();
                        return minutes / 60.0;
                    })
                    .average()
                    .orElse(0.0);
        } catch (Exception e) {
            return null;
        }
    }
}

