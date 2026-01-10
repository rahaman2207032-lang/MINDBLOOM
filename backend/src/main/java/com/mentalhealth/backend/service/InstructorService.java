package com.mentalhealth.backend.service;

import com.mentalhealth.backend.model.Instructor;
import com.mentalhealth.backend.model.Message;
import com.mentalhealth.backend.model.MoodLog;
import com.mentalhealth.backend.model.Notification;
import com.mentalhealth.backend.model.SessionRequest;
import com.mentalhealth.backend.model.StressAssessment;
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

    @Autowired(required = false)
    private NotificationRepository notificationRepository;


    public List<Instructor> getAllInstructorsForSelection() {
        if (instructorRepository != null) {
            return instructorRepository.findAll();
        }
        return new ArrayList<>();
    }


    public Map<String, Integer> getDashboardStats(Long instructorId) {
        Map<String, Integer> stats = new HashMap<>();

        try {

            int pendingRequests = (int) sessionRequestRepository
                    .findByInstructorIdAndStatusOrderByCreatedAtDesc(instructorId, SessionRequest.RequestStatus.PENDING)
                    .size();


            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            List<TherapySession> todaySessions = therapySessionRepository
                    .findByInstructorIdAndSessionDateBetween(instructorId, startOfDay, endOfDay);


            List<TherapySession> allSessions = therapySessionRepository
                    .findByInstructorId(instructorId);
            Set<Long> uniqueClients = allSessions.stream()
                    .map(TherapySession::getClientId)
                    .collect(Collectors.toSet());


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


    public List<Map<String, Object>> getAllClients(Long instructorId) {
        List<Map<String, Object>> clients = new ArrayList<>();

        try {

            List<User> allUsers = userRepository.findByRole(UserRole.USER);

            for (User user : allUsers) {
                Map<String, Object> clientOverview = new HashMap<>();
                clientOverview.put("clientId", user.getId());
                clientOverview.put("clientName", user.getUsername());


                Double avgMood = calculateAverageMood(user.getId());
                clientOverview.put("averageMood", avgMood);


                String stressLevel = getLatestStressLevel(user.getId());
                clientOverview.put("stressLevel", stressLevel);


                List<TherapySession> clientSessions = therapySessionRepository
                        .findByClientIdOrderBySessionDateDesc(user.getId());
                clientOverview.put("totalSessions", clientSessions.size());


                String lastSession = null;
                if (!clientSessions.isEmpty()) {
                    lastSession = clientSessions.get(0).getSessionDate()
                            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                }
                clientOverview.put("lastSessionDate", lastSession);


                clientOverview.put("consentGranted", true);

                clients.add(clientOverview);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clients;
    }


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


    public Map<String, Object> getAnalytics(Long instructorId, String timeRange) {
        Map<String, Object> analytics = new HashMap<>();

        try {
            LocalDateTime startDate = calculateStartDate(timeRange);


            List<TherapySession> sessions = therapySessionRepository
                    .findByInstructorIdAndSessionDateAfter(instructorId, startDate);


            analytics.put("totalSessions", sessions.size());


            long completedSessions = sessions.stream()
                    .filter(s -> s.getStatus() == TherapySession.SessionStatus.COMPLETED)
                    .count();
            analytics.put("completedSessions", (int) completedSessions);


            Double avgRating = calculateAverageRating(instructorId, startDate);
            analytics.put("avgRating", avgRating);

        } catch (Exception e) {
            analytics.put("totalSessions", 0);
            analytics.put("completedSessions", 0);
            analytics.put("avgRating", 0.0);
        }

        return analytics;
    }


    public List<Map<String, Object>> getConversations(Long instructorId) {
        System.out.println(" Getting conversations for instructor ID: " + instructorId);
        List<Map<String, Object>> conversations = new ArrayList<>();

        try {
            // Get ALL users with role=USER (all potential clients)
            List<User> allClients = userRepository.findByRole(UserRole.USER);
            System.out.println(" Found " + allClients.size() + " users with role=USER");

            if (allClients.isEmpty()) {
                System.out.println(" WARNING: No users found with role=USER!");
                System.out.println("   Check your users table - make sure users have role='USER'");
            }

            for (User client : allClients) {
                Map<String, Object> conv = new HashMap<>();
                conv.put("clientId", client.getId());
                conv.put("clientName", client.getUsername());

                System.out.println("   Processing client: " + client.getUsername() + " (ID: " + client.getId() + ")");


                List<Message> conversationMessages = messageRepository
                        .findConversationByInstructorAndClient(instructorId, client.getId());


                if (!conversationMessages.isEmpty()) {
                    Message lastMessage = conversationMessages.get(0);
                    conv.put("lastMessage", lastMessage.getMessageText());
                    conv.put("lastMessageTime", lastMessage.getSentAt().toString());


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

            System.out.println(" Returning " + conversations.size() + " conversations");

        } catch (Exception e) {
            System.err.println(" ERROR getting conversations: " + e.getMessage());
            e.printStackTrace();
        }

        return conversations;
    }


    private Double calculateAverageMood(Long clientId) {
        try {
            if (moodLogRepository == null) {
                return null;
            }


            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<MoodLog> recentMoods = moodLogRepository
                    .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(clientId, thirtyDaysAgo);

            if (recentMoods.isEmpty()) {
                return null;
            }


            return recentMoods.stream()
                    .mapToInt(MoodLog::getMoodRating)
                    .average()
                    .orElse(0.0);

        } catch (Exception e) {
            System.err.println("️ Error calculating average mood for client " + clientId + ": " + e.getMessage());
            return null;
        }
    }

    private String getLatestStressLevel(Long clientId) {
        try {
            if (stressAssessmentRepository == null) {
                return "N/A";
            }

            // Get most recent stress assessment
            List<StressAssessment> latestAssessments = stressAssessmentRepository
                    .findTop1ByUserIdOrderByCreatedAtDesc(clientId);

            if (latestAssessments.isEmpty()) {
                return "N/A";
            }

            StressAssessment latest = latestAssessments.get(0);
            return latest.getStressLevel(); // Returns "Low", "Moderate", "High", etc.

        } catch (Exception e) {
            System.err.println("️ Error getting stress level for client " + clientId + ": " + e.getMessage());
            return "N/A";
        }
    }


    private String getLastSessionDate(List<TherapySession> sessions) {
        if (sessions.isEmpty()) {
            return "N/A";
        }


        TherapySession lastSession = sessions.stream()
                .max(Comparator.comparing(TherapySession::getSessionDate))
                .orElse(null);

        if (lastSession == null) {
            return "N/A";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return lastSession.getSessionDate().format(formatter);
    }


    private Double calculateAverageRating(Long instructorId, LocalDateTime startDate) {
        try {

            List<TherapySession> sessions = therapySessionRepository
                    .findByInstructorIdAndSessionDateAfter(instructorId, startDate);

            if (sessions.isEmpty()) {
                return null;
            }


            List<Integer> ratings = sessions.stream()
                    .map(TherapySession::getRating)
                    .filter(rating -> rating != null && rating > 0)
                    .collect(Collectors.toList());

            if (ratings.isEmpty()) {
                return null;
            }

            return ratings.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

        } catch (Exception e) {
            System.err.println("️ Error calculating average rating for instructor " + instructorId + ": " + e.getMessage());
            return null;
        }
    }


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
            System.err.println(" ERROR getting pending session requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }


    public Map<String, Object> acceptSessionRequest(Long requestId, String zoomLink) {
        System.out.println("========================================");
        System.out.println(" ACCEPTING SESSION REQUEST - NEW WORKFLOW");
        System.out.println("========================================");
        System.out.println("Request ID: " + requestId);

        Map<String, Object> result = new HashMap<>();

        try {

            System.out.println(" Step 1: Finding session request...");
            SessionRequest request = sessionRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Session request not found"));

            System.out.println(" Request found:");
            System.out.println("   Client ID: " + request.getClientId());
            System.out.println("   Client Name: " + request.getClientName());
            System.out.println("   Instructor ID: " + request.getInstructorId());
            System.out.println("   Requested Date: " + request.getRequestedDate());


            String clientName = request.getClientName();
            if (clientName == null || clientName.isEmpty()) {
                clientName = userRepository.findById(request.getClientId())
                    .map(User::getUsername)
                    .orElse("Client");
            }


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

                    System.out.println(" Zoom meeting created!");
                    System.out.println("   Join URL: " + generatedZoomLink);
                    result.put("zoomCreationMethod", "automatic");

                } catch (Exception zoomError) {
                    System.err.println(" Zoom API failed: " + zoomError.getMessage());


                    generatedZoomLink = "https://zoom.us/j/TEST" + System.currentTimeMillis() + "?pwd=TESTPASSWORD";
                    result.put("zoomCreationMethod", "test_fallback");
                    System.out.println(" Using TEST link: " + generatedZoomLink);
                }
            } else {

                generatedZoomLink = "https://zoom.us/j/TEST" + System.currentTimeMillis() + "?pwd=TESTPASSWORD";
                result.put("zoomCreationMethod", "test_no_api");
                System.out.println(" Zoom API not configured, using TEST link: " + generatedZoomLink);
            }


            System.out.println(" Step 3: Creating therapy session...");
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

            System.out.println(" Therapy session CREATED!");
            System.out.println("   Session ID: " + savedSession.getId());
            System.out.println("   Zoom Link: " + savedSession.getZoomLink());


            if (savedSession.getZoomLink() == null || savedSession.getZoomLink().isEmpty()) {
                System.err.println(" CRITICAL: Zoom link NULL in therapy_session!");
            }


            System.out.println("🗑️ Step 4: Deleting session request...");
            sessionRequestRepository.deleteById(requestId);
            sessionRequestRepository.flush(); // Force immediate commit
            System.out.println(" Session request DELETED from session_requests table");
            System.out.println("   Request ID " + requestId + " should NO LONGER exist in database");


            if (sessionRequestRepository.findById(requestId).isPresent()) {
                System.err.println(" WARNING: Request still exists after delete!");
            } else {
                System.out.println(" Verified: Request successfully deleted from database");
            }


            if (notificationService != null) {
                System.out.println(" Step 5: Sending notification to user...");
                notificationService.sendNotification(
                    request.getClientId(),
                    "SESSION_ACCEPTED",
                    "Session Accepted! 🎉",
                    "Your therapy session has been scheduled. Check your sessions tab to join.",
                    savedSession.getId()
                );
                System.out.println(" Notification sent");
            }

            // Return result
            result.put("success", true);
            result.put("message", "Session accepted and created successfully!");
            result.put("sessionId", savedSession.getId());
            result.put("zoomLink", generatedZoomLink);
            result.put("sessionDate", savedSession.getSessionDate().toString());
            result.put("clientName", clientName);

            System.out.println("========================================");
            System.out.println(" SUCCESS - NEW WORKFLOW COMPLETE");
            System.out.println("========================================");
            System.out.println("   Therapy Session ID: " + savedSession.getId());
            System.out.println("   Zoom Link: " + generatedZoomLink);
            System.out.println("   Request DELETED from session_requests");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println(" ERROR accepting session request");
            System.err.println("========================================");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
            System.err.println("========================================");
        }

        return result;
    }


    public Map<String, Object> declineSessionRequest(Long requestId) {
        Map<String, Object> result = new HashMap<>();

        try {

            SessionRequest request = sessionRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Session request not found"));


            request.setStatus(SessionRequest.RequestStatus.DECLINED);
            request.setUpdatedAt(LocalDateTime.now());
            sessionRequestRepository.save(request);


            createNotificationForUser(request.getClientId(),
                "Session Declined",
                "Your session request has been declined. Please try another time or contact your instructor.",
                "SESSION_DECLINED",
                request.getId());

            result.put("success", true);
            result.put("message", "Session request declined");

        } catch (Exception e) {
            System.err.println(" ERROR declining session request: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }


    private void createNotificationForUser(Long userId, String title, String message, String type, Long relatedId) {
        try {
            System.out.println(" Creating notification for user " + userId);
            System.out.println("   Title: " + title);
            System.out.println("   Message: " + message);
            System.out.println("   Type: " + type);
            System.out.println("   Related ID: " + relatedId);

            if (notificationRepository == null) {
                System.err.println("⚠️ WARNING: NotificationRepository is null - cannot create notification");
                return;
            }


            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(type);
            notification.setRelatedId(relatedId);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());

            // Save to database
            Notification savedNotification = notificationRepository.save(notification);

            System.out.println(" Notification created successfully!");
            System.out.println("   Notification ID: " + savedNotification.getId());
            System.out.println("   User will see this in their notification dashboard");

        } catch (Exception e) {
            System.err.println(" ERROR creating notification: " + e.getMessage());
            e.printStackTrace();
        }
    }


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

            System.out.println(" Found " + sessions.size() + " upcoming sessions for instructor " + instructorId);
        } catch (Exception e) {
            System.err.println(" ERROR getting upcoming sessions: " + e.getMessage());
            e.printStackTrace();
        }

        return sessions;
    }
}
