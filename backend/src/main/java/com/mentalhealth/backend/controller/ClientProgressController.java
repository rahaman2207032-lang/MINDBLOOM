package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.service.ClientProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Client Progress Tracking
 * Handles aggregated client mental health data for instructors
 */
@RestController
@RequestMapping("/api/client-progress")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ClientProgressController {

    @Autowired
    private ClientProgressService clientProgressService;

    /**
     * GET /api/client-progress/{clientId}/summary
     * Returns aggregated summary of client's mental health data
     */
    @GetMapping("/{clientId}/summary")
    public ResponseEntity<Map<String, Object>> getClientSummary(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId) {

        Map<String, Object> summary = clientProgressService.getClientSummary(clientId, instructorId);
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/client-progress/{clientId}/mood-logs
     * Returns mood log history for client
     */
    @GetMapping("/{clientId}/mood-logs")
    public ResponseEntity<List<Map<String, Object>>> getMoodLogs(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {

        List<Map<String, Object>> moodLogs = clientProgressService.getMoodLogs(clientId, instructorId, days);
        return ResponseEntity.ok(moodLogs);
    }

    /**
     * GET /api/client-progress/{clientId}/stress-assessments
     * Returns stress assessment history for client
     */
    @GetMapping("/{clientId}/stress-assessments")
    public ResponseEntity<List<Map<String, Object>>> getStressAssessments(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {

        List<Map<String, Object>> assessments = clientProgressService.getStressAssessments(clientId, instructorId, days);
        return ResponseEntity.ok(assessments);
    }

    /**
     * GET /api/client-progress/{clientId}/habits
     * Returns habit tracking data for client
     */
    @GetMapping("/{clientId}/habits")
    public ResponseEntity<List<Map<String, Object>>> getHabits(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId) {

        List<Map<String, Object>> habits = clientProgressService.getHabits(clientId, instructorId);
        return ResponseEntity.ok(habits);
    }

    /**
     * GET /api/client-progress/{clientId}/sleep-data
     * Returns sleep tracking data for client
     */
    @GetMapping("/{clientId}/sleep-data")
    public ResponseEntity<List<Map<String, Object>>> getSleepData(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {

        List<Map<String, Object>> sleepData = clientProgressService.getSleepData(clientId, instructorId, days);
        return ResponseEntity.ok(sleepData);
    }
}

