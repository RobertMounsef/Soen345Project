package com.ticket.model;

import java.time.LocalDateTime;

public class Reservation {

    private String reservationId;

    /** Firebase push key of the User who made the reservation */
    private String userId;

    /** Firebase push key of the Event being reserved */
    private String eventId;

    private LocalDateTime reservationDate = LocalDateTime.now();
    private Status status = Status.CONFIRMED;

    public enum Status {
        CONFIRMED,
        CANCELLED
    }

    // Getters and Setters

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}