package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.model.TherapySession;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ NEW: Service for fetching therapy sessions (NEW WORKFLOW)
 * - Fetches from therapy_sessions table (not session_requests)
 * - Used after instructor accepts session request
 * - Zoom links are ALWAYS present in this table
 */
public class TherapySessionService {
    private static final String BASE_URL = "http://localhost:8080/api/therapy-sessions";
    private final Gson gson;

    public TherapySessionService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    /**
     * ✅ NEW: Get scheduled sessions for a user (NEW WORKFLOW)
     * Endpoint: GET /api/therapy-sessions/user/{userId}/scheduled
     * Returns sessions from therapy_sessions table with zoom links
     */
    public List<TherapySession> getScheduledSessionsForUser(Long userId) throws Exception {
        String endpoint = BASE_URL + "/user/" + userId + "/scheduled";
        System.out.println("📅 [TherapySessionService] Fetching scheduled sessions for user: " + userId);
        System.out.println("   Endpoint: " + endpoint);

        URI uri = new URI(endpoint);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("📡 [TherapySessionService] Response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseBody = response.toString();
                System.out.println("📄 [TherapySessionService] Response: " +
                        (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));

                Type listType = new TypeToken<ArrayList<TherapySession>>(){}.getType();
                List<TherapySession> sessions = gson.fromJson(responseBody, listType);
                System.out.println("✅ [TherapySessionService] Loaded " + sessions.size() + " scheduled sessions");

                // Log zoom link status for each session
                for (TherapySession session : sessions) {
                    String zoomStatus = (session.getZoomLink() != null && !session.getZoomLink().isEmpty())
                            ? "✅ Zoom link: " + session.getZoomLink()
                            : "⚠️ No zoom link (should not happen with new workflow)";
                    System.out.println("   Session ID: " + session.getId() + " - " + zoomStatus);
                }

                return sessions;
            }
        } else {
            System.err.println("❌ [TherapySessionService] Failed to fetch sessions. Response code: " + responseCode);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
                System.err.println("   Error response: " + errorResponse.toString());
            } catch (Exception e) {
                // Ignore if error stream is empty
            }
            return new ArrayList<>();
        }
    }

    /**
     * ✅ NEW: Get scheduled sessions for an instructor (NEW WORKFLOW)
     * Endpoint: GET /api/therapy-sessions/instructor/{instructorId}/scheduled
     */
    public List<TherapySession> getScheduledSessionsForInstructor(Long instructorId) throws Exception {
        String endpoint = BASE_URL + "/instructor/" + instructorId + "/scheduled";
        System.out.println("📅 [TherapySessionService] Fetching scheduled sessions for instructor: " + instructorId);
        System.out.println("   Endpoint: " + endpoint);

        URI uri = new URI(endpoint);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("📡 [TherapySessionService] Response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                Type listType = new TypeToken<ArrayList<TherapySession>>(){}.getType();
                List<TherapySession> sessions = gson.fromJson(response.toString(), listType);
                System.out.println("✅ [TherapySessionService] Loaded " + sessions.size() + " scheduled sessions");
                return sessions;
            }
        } else {
            System.err.println("❌ [TherapySessionService] Failed to fetch sessions. Response code: " + responseCode);
            return new ArrayList<>();
        }
    }

    /**
     * Get weekly sessions for calendar display
     * ✅ FIXED: Now uses dynamic instructor ID and /scheduled endpoint
     */
    public List<TherapySession> getWeeklySessions() throws Exception {
        // ✅ FIX: Get dynamic instructor ID from SessionManager with Dataholder fallback
        Long instructorId = com.example.mentalhealthdesktop.SessionManager.getInstance().getCurrentUserId();
        if (instructorId == null) {
            instructorId = com.example.mentalhealthdesktop.Dataholder.userId;
            System.out.println("⚠️ [TherapySessionService] Using Dataholder fallback");
        }

        // ✅ FIX: Use /scheduled endpoint instead of /weekly to get all scheduled sessions
        String endpoint = BASE_URL + "/instructor/" + instructorId + "/scheduled";
        System.out.println("📅 [TherapySessionService] Fetching sessions from: " + endpoint);

        URI uri = new URI(endpoint);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("📡 [TherapySessionService] Response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseBody = response.toString();
                System.out.println("📄 [TherapySessionService] Response: " +
                    (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));

                Type listType = new TypeToken<ArrayList<TherapySession>>(){}.getType();
                List<TherapySession> sessions = gson.fromJson(responseBody, listType);
                System.out.println("✅ [TherapySessionService] Loaded " + sessions.size() + " scheduled sessions for instructor: " + instructorId);

                // Log each session for debugging
                for (TherapySession session : sessions) {
                    System.out.println("   📌 Session ID: " + session.getId() +
                        ", Client: " + session.getClientName() +
                        ", Date: " + session.getSessionDate() +
                        ", Zoom: " + (session.getZoomLink() != null ? "✅" : "❌"));
                }

                return sessions;
            }
        } else {
            System.err.println("❌ [TherapySessionService] Failed to fetch sessions. Response code: " + responseCode);

            // Try to read error response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                System.err.println("   Error response: " + errorResponse.toString());
            } catch (Exception e) {
                // Ignore if can't read error
            }

            return new ArrayList<>();
        }
    }
}

