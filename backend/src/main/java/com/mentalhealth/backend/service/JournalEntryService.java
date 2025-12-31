package com.mentalhealth.backend.service;

import com.mentalhealth.backend.model.JournalEntry;
import com.mentalhealth.backend.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    /**
     * Create a new journal entry
     */
    public JournalEntry createEntry(JournalEntry entry) {
        System.out.println("SERVICE: Creating journal entry...");
        System.out.println("  - userId: " + entry.getUserId());
        System.out.println("  - content length: " + entry.getContent().length());

        try {
            JournalEntry saved = journalEntryRepository.save(entry);

            System.out.println("SERVICE: ✅ Journal entry saved to database!");
            System.out.println("  - Generated ID: " + saved.getId());
            System.out.println("  - Created at: " + saved.getCreatedAt());

            return saved;
        } catch (Exception e) {
            System.err.println("SERVICE: ❌ ERROR saving journal entry: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save journal entry: " + e.getMessage(), e);
        }
    }

    /**
     * Get ALL journal entries for a user (used for history view)
     */
    public List<JournalEntry> getEntriesByUser(Long userId) {
        System.out.println("SERVICE: Fetching journal entries for user: " + userId);
        List<JournalEntry> entries = journalEntryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        System.out.println("SERVICE: Found " + entries.size() + " entries");
        return entries;
    }

    /**
     * Get the LATEST journal entry for a user
     * (used when opening the journal page)
     */
    public Optional<JournalEntry> getLatestEntryByUser(Long userId) {
        System.out.println("SERVICE: Fetching latest journal entry for user: " + userId);
        Optional<JournalEntry> entry = journalEntryRepository.findTopByUserIdOrderByUpdatedAtDesc(userId);
        System.out.println("SERVICE: Latest entry found: " + entry.isPresent());
        return entry;
    }

    /**
     * Get a journal entry by ID
     */
    public Optional<JournalEntry> getEntryById(Long id) {
        return journalEntryRepository.findById(id);
    }

    /**
     * Update an existing journal entry
     */
    public JournalEntry updateEntry(Long id, String newContent) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Journal entry not found with id: " + id));

        entry.setContent(newContent);
        System.out.println("Updating journal entry: " + id);
        return journalEntryRepository.save(entry);
    }

    /**
     * Delete a journal entry
     */
    public void deleteEntry(Long id) {
        System.out.println("Deleting journal entry: " + id);
        journalEntryRepository.deleteById(id);
    }

    /**
     * Count how many entries a user has
     */
    public Long countEntriesByUser(Long userId) {
        return journalEntryRepository.countByUserId(userId);
    }
}


