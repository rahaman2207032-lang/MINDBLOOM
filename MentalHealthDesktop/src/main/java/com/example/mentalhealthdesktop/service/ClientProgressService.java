package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ClientProgressService {

    private static final String BASE_URL = "http://localhost:8080/api/client-progress";
    private final Gson gson = new Gson();

    // Get client's mood logs
    public List<Map<String, Object>> getClientMoodLogs(Long clientId) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/" + clientId + "/mood-logs");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con);
        return gson.fromJson(response, new TypeToken<List<Map<String, Object>>>(){}.getType());
    }

    // Get client's stress assessments
    public List<Map<String, Object>> getClientStressAssessments(Long clientId) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/" + clientId + "/stress-assessments");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con);
        return gson.fromJson(response, new TypeToken<List<Map<String, Object>>>(){}.getType());
    }

    // Get client's habit completion data
    public List<Map<String, Object>> getClientHabits(Long clientId) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/" + clientId + "/habits");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con);
        return gson.fromJson(response, new TypeToken<List<Map<String, Object>>>(){}.getType());
    }

    // Get client's sleep data
    public List<Map<String, Object>> getClientSleepData(Long clientId) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/" + clientId + "/sleep-data");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con);
        return gson.fromJson(response, new TypeToken<List<Map<String, Object>>>(){}.getType());
    }

    // Get client's overall progress summary
    public Map<String, Object> getClientProgressSummary(Long clientId) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/" + clientId + "/summary");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        String response = sendRequest(con);
        return gson.fromJson(response, new TypeToken<Map<String, Object>>(){}.getType());
    }

    private String sendRequest(HttpURLConnection con) throws Exception {
        int responseCode = con.getResponseCode();

        BufferedReader in;
        if (responseCode >= 200 && responseCode < 300) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        if (responseCode >= 400) {
            throw new Exception("HTTP Error " + responseCode + ": " + response.toString());
        }

        return response.toString();
    }

    private void checkLoggedIn() throws IllegalStateException {
        if (Dataholder.userId == null) {
            throw new IllegalStateException("User not logged in");
        }
    }
}

