package com.mentalhealth.backend.service;



import com.mentalhealth.backend.model.Notification;
import com.mentalhealth.backend.model.SessionRequest;
import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.repository.NotificationRepository;
import com.mentalhealth.backend.repository.SessionRequestRepository;
import com.mentalhealth.backend.repository.UserRepository;
import com.mentalhealth.backend.repository.InstructorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired(required = false)
    private SessionRequestRepository sessionRequestRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private InstructorRepository instructorRepository;

    public Notification sendNotification(Long userId, String type, String title,
                                         String message, Long relatedId) {
        System.out.println("🔔 Creating notification:");
        System.out.println("   User ID: " + userId);
        System.out.println("   Type: " + type);
        System.out.println("   Title: " + title);
        System.out.println("   Message: " + message);
        System.out.println("   Related ID: " + relatedId);

        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setNotificationType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setRelatedId(relatedId);
            notification.setCreatedAt(LocalDateTime.now());

            Notification saved = notificationRepository.save(notification);
            System.out.println("✅ Notification saved with ID: " + saved.getId());

            // Send real-time notification via WebSocket
            try {
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/notifications",
                        saved
                );
                System.out.println("✅ WebSocket notification sent to user: " + userId);
            } catch (Exception e) {
                System.err.println("⚠️ WebSocket error: " + e.getMessage());
            }

            return saved;
        } catch (Exception e) {
            System.err.println("❌ ERROR creating notification: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new RuntimeException("Notification not found");
        }
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }

    /**
     * Get notification details with action data
     * For SESSION_ACCEPTED: includes zoom link, session date/time
     * For MESSAGE: includes sender ID, sender name for reply
     */
    public Map<String, Object> getNotificationDetails(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        Map<String, Object> details = new HashMap<>();
        details.put("id", notification.getId());
        details.put("userId", notification.getUserId());
        details.put("notificationType", notification.getNotificationType());
        details.put("title", notification.getTitle());
        details.put("message", notification.getMessage());
        details.put("relatedId", notification.getRelatedId());
        details.put("isRead", notification.getIsRead());
        details.put("createdAt", notification.getCreatedAt());
        details.put("readAt", notification.getReadAt());

        // Add action-specific data based on notification type
        if (notification.getNotificationType() != null) {
            switch (notification.getNotificationType().toUpperCase()) {
                case "SESSION_ACCEPTED":
                case "SESSION_DECLINED":
                    addSessionDetails(details, notification);
                    break;
                case "MESSAGE":
                case "MESSAGE_RECEIVED":
                    addMessageSenderDetails(details, notification);
                    break;
            }
        }

        return details;
    }

    /**
     * Add session details (zoom link, session date) to notification
     */
    private void addSessionDetails(Map<String, Object> details, Notification notification) {
        if (sessionRequestRepository != null && notification.getRelatedId() != null) {
            try {
                SessionRequest sessionRequest = sessionRequestRepository
                        .findById(notification.getRelatedId())
                        .orElse(null);

                if (sessionRequest != null) {
                    details.put("zoomLink", sessionRequest.getZoomLink());
                    details.put("sessionDate", sessionRequest.getRequestedDate());
                    details.put("status", sessionRequest.getStatus().toString());
                    details.put("instructorId", sessionRequest.getInstructorId());
                    details.put("canJoin", sessionRequest.getZoomLink() != null);

                    // Add instructor name if available
                    if (instructorRepository != null && sessionRequest.getInstructorId() != null) {
                        instructorRepository.findById(sessionRequest.getInstructorId())
                                .ifPresent(instructor -> details.put("instructorName", instructor.getUsername()));
                    }

                    System.out.println("✅ Added session details: zoom=" + sessionRequest.getZoomLink());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error loading session details: " + e.getMessage());
            }
        }
    }

    /**
     * Add sender details for message notifications (for reply functionality)
     */
    private void addMessageSenderDetails(Map<String, Object> details, Notification notification) {
        if (notification.getRelatedId() != null) {
            try {
                // relatedId contains sender's user ID
                Long senderId = notification.getRelatedId();
                details.put("senderId", senderId);

                // Try to get sender's name from User table first
                boolean found = false;
                if (userRepository != null) {
                    Optional<User> userOpt = userRepository.findById(senderId);
                    if (userOpt.isPresent()) {
                        User sender = userOpt.get();
                        details.put("senderName", sender.getUsername());
                        details.put("senderRole", sender.getRole().toString());
                        found = true;
                        System.out.println("✅ Found sender in User table: " + sender.getUsername());
                    }
                }

                // If not found in User table, try Instructor table
                if (!found && instructorRepository != null) {
                    instructorRepository.findById(senderId).ifPresent(sender -> {
                        details.put("senderName", sender.getUsername());
                        details.put("senderRole", "INSTRUCTOR");
                        System.out.println("✅ Found sender in Instructor table: " + sender.getUsername());
                    });
                }

                details.put("canReply", true);
                System.out.println("✅ Added sender details: senderId=" + senderId);
            } catch (Exception e) {
                System.err.println("⚠️ Error loading sender details: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Get all notifications with details for a user
     */
    public List<Map<String, Object>> getUserNotificationsWithDetails(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(notification -> {
                    try {
                        return getNotificationDetails(notification.getId());
                    } catch (Exception e) {
                        System.err.println("⚠️ Error loading details for notification " + notification.getId());
                        // Return basic notification data if details fail
                        Map<String, Object> basic = new HashMap<>();
                        basic.put("id", notification.getId());
                        basic.put("title", notification.getTitle());
                        basic.put("message", notification.getMessage());
                        basic.put("notificationType", notification.getNotificationType());
                        basic.put("isRead", notification.getIsRead());
                        basic.put("createdAt", notification.getCreatedAt());
                        return basic;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Get ONLY UNREAD notifications with details for a user
     * (Used in user dashboard to show only unread after refresh)
     */
    public List<Map<String, Object>> getUnreadNotificationsWithDetails(Long userId) {
        System.out.println("📋 Service: Fetching UNREAD notifications for user: " + userId);

        // Get only unread notifications
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        System.out.println("   Found " + notifications.size() + " unread notifications");

        return notifications.stream()
                .map(notification -> {
                    try {
                        Map<String, Object> details = getNotificationDetails(notification.getId());
                        System.out.println("   ✅ Loaded details for notification " + notification.getId() + ": " + notification.getTitle());
                        return details;
                    } catch (Exception e) {
                        System.err.println("⚠️ Error loading details for notification " + notification.getId());
                        // Return basic notification data if details fail
                        Map<String, Object> basic = new HashMap<>();
                        basic.put("id", notification.getId());
                        basic.put("title", notification.getTitle());
                        basic.put("message", notification.getMessage());
                        basic.put("notificationType", notification.getNotificationType());
                        basic.put("isRead", false); // We know it's unread
                        basic.put("createdAt", notification.getCreatedAt());
                        return basic;
                    }
                })
                .collect(Collectors.toList());
    }
}


