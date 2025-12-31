package com.example.mentalhealthdesktop.model;

public class Instructor {
    private Long id;
    private String username;
    private String email;
    private String specialization;

    // Constructors
    public Instructor() {}

    public Instructor(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // For ComboBox display
    @Override
    public String toString() {
        return username + (specialization != null ? " - " + specialization : "");
    }
}

