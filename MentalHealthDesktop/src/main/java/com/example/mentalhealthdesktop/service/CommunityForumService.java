package com.example.mentalhealthdesktop.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CommunityForumService {

    private static final String BASE_URL = "http://localhost:8080/api/forum";
    private final HttpClient httpClient;
    private final Gson gson;

    public CommunityForumService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    /**
     * Create a new forum post
     */
    public JsonObject createPost(String title, String content, Long userId, boolean anonymous) throws Exception {
        JsonObject postData = new JsonObject();
        postData.addProperty("title", title);
        postData.addProperty("content", content);
        postData.addProperty("userId", userId);
        postData.addProperty("anonymous", anonymous);

        String requestBody = gson.toJson(postData);
        System.out.println("📤 [ForumService] Creating post with data: " + requestBody);
        System.out.println("📤 [ForumService] Sending to: " + BASE_URL + "/posts");

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/posts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        // If userId is provided, set X-User-Id header for backend to read
        if (userId != null) rb.header("X-User-Id", String.valueOf(userId));

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("📡 [ForumService] Response status: " + response.statusCode());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("✅ [ForumService] Post created successfully");
            System.out.println("📄 [ForumService] Response body: " + response.body());
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } else {
            // Log the error response body for debugging
            String errorBody = response.body();
            System.err.println("❌ [ForumService] Failed to create post!");
            System.err.println("❌ Status code: " + response.statusCode());
            System.err.println("❌ Error response: " + errorBody);

            // Try to extract meaningful error message
            String errorMessage = "Failed to create post. Status: " + response.statusCode();
            if (errorBody != null && !errorBody.isEmpty()) {
                System.err.println("❌ Backend says: " + errorBody);
                errorMessage += ". Backend error: " + (errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody);
            }

            throw new Exception(errorMessage);
        }
    }

    /**
     * Get all posts with sorting and user context
     */
    public JsonArray getPosts(String sortType, Long userId) throws Exception {
        String sortParam = sortType.toLowerCase().replace(" ", "_");
        String url = BASE_URL + "/posts?sort=" + sortParam;

        // Add userId parameter if provided
        if (userId != null && userId > 0) {
            url += "&userId=" + userId;
        }

        System.out.println("📥 [ForumService] Fetching posts from: " + url);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        if (userId != null) rb.header("X-User-Id", String.valueOf(userId));

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("✅ [ForumService] Posts retrieved successfully");
            String responseBody = response.body();

            // Handle empty response
            if (responseBody == null || responseBody.trim().isEmpty() || responseBody.equals("[]")) {
                System.out.println("ℹ️ [ForumService] No posts found");
                return new JsonArray();
            }

            return JsonParser.parseString(responseBody).getAsJsonArray();
        } else if (response.statusCode() == 404) {
            System.out.println("ℹ️ [ForumService] No posts endpoint found yet");
            return new JsonArray();
        } else {
            System.err.println("❌ [ForumService] Failed to fetch posts. Status: " + response.statusCode());
            System.err.println("Response: " + response.body());
            return new JsonArray(); // Return empty array instead of throwing exception
        }
    }

    /**
     * Get a single post by ID
     */
    public JsonObject getPost(Long postId) throws Exception {
        String url = BASE_URL + "/posts/" + postId;

        System.out.println("📥 [ForumService] Fetching post from: " + url);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        // no user context here
        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("✅ [ForumService] Post retrieved successfully");
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } else {
            throw new Exception("Failed to fetch post. Status: " + response.statusCode());
        }
    }

    /**
     * Toggle like on a post
     */
    public JsonObject toggleLike(Long postId, Long userId) throws Exception {
        JsonObject likeData = new JsonObject();
        likeData.addProperty("userId", userId);

        String requestBody = gson.toJson(likeData);
        System.out.println("❤️ [ForumService] Toggling like for post " + postId);
        System.out.println("❤️ [ForumService] Request URL: " + BASE_URL + "/posts/" + postId + "/like");
        System.out.println("❤️ [ForumService] Request body: " + requestBody);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/posts/" + postId + "/like"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        if (userId != null) rb.header("X-User-Id", String.valueOf(userId));

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("📡 [ForumService] Like response status: " + response.statusCode());

        if (response.statusCode() == 200) {
            System.out.println("✅ [ForumService] Like toggled successfully");
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } else {
            // Log the error response
            String errorBody = response.body();
            System.err.println("❌ [ForumService] Failed to toggle like!");
            System.err.println("❌ Status code: " + response.statusCode());
            System.err.println("❌ Error response: " + errorBody);

            String errorMessage = "Failed to toggle like. Status: " + response.statusCode();
            if (errorBody != null && !errorBody.isEmpty()) {
                errorMessage += ". Backend error: " + errorBody;
            }

            throw new Exception(errorMessage);
        }
    }

    /**
     * Get comments for a post
     */
    public JsonArray getComments(Long postId) throws Exception {
        String url = BASE_URL + "/posts/" + postId + "/comments";

        System.out.println("📥 [ForumService] Fetching comments from: " + url);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("✅ [ForumService] Comments retrieved successfully");
            return JsonParser.parseString(response.body()).getAsJsonArray();
        } else {
            throw new Exception("Failed to fetch comments. Status: " + response.statusCode());
        }
    }

    /**
     * Add a comment to a post
     * Returns the full response including commentCount from backend
     */
    public CommentResult addComment(Long postId, String content, Long userId, boolean anonymous) throws Exception {
        JsonObject commentData = new JsonObject();
        commentData.addProperty("content", content);
        commentData.addProperty("userId", userId);
        commentData.addProperty("anonymous", anonymous);

        String requestBody = gson.toJson(commentData);
        System.out.println("💬 [ForumService] Adding comment to post " + postId);
        System.out.println("💬 [ForumService] Request URL: " + BASE_URL + "/posts/" + postId + "/comments");
        System.out.println("💬 [ForumService] Request body: " + requestBody);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/posts/" + postId + "/comments"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        if (userId != null) rb.header("X-User-Id", String.valueOf(userId));

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("📡 [ForumService] Comment response status: " + response.statusCode());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("✅ [ForumService] Comment added successfully");
            JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();

            // Parse new backend response format: {success: true, commentCount: X, comment: {...}}
            if (responseJson.has("success") && responseJson.get("success").getAsBoolean()) {
                int commentCount = responseJson.get("commentCount").getAsInt();
                JsonObject commentJson = responseJson.getAsJsonObject("comment");

                System.out.println("📊 [ForumService] Updated comment count: " + commentCount);
                return new CommentResult(commentJson, commentCount);
            } else {
                // Fallback for old response format (just the comment object)
                System.out.println("⚠️ [ForumService] Old response format detected");
                return new CommentResult(responseJson, -1); // -1 indicates count not available
            }
        } else {
            // Log the error response
            String errorBody = response.body();
            System.err.println("❌ [ForumService] Failed to add comment!");
            System.err.println("❌ Status code: " + response.statusCode());
            System.err.println("❌ Error response: " + errorBody);

            String errorMessage = "Failed to add comment. Status: " + response.statusCode();
            if (errorBody != null && !errorBody.isEmpty()) {
                errorMessage += ". Backend error: " + errorBody;
            }

            throw new Exception(errorMessage);
        }
    }

    /**
     * Result object for addComment method containing the comment and updated count
     */
    public static class CommentResult {
        public final JsonObject comment;
        public final int updatedCommentCount;

        public CommentResult(JsonObject comment, int count) {
            this.comment = comment;
            this.updatedCommentCount = count;
        }
    }

    /**
     * Search posts
     */
    public JsonArray searchPosts(String query) throws Exception {
        String url = BASE_URL + "/posts/search?q=" + query;

        System.out.println("🔍 [ForumService] Searching posts: " + url);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("✅ [ForumService] Search completed successfully");
            return JsonParser.parseString(response.body()).getAsJsonArray();
        } else {
            throw new Exception("Failed to search posts. Status: " + response.statusCode());
        }
    }

    /**
     * Delete a post
     */
    public void deletePost(Long postId) throws Exception {
        System.out.println("🗑️ [ForumService] Deleting post: " + postId);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/posts/" + postId))
                .DELETE();

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 204) {
            System.out.println("✅ [ForumService] Post deleted successfully");
        } else {
            throw new Exception("Failed to delete post. Status: " + response.statusCode());
        }
    }

    /**
     * Delete a comment (only by author)
     * Returns the updated comment count and post ID from backend
     */
    public DeleteResult deleteComment(Long commentId, Long userId) throws Exception {
        System.out.println("🗑️ [ForumService] Deleting comment: " + commentId + " by user: " + userId);

        // ⭐ Add userId as query parameter as required by backend
        String url = BASE_URL + "/comments/" + commentId + "?userId=" + userId;

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE();

        // Also set X-User-Id header for consistency
        if (userId != null) rb.header("X-User-Id", String.valueOf(userId));

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("📡 [ForumService] Delete response status: " + response.statusCode());

        if (response.statusCode() == 200) {
            System.out.println("✅ [ForumService] Comment deleted successfully");

            // Parse new backend response format: {success: true, message: "...", commentCount: X, postId: Y}
            JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();

            if (responseJson.has("success") && responseJson.get("success").getAsBoolean()) {
                Long postId = responseJson.get("postId").getAsLong();
                int commentCount = responseJson.get("commentCount").getAsInt();
                String message = responseJson.has("message") ? responseJson.get("message").getAsString() : "Deleted";

                System.out.println("📊 [ForumService] Post " + postId + " now has " + commentCount + " comments");
                return new DeleteResult(postId, commentCount, message);
            } else {
                throw new Exception("Backend returned success=false");
            }
        } else if (response.statusCode() == 204) {
            // Old backend response (no content) - fallback behavior
            System.out.println("⚠️ [ForumService] Old response format (204 No Content)");
            return new DeleteResult(null, -1, "Deleted (count unavailable)");
        } else {
            String errorBody = response.body();
            System.err.println("❌ [ForumService] Failed to delete comment!");
            System.err.println("❌ Status code: " + response.statusCode());
            System.err.println("❌ Error response: " + errorBody);
            throw new Exception("Failed to delete comment. Status: " + response.statusCode());
        }
    }

    /**
     * Result object for deleteComment method containing post ID, updated count, and message
     */
    public static class DeleteResult {
        public final Long postId;
        public final int updatedCommentCount;
        public final String message;

        public DeleteResult(Long postId, int count, String message) {
            this.postId = postId;
            this.updatedCommentCount = count;
            this.message = message;
        }
    }

    // ==================== GROUP CHAT METHODS ====================

    /**
     * Send a chat message to the group
     */
    public JsonObject sendChatMessage(String message, Long userId, boolean anonymous) throws Exception {
        JsonObject messageData = new JsonObject();
        messageData.addProperty("message", message);
        messageData.addProperty("userId", userId);
        messageData.addProperty("anonymous", anonymous);

        String requestBody = gson.toJson(messageData);
        System.out.println("💬 [ForumService] Sending chat message");

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/chat/messages"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        if (userId != null) rb.header("X-User-Id", String.valueOf(userId));

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            System.out.println("✅ [ForumService] Chat message sent successfully");
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } else {
            throw new Exception("Failed to send chat message. Status: " + response.statusCode());
        }
    }

    /**
     * Get all chat messages (recent first)
     */
    public JsonArray getChatMessages() throws Exception {
        String url = BASE_URL + "/chat/messages?limit=100";

        System.out.println("📥 [ForumService] Fetching chat messages from: " + url);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("✅ [ForumService] Chat messages retrieved successfully");
            return JsonParser.parseString(response.body()).getAsJsonArray();
        } else {
            throw new Exception("Failed to fetch chat messages. Status: " + response.statusCode());
        }
    }

    /**
     * Get count of online users
     */
    public int getOnlineUsersCount() throws Exception {
        String url = BASE_URL + "/chat/online-count";

        HttpRequest.Builder rb = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET();

        HttpRequest request = rb.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject result = JsonParser.parseString(response.body()).getAsJsonObject();
            return result.get("count").getAsInt();
        } else {
            return 0; // Return 0 if failed
        }
    }
}

