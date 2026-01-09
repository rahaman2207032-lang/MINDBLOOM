package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.dto.ChatMessageRequest;
import com.mentalhealth.backend.dto.ChatMessageResponse;
import com.mentalhealth.backend.dto.OnlineUsersResponse;
import com.mentalhealth.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/forum/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * Send a chat message
     * POST /api/forum/chat/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        try {
            System.out.println("📨 POST /api/forum/chat/messages - Sending message");
            ChatMessageResponse response = chatService.sendMessage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get recent chat messages
     * GET /api/forum/chat/messages?limit=100
     */
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            System.out.println("📥 GET /api/forum/chat/messages - Fetching messages (limit: " + limit + ")");
            List<ChatMessageResponse> messages = chatService.getRecentMessages(limit);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("❌ Error fetching messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get count of online users
     * GET /api/forum/chat/online-count
     */
    @GetMapping("/online-count")
    public ResponseEntity<OnlineUsersResponse> getOnlineCount() {
        try {
            System.out.println("👥 GET /api/forum/chat/online-count - Fetching online users");
            int count = chatService.getOnlineUsersCount();
            return ResponseEntity.ok(new OnlineUsersResponse(count));
        } catch (Exception e) {
            System.err.println("❌ Error fetching online count: " + e.getMessage());
            return ResponseEntity.ok(new OnlineUsersResponse(0));
        }
    }

    /**
     * Get messages after a specific timestamp (for polling)
     * GET /api/forum/chat/messages/after?timestamp=2024-01-01T10:00:00
     */
    @GetMapping("/messages/after")
    public ResponseEntity<List<ChatMessageResponse>> getMessagesAfter(
            @RequestParam String timestamp) {
        try {
            System.out.println("📥 GET /api/forum/chat/messages/after - Timestamp: " + timestamp);
            LocalDateTime after = LocalDateTime.parse(timestamp);
            List<ChatMessageResponse> messages = chatService.getMessagesAfter(after);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            System.err.println("❌ Error fetching messages after timestamp: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
