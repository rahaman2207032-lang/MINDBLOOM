package com.mentalhealth.backend.repository;



import com.mentalhealth.backend.model.MoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoodLogRepository extends JpaRepository<MoodLog, Long> {

    List<MoodLog> findByUserIdOrderByLogDateDesc(Long userId);

    List<MoodLog> findByUserIdAndLogDateBetween(
            Long userId, LocalDate startDate, LocalDate endDate
    );

    Optional<MoodLog> findFirstByUserIdOrderByLogDateDesc(Long userId);

    Optional<MoodLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    @Query("SELECT AVG(m.moodRating) FROM MoodLog m WHERE m.userId = :userId " +
            "AND m.logDate BETWEEN :startDate AND :endDate")
    Double getAverageMoodRating(@Param("userId") Long userId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(m) FROM MoodLog m WHERE m.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    // New method for instructor dashboard
    List<MoodLog> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime createdAt);
}
