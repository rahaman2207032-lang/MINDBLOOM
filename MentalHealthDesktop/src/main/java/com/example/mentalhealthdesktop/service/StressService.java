package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.StressAssessment;
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

public class StressService {

    private static final String BASE_URL = "http://localhost:8080/api/stress";
    private final Gson gson;

    public StressService() {
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

    // Save stress assessment
    public StressAssessment saveStressAssessment(StressAssessment assessment) throws Exception {
        checkLoggedIn();
        assessment.setUserId(Dataholder.userId);
        assessment.calculateStressScore();

        String json = gson.toJson(assessment);

        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, json);
        return gson.fromJson(response, StressAssessment.class);
    }

    // Get all stress assessments for logged-in user
    public List<StressAssessment> getUserStressAssessments() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/user/" + Dataholder.userId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        if (response == null) return List.of();

        Type listType = new TypeToken<List<StressAssessment>>(){}.getType();
        return gson.fromJson(response, listType);
    }

    // Get stress assessments within date range
    public List<StressAssessment> getStressAssessmentsByDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/user/" + Dataholder.userId +
                         "/range?startDate=" + startDate + "&endDate=" + endDate);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        if (response == null) return List.of();

        Type listType = new TypeToken<List<StressAssessment>>(){}.getType();
        return gson.fromJson(response, listType);
    }

    // Get latest stress assessment
    public StressAssessment getLatestStressAssessment() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/user/" + Dataholder.userId + "/latest");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        if (response == null) return null;

        return gson.fromJson(response, StressAssessment.class);
    }

    // Helper: Check if user is logged in
    private void checkLoggedIn() {
        if (Dataholder.userId == null) {
            throw new IllegalStateException("User not logged in");
        }
    }

    // Get coping suggestion based on stress level
    public String getCopingSuggestion(String stressLevel) {
        switch (stressLevel) {
            case "HIGH":
                return "Your stress level is high. Consider:\n" +
                       "• Deep breathing exercises (5 minutes)\n" +
                       "• Take a short break from work\n" +
                       "• Journal your thoughts\n" +
                       "• Reach out to a friend or therapist";
            case "MODERATE":
                return "Your stress level is moderate. Try:\n" +
                       "• Quick meditation session\n" +
                       "• Brief walk outside\n" +
                       "• Practice mindfulness\n" +
                       "• Review your sleep habits";
            case "LOW":
                return "Your stress level is low. Keep it up!\n" +
                       "• Maintain your healthy habits\n" +
                       "• Continue regular exercise\n" +
                       "• Keep journaling\n" +
                       "• Stay socially connected";
            default:
                return "Take care of yourself!";
        }
    }
}

