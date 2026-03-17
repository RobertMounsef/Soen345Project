package com.example.ticketreservationapp.model;

public class ReservationRequest {

    private EventReference event;

    public ReservationRequest(Integer eventId) {
        this.event = new EventReference(eventId);
    }

    public EventReference getEvent() {
        return event;
    }

    public static class EventReference {
        private Integer eventId;

        public EventReference(Integer eventId) {
            this.eventId = eventId;
        }

        public Integer getEventId() {
            return eventId;
        }
    }
}