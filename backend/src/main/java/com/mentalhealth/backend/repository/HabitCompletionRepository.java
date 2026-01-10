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


    List<HabitCompletion> findByHabitIdOrderByCompletionDateDesc(Long habitId);


    Optional<HabitCompletion> findByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);


    boolean existsByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);


    @Query("SELECT hc FROM HabitCompletion hc WHERE hc.habitId = :habitId " +
            "AND hc.completionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY hc.completionDate DESC")
    List<HabitCompletion> findByHabitIdAndDateRange(
            @Param("habitId") Long habitId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    Optional<HabitCompletion> findFirstByHabitIdOrderByCompletionDateDesc(Long habitId);


    long countByHabitId(Long habitId);

    void deleteByHabitId(Long habitId);

    @Query("SELECT COUNT(hc) FROM HabitCompletion hc WHERE hc.userId = :userId " +
            "AND hc.completionDate BETWEEN :startDate AND :endDate")
    Long countCompletedHabits(@Param("userId") Long userId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);


    List<HabitCompletion> findByUserIdAndCompletionDateAfter(Long userId, LocalDate completionDate);

    List<HabitCompletion> findByHabitIdAndCompletionDateAfter(Long habitId, LocalDate completionDate);
}

