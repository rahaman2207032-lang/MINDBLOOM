package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.TherapyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TherapyNoteRepository extends JpaRepository<TherapyNote, Long> {
    List<TherapyNote> findByClientIdAndInstructorIdOrderBySessionDateDesc(Long clientId, Long instructorId);
    List<TherapyNote> findByInstructorIdOrderBySessionDateDesc(Long instructorId);

    /**
     * Find notes for specific client
     */
    List<TherapyNote> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<TherapyNote> findBySessionId(Long sessionId);
}