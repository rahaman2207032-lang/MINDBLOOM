package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.SleepEntry;
import com.google.gson.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepService {

    private static final String BASE_URL = "http://localhost:8080/api/sleep";
    private final Gson gson;

    public SleepService() {
        // Gson with LocalDateTime support
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (jsonElement, type, context) ->
                        LocalDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_DATE_TIME));
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)));
        this.gson = gsonBuilder.create();
    }

    /**
     * Save a new sleep entry
     */
    public SleepEntry saveSleepEntry(LocalDateTime startTime, LocalDateTime endTime,
                                     Integer quality, String notes) throws Exception {
        checkLoggedIn();

        URI uri = new URI(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", Dataholder.userId);
        data.put("sleepStartTime", startTime.format(DateTimeFormatter.ISO_DATE_TIME));
        data.put("sleepEndTime", endTime.format(DateTimeFormatter.ISO_DATE_TIME));
        data.put("sleepQuality", quality);
        data.put("notes", notes);

        String jsonInput = gson.toJson(data);

        try (OutputStream os = con.getOutputStream()) {
            os.write(jsonInput.getBytes());
            os.flush();
        }

        int responseCode = con.getResponseCode();
        if (responseCode == 200 || responseCode == 201) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return gson.fromJson(response.toString(), SleepEntry.class);
            }
        } else {
            throw new RuntimeException("Failed to save sleep entry. Response code: " + responseCode);
        }
    }

    /**
     * Get all sleep entries for the logged-in user
     */
    public List<SleepEntry> getAllSleepEntries() throws Exception {
        checkLoggedIn();

        URI uri = new URI(BASE_URL + "/user/" + Dataholder.userId);
        HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                JsonArray jsonArray = gson.fromJson(response.toString(), JsonArray.class);
                List<SleepEntry> entries = new ArrayList<>();
                for (JsonElement element : jsonArray) {
                    entries.add(gson.fromJson(element, SleepEntry.class));
                }
                return entries;
            }
        } else {
            throw new RuntimeException("Failed to fetch sleep entries. Response code: " + responseCode);
        }
    }

    /**
     * Get sleep entries for the last 7 days
     */
    public List<SleepEntry> getWeeklySleepEntries() throws Exception {
        checkLoggedIn();

        URI uri = new URI(BASE_URL + "/user/" + Dataholder.userId + "/weekly");
        HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                JsonArray jsonArray = gson.fromJson(response.toString(), JsonArray.class);
                List<SleepEntry> entries = new ArrayList<>();
                for (JsonElement element : jsonArray) {
                    entries.add(gson.fromJson(element, SleepEntry.class));
                }
                return entries;
            }
        } else {
            throw new RuntimeException("Failed to fetch weekly sleep entries. Response code: " + responseCode);
        }
    }

    /**
     * Delete a sleep entry
     */
    public void deleteSleepEntry(Long sleepId) throws Exception {
        checkLoggedIn();

        URI uri = new URI(BASE_URL + "/" + sleepId);
        HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
        con.setRequestMethod("DELETE");

        int responseCode = con.getResponseCode();
        if (responseCode != 200 && responseCode != 204) {
            throw new RuntimeException("Failed to delete sleep entry. Response code: " + responseCode);
        }
    }

    /**
     * Check if user is logged in
     */
    private void checkLoggedIn() {
        if (Dataholder.userId == null) {
            throw new IllegalStateException("User not logged in");
        }
    }
}

