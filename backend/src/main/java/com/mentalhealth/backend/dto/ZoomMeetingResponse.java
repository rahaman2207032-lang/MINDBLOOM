package com.mentalhealth.backend.dto;



import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoomMeetingResponse {
    private Long id;

    @JsonProperty("join_url")
    private String joinUrl;

    @JsonProperty("start_url")
    private String startUrl;

    private String topic;
    private String password;

    @JsonProperty("start_time")
    private String startTime;

    private Integer duration;

    // Constructors
    public ZoomMeetingResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJoinUrl() { return joinUrl; }
    public void setJoinUrl(String joinUrl) { this.joinUrl = joinUrl; }

    public String getStartUrl() { return startUrl; }
    public void setStartUrl(String startUrl) { this.startUrl = startUrl; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}