package com.example.ticketreservationapp.model;

public class Event {
    private String eventId;       // Firebase push key
    private String organizerId;   // Flat key reference (was nested Organizer object)
    private String title;
    private String category;
    private String eventDate;
    private String location;
    private Integer totalSpots;
    private Integer availableSpots;
    private String status;

    public String getEventId() {
        return eventId;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getLocation() {
        return location;
    }

    public Integer getTotalSpots() {
        return totalSpots;
    }

    public Integer getAvailableSpots() {
        return availableSpots;
    }

    public String getStatus() {
        return status;
    }
}