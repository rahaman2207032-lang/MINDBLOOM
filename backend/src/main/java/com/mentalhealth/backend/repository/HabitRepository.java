package com.mentalhealth.backend.repository;



import com.mentalhealth.backend.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {

    /**
     * Find all habits for a specific user
     */
    List<Habit> findByUserId(Long userId);

    /**
     * Find all active habits for a specific user
     */
    List<Habit> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find a specific habit by ID and user ID (for security)
     */
    Optional<Habit> findByIdAndUserId(Long id, Long userId);

    /**
     * Count total habits for a user
     */
    long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find habits with active streaks
     */
    List<Habit> findByUserIdAndCurrentStreakGreaterThan(Long userId, Integer streak);
}
