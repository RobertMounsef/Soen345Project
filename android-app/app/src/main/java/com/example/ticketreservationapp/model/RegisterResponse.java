package com.example.ticketreservationapp.model;

public class RegisterResponse {
    private String userId;   // Firebase push key (String, not Integer)
    private String name;
    private String email;
    private String phone;
    private String role;
    private String error;

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getError() {
        return error;
    }
}