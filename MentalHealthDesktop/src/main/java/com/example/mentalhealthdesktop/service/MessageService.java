package com.example.mentalhealthdesktop.service;

import com.example.mentalhealthdesktop.Dataholder;
import com.example.mentalhealthdesktop.model.Message;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageService {

    private static final String BASE_URL = "http://localhost:8080/api/messages";
    private final Gson gson;

    public MessageService() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                        new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)));
        gsonBuilder.registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, type, context) ->
                        LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME));
        this.gson = gsonBuilder.create();
    }


    private void applyCommonHeaders(HttpURLConnection con) {
        con.setRequestProperty("Content-Type", "application/json");
        if (Dataholder.userId != null) {
            con.setRequestProperty("X-User-Id", String.valueOf(Dataholder.userId));
        }
    }

    // Send a message
    public Message sendMessage(Long receiverId, String messageText) throws Exception {
        checkLoggedIn();

        Message message = new Message();
        message.setSenderId(Dataholder.userId);
        message.setReceiverId(receiverId);
        message.setMessageText(messageText);

        String json = gson.toJson(message);

        URL url = new URL(BASE_URL + "/send");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        applyCommonHeaders(con);

        String response = sendRequest(con, json);
        return gson.fromJson(response, Message.class);
    }

    // Get conversation with a specific user
    public List<Message> getConversationMessages(Long otherUserId) throws Exception {
        return getConversation(otherUserId);
    }

    // Get conversation with a specific user (legacy method)
    public List<Message> getConversation(Long otherUserId) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/conversation/" + Dataholder.userId + "/" + otherUserId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        applyCommonHeaders(con);

        String response = sendRequest(con, null);
        return gson.fromJson(response, new TypeToken<List<Message>>(){}.getType());
    }

    // Get all conversations for current user
    public List<ConversationSummary> getConversations() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/conversations/" + Dataholder.userId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        applyCommonHeaders(con);

        String response = sendRequest(con, null);
        return gson.fromJson(response, new TypeToken<List<ConversationSummary>>(){}.getType());
    }

    // Mark message as read
    public void markAsRead(Long messageId) throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/" + messageId + "/read");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        applyCommonHeaders(con);

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to mark message as read. Response code: " + responseCode);
        }
    }

    // Get unread message count
    public int getUnreadCount() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/unread/count/" + Dataholder.userId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        applyCommonHeaders(con);

        String response = sendRequest(con, null);
        return gson.fromJson(response, Integer.class);
    }

    // Get list of available users for messaging
    public List<Map<String, Object>> getAvailableUsers() throws Exception {
        checkLoggedIn();

        URL url = new URL(BASE_URL + "/available-users/" + Dataholder.userId);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        applyCommonHeaders(con);
        con.setRequestProperty("Accept", "application/json");

        System.out.println("📋 Fetching available users from: " + url);

        String response = sendRequest(con, null);

        // Parse as list of maps
        List<Map<String, Object>> users = gson.fromJson(response,
            new TypeToken<List<Map<String, Object>>>(){}.getType());

        System.out.println(" Found " + users.size() + " available users");
        return users;
    }


    public List<Map<String, Object>> getInstructorConversations(Long instructorId) throws Exception {

        String endpoint = "http://localhost:8080/api/instructors/" + instructorId + "/conversations";
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        applyCommonHeaders(con);
        con.setRequestProperty("Accept", "application/json");

        System.out.println(" [MessageService] Fetching instructor conversations from: " + endpoint);

        int responseCode = con.getResponseCode();
        System.out.println(" [MessageService] Response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = sendRequest(con, null);


            List<Map<String, Object>> conversations = gson.fromJson(response,
                new TypeToken<List<Map<String, Object>>>(){}.getType());

            System.out.println(" [MessageService] Found " + conversations.size() + " conversations");

            // Log conversation details for debugging
            for (Map<String, Object> conv : conversations) {
                System.out.println("  Client: " + conv.get("clientName") +
                                 ", Last msg: " + conv.get("lastMessage") +
                                 ", Unread: " + conv.get("unreadCount"));
            }

            return conversations;
        } else {
            System.err.println(" [MessageService] Failed to fetch conversations. Code: " + responseCode);
            return new ArrayList<>();
        }
    }


    public Message sendMessageToInstructor(Long userId, Long instructorId, String messageText) throws Exception {
        checkLoggedIn();

        Message message = new Message();
        message.setSenderId(userId);
        message.setReceiverId(instructorId);
        message.setMessageText(messageText);

        String json = gson.toJson(message);

        URL url = new URL(BASE_URL + "/send");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        applyCommonHeaders(con);

        String response = sendRequest(con, json);

        System.out.println(" Message sent from user " + userId + " to instructor " + instructorId);
        return gson.fromJson(response, Message.class);
    }


    public Long getInstructorIdForUser(Long userId) throws Exception {
        checkLoggedIn();


        URL url = new URL(BASE_URL + "/user/" + userId + "/instructor");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        applyCommonHeaders(con);

        try {
            String response = sendRequest(con, null);
            Map<String, Object> result = gson.fromJson(response,
                new TypeToken<Map<String, Object>>(){}.getType());

            if (result != null && result.containsKey("instructorId")) {
                Number instructorId = (Number) result.get("instructorId");
                return instructorId.longValue();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not fetch instructor ID from backend: " + e.getMessage());

        }

        return null; // or return a default instructor ID
    }


    public int markConversationAsRead(Long instructorId, Long clientId) throws Exception {
        String endpoint = "http://localhost:8080/api/messages/conversation/" + instructorId + "/" + clientId + "/mark-read";
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        applyCommonHeaders(con);

        System.out.println(" [MessageService] Marking messages as read: " + instructorId + " <-> " + clientId);

        int responseCode = con.getResponseCode();
        System.out.println(" [MessageService] Response code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = sendRequest(con, null);
            int count = gson.fromJson(response, Integer.class);
            System.out.println(" [MessageService] Marked " + count + " messages as read");
            return count;
        } else {
            System.err.println(" [MessageService] Failed to mark as read. Code: " + responseCode);
            return 0;
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

    // Inner class for conversation summary
    public static class ConversationSummary {
        private Long userId;
        private String userName;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
        private int unreadCount;

        // Getters and setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
        }

        public LocalDateTime getLastMessageTime() {
            return lastMessageTime;
        }

        public void setLastMessageTime(LocalDateTime lastMessageTime) {
            this.lastMessageTime = lastMessageTime;
        }

        public int getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
        }
    }
}
