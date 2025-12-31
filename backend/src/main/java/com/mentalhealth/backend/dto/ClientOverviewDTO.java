package com.mentalhealth.backend.dto;



public class ClientOverviewDTO {
    private Long clientId;
    private String clientName;
    private Double averageMood;
    private String stressLevel;
    private String lastSessionDate;
    private Integer totalSessions;
    private Boolean consentGranted;

    // Constructors
    public ClientOverviewDTO() {}

    public ClientOverviewDTO(Long clientId, String clientName, Double averageMood,
                             String stressLevel, String lastSessionDate,
                             Integer totalSessions, Boolean consentGranted) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.averageMood = averageMood;
        this.stressLevel = stressLevel;
        this.lastSessionDate = lastSessionDate;
        this.totalSessions = totalSessions;
        this.consentGranted = consentGranted;
    }

    // Getters and Setters
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public Double getAverageMood() { return averageMood; }
    public void setAverageMood(Double averageMood) { this.averageMood = averageMood; }

    public String getStressLevel() { return stressLevel; }
    public void setStressLevel(String stressLevel) { this.stressLevel = stressLevel; }

    public String getLastSessionDate() { return lastSessionDate; }
    public void setLastSessionDate(String lastSessionDate) { this.lastSessionDate = lastSessionDate; }

    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }

    public Boolean getConsentGranted() { return consentGranted; }
    public void setConsentGranted(Boolean consentGranted) { this.consentGranted = consentGranted; }
}