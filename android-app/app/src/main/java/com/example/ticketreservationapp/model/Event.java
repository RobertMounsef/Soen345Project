package com.example.ticketreservationapp.model;

public class Event {
    private Integer eventId;
    private Organizer organizer;
    private String title;
    private String category;
    private String eventDate;
    private String location;
    private Integer totalSpots;
    private Integer availableSpots;
    private String status;

    public Integer getEventId() {
        return eventId;
    }

    public Organizer getOrganizer() {
        return organizer;
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

    public static class Organizer {
        private Integer userId;
        private String name;

        public Integer getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }
    }
}