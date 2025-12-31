package com.mentalhealth.backend.repository;



import com.mentalhealth.backend.model.SleepEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SleepRepository extends JpaRepository<SleepEntry, Long> {

    /**
     * Find all sleep entries for a specific user, ordered by most recent first
     */
    List<SleepEntry> findByUserIdOrderBySleepStartTimeDesc(Long userId);

    /**
     * Find sleep entries for a user within a date range
     */
    @Query("SELECT s FROM SleepEntry s WHERE s.userId = :userId " +
            "AND s.sleepStartTime >= :startDate " +
            "ORDER BY s.sleepStartTime DESC")
    List<SleepEntry> findByUserIdAndSleepStartTimeAfter(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate
    );

    /**
     * Find sleep entries for the last N days
     */
    @Query("SELECT s FROM SleepEntry s WHERE s.userId = :userId " +
            "AND s.sleepStartTime >= :startDate " +
            "ORDER BY s.sleepStartTime ASC")
    List<SleepEntry> findWeeklySleepEntries(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate
    );

    /**
     * Count total sleep entries for a user
     */
    long countByUserId(Long userId);

    /**
     * Calculate average sleep hours for a user within a date range
     * Using native query because HQL doesn't support EXTRACT with duration calculations
     */
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (sleep_end_time - sleep_start_time))/3600.0) " +
            "FROM sleep_entries WHERE user_id = :userId " +
            "AND DATE(sleep_start_time) >= :startDate " +
            "AND DATE(sleep_end_time) <= :endDate",
            nativeQuery = true)
    Double getAverageSleepHours(@Param("userId") Long userId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /**
     * Find sleep entries for instructor dashboard
     */
    List<SleepEntry> findByUserIdAndSleepStartTimeAfterOrderBySleepStartTimeDesc(Long userId, LocalDateTime startDate);
}

