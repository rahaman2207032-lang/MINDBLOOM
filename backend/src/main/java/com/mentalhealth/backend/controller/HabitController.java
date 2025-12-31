package com.mentalhealth.backend.controller;



import com.mentalhealth.backend.model.Habit;
import com.mentalhealth.backend.model.HabitCompletion;
import com.mentalhealth.backend.service.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
// CORS handled globally in WebConfig.java
public class HabitController {

    private final HabitService habitService;

    /**
     * Create a new habit
     * POST /api/habits
     */
    @PostMapping
    public ResponseEntity<?> createHabit(@RequestBody Habit habit) {
        try {
            System.out.println("=== API: CREATE Habit ===");
            System.out.println("User ID: " + habit.getUserId());
            System.out.println("Habit Name: " + habit.getName());
            System.out.println("Frequency: " + habit.getFrequency());

            if (habit.getUserId() == null) {
                System.err.println("❌ ERROR: userId is NULL");
                return ResponseEntity.badRequest().body(createErrorResponse("userId is required"));
            }

            if (habit.getName() == null || habit.getName().trim().isEmpty()) {
                System.err.println("❌ ERROR: habit name is empty");
                return ResponseEntity.badRequest().body(createErrorResponse("habit name is required"));
            }

            System.out.println("Calling service to save habit...");
            Habit created = habitService.createHabit(habit);

            System.out.println("✅ Habit created successfully! ID: " + created.getId());
            System.out.println("Created at: " + created.getCreatedAt());

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ ERROR creating habit: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create habit: " + e.getMessage()));
        }
    }

    /**
     * Get all habits for a user
     * GET /api/habits/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllHabits(@PathVariable Long userId) {
        try {
            System.out.println("=== API: Get habits for user: " + userId + " ===");
            List<Habit> habits = habitService.getActiveHabits(userId);

            System.out.println("✅ Found " + habits.size() + " active habits for user " + userId);

            if (habits.isEmpty()) {
                System.out.println("⚠️ No habits found - returning empty list");
            }

            return ResponseEntity.ok(habits);
        } catch (Exception e) {
            System.err.println("❌ ERROR loading habits for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch habits: " + e.getMessage()));
        }
    }

    /**
     * Get a specific habit
     * GET /api/habits/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getHabit(@PathVariable Long id) {
        try {
            Habit habit = habitService.getHabitById(id);
            return ResponseEntity.ok(habit);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch habit: " + e.getMessage()));
        }
    }

    /**
     * Update a habit
     * PUT /api/habits/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHabit(
            @PathVariable Long id,
            @RequestBody Habit habit) {
        try {
            Habit updated = habitService.updateHabit(id, habit);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update habit: " + e.getMessage()));
        }
    }

    /**
     * Delete a habit
     * DELETE /api/habits/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHabit(@PathVariable Long id) {
        try {
            habitService.deleteHabit(id);
            return ResponseEntity.ok(createSuccessResponse("Habit deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete habit: " + e.getMessage()));
        }
    }

    /**
     * Mark habit as complete for a specific date
     * POST /api/habits/{habitId}/complete
     */
    @PostMapping("/{habitId}/complete")
    public ResponseEntity<?> completeHabit(
            @PathVariable Long habitId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String dateStr = request.get("completionDate").toString();
            LocalDate completionDate = LocalDate.parse(dateStr);
            String notes = request.containsKey("notes") ? request.get("notes").toString() : null;

            HabitCompletion completion = habitService.completeHabit(habitId, userId, completionDate, notes);
            return ResponseEntity.status(HttpStatus.CREATED).body(completion);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to complete habit: " + e.getMessage()));
        }
    }

    /**
     * Get completion history for a habit
     * GET /api/habits/{habitId}/completions
     */
    @GetMapping("/{habitId}/completions")
    public ResponseEntity<?> getHabitCompletions(@PathVariable Long habitId) {
        try {
            List<HabitCompletion> completions = habitService.getHabitCompletions(habitId);
            return ResponseEntity.ok(completions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch habit completions: " + e.getMessage()));
        }
    }

    /**
     * Check if habit is completed today
     * GET /api/habits/{habitId}/completed-today
     */
    @GetMapping("/{habitId}/completed-today")
    public ResponseEntity<?> isCompletedToday(@PathVariable Long habitId) {
        try {
            boolean completed = habitService.isCompletedToday(habitId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("completedToday", completed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to check completion status: " + e.getMessage()));
        }
    }

    /**
     * Get habit statistics for a user
     * GET /api/habits/user/{userId}/stats
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<?> getHabitStats(@PathVariable Long userId) {
        try {
            HabitService.HabitStats stats = habitService.getUserHabitStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch habit stats: " + e.getMessage()));
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    /**
     * Helper method to create success response
     */
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}

