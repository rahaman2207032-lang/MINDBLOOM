package com.mentalhealth.backend.service;



import com.mentalhealth.backend.model.SleepEntry;
import com.mentalhealth.backend.repository.SleepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SleepService {

    private final SleepRepository sleepRepository;

    /**
     * Create a new sleep entry
     */
    @Transactional
    public SleepEntry createSleepEntry(SleepEntry sleepEntry) {
        // Validate sleep quality is between 1-5
        if (sleepEntry.getSleepQuality() != null) {
            if (sleepEntry.getSleepQuality() < 1 || sleepEntry.getSleepQuality() > 5) {
                throw new IllegalArgumentException("Sleep quality must be between 1 and 5");
            }
        }

        // Validate end time is after start time
        if (sleepEntry.getSleepEndTime().isBefore(sleepEntry.getSleepStartTime())) {
            throw new IllegalArgumentException("Sleep end time must be after start time");
        }

        return sleepRepository.save(sleepEntry);
    }

    /**
     * Get all sleep entries for a user
     */
    public List<SleepEntry> getAllSleepEntries(Long userId) {
        return sleepRepository.findByUserIdOrderBySleepStartTimeDesc(userId);
    }

    /**
     * Get sleep entries for the last 7 days
     */
    public List<SleepEntry> getWeeklySleepEntries(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return sleepRepository.findWeeklySleepEntries(userId, sevenDaysAgo);
    }

    /**
     * Get a specific sleep entry
     */
    public SleepEntry getSleepEntryById(Long id) {
        return sleepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sleep entry not found with id: " + id));
    }

    /**
     * Update a sleep entry
     */
    @Transactional
    public SleepEntry updateSleepEntry(Long id, SleepEntry updatedEntry) {
        SleepEntry existingEntry = getSleepEntryById(id);

        // Update fields
        if (updatedEntry.getSleepStartTime() != null) {
            existingEntry.setSleepStartTime(updatedEntry.getSleepStartTime());
        }
        if (updatedEntry.getSleepEndTime() != null) {
            existingEntry.setSleepEndTime(updatedEntry.getSleepEndTime());
        }
        if (updatedEntry.getSleepQuality() != null) {
            if (updatedEntry.getSleepQuality() < 1 || updatedEntry.getSleepQuality() > 5) {
                throw new IllegalArgumentException("Sleep quality must be between 1 and 5");
            }
            existingEntry.setSleepQuality(updatedEntry.getSleepQuality());
        }
        if (updatedEntry.getNotes() != null) {
            existingEntry.setNotes(updatedEntry.getNotes());
        }

        // Validate end time is after start time
        if (existingEntry.getSleepEndTime().isBefore(existingEntry.getSleepStartTime())) {
            throw new IllegalArgumentException("Sleep end time must be after start time");
        }

        return sleepRepository.save(existingEntry);
    }

    /**
     * Delete a sleep entry
     */
    @Transactional
    public void deleteSleepEntry(Long id) {
        if (!sleepRepository.existsById(id)) {
            throw new RuntimeException("Sleep entry not found with id: " + id);
        }
        sleepRepository.deleteById(id);
    }

    /**
     * Calculate average sleep duration for a user
     */
    public double calculateAverageSleepHours(Long userId) {
        List<SleepEntry> entries = getAllSleepEntries(userId);
        if (entries.isEmpty()) {
            return 0.0;
        }

        double totalHours = entries.stream()
                .mapToDouble(SleepEntry::getSleepDurationHours)
                .sum();

        return totalHours / entries.size();
    }

    /**
     * Get total count of sleep entries for a user
     */
    public long getTotalEntriesCount(Long userId) {
        return sleepRepository.countByUserId(userId);
    }
}


