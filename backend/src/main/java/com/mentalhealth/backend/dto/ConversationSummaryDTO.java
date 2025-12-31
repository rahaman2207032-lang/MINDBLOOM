package com.mentalhealth.backend.dto;



import java.time.LocalDateTime;

public class ConversationSummaryDTO {
    private Long userId;
    private String userName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;

    // Constructors
    public ConversationSummaryDTO() {}

    public ConversationSummaryDTO(Long userId, String userName, String lastMessage,
                                  LocalDateTime lastMessageTime, Integer unreadCount) {
        this.userId = userId;
        this.userName = userName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
}