package com.mentalhealth.backend.controller;



import com.mentalhealth.backend.dto.ConversationSummaryDTO;
import com.mentalhealth.backend.model.Message;
import com.mentalhealth.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
// CORS handled globally in WebConfig.java
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        try {
            System.out.println("📨 Sending message from " + message.getSenderId() + " to " + message.getReceiverId());
            Message sent = messageService.sendMessage(message);
            System.out.println("✅ Message sent successfully with ID: " + sent.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(sent);
        } catch (Exception e) {
            System.err.println("❌ ERROR sending message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get available users for messaging (for current user)
     * Returns all users except the current user
     */
    @GetMapping("/available-users/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getAvailableUsers(@PathVariable Long userId) {
        try {
            List<Map<String, Object>> users = messageService.getAvailableUsers(userId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("❌ ERROR getting available users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    public ResponseEntity<List<Message>> getConversation(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        try {
            List<Message> messages = messageService.getConversation(userId1, userId2);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationSummaryDTO>> getConversations(@PathVariable Long userId) {
        try {
            System.out.println("📡 MessageController conversations endpoint called with userId: " + userId);

            if (userId == null || userId <= 0) {
                System.err.println("❌ Invalid user ID: " + userId);
                return ResponseEntity.badRequest().build();
            }

            List<ConversationSummaryDTO> conversations = messageService.getConversations(userId);
            System.out.println("✅ MessageController returning " + conversations.size() + " conversations");

            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            System.err.println("❌ ERROR in MessageController.getConversations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        try {
            messageService.markAsRead(messageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark all messages in a conversation as read
     * This is called when instructor opens a conversation
     */
    @PutMapping("/conversation/{userId1}/{userId2}/mark-read")
    public ResponseEntity<Map<String, Object>> markConversationAsRead(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        try {
            System.out.println("📖 Marking conversation as read: " + userId1 + " <-> " + userId2);

            int markedCount = messageService.markConversationAsRead(userId1, userId2);

            System.out.println("✅ Marked " + markedCount + " messages as read");

            Map<String, Object> response = new HashMap<>();
            response.put("markedCount", markedCount);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ ERROR marking conversation as read: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/instructor")
    public ResponseEntity<Map<String, Object>> getInstructorForUser(@PathVariable Long userId) {
        try {
            Long instructorId = messageService.getInstructorIdForUser(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("instructorId", instructorId);
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ ERROR getting instructor for user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/unread/count/{userId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        try {
            long count = messageService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}