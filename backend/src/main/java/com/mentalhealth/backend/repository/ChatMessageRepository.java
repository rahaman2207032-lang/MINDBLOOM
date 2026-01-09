package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get recent messages (last N messages)
    @Query("SELECT m FROM ChatMessage m ORDER BY m.createdAt DESC")
    List<ChatMessage> findRecentMessages(Pageable pageable);

    // Get messages after a specific time (for polling)
    List<ChatMessage> findByCreatedAtAfterOrderByCreatedAtAsc(LocalDateTime after);

    // Count messages from last 5 minutes (to determine active users)
    @Query("SELECT COUNT(DISTINCT m.user.id) FROM ChatMessage m WHERE m.createdAt > :since")
    long countDistinctUsersSince(@Param("since") LocalDateTime since);
}

