package com.mentalhealth.backend.controller;



import com.mentalhealth.backend.model.SessionRequest;
import com.mentalhealth.backend.service.SessionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session-requests")
// CORS handled globally in WebConfig.java
public class SessionRequestController {

    @Autowired
    private SessionRequestService sessionRequestService;

    @PostMapping
    public ResponseEntity<SessionRequest> createRequest(@RequestBody SessionRequest request) {
        try {
            System.out.println("📝 Received session request from frontend:");
            System.out.println("   Raw request object: " + request);
            System.out.println("   userId (from getUserId): " + request.getUserId());
            System.out.println("   clientId (from getClientId): " + request.getClientId());
            System.out.println("   instructorId: " + request.getInstructorId());
            System.out.println("   requestedDate: " + request.getRequestedDate());
            System.out.println("   reason: " + request.getReason());
            System.out.println("   sessionType: " + (request.getReason() != null ? request.getReason().substring(0, Math.min(50, request.getReason().length())) : "null"));

            // Validate required fields
            if (request.getInstructorId() == null) {
                System.err.println("❌ ERROR: instructorId is NULL! Frontend must send instructorId.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            if (request.getClientId() == null && request.getUserId() == null) {
                System.err.println("❌ ERROR: Both clientId and userId are NULL! Frontend must send userId or clientId.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            SessionRequest created = sessionRequestService.createRequest(request);
            System.out.println("✅ Session request created successfully with ID: " + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            System.err.println("❌ ERROR in SessionRequestController: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/instructor/{instructorId}/pending")
    public ResponseEntity<List<SessionRequest>> getPendingRequests(@PathVariable Long instructorId) {
        try {
            List<SessionRequest> requests = sessionRequestService.getPendingRequestsForInstructor(instructorId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<SessionRequest> acceptRequest(
            @PathVariable Long requestId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String zoomLink = (body != null && body.containsKey("zoomLink"))
                    ? body.get("zoomLink") : null;
            SessionRequest accepted = sessionRequestService.acceptRequest(requestId, zoomLink);
            return ResponseEntity.ok(accepted);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{requestId}/decline")
    public ResponseEntity<SessionRequest> declineRequest(@PathVariable Long requestId) {
        try {
            SessionRequest declined = sessionRequestService.declineRequest(requestId);
            return ResponseEntity.ok(declined);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/instructor/{instructorId}/count")
    public ResponseEntity<Map<String, Long>> getPendingCount(@PathVariable Long instructorId) {
        try {
            long count = sessionRequestService.getPendingRequestCount(instructorId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all session requests for a user (client)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionRequest>> getUserSessionRequests(@PathVariable Long userId) {
        try {
            System.out.println("📋 Getting session requests for user ID: " + userId);
            List<SessionRequest> requests = sessionRequestService.getRequestsForUser(userId);
            System.out.println("✅ Found " + requests.size() + " session requests for user");
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            System.err.println("❌ ERROR getting user session requests: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get confirmed/accepted sessions for a user
     */
    @GetMapping("/user/{userId}/confirmed")
    public ResponseEntity<List<SessionRequest>> getUserConfirmedSessions(@PathVariable Long userId) {
        try {
            System.out.println("📋 Getting confirmed sessions for user ID: " + userId);
            List<SessionRequest> confirmed = sessionRequestService.getConfirmedSessionsForUser(userId);
            System.out.println("✅ Found " + confirmed.size() + " confirmed sessions for user");
            return ResponseEntity.ok(confirmed);
        } catch (Exception e) {
            System.err.println("❌ ERROR getting confirmed sessions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get pending session requests for a user
     */
    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<SessionRequest>> getUserPendingRequests(@PathVariable Long userId) {
        try {
            System.out.println("📋 Getting pending requests for user ID: " + userId);
            List<SessionRequest> pending = sessionRequestService.getPendingRequestsForUser(userId);
            System.out.println("✅ Found " + pending.size() + " pending requests for user");
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            System.err.println("❌ ERROR getting pending requests: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}