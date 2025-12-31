package com.mentalhealth.backend.repository;


import com.mentalhealth.backend.model.TherapySession;
import com.mentalhealth.backend.model.TherapySession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TherapySessionRepository extends JpaRepository<TherapySession, Long> {
    List<TherapySession> findByInstructorIdOrderBySessionDateDesc(Long instructorId);
    List<TherapySession> findByClientIdOrderBySessionDateDesc(Long clientId);

    @Query("SELECT t FROM TherapySession t WHERE t.instructorId = :instructorId AND t.sessionDate >= :startDate AND t.sessionDate < :endDate ORDER BY t.sessionDate")
    List<TherapySession> findByInstructorIdAndDateRange(@Param("instructorId") Long instructorId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM TherapySession t WHERE t.instructorId = :instructorId " +
            "AND t.sessionDate >= :startOfDay AND t.sessionDate < :endOfDay " +
            "ORDER BY t.sessionDate ASC")
    List<TherapySession> findTodaySessionsByInstructor(
            @Param("instructorId") Long instructorId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    long countByInstructorIdAndStatus(Long instructorId, SessionStatus status);

    List<TherapySession> findByInstructorIdAndSessionDateBetween(Long instructorId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<TherapySession> findByInstructorId(Long instructorId);
    List<TherapySession> findByInstructorIdAndSessionDateAfter(Long instructorId, LocalDateTime startDate);


    /**
     * Count sessions for instructor and client
     */
    int countByInstructorIdAndClientId(Long instructorId, Long clientId);

    /**
     * Get weekly sessions for instructor
     * Usage: findWeeklySessions(instructorId, startOfWeek, endOfWeek)
     */
    @Query("SELECT t FROM TherapySession t WHERE t.instructorId = :instructorId " +
            "AND t.sessionDate >= :startOfWeek AND t.sessionDate < :endOfWeek " +
            "ORDER BY t.sessionDate ASC")
    List<TherapySession> findWeeklySessions(
            @Param("instructorId") Long instructorId,
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek
    );


    /**
     * Count distinct clients for instructor (if you need this)
     * Note: This can also be done in the service layer
     */
    @Query("SELECT COUNT(DISTINCT t.clientId) FROM TherapySession t WHERE t.instructorId = :instructorId")
    long countDistinctClientsByInstructorId(@Param("instructorId") Long instructorId);

    /**
     * Find instructor ID for a user/client (for reply functionality)
     * Returns the most recent instructor who had a session with this user
     */
    @Query("SELECT t.instructorId FROM TherapySession t WHERE t.clientId = :userId ORDER BY t.sessionDate DESC LIMIT 1")
    Long findInstructorIdByUserId(@Param("userId") Long userId);
}