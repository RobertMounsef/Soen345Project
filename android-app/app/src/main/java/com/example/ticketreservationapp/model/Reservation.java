package com.example.ticketreservationapp.model;

public class Reservation {
    private String reservationId;  // Firebase push key
    private String userId;         // Flat key reference
    private String eventId;        // Flat key reference
    private String reservationDate;
    private String status;

    public String getReservationId() {
        return reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public String getStatus() {
        return status;
    }
}