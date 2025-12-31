package com.mentalhealth.backend.service;



import com.mentalhealth.backend.model.Habit;
import com.mentalhealth.backend.model.HabitCompletion;
import com.mentalhealth.backend.repository.HabitCompletionRepository;
import com.mentalhealth.backend.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitCompletionRepository habitCompletionRepository;

    /**
     * Create a new habit
     */
    @Transactional
    public Habit createHabit(Habit habit) {
        System.out.println("SERVICE: Creating habit...");
        System.out.println("  - userId: " + habit.getUserId());
        System.out.println("  - name: " + habit.getName());
        System.out.println("  - frequency: " + habit.getFrequency());

        // Validate frequency
        if (!habit.getFrequency().equals("DAILY") && !habit.getFrequency().equals("WEEKLY")) {
            System.err.println("SERVICE: ❌ Invalid frequency: " + habit.getFrequency());
            throw new IllegalArgumentException("Frequency must be either DAILY or WEEKLY");
        }

        try {
            Habit saved = habitRepository.save(habit);

            System.out.println("SERVICE: ✅ Habit saved to database!");
            System.out.println("  - Generated ID: " + saved.getId());
            System.out.println("  - Created at: " + saved.getCreatedAt());

            return saved;
        } catch (Exception e) {
            System.err.println("SERVICE: ❌ ERROR saving habit: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save habit: " + e.getMessage(), e);
        }
    }

    /**
     * Get all habits for a user
     */
    public List<Habit> getAllHabits(Long userId) {
        return habitRepository.findByUserId(userId);
    }

    /**
     * Get all active habits for a user
     */
    public List<Habit> getActiveHabits(Long userId) {
        System.out.println("SERVICE: Fetching active habits for user: " + userId);
        List<Habit> habits = habitRepository.findByUserIdAndIsActiveTrue(userId);
        System.out.println("SERVICE: Found " + habits.size() + " active habits");
        return habits;
    }

    /**
     * Get a specific habit
     */
    public Habit getHabitById(Long id) {
        return habitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habit not found with id: " + id));
    }

    /**
     * Update a habit
     */
    @Transactional
    public Habit updateHabit(Long id, Habit updatedHabit) {
        Habit existingHabit = getHabitById(id);

        // Update fields
        if (updatedHabit.getName() != null) {
            existingHabit.setName(updatedHabit.getName());
        }
        if (updatedHabit.getDescription() != null) {
            existingHabit.setDescription(updatedHabit.getDescription());
        }
        if (updatedHabit.getFrequency() != null) {
            if (!updatedHabit.getFrequency().equals("DAILY") && !updatedHabit.getFrequency().equals("WEEKLY")) {
                throw new IllegalArgumentException("Frequency must be either DAILY or WEEKLY");
            }
            existingHabit.setFrequency(updatedHabit.getFrequency());
        }
        if (updatedHabit.getTargetDays() != null) {
            existingHabit.setTargetDays(updatedHabit.getTargetDays());
        }
        if (updatedHabit.getIsActive() != null) {
            existingHabit.setIsActive(updatedHabit.getIsActive());
        }

        return habitRepository.save(existingHabit);
    }

    /**
     * Delete a habit (also deletes all completions via cascade)
     */
    @Transactional
    public void deleteHabit(Long id) {
        if (!habitRepository.existsById(id)) {
            throw new RuntimeException("Habit not found with id: " + id);
        }

        // Delete all completions first
        habitCompletionRepository.deleteByHabitId(id);

        // Delete the habit
        habitRepository.deleteById(id);
    }

    /**
     * Mark habit as complete for a specific date
     */
    @Transactional
    public HabitCompletion completeHabit(Long habitId, Long userId, LocalDate completionDate, String notes) {
        // Check if habit exists
        Habit habit = getHabitById(habitId);

        // Check if already completed for this date
        if (habitCompletionRepository.existsByHabitIdAndCompletionDate(habitId, completionDate)) {
            throw new IllegalStateException("Habit already completed for this date");
        }

        // Create completion record
        HabitCompletion completion = new HabitCompletion();
        completion.setHabitId(habitId);
        completion.setUserId(userId);
        completion.setCompletionDate(completionDate);
        completion.setNotes(notes);

        HabitCompletion savedCompletion = habitCompletionRepository.save(completion);

        // Update streak
        updateStreak(habit);

        return savedCompletion;
    }

    /**
     * Update habit streak after completion
     */
    @Transactional
    public void updateStreak(Habit habit) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Check if completed today
        boolean completedToday = habitCompletionRepository.existsByHabitIdAndCompletionDate(habit.getId(), today);

        // Check if completed yesterday
        boolean completedYesterday = habitCompletionRepository.existsByHabitIdAndCompletionDate(habit.getId(), yesterday);

        if (completedToday) {
            if (completedYesterday || habit.getCurrentStreak() == 0) {
                // Continue or start streak
                habit.setCurrentStreak(habit.getCurrentStreak() + 1);
            } else {
                // Check if streak was broken (no completion yesterday)
                habit.setCurrentStreak(1);
            }

            // Update longest streak if current is higher
            if (habit.getCurrentStreak() > habit.getLongestStreak()) {
                habit.setLongestStreak(habit.getCurrentStreak());
            }

            habit.setLastCompletedAt(java.time.LocalDateTime.now());
            habitRepository.save(habit);
        }
    }

    /**
     * Get completion history for a habit
     */
    public List<HabitCompletion> getHabitCompletions(Long habitId) {
        return habitCompletionRepository.findByHabitIdOrderByCompletionDateDesc(habitId);
    }

    /**
     * Get habits with active streaks
     */
    public List<Habit> getHabitsWithStreaks(Long userId) {
        return habitRepository.findByUserIdAndCurrentStreakGreaterThan(userId, 0);
    }

    /**
     * Check if habit is completed for today
     */
    public boolean isCompletedToday(Long habitId) {
        return habitCompletionRepository.existsByHabitIdAndCompletionDate(habitId, LocalDate.now());
    }

    /**
     * Get statistics for user habits
     */
    public HabitStats getUserHabitStats(Long userId) {
        long totalHabits = habitRepository.countByUserIdAndIsActiveTrue(userId);
        List<Habit> habitsWithStreaks = getHabitsWithStreaks(userId);

        return new HabitStats(totalHabits, habitsWithStreaks.size());
    }

    // Inner class for statistics
    public static class HabitStats {
        public long totalHabits;
        public int activeStreaks;

        public HabitStats(long totalHabits, int activeStreaks) {
            this.totalHabits = totalHabits;
            this.activeStreaks = activeStreaks;
        }
    }
}

