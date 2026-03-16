package com.example.ticketreservationapp.model;

public class RegisterResponse {
    private Integer userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String error;

    public Integer getUserId() {
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