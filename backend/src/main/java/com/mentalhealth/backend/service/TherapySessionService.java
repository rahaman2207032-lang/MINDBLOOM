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
import java.util.stream.Collectors;

@Service
public class TherapySessionService {

    @Autowired
    private TherapySessionRepository therapySessionRepository;

    @Autowired(required = false)
    private ZoomService zoomService;


    public TherapySession createSession(TherapySession session) {
        session.setCreatedAt(LocalDateTime.now());
        session.setStatus(SessionStatus.SCHEDULED); // default status
        return therapySessionRepository.save(session);
    }


    public List<TherapySession> getWeeklySessionsForInstructor(Long instructorId) {
        LocalDateTime startOfWeek = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        return therapySessionRepository.findByInstructorIdAndDateRange(
                instructorId, startOfWeek, endOfWeek);
    }


    public List<TherapySession> getTodaySessionsForInstructor(Long instructorId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return therapySessionRepository.findTodaySessionsByInstructor(
                instructorId, startOfDay, endOfDay);
    }


    public List<TherapySession> getAllSessionsForInstructor(Long instructorId) {
        return therapySessionRepository
                .findByInstructorIdOrderBySessionDateDesc(instructorId);
    }


    public List<TherapySession> getClientSessions(Long clientId) {
        return therapySessionRepository
                .findByClientIdOrderBySessionDateDesc(clientId);
    }


    public TherapySession updateSessionStatus(Long sessionId, SessionStatus status) {

        TherapySession session = therapySessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));


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


    public TherapySession updateSessionStatus(Long sessionId, String statusString) {
        SessionStatus status = SessionStatus.valueOf(statusString.toUpperCase());
        return updateSessionStatus(sessionId, status);
    }


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


    public long getCompletedSessionCount(Long instructorId) {
        return therapySessionRepository
                .countByInstructorIdAndStatus(instructorId, SessionStatus.COMPLETED);
    }


    public List<TherapySession> getScheduledSessionsForUser(Long userId) {
        System.out.println("📋 Service: Fetching SCHEDULED sessions for user: " + userId);
        List<TherapySession> sessions = therapySessionRepository.findByClientIdAndStatus(userId, SessionStatus.SCHEDULED);
        System.out.println("   Found " + sessions.size() + " scheduled sessions");


        LocalDateTime now = LocalDateTime.now();
        List<TherapySession> upcomingSessions = sessions.stream()
                .filter(session -> {

                    LocalDateTime sessionDateTime = session.getSessionDate();
                    if (sessionDateTime == null) {
                        System.out.println("    Session " + session.getId() + " has null date, excluding");
                        return false;
                    }

                    boolean isUpcoming = sessionDateTime.isAfter(now);
                    if (!isUpcoming) {
                        System.out.println("    Session " + session.getId() + " expired (" + sessionDateTime + "), excluding");
                    }
                    return isUpcoming;
                })
                .collect(Collectors.toList());

        System.out.println("    Returning " + upcomingSessions.size() + " upcoming (non-expired) sessions");

        for (TherapySession session : upcomingSessions) {
            System.out.println("   📌 Session ID: " + session.getId() + ", Date: " + session.getSessionDate() + ", Zoom: " + session.getZoomLink());
        }

        return upcomingSessions;
    }


    public List<TherapySession> getScheduledSessionsForInstructor(Long instructorId) {
        System.out.println("📋 Service: Fetching SCHEDULED sessions for instructor: " + instructorId);
        List<TherapySession> sessions = therapySessionRepository.findByInstructorIdAndStatus(instructorId, SessionStatus.SCHEDULED);
        System.out.println("   Found " + sessions.size() + " scheduled sessions");


        LocalDateTime now = LocalDateTime.now();
        List<TherapySession> upcomingSessions = sessions.stream()
                .filter(session -> {

                    LocalDateTime sessionDateTime = session.getSessionDate();
                    if (sessionDateTime == null) {
                        System.out.println("   ⚠️ Session " + session.getId() + " has null date, excluding");
                        return false;
                    }

                    boolean isUpcoming = sessionDateTime.isAfter(now);
                    if (!isUpcoming) {
                        System.out.println("   ⏰ Session " + session.getId() + " expired (" + sessionDateTime + "), excluding");
                    }
                    return isUpcoming;
                })
                .collect(Collectors.toList());

        System.out.println("    Returning " + upcomingSessions.size() + " upcoming (non-expired) sessions");

        for (TherapySession session : upcomingSessions) {
            System.out.println("    Session ID: " + session.getId() + ", Client: " + session.getClientName() + ", Date: " + session.getSessionDate() + ", Zoom: " + session.getZoomLink());
        }

        return upcomingSessions;
    }
}
