package com.mentalhealth.backend.service;

import com.mentalhealth.backend.dto.ConversationSummaryDTO;
import com.mentalhealth.backend.model.Message;
import com.mentalhealth.backend.repository.MessageRepository;
import com.mentalhealth.backend.repository.TherapySessionRepository;
import com.mentalhealth.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TherapySessionRepository therapySessionRepository;

    @Autowired(required = false)
    private com.mentalhealth.backend.repository.InstructorRepository instructorRepository;

    @Transactional
    public Message sendMessage(Message message) {
        System.out.println("💬 Sending message:");
        System.out.println("   From (Sender ID): " + message.getSenderId());
        System.out.println("   To (Receiver ID): " + message.getReceiverId());
        System.out.println("   Message: " + message.getMessageText());

        message.setSentAt(LocalDateTime.now());

        // Get sender name - check both User and Instructor tables
        String senderName = getUserOrInstructorName(message.getSenderId());
        if (senderName != null) {
            message.setSenderName(senderName);
            System.out.println("   Sender Name: " + senderName);
        }

        // Get receiver name - check both User and Instructor tables
        String receiverName = getUserOrInstructorName(message.getReceiverId());
        if (receiverName != null) {
            message.setReceiverName(receiverName);
            System.out.println("   Receiver Name: " + receiverName);
        }

        Message saved = messageRepository.save(message);
        System.out.println("✅ Message saved with ID: " + saved.getId());

        // Send real-time message via WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverId().toString(),
                    "/queue/messages",
                    saved
            );
            System.out.println("✅ WebSocket notification sent");
        } catch (Exception e) {
            System.err.println("⚠️ WebSocket error: " + e.getMessage());
        }

        // Create notification for receiver with sender ID in relatedEntityId
        try {
            notificationService.sendNotification(
                    message.getReceiverId(),
                    "MESSAGE",
                    "New Message from " + message.getSenderName() + " 💬",
                    message.getMessageText(),
                    message.getSenderId() // Store sender ID for reply functionality
            );
            System.out.println("✅ Notification created for user: " + message.getReceiverId());
        } catch (Exception e) {
            System.err.println("❌ ERROR creating notification: " + e.getMessage());
            e.printStackTrace();
        }

        return saved;
    }

    /**
     * Get username from either User or Instructor table
     */
    private String getUserOrInstructorName(Long userId) {
        // Try User table first
        Optional<com.mentalhealth.backend.model.User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get().getUsername();
        }

        // Try Instructor table
        if (instructorRepository != null) {
            Optional<com.mentalhealth.backend.model.Instructor> instructor = instructorRepository.findById(userId);
            if (instructor.isPresent()) {
                return instructor.get().getUsername();
            }
        }

        return "Unknown User";
    }

    public List<Message> getConversation(Long userId1, Long userId2) {
        return messageRepository.findConversation(userId1, userId2);
    }

    public List<ConversationSummaryDTO> getConversations(Long userId) {
        List<Message> allMessages = messageRepository.findAll();

        // Group messages by conversation partner
        Map<Long, List<Message>> conversations = new HashMap<>();

        for (Message msg : allMessages) {
            if (msg.getSenderId().equals(userId)) {
                conversations.computeIfAbsent(msg.getReceiverId(), k -> new ArrayList<>()).add(msg);
            } else if (msg.getReceiverId().equals(userId)) {
                conversations.computeIfAbsent(msg.getSenderId(), k -> new ArrayList<>()).add(msg);
            }
        }

        // Create summary for each conversation
        List<ConversationSummaryDTO> summaries = new ArrayList<>();

        for (Map.Entry<Long, List<Message>> entry : conversations.entrySet()) {
            Long partnerId = entry.getKey();
            List<Message> msgs = entry.getValue();

            // Get last message
            Message lastMsg = msgs.stream()
                    .max(Comparator.comparing(Message::getSentAt))
                    .orElse(null);

            if (lastMsg != null) {
                ConversationSummaryDTO summary = new ConversationSummaryDTO();
                summary.setUserId(partnerId);

                // Get partner name
                userRepository.findById(partnerId).ifPresent(user -> {
                    summary.setUserName(user.getUsername());
                });

                summary.setLastMessage(lastMsg.getMessageText());
                summary.setLastMessageTime(lastMsg.getSentAt());

                // Count unread messages
                long unread = msgs.stream()
                        .filter(m -> m.getReceiverId().equals(userId) && m.getReadAt() == null)
                        .count();
                summary.setUnreadCount((int) unread);

                summaries.add(summary);
            }
        }

        // Sort by last message time
        summaries.sort(Comparator.comparing(ConversationSummaryDTO::getLastMessageTime).reversed());

        return summaries;
    }

    @Transactional
    public void markAsRead(Long messageId) {
        messageRepository.markAsRead(messageId);
    }

    /**
     * Mark all messages in a conversation as read
     * Called when instructor opens a conversation
     */
    @Transactional
    public int markConversationAsRead(Long receiverId, Long senderId) {
        System.out.println("📖 Marking messages as read:");
        System.out.println("   Receiver (who is reading): " + receiverId);
        System.out.println("   Sender (messages from): " + senderId);

        // Get all unread messages in this conversation
        List<Message> messages = messageRepository.findConversation(receiverId, senderId);

        int markedCount = 0;
        for (Message message : messages) {
            // Mark as read if: message was sent TO receiverId and is unread
            if (message.getReceiverId().equals(receiverId) && message.getReadAt() == null) {
                message.setReadAt(LocalDateTime.now());
                messageRepository.save(message);
                markedCount++;
                System.out.println("   ✅ Marked message " + message.getId() + " as read");
            }
        }

        System.out.println("✅ Total marked as read: " + markedCount);
        return markedCount;
    }

    public long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    /**
     * Get instructor ID for a user (for reply functionality)
     * Finds the most recent instructor who had a session with this user
     */
    public Long getInstructorIdForUser(Long userId) {
        return therapySessionRepository.findInstructorIdByUserId(userId);
    }

    /**
     * Get available users for messaging
     * Returns all users except the current user
     */
    public List<Map<String, Object>> getAvailableUsers(Long currentUserId) {
        List<Map<String, Object>> availableUsers = new ArrayList<>();

        // Get all users
        List<com.mentalhealth.backend.model.User> allUsers = userRepository.findAll();

        for (com.mentalhealth.backend.model.User user : allUsers) {
            // Skip the current user
            if (!user.getId().equals(currentUserId)) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("role", user.getRole().toString());

                availableUsers.add(userInfo);
            }
        }

        return availableUsers;
    }
}