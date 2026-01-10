package com.mentalhealth.backend.service;

import com.mentalhealth.backend.dto.ChatMessageRequest;
import com.mentalhealth.backend.dto.ChatMessageResponse;
import com.mentalhealth.backend.model.ChatMessage;
import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.repository.ChatMessageRepository;
import com.mentalhealth.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;


    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        System.out.println(" Sending chat message from user: " + request.getUserId());
        System.out.println("   Message: " + request.getMessage());
        System.out.println("   Anonymous: " + request.isAnonymous());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        ChatMessage message = new ChatMessage(
            user,
            request.getMessage(),
            request.isAnonymous()
        );

        ChatMessage savedMessage = chatMessageRepository.save(message);
        System.out.println(" Chat message saved with ID: " + savedMessage.getId());

        return new ChatMessageResponse(savedMessage);
    }


    public List<ChatMessageResponse> getRecentMessages(int limit) {
        System.out.println(" Fetching recent " + limit + " chat messages");

        PageRequest pageRequest = PageRequest.of(0, limit);
        List<ChatMessage> messages = chatMessageRepository.findRecentMessages(pageRequest);

        System.out.println(" Found " + messages.size() + " messages");


        return messages.stream()
            .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
            .map(ChatMessageResponse::new)
            .collect(Collectors.toList());
    }


    public int getOnlineUsersCount() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        int count = (int) chatMessageRepository.countDistinctUsersSince(fiveMinutesAgo);
        System.out.println("👥 Online users count: " + count);
        return count;
    }


    public List<ChatMessageResponse> getMessagesAfter(LocalDateTime after) {
        System.out.println(" Fetching messages after: " + after);

        List<ChatMessage> messages = chatMessageRepository.findByCreatedAtAfterOrderByCreatedAtAsc(after);

        System.out.println(" Found " + messages.size() + " new messages");

        return messages.stream()
            .map(ChatMessageResponse::new)
            .collect(Collectors.toList());
    }
}

