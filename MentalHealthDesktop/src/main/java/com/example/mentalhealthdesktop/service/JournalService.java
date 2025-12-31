package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.JournalEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class JournalService {

    private static final String BASE_URL = "http://localhost:8080/api/journal";
    private final Gson gson;

    public JournalService() {
        // Gson with LocalDateTime support
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)));
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, type, context) ->
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME));
        this.gson = gsonBuilder.create();
    }

    // -------------------------------
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

    // -------------------------------
    // Fetch latest journal entry for logged-in user
    public JournalEntry getJournalEntry() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/user/" + Dataholder.userId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        if (response == null) return null;

        return gson.fromJson(response, JournalEntry.class);
    }


    // -------------------------------
    // Save new journal entry
    public JournalEntry saveJournalEntry(String content) throws Exception {
        checkLoggedIn();

        // create JournalEntry object
        JournalEntry entry = new JournalEntry();
        entry.setUserId(Dataholder.userId);
        entry.setContent(content);

        String json = gson.toJson(entry);

        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, json);
        return gson.fromJson(response, JournalEntry.class);
    }


    // -------------------------------
    // Update existing journal entry
    public JournalEntry updateJournalEntry(Long journalId, String content) throws Exception {
        checkLoggedIn();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", content);
        requestBody.put("userId", Dataholder.userId);

        String json = gson.toJson(requestBody);

        URL url = new URL(BASE_URL + "/" + journalId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, json);
        return gson.fromJson(response, JournalEntry.class);
    }

    // -------------------------------
    // Helper: Check if user is logged in
    private void checkLoggedIn() {
        if (Dataholder.userId == null) {
            throw new IllegalStateException("User not logged in");
        }
    }
}
