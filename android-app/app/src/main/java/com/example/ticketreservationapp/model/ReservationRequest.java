package com.example.ticketreservationapp.model;

public class ReservationRequest {
    // Backend expects: { "eventId": "..." }
    private String eventId;

    public ReservationRequest(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }
}