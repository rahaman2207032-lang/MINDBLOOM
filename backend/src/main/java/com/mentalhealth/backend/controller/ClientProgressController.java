package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.service.ClientProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/client-progress")
public class ClientProgressController {

    @Autowired
    private ClientProgressService clientProgressService;


    @GetMapping("/{clientId}/summary")
    public ResponseEntity<Map<String, Object>> getClientSummary(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId) {

        Map<String, Object> summary = clientProgressService.getClientSummary(clientId, instructorId);
        return ResponseEntity.ok(summary);
    }


    @GetMapping("/{clientId}/mood-logs")
    public ResponseEntity<List<Map<String, Object>>> getMoodLogs(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {

        List<Map<String, Object>> moodLogs = clientProgressService.getMoodLogs(clientId, instructorId, days);
        return ResponseEntity.ok(moodLogs);
    }


    @GetMapping("/{clientId}/stress-assessments")
    public ResponseEntity<List<Map<String, Object>>> getStressAssessments(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {

        List<Map<String, Object>> assessments = clientProgressService.getStressAssessments(clientId, instructorId, days);
        return ResponseEntity.ok(assessments);
    }


    @GetMapping("/{clientId}/habits")
    public ResponseEntity<List<Map<String, Object>>> getHabits(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId) {

        List<Map<String, Object>> habits = clientProgressService.getHabits(clientId, instructorId);
        return ResponseEntity.ok(habits);
    }


    @GetMapping("/{clientId}/sleep-data")
    public ResponseEntity<List<Map<String, Object>>> getSleepData(
            @PathVariable Long clientId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {

        List<Map<String, Object>> sleepData = clientProgressService.getSleepData(clientId, instructorId, days);
        return ResponseEntity.ok(sleepData);
    }
}
