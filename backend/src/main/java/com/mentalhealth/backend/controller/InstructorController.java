package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.model.Instructor;
import com.mentalhealth.backend.service.InstructorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;

/**
 * REST Controller for Instructor Dashboard
 * Handles instructor-specific endpoints for dashboard, clients, analytics, etc.
 */
@RestController
@RequestMapping("/api/instructors")  // Changed to plural to match frontend
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class InstructorController {

    @Autowired
    private InstructorService instructorService;

    /**
     * Get all instructors (for session request dropdown)
     */
    @GetMapping
    public ResponseEntity<List<Instructor>> getAllInstructors() {
        List<Instructor> instructors = instructorService.getAllInstructorsForSelection();
        return ResponseEntity.ok(instructors);
    }

    /**
     * Get dashboard statistics
     * Returns: pending requests, today's sessions, total clients, available slots
     */
    @GetMapping("/{instructorId}/stats")
    public ResponseEntity<Map<String, Integer>> getDashboardStats(@PathVariable Long instructorId) {
        Map<String, Integer> stats = instructorService.getDashboardStats(instructorId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all clients for instructor with their mental health overview
     */
    @GetMapping("/{instructorId}/clients")
    public ResponseEntity<List<Map<String, Object>>> getAllClients(@PathVariable Long instructorId) {
        List<Map<String, Object>> clients = instructorService.getAllClients(instructorId);
        return ResponseEntity.ok(clients);
    }

    /**
     * Search clients by name
     */
    @GetMapping("/{instructorId}/clients/search")
    public ResponseEntity<List<Map<String, Object>>> searchClients(
            @PathVariable Long instructorId,
            @RequestParam String q) {
        List<Map<String, Object>> clients = instructorService.searchClients(instructorId, q);
        return ResponseEntity.ok(clients);
    }

    /**
     * Get analytics data for specified time range
     * Time ranges: "Last 7 Days", "Last 30 Days", "Last 90 Days"
     */
    @GetMapping("/{instructorId}/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @PathVariable Long instructorId,
            @RequestParam String range) {
        Map<String, Object> analytics = instructorService.getAnalytics(instructorId, range);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get list of clients with whom instructor has message conversations
     */
    @GetMapping("/{instructorId}/conversations")
    public ResponseEntity<List<Map<String, Object>>> getConversations(@PathVariable Long instructorId) {
        try {
            System.out.println("📡 Conversations endpoint called with instructorId: " + instructorId);

            if (instructorId == null || instructorId <= 0) {
                System.err.println("❌ Invalid instructor ID: " + instructorId);
                return ResponseEntity.badRequest().build();
            }

            List<Map<String, Object>> conversations = instructorService.getConversations(instructorId);
            System.out.println("✅ Returning " + conversations.size() + " conversations");

            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            System.err.println("❌ ERROR in getConversations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get pending session requests for instructor
     */
    @GetMapping("/{instructorId}/session-requests")
    public ResponseEntity<List<Map<String, Object>>> getSessionRequests(@PathVariable Long instructorId) {
        List<Map<String, Object>> requests = instructorService.getPendingSessionRequests(instructorId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Accept a session request - Automatically creates Zoom meeting!
     * No need to provide zoomLink - it's created automatically via Zoom API
     */
    @PostMapping("/session-requests/{requestId}/accept")
    public ResponseEntity<Map<String, Object>> acceptSessionRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            System.out.println("🎯 Accepting session request #" + requestId);

            // Extract manual zoom link if provided (optional - used as fallback)
            String manualZoomLink = null;
            if (body != null && body.containsKey("zoomLink")) {
                manualZoomLink = body.get("zoomLink");
                System.out.println("📝 Manual zoom link provided: " + manualZoomLink);
            } else {
                System.out.println("🎥 No manual link - will create via Zoom API automatically!");
            }

            Map<String, Object> result = instructorService.acceptSessionRequest(requestId, manualZoomLink);

            if (result.get("success").equals(true)) {
                System.out.println("✅ Session request accepted successfully!");
                System.out.println("   Zoom Link: " + result.get("zoomLink"));
                System.out.println("   Method: " + result.get("zoomCreationMethod"));
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ ERROR accepting session request: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * Decline a session request
     */
    @PostMapping("/session-requests/{requestId}/decline")
    public ResponseEntity<Map<String, Object>> declineSessionRequest(@PathVariable Long requestId) {
        try {
            Map<String, Object> result = instructorService.declineSessionRequest(requestId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ ERROR declining session request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get upcoming accepted sessions for instructor
     * GET /api/instructors/{instructorId}/upcoming-sessions
     */
    @GetMapping("/{instructorId}/upcoming-sessions")
    public ResponseEntity<List<Map<String, Object>>> getUpcomingSessions(@PathVariable Long instructorId) {
        try {
            List<Map<String, Object>> sessions = instructorService.getUpcomingSessions(instructorId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            System.err.println("❌ ERROR getting upcoming sessions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
