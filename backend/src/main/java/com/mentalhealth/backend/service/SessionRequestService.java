package com.mentalhealth.backend.service;



import com.mentalhealth.backend.model.SessionRequest;
import com.mentalhealth.backend.model.SessionRequest.RequestStatus;
import com.mentalhealth.backend.model.TherapySession;
import com.mentalhealth.backend.model.TherapySession.SessionStatus;
import com.mentalhealth.backend.repository.SessionRequestRepository;
import com.mentalhealth.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionRequestService {

    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    @Autowired
    private TherapySessionService therapySessionService;

    @Autowired
    private ZoomService zoomService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    public List<SessionRequest> getPendingRequestsForInstructor(Long instructorId) {
        return sessionRequestRepository.findByInstructorIdAndStatus(instructorId, RequestStatus.PENDING);
    }

    public SessionRequest createRequest(SessionRequest request) {
        System.out.println("📝 Creating session request:");
        System.out.println("   Client ID: " + request.getClientId());
        System.out.println("   Instructor ID: " + request.getInstructorId());
        System.out.println("   Requested Date: " + request.getRequestedDate());

        try {
            request.setStatus(RequestStatus.PENDING);
            request.setCreatedAt(LocalDateTime.now());

            // Get client name
            if (request.getClientId() != null) {
                userRepository.findById(request.getClientId()).ifPresent(user -> {
                    request.setClientName(user.getUsername());
                    System.out.println("   Client Name: " + user.getUsername());
                });
            }

            SessionRequest saved = sessionRequestRepository.save(request);
            System.out.println("✅ Session request saved with ID: " + saved.getId());

            // Send notification to instructor
            notificationService.sendNotification(
                    request.getInstructorId(),
                    "session_request",
                    "New Session Request",
                    request.getClientName() + " has requested a session on " + request.getRequestedDate(),
                    saved.getId()
            );
            System.out.println("✅ Notification sent to instructor: " + request.getInstructorId());

            return saved;
        } catch (Exception e) {
            System.err.println("❌ ERROR in createRequest: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public SessionRequest acceptRequest(Long requestId, String zoomLink) {
        System.out.println("========================================");
        System.out.println("🎯 ACCEPTING SESSION REQUEST");
        System.out.println("========================================");
        System.out.println("Request ID: " + requestId);
        System.out.println("Manual Zoom Link provided: " + (zoomLink != null ? zoomLink : "NO"));

        SessionRequest request = sessionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        System.out.println("✅ Request found:");
        System.out.println("   Client ID: " + request.getClientId());
        System.out.println("   Client Name: " + request.getClientName());
        System.out.println("   Instructor ID: " + request.getInstructorId());
        System.out.println("   Requested Date: " + request.getRequestedDate());
        System.out.println("   Current Status: " + request.getStatus());

        // Change status first
        request.setStatus(RequestStatus.ACCEPTED);
        request.setUpdatedAt(LocalDateTime.now());
        System.out.println("✅ Status changed to ACCEPTED");

        // Generate Zoom link if not provided
        String finalZoomLink = zoomLink;
        if (finalZoomLink == null || finalZoomLink.isEmpty()) {
            System.out.println("🔗 Generating Zoom meeting link via API...");
            try {
                finalZoomLink = zoomService.createMeeting(
                        "Therapy Session with " + request.getClientName(),
                        request.getRequestedDate(),
                        60
                );
                System.out.println("✅ Zoom link generated: " + finalZoomLink);
            } catch (Exception e) {
                System.err.println("❌ ERROR creating Zoom meeting: " + e.getMessage());
                e.printStackTrace();
                // Create a placeholder if Zoom fails
                finalZoomLink = "https://zoom.us/j/placeholder-" + System.currentTimeMillis();
                System.out.println("⚠️ Using placeholder link: " + finalZoomLink);
            }
        } else {
            System.out.println("📝 Using provided manual zoom link: " + finalZoomLink);
        }

        // ✅ CRITICAL: Save zoom link to request
        request.setZoomLink(finalZoomLink);
        System.out.println("✅ Zoom link SET on request object: " + request.getZoomLink());

        // Save request FIRST to ensure zoom link is persisted
        SessionRequest savedRequest = sessionRequestRepository.save(request);
        System.out.println("✅ Request SAVED to database");
        System.out.println("   Saved Request ID: " + savedRequest.getId());
        System.out.println("   Saved Zoom Link: " + savedRequest.getZoomLink());
        System.out.println("   Saved Status: " + savedRequest.getStatus());

        // Create therapy session
        try {
            TherapySession session = new TherapySession();
            session.setClientId(savedRequest.getClientId());
            session.setClientName(savedRequest.getClientName());
            session.setInstructorId(savedRequest.getInstructorId());
            session.setSessionDate(savedRequest.getRequestedDate());
            session.setSessionType("Initial Consultation");
            session.setZoomLink(finalZoomLink);
            session.setStatus(SessionStatus.SCHEDULED);

            TherapySession savedSession = therapySessionService.createSession(session);
            System.out.println("✅ Therapy session created with ID: " + savedSession.getId());
        } catch (Exception e) {
            System.err.println("⚠️ ERROR creating therapy session: " + e.getMessage());
            // Continue even if therapy session fails
        }

        // Send notification to client with zoom link
        try {
            notificationService.sendNotification(
                    savedRequest.getClientId(),
                    "SESSION_ACCEPTED",
                    "Session Request Accepted ✅",
                    "Your therapy session has been scheduled!",
                    savedRequest.getId()  // Links to session request with zoom link
            );
            System.out.println("✅ Notification sent to client: " + savedRequest.getClientId());
        } catch (Exception e) {
            System.err.println("⚠️ ERROR sending notification: " + e.getMessage());
        }

        System.out.println("========================================");
        System.out.println("✅ SESSION ACCEPTANCE COMPLETE");
        System.out.println("   Final Zoom Link: " + savedRequest.getZoomLink());
        System.out.println("========================================");

        return savedRequest;
    }

    @Transactional
    public SessionRequest declineRequest(Long requestId) {
        SessionRequest request = sessionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(RequestStatus.DECLINED);
        request.setUpdatedAt(LocalDateTime.now());

        // Send notification to client
        notificationService.sendNotification(
                request.getClientId(),
                "session_declined",
                "Session Request Declined",
                "Your session request for " + request.getRequestedDate() + " has been declined.",
                requestId
        );

        return sessionRequestRepository.save(request);
    }

    public long getPendingRequestCount(Long instructorId) {
        return sessionRequestRepository.countByInstructorIdAndStatus(instructorId, RequestStatus.PENDING);
    }

    /**
     * Get all session requests for a user (all statuses)
     */
    public List<SessionRequest> getRequestsForUser(Long userId) {
        return sessionRequestRepository.findByClientIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get confirmed/accepted sessions for a user
     */
    public List<SessionRequest> getConfirmedSessionsForUser(Long userId) {
        return sessionRequestRepository.findByClientIdAndStatus(userId, RequestStatus.ACCEPTED);
    }

    /**
     * Get pending session requests for a user
     */
    public List<SessionRequest> getPendingRequestsForUser(Long userId) {
        return sessionRequestRepository.findByClientIdAndStatus(userId, RequestStatus.PENDING);
    }
}
