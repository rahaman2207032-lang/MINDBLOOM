package com.mentalhealth.backend.controller;
import com.mentalhealth.backend.model.MoodLog;
import com.mentalhealth.backend.service.MoodLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/mood")
// CORS handled globally in WebConfig.java
public class MoodLogController {

    @Autowired
    private MoodLogService moodLogService;

    @PostMapping
    public ResponseEntity<MoodLog> createMoodLog(@RequestBody MoodLog moodLog) {
        try {
            MoodLog saved = moodLogService.saveMoodLog(moodLog);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MoodLog>> getMoodLogsByUser(@PathVariable Long userId) {
        try {
            List<MoodLog> logs = moodLogService.getMoodLogsByUser(userId);
            return new ResponseEntity<>(logs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<MoodLog>> getMoodLogsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<MoodLog> logs = moodLogService.getMoodLogsByDateRange(userId, startDate, endDate);
            return new ResponseEntity<>(logs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<MoodLog> getLatestMoodLog(@PathVariable Long userId) {
        try {
            return moodLogService.getLatestMoodLog(userId)
                    .map(log -> new ResponseEntity<>(log, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}