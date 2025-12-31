package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.model.StressAssessment;
import com.mentalhealth.backend.service.StressAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stress")
// CORS handled globally in WebConfig.java
public class StressAssessmentController {

    @Autowired
    private StressAssessmentService stressAssessmentService;

    @PostMapping
    public ResponseEntity<StressAssessment> createStressAssessment(@RequestBody StressAssessment assessment) {
        try {
            StressAssessment saved = stressAssessmentService.saveStressAssessment(assessment);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StressAssessment>> getStressAssessmentsByUser(@PathVariable Long userId) {
        try {
            List<StressAssessment> assessments = stressAssessmentService.getStressAssessmentsByUser(userId);
            return new ResponseEntity<>(assessments, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<StressAssessment>> getStressAssessmentsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<StressAssessment> assessments = stressAssessmentService
                    .getStressAssessmentsByDateRange(userId, startDate, endDate);
            return new ResponseEntity<>(assessments, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<StressAssessment> getLatestStressAssessment(@PathVariable Long userId) {
        try {
            return stressAssessmentService.getLatestStressAssessment(userId)
                    .map(assessment -> new ResponseEntity<>(assessment, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}