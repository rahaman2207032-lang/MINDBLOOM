package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.model.TherapyNote;
import com.mentalhealth.backend.service.TherapyNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/therapy-notes")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class TherapyNoteController {

    @Autowired
    private TherapyNoteService therapyNoteService;

    @PostMapping
    public ResponseEntity<TherapyNote> createNote(@RequestBody TherapyNote note) {
        try {
            System.out.println("📝 Received therapy note request:");
            System.out.println("   Client ID: " + note.getClientId());
            System.out.println("   Instructor ID: " + note.getInstructorId());
            System.out.println("   Session Date: " + note.getSessionDate());
            System.out.println("   Notes: " + note.getNotes());

            // Validation
            if (note.getClientId() == null) {
                System.err.println("❌ ERROR: Client ID is null");
                return ResponseEntity.badRequest().build();
            }
            if (note.getInstructorId() == null) {
                System.err.println("❌ ERROR: Instructor ID is null");
                return ResponseEntity.badRequest().build();
            }
            if (note.getNotes() == null || note.getNotes().trim().isEmpty()) {
                System.err.println("❌ ERROR: Notes text is empty");
                return ResponseEntity.badRequest().build();
            }
            if (note.getSessionDate() == null) {
                System.err.println("❌ ERROR: Session date is null");
                return ResponseEntity.badRequest().build();
            }

            TherapyNote created = therapyNoteService.createNote(note);
            System.out.println("✅ Therapy note saved successfully with ID: " + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            System.err.println("❌ ERROR saving therapy note: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/client/{clientId}/instructor/{instructorId}")
    public ResponseEntity<List<TherapyNote>> getClientNotes(
            @PathVariable Long clientId,
            @PathVariable Long instructorId) {
        try {
            List<TherapyNote> notes = therapyNoteService.getNotesForClient(clientId, instructorId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            System.err.println("❌ ERROR getting client notes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<TherapyNote>> getInstructorNotes(@PathVariable Long instructorId) {
        try {
            List<TherapyNote> notes = therapyNoteService.getAllNotesForInstructor(instructorId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            System.err.println("❌ ERROR getting instructor notes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<TherapyNote> updateNote(
            @PathVariable Long noteId,
            @RequestBody Map<String, String> body) {
        try {
            String notes = body.get("notes");
            TherapyNote updated = therapyNoteService.updateNote(noteId, notes);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("❌ ERROR updating note: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
        try {
            therapyNoteService.deleteNote(noteId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("❌ ERROR deleting note: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

