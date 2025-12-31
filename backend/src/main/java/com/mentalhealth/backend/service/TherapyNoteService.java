package com.mentalhealth.backend.service;


import com.mentalhealth.backend.model.TherapyNote;
import com.mentalhealth.backend.repository.TherapyNoteRepository;
import com.mentalhealth.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TherapyNoteService {

    @Autowired
    private TherapyNoteRepository therapyNoteRepository;

    @Autowired
    private UserRepository userRepository;

    public TherapyNote createNote(TherapyNote note) {
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        // Get client name
        userRepository.findById(note.getClientId()).ifPresent(user -> {
            note.setClientName(user.getUsername());
        });

        return therapyNoteRepository.save(note);
    }

    public List<TherapyNote> getNotesForClient(Long clientId, Long instructorId) {
        return therapyNoteRepository.findByClientIdAndInstructorIdOrderBySessionDateDesc(
                clientId, instructorId);
    }

    public List<TherapyNote> getAllNotesForInstructor(Long instructorId) {
        return therapyNoteRepository.findByInstructorIdOrderBySessionDateDesc(instructorId);
    }

    public TherapyNote updateNote(Long noteId, String updatedNotes) {
        TherapyNote note = therapyNoteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        note.setNotes(updatedNotes);
        note.setUpdatedAt(LocalDateTime.now());

        return therapyNoteRepository.save(note);
    }

    public void deleteNote(Long noteId) {
        therapyNoteRepository.deleteById(noteId);
    }
}