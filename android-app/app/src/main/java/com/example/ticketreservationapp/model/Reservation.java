package com.example.ticketreservationapp.model;

public class Reservation {
    private String reservationId;
    private String userId;
    private String userName;
    private String eventId;
    private String eventTitle;
    private String eventDate;
    private String eventLocation;
    private String reservationDate;
    private String status;

    public String getReservationId() { return reservationId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getEventId() { return eventId; }
    public String getEventTitle() { return eventTitle; }
    public String getEventDate() { return eventDate; }
    public String getEventLocation() { return eventLocation; }
    public String getReservationDate() { return reservationDate; }
    public String getStatus() { return status; }
}