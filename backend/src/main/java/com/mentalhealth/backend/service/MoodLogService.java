package com.mentalhealth.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


import com.mentalhealth.backend.model.MoodLog;
import com.mentalhealth.backend.repository.MoodLogRepository;


@Service
@Transactional
public class MoodLogService {

    @Autowired
    private MoodLogRepository moodLogRepository;

    public MoodLog saveMoodLog(MoodLog moodLog) {
        moodLog.setMoodEmojiFromRating();
        return moodLogRepository.save(moodLog);
    }

    public List<MoodLog> getMoodLogsByUser(Long userId) {
        return moodLogRepository.findByUserIdOrderByLogDateDesc(userId);
    }

    public List<MoodLog> getMoodLogsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return moodLogRepository.findByUserIdAndLogDateBetween(userId, startDate, endDate);
    }

    public Optional<MoodLog> getLatestMoodLog(Long userId) {
        return moodLogRepository.findFirstByUserIdOrderByLogDateDesc(userId);
    }

    public Optional<MoodLog> getMoodLogForDate(Long userId, LocalDate date) {
        return moodLogRepository.findByUserIdAndLogDate(userId, date);
    }

    public Double getAverageMoodRating(Long userId, LocalDate startDate, LocalDate endDate) {
        Double avg = moodLogRepository.getAverageMoodRating(userId, startDate, endDate);
        return avg != null ? avg : 0.0;
    }

    public Long getTotalMoodLogs(Long userId) {
        return moodLogRepository.countByUserId(userId);
    }

    public String determineMoodTrend(Long userId) {
        List<MoodLog> logs = moodLogRepository.findByUserIdOrderByLogDateDesc(userId);

        if (logs.size() < 2) return "STABLE";

        // Compare recent average with older average
        int midPoint = logs.size() / 2;
        double recentAvg = logs.subList(0, midPoint).stream()
                .mapToInt(MoodLog::getMoodRating)
                .average()
                .orElse(0);

        double olderAvg = logs.subList(midPoint, logs.size()).stream()
                .mapToInt(MoodLog::getMoodRating)
                .average()
                .orElse(0);

        if (recentAvg > olderAvg + 0.5) return "IMPROVING";
        if (recentAvg < olderAvg - 0.5) return "DECLINING";
        return "STABLE";
    }

    public int calculateConsecutiveLoggingDays(Long userId) {
        List<MoodLog> logs = moodLogRepository.findByUserIdOrderByLogDateDesc(userId);

        if (logs.isEmpty()) return 0;

        int streak = 0;
        LocalDate expectedDate = LocalDate.now();

        for (MoodLog log : logs) {
            if (log.getLogDate().equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }
}
