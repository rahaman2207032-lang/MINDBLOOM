package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.model.JournalEntry;
import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.repository.JournalEntryRepository;
import com.mentalhealth.backend.service.JournalEntryService;
import com.mentalhealth.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/journal")
// CORS handled globally in WebConfig.java
public class JournalController {

    private final JournalEntryService journalEntryService;
    private final UserService userService; // ✅ Add this
    public JournalController(JournalEntryService journalEntryService, UserService userService) {
        this.journalEntryService = journalEntryService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry entry) {
        System.out.println("API: Create journal entry for user: " + entry.getUserId());

        if (entry.getContent() == null || entry.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        JournalEntry created = journalEntryService.createEntry(entry);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/create")
    public ResponseEntity<JournalEntry> createEntryAlt(@RequestBody JournalEntry entry) {
        System.out.println("API: Create journal entry payload: " + entry);

        return createEntry(entry);

    }

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<JournalEntry>> getEntriesByUser(@PathVariable Long userId) {
//        System.out.println("API: Get journal entries for user: " + userId);
//        List<JournalEntry> entries = journalEntryService.getEntriesByUser(userId);
//        return ResponseEntity.ok(entries);
//    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<JournalEntry> getEntryByUser(@PathVariable Long userId) {
        System.out.println("API: Get journal entry for user: " + userId);

        return journalEntryService.getLatestEntryByUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<JournalEntry> getLatestEntry(@PathVariable Long userId) {
        System.out.println("API: Get latest journal entry for user: " + userId);
        List<JournalEntry> entries = journalEntryService.getEntriesByUser(userId);

        if (entries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(entries.get(0));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntry> getEntryById(@PathVariable Long id) {
        return journalEntryService.getEntryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalEntry> updateEntry(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newContent = request.get("content");
        if (newContent == null || newContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            JournalEntry updated = journalEntryService.updateEntry(id, newContent);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        System.out.println("API: Delete journal entry: " + id);
        try {
            journalEntryService.deleteEntry(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Long>> getEntryCount(@PathVariable Long userId) {
        Long count = journalEntryService.countEntriesByUser(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User savedUser = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

}
