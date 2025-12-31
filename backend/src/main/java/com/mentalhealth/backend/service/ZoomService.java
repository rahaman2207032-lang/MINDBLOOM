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
@ConditionalOnProperty(prefix = "zoom.api", name = "account-id")
public class ZoomService {

    @Value("${zoom.api.account-id}")
    private String zoomAccountId;

    @Value("${zoom.api.key}")
    private String zoomApiKey;      // Client ID

    @Value("${zoom.api.secret}")
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

        String tokenUrl = "https://zoom.us/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(zoomApiKey, zoomApiSecret);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "account_credentials");
        body.put("account_id", zoomAccountId);

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK
                && response.getBody() != null) {

            return response.getBody().get("access_token").toString();
        }

        throw new RuntimeException("Failed to obtain Zoom access token");
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
