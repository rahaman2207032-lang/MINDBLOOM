package com.mentalhealth.backend.service;

import com.mentalhealth.backend.model.StressAssessment;
import com.mentalhealth.backend.repository.StressAssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StressAssessmentService {

    @Autowired
    private StressAssessmentRepository stressAssessmentRepository;

    public StressAssessment saveStressAssessment(StressAssessment assessment) {
        assessment.calculateStressScore();
        return stressAssessmentRepository.save(assessment);
    }

    public List<StressAssessment> getStressAssessmentsByUser(Long userId) {
        return stressAssessmentRepository.findByUserIdOrderByAssessmentDateDesc(userId);
    }

    public List<StressAssessment> getStressAssessmentsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return stressAssessmentRepository.findByUserIdAndAssessmentDateBetween(userId, startDate, endDate);
    }

    public Optional<StressAssessment> getLatestStressAssessment(Long userId) {
        return stressAssessmentRepository.findFirstByUserIdOrderByAssessmentDateDesc(userId);
    }

    public Double getAverageStressScore(Long userId, LocalDate startDate, LocalDate endDate) {
        Double avg = stressAssessmentRepository.getAverageStressScore(userId, startDate, endDate);
        return avg != null ? avg : 0.0;
    }

    public String determineStressTrend(Long userId) {
        List<StressAssessment> assessments = stressAssessmentRepository
                .findByUserIdOrderByAssessmentDateDesc(userId);

        if (assessments.size() < 2) return "STABLE";

        // Compare recent average with older average
        int midPoint = assessments.size() / 2;
        double recentAvg = assessments.subList(0, midPoint).stream()
                .mapToInt(StressAssessment::getStressScore)
                .average()
                .orElse(0);

        double olderAvg = assessments.subList(midPoint, assessments.size()).stream()
                .mapToInt(StressAssessment::getStressScore)
                .average()
                .orElse(0);

        if (recentAvg < olderAvg - 2) return "IMPROVING";
        if (recentAvg > olderAvg + 2) return "WORSENING";
        return "STABLE";
    }
}

// ============================================


