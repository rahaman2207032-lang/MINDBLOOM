package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.TherapyNote;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TherapyNoteService {

    private static final String BASE_URL = "http://localhost:8080/api/therapy-notes";
    private final Gson gson;

    public TherapyNoteService() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)));
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, type, context) ->
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME));
        gsonBuilder.registerTypeAdapter(LocalDate.class,
                (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                        new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE)));
        gsonBuilder.registerTypeAdapter(LocalDate.class,
                (JsonDeserializer<LocalDate>) (json, type, context) ->
                        LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_DATE));
        this.gson = gsonBuilder.create();
    }

    // Save a new therapy note
    public TherapyNote createNote(TherapyNote note) throws Exception {
        checkLoggedIn();

        note.setInstructorId(Dataholder.userId);

        // Log what we're sending
        System.out.println("📝 Sending therapy note to backend:");
        System.out.println("   Client ID: " + note.getClientId());
        System.out.println("   Instructor ID: " + note.getInstructorId());
        System.out.println("   Note Text: " + note.getNoteText());
        System.out.println("   Session Date: " + note.getSessionDate());

        String json = gson.toJson(note);
        System.out.println("   JSON: " + json);

        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, json);
        System.out.println("✅ Therapy note saved successfully!");
        return gson.fromJson(response, TherapyNote.class);
    }

    // Save a new therapy note (legacy method)
    public TherapyNote saveNote(Long clientId, LocalDate sessionDate, String sessionType, String notes) throws Exception {
        checkLoggedIn();

        TherapyNote note = new TherapyNote();
        note.setInstructorId(Dataholder.userId);
        note.setClientId(clientId);
        note.setSessionDate(sessionDate);
        note.setSessionType(sessionType);
        note.setNotes(notes);

        String json = gson.toJson(note);

        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, json);
        return gson.fromJson(response, TherapyNote.class);
    }

    // Get notes for a specific client
    public List<TherapyNote> getNotesByClient(Long clientId) throws Exception {
        return getClientNotes(clientId);
    }

    // Get notes for a specific client (legacy method)
    public List<TherapyNote> getClientNotes(Long clientId) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/client/" + clientId + "/instructor/" + Dataholder.userId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, null);
        return gson.fromJson(response, new TypeToken<List<TherapyNote>>(){}.getType());
    }

    // Update a therapy note
    public TherapyNote updateNote(Long noteId, String notes) throws Exception {
        checkLoggedIn();

        String json = gson.toJson(new NoteUpdate(notes));

        URL url = new URL(BASE_URL + "/" + noteId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con, json);
        return gson.fromJson(response, TherapyNote.class);
    }

    // Delete a therapy note
    public void deleteNote(Long noteId) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/" + noteId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Content-Type", "application/json");

        int responseCode = con.getResponseCode();
        if (responseCode != 200 && responseCode != 204) {
            throw new RuntimeException("Failed to delete note. Response code: " + responseCode);
        }
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

    private static class NoteUpdate {
        private String notes;

        public NoteUpdate(String notes) {
            this.notes = notes;
        }
    }
}

