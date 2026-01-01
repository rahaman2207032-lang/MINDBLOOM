package com.mentalhealth.backend.service;

import com.mentalhealth.backend.dto.ZoomMeetingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "zoom", name = "account-id")
public class ZoomService {

    @Value("${zoom.account-id}")
    private String zoomAccountId;

    @Value("${zoom.client-id}")
    private String zoomApiKey;      // Client ID

    @Value("${zoom.client-secret}")
    private String zoomApiSecret;   // Client Secret

    @Value("${zoom.api.base-url:https://api.zoom.us/v2}")
    private String zoomApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Create a Zoom meeting
     *
     * @param topic      Meeting topic/title
     * @param startTime  Meeting start time
     * @param duration  Duration in minutes
     * @return Zoom meeting join URL
     */
    public String createMeeting(String topic, LocalDateTime startTime, int duration) {

        String accessToken = generateAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("topic", topic);
        requestBody.put("type", 2); // Scheduled meeting
        requestBody.put("start_time", startTime.format(DateTimeFormatter.ISO_DATE_TIME));
        requestBody.put("duration", duration);
        requestBody.put("timezone", "UTC");

        Map<String, Object> settings = new HashMap<>();
        settings.put("host_video", true);
        settings.put("participant_video", true);
        settings.put("join_before_host", false);
        settings.put("mute_upon_entry", true);
        settings.put("waiting_room", true);

        requestBody.put("settings", settings);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<ZoomMeetingResponse> response =
                restTemplate.exchange(
                        zoomApiBaseUrl + "/users/me/meetings",
                        HttpMethod.POST,
                        entity,
                        ZoomMeetingResponse.class
                );

        if (response.getStatusCode() == HttpStatus.CREATED
                && response.getBody() != null) {

            return response.getBody().getJoinUrl();
        }

        throw new RuntimeException("Failed to create Zoom meeting");
    }

    /**
     * Generate OAuth access token (Server-to-Server OAuth)
     */
    private String generateAccessToken() {
        System.out.println("🔑 Generating Zoom OAuth token...");
        System.out.println("   Account ID: " + (zoomAccountId != null ? zoomAccountId : "NULL"));
        System.out.println("   Client ID: " + (zoomApiKey != null ? zoomApiKey.substring(0, Math.min(10, zoomApiKey.length())) + "..." : "NULL"));

        String tokenUrl = "https://zoom.us/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(zoomApiKey, zoomApiSecret);

        // IMPORTANT: For Server-to-Server OAuth, use URL-encoded string, not Map!
        String body = "grant_type=account_credentials&account_id=" + zoomAccountId;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            System.out.println("📡 Requesting token from: " + tokenUrl);
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(tokenUrl, request, Map.class);

            System.out.println("📊 Response status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null) {

                String token = response.getBody().get("access_token").toString();
                System.out.println("✅ OAuth token generated successfully");
                System.out.println("   Token: " + token.substring(0, Math.min(20, token.length())) + "...");
                return token;
            }

            System.err.println("❌ Failed to get OAuth token");
            System.err.println("   Status: " + response.getStatusCode());
            System.err.println("   Body: " + response.getBody());
            throw new RuntimeException("Failed to obtain Zoom access token");

        } catch (Exception e) {
            System.err.println("❌ ERROR generating OAuth token: " + e.getMessage());
            System.err.println("   Error Type: " + e.getClass().getSimpleName());
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                System.err.println("   ⚠️ 401 Unauthorized - Check your Client ID and Client Secret!");
            } else if (e.getMessage() != null && e.getMessage().contains("400")) {
                System.err.println("   ⚠️ 400 Bad Request - Check your Account ID!");
            }
            e.printStackTrace();
            throw new RuntimeException("Failed to obtain Zoom access token: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a Zoom meeting
     *
     * @param meetingId Zoom meeting ID
     */
    public void deleteMeeting(String meetingId) {

        String accessToken = generateAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                zoomApiBaseUrl + "/meetings/" + meetingId,
                HttpMethod.DELETE,
                entity,
                Void.class
        );
    }
}
