package com.example.mentalhealthdesktop.model;

import com.google.gson.annotations.SerializedName;

public class ClientOverview {
    @SerializedName("clientId")
    private Long clientId;

    @SerializedName("clientName")
    private String clientName;

    @SerializedName("averageMood")
    private Double averageMood;

    @SerializedName("stressLevel")
    private String stressLevel; // Low, Moderate, High

    @SerializedName("lastSessionDate")
    private String lastSessionDate;

    @SerializedName("totalSessions")
    private Integer totalSessions;

    public ClientOverview() {
    }

    public ClientOverview(Long clientId, String clientName, Double averageMood,
                         String stressLevel, String lastSessionDate, Integer totalSessions) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.averageMood = averageMood;
        this.stressLevel = stressLevel;
        this.lastSessionDate = lastSessionDate;
        this.totalSessions = totalSessions;
    }

    // Getters and Setters
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Double getAverageMood() {
        return averageMood;
    }

    public void setAverageMood(Double averageMood) {
        this.averageMood = averageMood;
    }

    public String getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(String stressLevel) {
        this.stressLevel = stressLevel;
    }

    public String getLastSessionDate() {
        return lastSessionDate;
    }

    public void setLastSessionDate(String lastSessionDate) {
        this.lastSessionDate = lastSessionDate;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
    }
}

