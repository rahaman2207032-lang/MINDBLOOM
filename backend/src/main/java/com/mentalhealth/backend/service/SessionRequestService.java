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
        SessionRequest request = sessionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        System.out.println("✅ Accepting session request ID: " + requestId);

        request.setStatus(RequestStatus.ACCEPTED);
        request.setUpdatedAt(LocalDateTime.now());

        // Generate Zoom link if not provided
        if (zoomLink == null || zoomLink.isEmpty()) {
            System.out.println("🔗 Generating Zoom meeting link...");
            zoomLink = zoomService.createMeeting(
                    "Therapy Session",
                    request.getRequestedDate(),
                    60
            );
        }

        // ✅ SAVE ZOOM LINK TO REQUEST (this was missing!)
        request.setZoomLink(zoomLink);
        System.out.println("✅ Zoom link saved to session request: " + zoomLink);

        // Create therapy session
        TherapySession session = new TherapySession();
        session.setClientId(request.getClientId());
        session.setClientName(request.getClientName());
        session.setInstructorId(request.getInstructorId());
        session.setSessionDate(request.getRequestedDate());
        session.setSessionType("Initial Consultation");
        session.setZoomLink(zoomLink);
        session.setStatus(SessionStatus.SCHEDULED);

        therapySessionService.createSession(session);
        System.out.println("✅ Therapy session created");

        // Send notification to client with zoom link
        notificationService.sendNotification(
                request.getClientId(),
                "SESSION_ACCEPTED",
                "Session Request Accepted ✅",
                "Your session request has been accepted. Click to join the meeting!",
                requestId  // This links notification to session request with zoom link
        );
        System.out.println("✅ Notification sent to client: " + request.getClientId());

        return sessionRequestRepository.save(request);
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
