package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId")
    List<Long> findConversationPartners(@Param("userId") Long userId);

    long countByReceiverIdAndReadFalse(Long receiverId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :userId AND m.readAt IS NULL")
    long countUnreadMessages(@Param("userId") Long userId);

    List<Message> findByReceiverIdAndReadAtIsNullOrderBySentAtDesc(Long receiverId);

    List<Message> findBySenderIdOrReceiverId(Long instructorId, Long instructorId1);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.read = true WHERE m.id = :messageId")
    void markAsRead(@Param("messageId") Long messageId);

    /**
     * Find messages between instructor and client, ordered by date
     */
    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :instructorId AND m.receiverId = :clientId) OR " +
            "(m.senderId = :clientId AND m.receiverId = :instructorId) " +
            "ORDER BY m.createdAt DESC")
    List<Message> findConversationByInstructorAndClient(
            @Param("instructorId") Long instructorId,
            @Param("clientId") Long clientId
    );
}
