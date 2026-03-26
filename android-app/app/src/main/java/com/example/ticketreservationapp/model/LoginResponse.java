package com.example.ticketreservationapp.model;

public class LoginResponse {
    private String message;
    private String userId;   // Firebase push key (String, not Integer)
    private String role;
    private String error;

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getError() {
        return error;
    }
}