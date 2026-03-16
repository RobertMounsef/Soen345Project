package com.example.ticketreservationapp.model;

public class Reservation {
    private Integer reservationId;
    private String reservationDate;
    private String status;
    private Event event;

    public Integer getReservationId() {
        return reservationId;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public String getStatus() {
        return status;
    }

    public Event getEvent() {
        return event;
    }
}