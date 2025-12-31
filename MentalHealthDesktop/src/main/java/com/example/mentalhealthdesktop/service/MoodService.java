package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.MoodLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MoodService {

    private static final String BASE_URL = "http://localhost:8080/api/mood";
    private final Gson gson;

    public MoodService() {
        // Gson with LocalDate support
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class,
                (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE)));
        gsonBuilder.registerTypeAdapter(LocalDate.class,
                (JsonDeserializer<LocalDate>) (json, type, context) ->
                        LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_DATE));
        this.gson = gsonBuilder.create();
    }

    // Helper method to send HTTP request and get JSON response
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
        } else if (responseCode == 404) {
            return null;
        } else {
            throw new RuntimeException("HTTP request failed. Response code: " + responseCode);
        }
    }

    // Save mood log
    public MoodLog saveMoodLog(MoodLog moodLog) throws Exception {
        checkLoggedIn();
        moodLog.setUserId(Dataholder.userId);
        moodLog.setMoodEmojiFromRating();

        String json = gson.toJson(moodLog);

        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, json);
        return gson.fromJson(response, MoodLog.class);
    }

    // Get all mood logs for logged-in user
    public List<MoodLog> getUserMoodLogs() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/user/" + Dataholder.userId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        if (response == null) return List.of();

        Type listType = new TypeToken<List<MoodLog>>(){}.getType();
        return gson.fromJson(response, listType);
    }

    // Get mood logs within date range
    public List<MoodLog> getMoodLogsByDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/user/" + Dataholder.userId +
                         "/range?startDate=" + startDate + "&endDate=" + endDate);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        if (response == null) return List.of();

        Type listType = new TypeToken<List<MoodLog>>(){}.getType();
        return gson.fromJson(response, listType);
    }

    // Get latest mood log
    public MoodLog getLatestMoodLog() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/user/" + Dataholder.userId + "/latest");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        if (response == null) return null;

        return gson.fromJson(response, MoodLog.class);
    }

    // Helper: Check if user is logged in
    private void checkLoggedIn() {
        if (Dataholder.userId == null) {
            throw new IllegalStateException("User not logged in");
        }
    }

    // Calculate average mood for a period
    public double calculateAverageMood(List<MoodLog> moodLogs) {
        if (moodLogs == null || moodLogs.isEmpty()) {
            return 0.0;
        }
        return moodLogs.stream()
                .mapToInt(MoodLog::getMoodRating)
                .average()
                .orElse(0.0);
    }
}

