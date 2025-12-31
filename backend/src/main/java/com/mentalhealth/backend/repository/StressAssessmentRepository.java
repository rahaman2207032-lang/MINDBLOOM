package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.StressAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StressAssessmentRepository extends JpaRepository<StressAssessment, Long> {

    List<StressAssessment> findByUserIdOrderByAssessmentDateDesc(Long userId);

    List<StressAssessment> findByUserIdAndAssessmentDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate
    );

    Optional<StressAssessment> findFirstByUserIdOrderByAssessmentDateDesc(Long userId);

    @Query("SELECT AVG(s.stressScore) FROM StressAssessment s WHERE s.userId = :userId " +
            "AND s.assessmentDate BETWEEN :startDate AND :endDate")
    Double getAverageStressScore(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    // New methods for instructor dashboard
    List<StressAssessment> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime createdAt);

    List<StressAssessment> findTop1ByUserIdOrderByCreatedAtDesc(Long userId);
}