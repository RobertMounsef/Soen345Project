package com.example.ticketreservationapp.model;

public class LoginResponse {
    private String message;
    private Integer userId;
    private String role;
    private String error;

    public String getMessage() {
        return message;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getError() {
        return error;
    }
}