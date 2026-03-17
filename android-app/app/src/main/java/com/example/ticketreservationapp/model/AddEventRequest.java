package com.example.ticketreservationapp.model;

public class AddEventRequest {
    private String title;
    private String category;
    private String eventDate;
    private String location;
    private Integer totalSpots;
    private Integer availableSpots;
    private String status;

    public AddEventRequest(String title, String category, String eventDate,
                           String location, Integer totalSpots,
                           Integer availableSpots, String status) {
        this.title = title;
        this.category = category;
        this.eventDate = eventDate;
        this.location = location;
        this.totalSpots = totalSpots;
        this.availableSpots = availableSpots;
        this.status = status;
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