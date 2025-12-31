package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.ClientOverview;
import com.example.mentalhealthdesktop.model.Instructor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructorService {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final Gson gson;

    public InstructorService() {
        this.gson = new Gson();
    }

    /**
     * Get all available instructors from backend
     */
    public List<Instructor> getAllInstructors() throws Exception {
        System.out.println("🌐 [InstructorService] Fetching all instructors from: " + BASE_URL + "/instructors");

        URI uri = new URI(BASE_URL + "/instructors" +
                "");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("📡 [InstructorService] Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                String responseBody = response.toString();
                System.out.println("📄 [InstructorService] Response: " +
                    (responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody));

                List<Instructor> instructors = gson.fromJson(responseBody,
                    new TypeToken<List<Instructor>>(){}.getType());

                System.out.println("✅ [InstructorService] Parsed " + instructors.size() + " instructors");
                return instructors;
            }
        } else {
            System.err.println("❌ [InstructorService] Failed to fetch instructors. Response code: " + responseCode);

            // Try to read error response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                System.err.println("❌ Error response: " + errorResponse.toString());
            } catch (Exception e) {
                // Ignore if can't read error
            }

            return new ArrayList<>();
        }
    }

    /**
     * Get user's assigned instructor (if applicable)
     */
    public Instructor getAssignedInstructor(Long userId) throws Exception {
        System.out.println("🌐 [InstructorService] Fetching assigned instructor for user: " + userId);

        URI uri = new URI(BASE_URL + "/users/" + userId + "/assigned-instructor");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                Instructor instructor = gson.fromJson(response.toString(), Instructor.class);
                System.out.println("✅ [InstructorService] Found assigned instructor: " + instructor.getUsername());
                return instructor;
            }
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            System.out.println("ℹ️ [InstructorService] No assigned instructor for user " + userId);
            return null; // No assigned instructor
        } else {
            System.err.println("❌ [InstructorService] Failed to fetch assigned instructor. Response code: " + responseCode);
            return null;
        }
    }

    /**
     * Get dashboard statistics for instructor
     */
    public Map<String, Integer> getDashboardStats() throws Exception {
        System.out.println("🌐 [InstructorService] Fetching dashboard stats for instructor: " + Dataholder.userId);

        URI uri = new URI(BASE_URL + "/instructors/" + Dataholder.userId + "/dashboard-stats");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                Map<String, Integer> stats = gson.fromJson(response.toString(),
                    new TypeToken<Map<String, Integer>>(){}.getType());
                System.out.println("✅ [InstructorService] Dashboard stats loaded");
                return stats;
            }
        } else {
            System.err.println("❌ [InstructorService] Failed to fetch dashboard stats. Response code: " + responseCode);
            // Return default values
            Map<String, Integer> defaultStats = new HashMap<>();
            defaultStats.put("pendingRequests", 0);
            defaultStats.put("todaySessions", 0);
            defaultStats.put("totalClients", 0);
            defaultStats.put("availableSlots", 0);
            return defaultStats;
        }
    }

    /**
     * Get all clients for instructor
     */
    public List<ClientOverview> getAllClients() throws Exception {
        System.out.println("🌐 [InstructorService] Fetching all clients for instructor: " + Dataholder.userId);

        URI uri = new URI(BASE_URL + "/instructors/" + Dataholder.userId + "/clients");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                String responseBody = response.toString();
                System.out.println("📄 [InstructorService] Clients response: " +
                    (responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody));

                List<ClientOverview> clients = gson.fromJson(responseBody,
                    new TypeToken<List<ClientOverview>>(){}.getType());

                System.out.println("✅ [InstructorService] Loaded " + clients.size() + " clients");
                return clients;
            }
        } else {
            System.err.println("❌ [InstructorService] Failed to fetch clients. Response code: " + responseCode);
            return new ArrayList<>();
        }
    }

    /**
     * Get analytics for instructor
     */
    public Map<String, Object> getAnalytics(String timeRange) throws Exception {
        System.out.println("🌐 [InstructorService] Fetching analytics for: " + timeRange);

        URI uri = new URI(BASE_URL + "/instructors/" + Dataholder.userId + "/analytics?timeRange=" + timeRange);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                Map<String, Object> analytics = gson.fromJson(response.toString(),
                    new TypeToken<Map<String, Object>>(){}.getType());
                System.out.println("✅ [InstructorService] Analytics loaded");
                return analytics;
            }
        } else {
            System.err.println("❌ [InstructorService] Failed to fetch analytics. Response code: " + responseCode);
            // Return default values
            Map<String, Object> defaultAnalytics = new HashMap<>();
            defaultAnalytics.put("totalSessions", 0);
            defaultAnalytics.put("completedSessions", 0);
            defaultAnalytics.put("avgRating", 0.0);
            return defaultAnalytics;
        }
    }
}

