package com.mentalhealth.backend.controller;


import com.mentalhealth.backend.model.SleepEntry;
import com.mentalhealth.backend.service.SleepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sleep")
@RequiredArgsConstructor
// CORS handled globally in WebConfig.java
public class SleepController {

    private final SleepService sleepService;

    /**
     * Create a new sleep entry
     * POST /api/sleep
     */
    @PostMapping
    public ResponseEntity<?> createSleepEntry(@RequestBody SleepEntry sleepEntry) {
        try {
            SleepEntry created = sleepService.createSleepEntry(sleepEntry);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create sleep entry: " + e.getMessage()));
        }
    }

    /**
     * Get all sleep entries for a user
     * GET /api/sleep/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllSleepEntries(@PathVariable Long userId) {
        try {
            List<SleepEntry> entries = sleepService.getAllSleepEntries(userId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch sleep entries: " + e.getMessage()));
        }
    }

    /**
     * Get sleep entries for the last 7 days
     * GET /api/sleep/user/{userId}/weekly
     */
    @GetMapping("/user/{userId}/weekly")
    public ResponseEntity<?> getWeeklySleepEntries(@PathVariable Long userId) {
        try {
            List<SleepEntry> entries = sleepService.getWeeklySleepEntries(userId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch weekly sleep entries: " + e.getMessage()));
        }
    }

    /**
     * Get a specific sleep entry
     * GET /api/sleep/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSleepEntry(@PathVariable Long id) {
        try {
            SleepEntry entry = sleepService.getSleepEntryById(id);
            return ResponseEntity.ok(entry);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch sleep entry: " + e.getMessage()));
        }
    }

    /**
     * Update a sleep entry
     * PUT /api/sleep/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSleepEntry(
            @PathVariable Long id,
            @RequestBody SleepEntry sleepEntry) {
        try {
            SleepEntry updated = sleepService.updateSleepEntry(id, sleepEntry);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update sleep entry: " + e.getMessage()));
        }
    }

    /**
     * Delete a sleep entry
     * DELETE /api/sleep/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSleepEntry(@PathVariable Long id) {
        try {
            sleepService.deleteSleepEntry(id);
            return ResponseEntity.ok(createSuccessResponse("Sleep entry deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete sleep entry: " + e.getMessage()));
        }
    }

    /**
     * Get sleep statistics for a user
     * GET /api/sleep/user/{userId}/stats
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<?> getSleepStats(@PathVariable Long userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("averageSleepHours", sleepService.calculateAverageSleepHours(userId));
            stats.put("totalEntries", sleepService.getTotalEntriesCount(userId));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch sleep stats: " + e.getMessage()));
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

