package com.mentalhealth.backend.service;

import com.mentalhealth.backend.model.Instructor;
import com.mentalhealth.backend.model.Message;
import com.mentalhealth.backend.model.SessionRequest;
import com.mentalhealth.backend.model.TherapySession;
import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.model.UserRole;
import com.mentalhealth.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Instructor Dashboard functionality
 * Handles dashboard stats, client management, analytics, etc.
 */
@Service
public class InstructorService {

    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    @Autowired
    private TherapySessionRepository therapySessionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private InstructorRepository instructorRepository;

    @Autowired(required = false)
    private MoodLogRepository moodLogRepository;

    @Autowired(required = false)
    private StressAssessmentRepository stressAssessmentRepository;

    @Autowired(required = false)
    private ZoomService zoomService;

    @Autowired(required = false)
    private NotificationService notificationService;

    /**
     * Get all instructors for session request selection dropdown
     */
    public List<Instructor> getAllInstructorsForSelection() {
        if (instructorRepository != null) {
            return instructorRepository.findAll();
        }
        return new ArrayList<>();
    }

    /**
     * Get dashboard statistics for instructor
     */
    public Map<String, Integer> getDashboardStats(Long instructorId) {
        Map<String, Integer> stats = new HashMap<>();

        try {
            // Count pending session requests
            int pendingRequests = (int) sessionRequestRepository
                    .findByInstructorIdAndStatusOrderByCreatedAtDesc(instructorId, SessionRequest.RequestStatus.PENDING)
                    .size();

            // Count today's sessions
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            List<TherapySession> todaySessions = therapySessionRepository
                    .findByInstructorIdAndSessionDateBetween(instructorId, startOfDay, endOfDay);

            // Count total unique clients
            List<TherapySession> allSessions = therapySessionRepository
                    .findByInstructorId(instructorId);
            Set<Long> uniqueClients = allSessions.stream()
                    .map(TherapySession::getClientId)
                    .collect(Collectors.toSet());

            // Available slots (customize this based on your availability logic)
            int availableSlots = 8; // Default value

            stats.put("pendingRequests", pendingRequests);
            stats.put("todaySessions", todaySessions.size());
            stats.put("totalClients", uniqueClients.size());
            stats.put("availableSlots", availableSlots);

        } catch (Exception e) {
            // Return default values if error
            stats.put("pendingRequests", 0);
            stats.put("todaySessions", 0);
            stats.put("totalClients", 0);
            stats.put("availableSlots", 0);
        }

        return stats;
    }

    /**
     * Get all clients (ALL users with role='USER') with their overview
     * NO CONSENT CHECKING - Instructors can see all clients
     */
    public List<Map<String, Object>> getAllClients(Long instructorId) {
        List<Map<String, Object>> clients = new ArrayList<>();

        try {
            // Get ALL users with role=USER (all clients in the system)
            List<User> allUsers = userRepository.findByRole(UserRole.USER);

            for (User user : allUsers) {
                Map<String, Object> clientOverview = new HashMap<>();
                clientOverview.put("clientId", user.getId());
                clientOverview.put("clientName", user.getUsername());

                // Calculate average mood from mood logs (if available)
                Double avgMood = calculateAverageMood(user.getId());
                clientOverview.put("averageMood", avgMood);

                // Get latest stress level (if available)
                String stressLevel = getLatestStressLevel(user.getId());
                clientOverview.put("stressLevel", stressLevel);

                // Get session count for this client
                List<TherapySession> clientSessions = therapySessionRepository
                        .findByClientIdOrderBySessionDateDesc(user.getId());
                clientOverview.put("totalSessions", clientSessions.size());

                // Get last session date
                String lastSession = null;
                if (!clientSessions.isEmpty()) {
                    lastSession = clientSessions.get(0).getSessionDate()
                            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                }
                clientOverview.put("lastSessionDate", lastSession);

                // No consent needed - removed consent system
                clientOverview.put("consentGranted", true);

                clients.add(clientOverview);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clients;
    }

    /**
     * Search clients by name
     */
    public List<Map<String, Object>> searchClients(Long instructorId, String searchTerm) {
        List<Map<String, Object>> allClients = getAllClients(instructorId);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allClients;
        }

        String lowerSearch = searchTerm.toLowerCase();
        return allClients.stream()
                .filter(client -> {
                    String name = (String) client.get("clientName");
                    return name != null && name.toLowerCase().contains(lowerSearch);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get analytics data for specified time range
     */
    public Map<String, Object> getAnalytics(Long instructorId, String timeRange) {
        Map<String, Object> analytics = new HashMap<>();

        try {
            LocalDateTime startDate = calculateStartDate(timeRange);

            // Get sessions in time range
            List<TherapySession> sessions = therapySessionRepository
                    .findByInstructorIdAndSessionDateAfter(instructorId, startDate);

            // Total sessions
            analytics.put("totalSessions", sessions.size());

            // Completed sessions
            long completedSessions = sessions.stream()
                    .filter(s -> s.getStatus() == TherapySession.SessionStatus.COMPLETED)
                    .count();
            analytics.put("completedSessions", (int) completedSessions);

            // Average rating (implement if you have session feedback)
            Double avgRating = calculateAverageRating(instructorId, startDate);
            analytics.put("avgRating", avgRating);

        } catch (Exception e) {
            analytics.put("totalSessions", 0);
            analytics.put("completedSessions", 0);
            analytics.put("avgRating", 0.0);
        }

        return analytics;
    }

    /**
     * Get conversations list - Returns ALL clients (users) so instructor can message anyone
     */
    public List<Map<String, Object>> getConversations(Long instructorId) {
        System.out.println("📋 Getting conversations for instructor ID: " + instructorId);
        List<Map<String, Object>> conversations = new ArrayList<>();

        try {
            // Get ALL users with role=USER (all potential clients)
            List<User> allClients = userRepository.findByRole(UserRole.USER);
            System.out.println("✅ Found " + allClients.size() + " users with role=USER");

            if (allClients.isEmpty()) {
                System.out.println("⚠️ WARNING: No users found with role=USER!");
                System.out.println("   Check your users table - make sure users have role='USER'");
            }

            for (User client : allClients) {
                Map<String, Object> conv = new HashMap<>();
                conv.put("clientId", client.getId());
                conv.put("clientName", client.getUsername());

                System.out.println("   Processing client: " + client.getUsername() + " (ID: " + client.getId() + ")");

                // Get message history between instructor and this client
                List<Message> conversationMessages = messageRepository
                        .findConversationByInstructorAndClient(instructorId, client.getId());

                // Add last message info if exists
                if (!conversationMessages.isEmpty()) {
                    Message lastMessage = conversationMessages.get(0);
                    conv.put("lastMessage", lastMessage.getMessageText());
                    conv.put("lastMessageTime", lastMessage.getSentAt().toString());

                    // Count unread messages from this client
                    long unreadCount = conversationMessages.stream()
                            .filter(m -> m.getReceiverId().equals(instructorId) && m.getReadAt() == null)
                            .count();
                    conv.put("unreadCount", (int) unreadCount);

                    System.out.println("      Last message: " + lastMessage.getMessageText());
                } else {
                    conv.put("lastMessage", "No messages yet");
                    conv.put("lastMessageTime", null);
                    conv.put("unreadCount", 0);

                    System.out.println("      No messages yet - but showing client");
                }

                conversations.add(conv);
            }

            System.out.println("✅ Returning " + conversations.size() + " conversations");

        } catch (Exception e) {
            System.err.println("❌ ERROR getting conversations: " + e.getMessage());
            e.printStackTrace();
        }

        return conversations;
    }

    // ========== Helper Methods ==========

    /**
     * Calculate average mood for client (customize based on your mood_logs table)
     */
    private Double calculateAverageMood(Long clientId) {
        // TODO: Implement actual mood calculation from mood_logs table
        // Example:
        // List<MoodLog> moods = moodLogRepository.findTop30ByUserIdOrderByCreatedAtDesc(clientId);
        // if (moods.isEmpty()) return null;
        // return moods.stream().mapToDouble(MoodLog::getMoodValue).average().orElse(0.0);

        return 3.5; // Placeholder
    }

    /**
     * Get latest stress level for client (customize based on your stress_assessments table)
     */
    private String getLatestStressLevel(Long clientId) {
        // TODO: Implement actual stress level lookup
        // Example:
        // StressAssessment latest = stressAssessmentRepository
        //     .findTopByUserIdOrderByCreatedAtDesc(clientId);
        // if (latest == null) return "N/A";
        // return latest.getStressLevel();

        return "Moderate"; // Placeholder
    }

    /**
     * Get formatted last session date
     */
    private String getLastSessionDate(List<TherapySession> sessions) {
        if (sessions.isEmpty()) {
            return "N/A";
        }

        // Get most recent session
        TherapySession lastSession = sessions.stream()
                .max(Comparator.comparing(TherapySession::getSessionDate))
                .orElse(null);

        if (lastSession == null) {
            return "N/A";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return lastSession.getSessionDate().format(formatter);
    }

    /**
     * Calculate average rating (customize based on your session_feedback table)
     */
    private Double calculateAverageRating(Long instructorId, LocalDateTime startDate) {
        // TODO: Implement actual rating calculation from session_feedback table
        // Example:
        // List<SessionFeedback> feedback = sessionFeedbackRepository
        //     .findByInstructorIdAndCreatedAtAfter(instructorId, startDate);
        // if (feedback.isEmpty()) return null;
        // return feedback.stream().mapToDouble(SessionFeedback::getRating).average().orElse(0.0);

        return 4.5; // Placeholder
    }

    /**
     * Calculate start date based on time range string
     */
    private LocalDateTime calculateStartDate(String timeRange) {
        if (timeRange == null) {
            return LocalDateTime.now().minusDays(30);
        }

        switch (timeRange) {
            case "Last 7 Days":
                return LocalDateTime.now().minusDays(7);
            case "Last 30 Days":
                return LocalDateTime.now().minusDays(30);
            case "Last 90 Days":
                return LocalDateTime.now().minusDays(90);
            case "This Month":
                return LocalDate.now().withDayOfMonth(1).atStartOfDay();
            case "This Year":
                return LocalDate.now().withDayOfYear(1).atStartOfDay();
            default:
                return LocalDateTime.now().minusDays(30);
        }
    }

    /**
     * Get pending session requests for instructor
     */
    public List<Map<String, Object>> getPendingSessionRequests(Long instructorId) {
        List<Map<String, Object>> requests = new ArrayList<>();

        try {
            List<SessionRequest> pendingRequests = sessionRequestRepository
                    .findByInstructorIdAndStatusOrderByCreatedAtDesc(instructorId, SessionRequest.RequestStatus.PENDING);

            for (SessionRequest request : pendingRequests) {
                Map<String, Object> requestInfo = new HashMap<>();
                requestInfo.put("id", request.getId());
                requestInfo.put("clientId", request.getClientId());
                requestInfo.put("clientName", request.getClientName());
                requestInfo.put("requestedDate", request.getRequestedDate().toString());
                requestInfo.put("reason", request.getReason());
                requestInfo.put("status", request.getStatus().toString());
                requestInfo.put("createdAt", request.getCreatedAt().toString());

                requests.add(requestInfo);
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR getting pending session requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Accept session request and create therapy session with Zoom link
     * NEW WORKFLOW: Create therapy_session → Delete session_request
     */
    public Map<String, Object> acceptSessionRequest(Long requestId, String zoomLink) {
        System.out.println("========================================");
        System.out.println("🎯 ACCEPTING SESSION REQUEST - NEW WORKFLOW");
        System.out.println("========================================");
        System.out.println("Request ID: " + requestId);

        Map<String, Object> result = new HashMap<>();

        try {
            // Step 1: Find the session request
            System.out.println("📋 Step 1: Finding session request...");
            SessionRequest request = sessionRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Session request not found"));

            System.out.println("✅ Request found:");
            System.out.println("   Client ID: " + request.getClientId());
            System.out.println("   Client Name: " + request.getClientName());
            System.out.println("   Instructor ID: " + request.getInstructorId());
            System.out.println("   Requested Date: " + request.getRequestedDate());

            // Get client name
            String clientName = request.getClientName();
            if (clientName == null || clientName.isEmpty()) {
                clientName = userRepository.findById(request.getClientId())
                    .map(User::getUsername)
                    .orElse("Client");
            }

            // Step 2: Generate Zoom link
            System.out.println("🎥 Step 2: Creating Zoom meeting...");
            String generatedZoomLink = null;

            if (zoomService != null) {
                try {
                    String meetingTopic = "Therapy Session - " + clientName;
                    int duration = 60;

                    generatedZoomLink = zoomService.createMeeting(
                        meetingTopic,
                        request.getRequestedDate(),
                        duration
                    );

                    System.out.println("✅ Zoom meeting created!");
                    System.out.println("   Join URL: " + generatedZoomLink);
                    result.put("zoomCreationMethod", "automatic");

                } catch (Exception zoomError) {
                    System.err.println("⚠️ Zoom API failed: " + zoomError.getMessage());

                    // Fallback to TEST link
                    generatedZoomLink = "https://zoom.us/j/TEST" + System.currentTimeMillis() + "?pwd=TESTPASSWORD";
                    result.put("zoomCreationMethod", "test_fallback");
                    System.out.println("⚠️ Using TEST link: " + generatedZoomLink);
                }
            } else {
                // No Zoom service - use TEST link
                generatedZoomLink = "https://zoom.us/j/TEST" + System.currentTimeMillis() + "?pwd=TESTPASSWORD";
                result.put("zoomCreationMethod", "test_no_api");
                System.out.println("⚠️ Zoom API not configured, using TEST link: " + generatedZoomLink);
            }

            // Step 3: Create TherapySession with zoom link
            System.out.println("💾 Step 3: Creating therapy session...");
            TherapySession session = new TherapySession();
            session.setClientId(request.getClientId());
            session.setClientName(clientName);
            session.setInstructorId(request.getInstructorId());
            session.setSessionDate(request.getRequestedDate());
            session.setSessionType("Therapy Session");
            session.setZoomLink(generatedZoomLink);
            session.setStatus(TherapySession.SessionStatus.SCHEDULED);
            session.setDurationMinutes(60);
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());

            TherapySession savedSession = therapySessionRepository.save(session);

            System.out.println("✅ Therapy session CREATED!");
            System.out.println("   Session ID: " + savedSession.getId());
            System.out.println("   Zoom Link: " + savedSession.getZoomLink());

            // Verify zoom link was saved
            if (savedSession.getZoomLink() == null || savedSession.getZoomLink().isEmpty()) {
                System.err.println("❌❌❌ CRITICAL: Zoom link NULL in therapy_session! ❌❌❌");
            }

            // Step 4: DELETE the session request
            System.out.println("🗑️ Step 4: Deleting session request...");
            sessionRequestRepository.deleteById(requestId);
            sessionRequestRepository.flush(); // Force immediate commit
            System.out.println("✅ Session request DELETED from session_requests table");
            System.out.println("   Request ID " + requestId + " should NO LONGER exist in database");

            // Verify deletion
            if (sessionRequestRepository.findById(requestId).isPresent()) {
                System.err.println("⚠️⚠️⚠️ WARNING: Request still exists after delete!");
            } else {
                System.out.println("✅ Verified: Request successfully deleted from database");
            }

            // Step 5: Send notification to user
            if (notificationService != null) {
                System.out.println("📧 Step 5: Sending notification to user...");
                notificationService.sendNotification(
                    request.getClientId(),
                    "SESSION_ACCEPTED",
                    "Session Accepted! 🎉",
                    "Your therapy session has been scheduled. Check your sessions tab to join.",
                    savedSession.getId()
                );
                System.out.println("✅ Notification sent");
            }

            // Return result
            result.put("success", true);
            result.put("message", "Session accepted and created successfully!");
            result.put("sessionId", savedSession.getId());
            result.put("zoomLink", generatedZoomLink);
            result.put("sessionDate", savedSession.getSessionDate().toString());
            result.put("clientName", clientName);

            System.out.println("========================================");
            System.out.println("✅ SUCCESS - NEW WORKFLOW COMPLETE");
            System.out.println("========================================");
            System.out.println("   Therapy Session ID: " + savedSession.getId());
            System.out.println("   Zoom Link: " + generatedZoomLink);
            System.out.println("   Request DELETED from session_requests");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ ERROR accepting session request");
            System.err.println("========================================");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
            System.err.println("========================================");
        }

        return result;
    }

    /**
     * Decline session request
     */
    public Map<String, Object> declineSessionRequest(Long requestId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Find the session request
            SessionRequest request = sessionRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Session request not found"));

            // Update request status to DECLINED
            request.setStatus(SessionRequest.RequestStatus.DECLINED);
            request.setUpdatedAt(LocalDateTime.now());
            sessionRequestRepository.save(request);

            // Create notification for user
            createNotificationForUser(request.getClientId(),
                "Session Declined",
                "Your session request has been declined. Please try another time or contact your instructor.",
                "SESSION_DECLINED",
                request.getId());

            result.put("success", true);
            result.put("message", "Session request declined");

        } catch (Exception e) {
            System.err.println("❌ ERROR declining session request: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Create notification for user
     */
    private void createNotificationForUser(Long userId, String title, String message, String type, Long relatedId) {
        try {
            // Note: You'll need to inject NotificationRepository and create a Notification entity
            // This is a placeholder - implement according to your Notification model
            System.out.println("📢 Creating notification for user " + userId + ": " + title);
            // TODO: Implement actual notification creation
            // Notification notification = new Notification();
            // notification.setUserId(userId);
            // notification.setTitle(title);
            // notification.setMessage(message);
            // notification.setNotificationType(type);
            // notification.setRelatedId(relatedId);
            // notification.setIsRead(false);
            // notification.setCreatedAt(LocalDateTime.now());
            // notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not create notification: " + e.getMessage());
        }
    }

    /**
     * Get upcoming accepted sessions for instructor
     */
    public List<Map<String, Object>> getUpcomingSessions(Long instructorId) {
        List<Map<String, Object>> sessions = new ArrayList<>();

        try {
            List<SessionRequest> upcomingSessions = sessionRequestRepository
                    .findUpcomingSessionsByInstructorId(instructorId, LocalDateTime.now());

            for (SessionRequest session : upcomingSessions) {
                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("id", session.getId());
                sessionInfo.put("clientId", session.getClientId());
                sessionInfo.put("clientName", session.getClientName());
                sessionInfo.put("sessionDate", session.getRequestedDate().toString());
                sessionInfo.put("zoomLink", session.getZoomLink());
                sessionInfo.put("status", session.getStatus().toString());
                sessionInfo.put("reason", session.getReason());

                sessions.add(sessionInfo);
            }

            System.out.println("✅ Found " + sessions.size() + " upcoming sessions for instructor " + instructorId);
        } catch (Exception e) {
            System.err.println("❌ ERROR getting upcoming sessions: " + e.getMessage());
            e.printStackTrace();
        }

        return sessions;
    }
}
