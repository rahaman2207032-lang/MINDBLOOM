package com.mentalhealth.backend.service;

import com.mentalhealth.backend.model.TherapySession;
import com.mentalhealth.backend.model.TherapySession.SessionStatus;
import com.mentalhealth.backend.repository.TherapySessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class TherapySessionService {

    @Autowired
    private TherapySessionRepository therapySessionRepository;

    @Autowired(required = false)
    private ZoomService zoomService;

    /**
     * Create a therapy session request
     * (NO Zoom meeting created here)
     */
    public TherapySession createSession(TherapySession session) {
        session.setCreatedAt(LocalDateTime.now());
        session.setStatus(SessionStatus.SCHEDULED); // default status
        return therapySessionRepository.save(session);
    }

    /**
     * Get weekly sessions for instructor
     */
    public List<TherapySession> getWeeklySessionsForInstructor(Long instructorId) {
        LocalDateTime startOfWeek = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        return therapySessionRepository.findByInstructorIdAndDateRange(
                instructorId, startOfWeek, endOfWeek);
    }

    /**
     * Get today's sessions for instructor
     */
    public List<TherapySession> getTodaySessionsForInstructor(Long instructorId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return therapySessionRepository.findTodaySessionsByInstructor(
                instructorId, startOfDay, endOfDay);
    }

    /**
     * Get all sessions for instructor
     */
    public List<TherapySession> getAllSessionsForInstructor(Long instructorId) {
        return therapySessionRepository
                .findByInstructorIdOrderBySessionDateDesc(instructorId);
    }

    /**
     * Get all sessions for a client
     */
    public List<TherapySession> getClientSessions(Long clientId) {
        return therapySessionRepository
                .findByClientIdOrderBySessionDateDesc(clientId);
    }

    /**
     * Update session status
     * Zoom meeting is created ONLY when session is CONFIRMED
     */
    public TherapySession updateSessionStatus(Long sessionId, SessionStatus status) {

        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Create Zoom meeting only once when confirmed
        if (status == SessionStatus.SCHEDULED && session.getZoomLink() == null) {

            String zoomLink = zoomService.createMeeting(
                    "Therapy Session",
                    session.getSessionDate(), // must exist in entity
                    60                          // duration in minutes
            );

            session.setZoomLink(zoomLink);
        }

        session.setStatus(status);
        session.setUpdatedAt(LocalDateTime.now());

        return therapySessionRepository.save(session);
    }

    /**
     * Update session status from string (for backward compatibility)
     */
    public TherapySession updateSessionStatus(Long sessionId, String statusString) {
        SessionStatus status = SessionStatus.valueOf(statusString.toUpperCase());
        return updateSessionStatus(sessionId, status);
    }

    /**
     * Rate a completed session
     */
    public TherapySession rateSession(Long sessionId, int rating) {

        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        session.setRating(rating);
        session.setUpdatedAt(LocalDateTime.now());

        return therapySessionRepository.save(session);
    }

    /**
     * Get completed session count for instructor
     */
    public long getCompletedSessionCount(Long instructorId) {
        return therapySessionRepository
                .countByInstructorIdAndStatus(instructorId, SessionStatus.COMPLETED);
    }

    /**
     * NEW: Get scheduled sessions for user (with zoom links)
     */
    public List<TherapySession> getScheduledSessionsForUser(Long userId) {
        System.out.println("📋 Service: Fetching SCHEDULED sessions for user: " + userId);
        List<TherapySession> sessions = therapySessionRepository.findByClientIdAndStatus(userId, SessionStatus.SCHEDULED);
        System.out.println("   Found " + sessions.size() + " scheduled sessions");

        for (TherapySession session : sessions) {
            System.out.println("   📌 Session ID: " + session.getId() + ", Zoom: " + session.getZoomLink());
        }

        return sessions;
    }

    /**
     * NEW: Get scheduled sessions for instructor (with zoom links)
     */
    public List<TherapySession> getScheduledSessionsForInstructor(Long instructorId) {
        System.out.println("📋 Service: Fetching SCHEDULED sessions for instructor: " + instructorId);
        List<TherapySession> sessions = therapySessionRepository.findByInstructorIdAndStatus(instructorId, SessionStatus.SCHEDULED);
        System.out.println("   Found " + sessions.size() + " scheduled sessions");

        for (TherapySession session : sessions) {
            System.out.println("   📌 Session ID: " + session.getId() + ", Client: " + session.getClientName() + ", Zoom: " + session.getZoomLink());
        }

        return sessions;
    }
}
