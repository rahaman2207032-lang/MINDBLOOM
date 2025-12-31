package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.dto.ProgressData;
import com.mentalhealth.backend.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/progress")
// CORS handled globally in WebConfig.java
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProgressData> getProgressData(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            ProgressData progressData = progressService.getProgressData(userId, startDate, endDate);
            return ResponseEntity.ok(progressData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
