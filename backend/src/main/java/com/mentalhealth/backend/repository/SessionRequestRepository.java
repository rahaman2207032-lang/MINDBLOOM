package com.mentalhealth.backend.repository;


import com.mentalhealth.backend.model.SessionRequest;
import com.mentalhealth.backend.model.SessionRequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRequestRepository extends JpaRepository<SessionRequest, Long> {
    List<SessionRequest> findByInstructorIdAndStatus(Long instructorId, RequestStatus status);
    List<SessionRequest> findByClientIdAndStatus(Long clientId, RequestStatus status);
    List<SessionRequest> findByInstructorIdOrderByCreatedAtDesc(Long instructorId);
    List<SessionRequest> findByClientIdOrderByCreatedAtDesc(Long clientId);
    long countByInstructorIdAndStatus(Long instructorId, RequestStatus status);

    List<SessionRequest> findByInstructorIdAndStatusOrderByCreatedAtDesc(Long instructorId, RequestStatus status);

    // Get upcoming accepted sessions for instructor
    @Query("SELECT s FROM SessionRequest s WHERE s.instructorId = :instructorId " +
           "AND s.status = 'ACCEPTED' AND s.requestedDate >= :now ORDER BY s.requestedDate ASC")
    List<SessionRequest> findUpcomingSessionsByInstructorId(@Param("instructorId") Long instructorId,
                                                              @Param("now") LocalDateTime now);

    // Get accepted sessions for user
    @Query("SELECT s FROM SessionRequest s WHERE s.clientId = :clientId " +
           "AND s.status = 'ACCEPTED' AND s.requestedDate >= :now ORDER BY s.requestedDate ASC")
    List<SessionRequest> findUpcomingSessionsByClientId(@Param("clientId") Long clientId,
                                                         @Param("now") LocalDateTime now);
}