package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.model.Notification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationService {
    private static final String BASE_URL = "http://localhost:8080/api/notifications";
    private final Gson gson;

    public NotificationService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    /**
     * ✅ NEW: Get notifications with action details (zoom links, sender info, etc.)
     * This is the RECOMMENDED method to use for notifications
     */
    public List<Map<String, Object>> getUserNotificationsWithDetails(Long userId) throws Exception {
        System.out.println("🌐 [NotificationService] Calling API: GET " + BASE_URL + "/user/" + userId + "/with-details");

        URI uri = new URI(BASE_URL + "/user/" + userId + "/with-details");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("📡 [NotificationService] API Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseBody = response.toString();
                System.out.println("📄 [NotificationService] Response Body: " +
                    (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));

                Type listType = new TypeToken<ArrayList<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> notifications = gson.fromJson(responseBody, listType);
                System.out.println("✅ [NotificationService] Parsed " + notifications.size() + " notifications with details");

                // Log details for debugging
                for (Map<String, Object> notif : notifications) {
                    String type = (String) notif.get("notificationType");
                    String title = (String) notif.get("title");
                    System.out.println("   📌 " + type + ": " + title);

                    // Log action details
                    if ("SESSION_ACCEPTED".equals(type) && notif.containsKey("zoomLink")) {
                        System.out.println("      🎥 Has zoom link: " + notif.get("canJoin"));
                    } else if ("MESSAGE".equals(type) && notif.containsKey("senderId")) {
                        System.out.println("      💬 From: " + notif.get("senderName") + " (ID: " + notif.get("senderId") + ")");
                    }
                }

                return notifications;
            }
        } else {
            System.err.println("❌ [NotificationService] Failed to fetch notifications. Response code: " + responseCode);

            // Try to read error response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                System.err.println("❌ [NotificationService] Error response: " + errorResponse.toString());
            } catch (Exception e) {
                // Ignore if can't read error
            }

            return new ArrayList<>();
        }
    }

    /**
     * Get all notifications for a specific user (OLD METHOD - kept for backward compatibility)
     * DEPRECATED: Use getUserNotificationsWithDetails() instead
     */
    public List<Notification> getUserNotifications(Long userId) throws Exception {
        System.out.println("🌐 [NotificationService] Calling API: GET " + BASE_URL + "/user/" + userId);

        URI uri = new URI(BASE_URL + "/user/" + userId);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("📡 [NotificationService] API Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseBody = response.toString();
                System.out.println("📄 [NotificationService] Response Body: " +
                    (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));

                Type listType = new TypeToken<ArrayList<Notification>>(){}.getType();
                List<Notification> notifications = gson.fromJson(responseBody, listType);
                System.out.println("✅ [NotificationService] Parsed " + notifications.size() + " notifications");
                return notifications;
            }
        } else {
            System.err.println("❌ [NotificationService] Failed to fetch notifications. Response code: " + responseCode);

            // Try to read error response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                System.err.println("❌ [NotificationService] Error response: " + errorResponse.toString());
            } catch (Exception e) {
                // Ignore if can't read error
            }

            return new ArrayList<>();
        }
    }

    /**
     * Get unread notifications count for a user
     */
    public int getUnreadCount(Long userId) throws Exception {
        URI uri = new URI(BASE_URL + "/user/" + userId + "/unread-count");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return Integer.parseInt(response.toString());
            }
        } else {
            return 0;
        }
    }

    /**
     * Mark a notification as read
     */
    public void markAsRead(Long notificationId) throws Exception {
        URI uri = new URI(BASE_URL + "/" + notificationId + "/read");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("PUT");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to mark notification as read. Response code: " + responseCode);
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(Long userId) throws Exception {
        URI uri = new URI(BASE_URL + "/user/" + userId + "/read-all");
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("PUT");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to mark all notifications as read. Response code: " + responseCode);
        }
    }

    /**
     * Create a new notification (used by system/instructors)
     */
    public Notification createNotification(Notification notification) throws Exception {
        URI uri = new URI(BASE_URL);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonRequest = gson.toJson(notification);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonRequest.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return gson.fromJson(response.toString(), Notification.class);
            }
        } else {
            throw new Exception("Failed to create notification. Response code: " + responseCode);
        }
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(Long notificationId) throws Exception {
        URI uri = new URI(BASE_URL + "/" + notificationId);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("DELETE");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new Exception("Failed to delete notification. Response code: " + responseCode);
        }
    }
}

