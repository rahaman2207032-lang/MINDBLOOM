package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.Habit;
import com.example.mentalhealthdesktop.model.HabitCompletion;
import com.google.gson.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabitService {

    private static final String BASE_URL = "http://localhost:8080/api/habits";
    private final Gson gson;

    public HabitService() {
        // Gson with LocalDateTime and LocalDate support
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (jsonElement, type, context) ->
                        LocalDateTime.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_DATE_TIME));
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)));
        gsonBuilder.registerTypeAdapter(LocalDate.class,
                (JsonDeserializer<LocalDate>) (jsonElement, type, context) ->
                        LocalDate.parse(jsonElement.getAsString()));
        gsonBuilder.registerTypeAdapter(LocalDate.class,
                (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.toString()));
        this.gson = gsonBuilder.create();
    }

    /**
     * Create a new habit
     */
    public Habit createHabit(String name, String description, String frequency, String targetDays) throws Exception {
        checkLoggedIn();

        URI uri = new URI(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", Dataholder.userId);
        data.put("name", name);
        data.put("description", description);
        data.put("frequency", frequency);
        data.put("targetDays", targetDays);
        data.put("currentStreak", 0);
        data.put("longestStreak", 0);
        data.put("isActive", true);

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
                return gson.fromJson(response.toString(), Habit.class);
            }
        } else {
            throw new RuntimeException("Failed to create habit. Response code: " + responseCode);
        }
    }

    /**
     * Get all active habits for the logged-in user
     */
    public List<Habit> getAllHabits() throws Exception {
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
                List<Habit> habits = new ArrayList<>();
                for (JsonElement element : jsonArray) {
                    habits.add(gson.fromJson(element, Habit.class));
                }
                return habits;
            }
        } else {
            throw new RuntimeException("Failed to fetch habits. Response code: " + responseCode);
        }
    }

    /**
     * Mark habit as completed for today
     */
    public HabitCompletion completeHabit(Long habitId, String notes) throws Exception {
        checkLoggedIn();

        URI uri = new URI(BASE_URL + "/" + habitId + "/complete");
        HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", Dataholder.userId);
        data.put("habitId", habitId);
        data.put("completionDate", LocalDate.now().toString());
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
                return gson.fromJson(response.toString(), HabitCompletion.class);
            }
        } else {
            throw new RuntimeException("Failed to complete habit. Response code: " + responseCode);
        }
    }

    /**
     * Get completion history for a specific habit
     */
    public List<HabitCompletion> getHabitCompletions(Long habitId) throws Exception {
        checkLoggedIn();

        URI uri = new URI(BASE_URL + "/" + habitId + "/completions");
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
                List<HabitCompletion> completions = new ArrayList<>();
                for (JsonElement element : jsonArray) {
                    completions.add(gson.fromJson(element, HabitCompletion.class));
                }
                return completions;
            }
        } else {
            throw new RuntimeException("Failed to fetch habit completions. Response code: " + responseCode);
        }
    }

    /**
     * Update habit
     */
    public Habit updateHabit(Habit habit) throws Exception {
        checkLoggedIn();

        URI uri = new URI(BASE_URL + "/" + habit.getId());
        HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String jsonInput = gson.toJson(habit);

        try (OutputStream os = con.getOutputStream()) {
            os.write(jsonInput.getBytes());
            os.flush();
        }

        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return gson.fromJson(response.toString(), Habit.class);
            }
        } else {
            throw new RuntimeException("Failed to update habit. Response code: " + responseCode);
        }
    }

    /**
     * Delete habit
     */
    public void deleteHabit(Long habitId) throws Exception {
        checkLoggedIn();

        URI uri = new URI(BASE_URL + "/" + habitId);
        HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
        con.setRequestMethod("DELETE");

        int responseCode = con.getResponseCode();
        if (responseCode != 200 && responseCode != 204) {
            throw new RuntimeException("Failed to delete habit. Response code: " + responseCode);
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

