package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.SessionRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionRequestService {
    private static final String BASE_URL = "http://localhost:8080/api/session-requests";
    private final Gson gson;

    public SessionRequestService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    // helper to apply common headers
    private void applyCommonHeaders(HttpURLConnection connection) {
        connection.setRequestProperty("Content-Type", "application/json");
        if (Dataholder.userId != null) connection.setRequestProperty("X-User-Id", String.valueOf(Dataholder.userId));
    }

    /**
     * Create a new session request
     */
    public SessionRequest createSessionRequest(SessionRequest request) throws Exception {
        URI uri = new URI(BASE_URL);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        applyCommonHeaders(connection);
        connection.setDoOutput(true);

        String jsonRequest = gson.toJson(request);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonRequest.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return gson.fromJson(response.toString(), SessionRequest.class);
            }
        } else {
            throw new Exception("Failed to create session request. Response code: " + responseCode);
        }
    }

    /**
     * Get all session requests for a specific user
     */
    public List<SessionRequest> getUserSessionRequests(Long userId) throws Exception {
        URI uri = new URI(BASE_URL + "/user/" + userId);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        applyCommonHeaders(connection);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                Type listType = new TypeToken<ArrayList<SessionRequest>>(){}.getType();
                return gson.fromJson(response.toString(), listType);
            }
        } else {
            System.err.println("Failed to fetch session requests. Response code: " + responseCode);
            return new ArrayList<>();
        }
    }

    /**
     * Get confirmed sessions for a user
     */
    public List<SessionRequest> getConfirmedSessions(Long userId) throws Exception {
        URI uri = new URI(BASE_URL + "/user/" + userId + "/confirmed");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        applyCommonHeaders(connection);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                Type listType = new TypeToken<ArrayList<SessionRequest>>(){}.getType();
                return gson.fromJson(response.toString(), listType);
            }
        } else {
            System.err.println("Failed to fetch confirmed sessions. Response code: " + responseCode);
            return new ArrayList<>();
        }
    }

    /**
     * Get all pending session requests (for instructors)
     */
    public List<SessionRequest> getPendingRequests() throws Exception {
        // ✅ UPDATED: Using instructor-specific endpoint
        URI uri = new URI("http://localhost:8080/api/instructors/" + Dataholder.userId + "/session-requests");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        applyCommonHeaders(connection);

        System.out.println("🌐 [SessionRequestService] Fetching pending requests for instructor: " + Dataholder.userId);

        int responseCode = connection.getResponseCode();
        System.out.println("📡 [SessionRequestService] Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseBody = response.toString();
                System.out.println("📄 [SessionRequestService] Response: " +
                    (responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody));

                Type listType = new TypeToken<ArrayList<SessionRequest>>(){}.getType();
                List<SessionRequest> requests = gson.fromJson(responseBody, listType);
                System.out.println("✅ [SessionRequestService] Loaded " + requests.size() + " pending requests");
                return requests;
            }
        } else {
            System.err.println("❌ [SessionRequestService] Failed to fetch pending requests. Response code: " + responseCode);
            return new ArrayList<>();
        }
    }

    /**
     * Confirm a session request (instructor action)
     */
    public SessionRequest confirmRequest(Long requestId, Long instructorId, String zoomLink) throws Exception {
        // ✅ UPDATED: Using instructor-specific accept endpoint with POST
        URI uri = new URI("http://localhost:8080/api/instructors/session-requests/" + requestId + "/accept");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST"); // ✅ Changed from PUT to POST
        connection.setRequestProperty("Content-Type", "application/json");
        applyCommonHeaders(connection);
        connection.setDoOutput(true);

        // ✅ UPDATED: Send empty body for automatic Zoom creation, or manual link if provided
        String jsonRequest;
        if (zoomLink == null || zoomLink.isEmpty()) {
            jsonRequest = "{}";  // Empty body - backend creates Zoom automatically!
            System.out.println("🎥 [SessionRequestService] Accepting request " + requestId + " - Zoom will be created automatically");
        } else {
            jsonRequest = "{\"zoomLink\":\"" + zoomLink + "\"}";
            System.out.println("🌐 [SessionRequestService] Accepting request " + requestId + " with manual zoom link");
        }

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonRequest.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        System.out.println("📡 [SessionRequestService] Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseBody = response.toString();
                System.out.println("📄 [SessionRequestService] Response: " +
                    (responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody));
                System.out.println("✅ [SessionRequestService] Session request accepted successfully");

                return gson.fromJson(responseBody, SessionRequest.class);
            }
        } else {
            // Try to read error response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                System.err.println("❌ [SessionRequestService] Error response: " + errorResponse.toString());
            } catch (Exception e) {
                // Ignore if can't read error
            }

            System.err.println("❌ [SessionRequestService] Failed to accept. Response code: " + responseCode);
            throw new Exception("Failed to confirm session request. Response code: " + responseCode);
        }
    }

    /**
     * Accept a session request - Alias for confirmRequest (used by instructor dashboard)
     */
    public SessionRequest acceptRequest(Long requestId, String zoomLink) throws Exception {
        Long instructorId = Dataholder.userId;
        return confirmRequest(requestId, instructorId, zoomLink);
    }

    /**
     * Reject a session request (instructor action)
     */
    public void rejectRequest(Long requestId) throws Exception {
        // ✅ UPDATED: Using instructor-specific decline endpoint with POST
        URI uri = new URI("http://localhost:8080/api/instructors/session-requests/" + requestId + "/decline");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST"); // ✅ Changed from PUT to POST
        applyCommonHeaders(connection);

        System.out.println("🌐 [SessionRequestService] Declining request " + requestId);

        int responseCode = connection.getResponseCode();
        System.out.println("📡 [SessionRequestService] Response Code: " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            System.err.println("❌ [SessionRequestService] Failed to decline. Response code: " + responseCode);
            throw new Exception("Failed to reject session request. Response code: " + responseCode);
        }

        System.out.println("✅ [SessionRequestService] Session request declined successfully");
    }

    /**
     * Decline a session request - Alias for rejectRequest (used by instructor dashboard)
     */
    public void declineRequest(Long requestId) throws Exception {
        rejectRequest(requestId);
    }

    // Helper class for confirm request
    private static class ConfirmRequest {
        private Long instructorId;
        private String zoomLink;

        public ConfirmRequest(Long instructorId, String zoomLink) {
            this.instructorId = instructorId;
            this.zoomLink = zoomLink;
        }

        public Long getInstructorId() {
            return instructorId;
        }

        public String getZoomLink() {
            return zoomLink;
        }
    }
}

