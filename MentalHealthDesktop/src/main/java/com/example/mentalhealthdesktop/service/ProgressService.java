package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.ProgressData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProgressService {

    private static final String BASE_URL = "http://localhost:8080/api/progress";
    private final Gson gson;

    public ProgressService() {
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
    private String sendRequest(HttpURLConnection con) throws Exception {
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

    // Get progress data for logged-in user
    public ProgressData getProgressData(LocalDate startDate, LocalDate endDate) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/user/" + Dataholder.userId +
                         "?startDate=" + startDate + "&endDate=" + endDate);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con);
        if (response == null) {
            // Return empty progress data if none exists
            ProgressData emptyData = new ProgressData(Dataholder.userId);
            emptyData.setStartDate(startDate);
            emptyData.setEndDate(endDate);
            return emptyData;
        }

        return gson.fromJson(response, ProgressData.class);
    }

    // Get progress data for default period (last 30 days)
    public ProgressData getProgressData() throws Exception {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        return getProgressData(startDate, endDate);
    }

    // Helper: Check if user is logged in
    private void checkLoggedIn() {
        if (Dataholder.userId == null) {
            throw new IllegalStateException("User not logged in");
        }
    }
}

