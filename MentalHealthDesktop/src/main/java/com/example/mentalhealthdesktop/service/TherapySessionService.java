package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.TherapySession;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TherapySessionService {

    private static final String BASE_URL = "http://localhost:8080/api/therapy-sessions";
    private final Gson gson;

    public TherapySessionService() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)));
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, type, context) ->
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME));
        this.gson = gsonBuilder.create();
    }

    // Get sessions for current week
    public List<TherapySession> getWeeklySessions() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/instructor/" + Dataholder.userId + "/week");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        return gson.fromJson(response, new TypeToken<List<TherapySession>>(){}.getType());
    }

    // Get today's sessions
    public List<TherapySession> getTodaySessions() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/instructor/" + Dataholder.userId + "/today");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        return gson.fromJson(response, new TypeToken<List<TherapySession>>(){}.getType());
    }

    // Get all sessions for an instructor
    public List<TherapySession> getInstructorSessions() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/instructor/" + Dataholder.userId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        return gson.fromJson(response, new TypeToken<List<TherapySession>>(){}.getType());
    }

    // Update session status
    public TherapySession updateSessionStatus(Long sessionId, String status) throws Exception {
        checkLoggedIn();

        String json = gson.toJson(new StatusUpdate(status));

        URL url = new URL(BASE_URL + "/" + sessionId + "/status");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, json);
        return gson.fromJson(response, TherapySession.class);
    }

    // Rate a session
    public TherapySession rateSession(Long sessionId, int rating) throws Exception {
        checkLoggedIn();

        String json = gson.toJson(new RatingUpdate(rating));

        URL url = new URL(BASE_URL + "/" + sessionId + "/rate");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, json);
        return gson.fromJson(response, TherapySession.class);
    }

    private String sendRequest(HttpURLConnection con, String json) throws Exception {
        if (json != null) {
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }
        }

        int responseCode = con.getResponseCode();
        if (responseCode == 200 || responseCode == 201) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            throw new RuntimeException("HTTP request failed. Response code: " + responseCode);
        }
    }

    private void checkLoggedIn() {
        if (Dataholder.userId == null) {
            throw new IllegalStateException("User not logged in");
        }
    }

    // Inner classes for requests
    private static class StatusUpdate {
        private String status;

        public StatusUpdate(String status) {
            this.status = status;
        }
    }

    private static class RatingUpdate {
        private int rating;

        public RatingUpdate(int rating) {
            this.rating = rating;
        }
    }
}

