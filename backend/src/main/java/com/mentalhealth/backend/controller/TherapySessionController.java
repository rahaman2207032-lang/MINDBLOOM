package com.mentalhealth.backend.controller;


import com.mentalhealth.backend.model.TherapySession;
import com.mentalhealth.backend.service.TherapySessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/therapy-sessions")
// CORS handled globally in WebConfig.java
public class TherapySessionController {

    @Autowired
    private TherapySessionService therapySessionService;

    @PostMapping
    public ResponseEntity<TherapySession> createSession(@RequestBody TherapySession session) {
        try {
            TherapySession created = therapySessionService.createSession(session);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<TherapySession>> getInstructorSessions(@PathVariable Long instructorId) {
        try {
            List<TherapySession> sessions = therapySessionService.getAllSessionsForInstructor(instructorId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/instructor/{instructorId}/week")
    public ResponseEntity<List<TherapySession>> getWeeklySessions(@PathVariable Long instructorId) {
        try {
            List<TherapySession> sessions = therapySessionService.getWeeklySessionsForInstructor(instructorId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/instructor/{instructorId}/today")
    public ResponseEntity<List<TherapySession>> getTodaySessions(@PathVariable Long instructorId) {
        try {
            List<TherapySession> sessions = therapySessionService.getTodaySessionsForInstructor(instructorId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<TherapySession>> getClientSessions(@PathVariable Long clientId) {
        try {
            List<TherapySession> sessions = therapySessionService.getClientSessions(clientId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{sessionId}/status")
    public ResponseEntity<TherapySession> updateStatus(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            TherapySession updated = therapySessionService.updateSessionStatus(sessionId, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{sessionId}/rate")
    public ResponseEntity<TherapySession> rateSession(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Integer> body) {
        try {
            int rating = body.get("rating");
            TherapySession updated = therapySessionService.rateSession(sessionId, rating);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
