package com.mentalhealth.backend.dto;

public class OnlineUsersResponse {
    private int count;

    // Constructors
    public OnlineUsersResponse() {}

    public OnlineUsersResponse(int count) {
        this.count = count;
    }

    // Getters and Setters
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}

