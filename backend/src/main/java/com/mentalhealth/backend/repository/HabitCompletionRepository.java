package com.mentalhealth.backend.repository;



import com.mentalhealth.backend.model.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    /**
     * Find all completions for a specific habit, ordered by date descending
     */
    List<HabitCompletion> findByHabitIdOrderByCompletionDateDesc(Long habitId);

    /**
     * Find completion for a habit on a specific date
     */
    Optional<HabitCompletion> findByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);

    /**
     * Check if habit was completed on a specific date
     */
    boolean existsByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);

    /**
     * Find completions for a habit within a date range
     */
    @Query("SELECT hc FROM HabitCompletion hc WHERE hc.habitId = :habitId " +
            "AND hc.completionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY hc.completionDate DESC")
    List<HabitCompletion> findByHabitIdAndDateRange(
            @Param("habitId") Long habitId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Get the most recent completion for a habit
     */
    Optional<HabitCompletion> findFirstByHabitIdOrderByCompletionDateDesc(Long habitId);

    /**
     * Count total completions for a habit
     */
    long countByHabitId(Long habitId);

    /**
     * Delete all completions for a habit (when habit is deleted)
     */
    void deleteByHabitId(Long habitId);

    /**
     * Count completed habits for a user within a date range
     */
    @Query("SELECT COUNT(hc) FROM HabitCompletion hc WHERE hc.userId = :userId " +
            "AND hc.completionDate BETWEEN :startDate AND :endDate")
    Long countCompletedHabits(@Param("userId") Long userId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);

    /**
     * Find completions by user and after a date (for instructor dashboard)
     */
    List<HabitCompletion> findByUserIdAndCompletionDateAfter(Long userId, LocalDate completionDate);

    /**
     * Find completions by habit and after a date
     */
    List<HabitCompletion> findByHabitIdAndCompletionDateAfter(Long habitId, LocalDate completionDate);
}

