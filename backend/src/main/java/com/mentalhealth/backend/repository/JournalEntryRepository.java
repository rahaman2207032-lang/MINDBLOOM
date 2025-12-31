package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByUserIdOrderByCreatedAtDesc(Long userId);

    Long countByUserId(Long userId);

    Optional<JournalEntry> findTopByUserIdOrderByUpdatedAtDesc(Long userId);

}

