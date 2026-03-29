package com.ticket.service;

import com.ticket.model.Event;
import com.ticket.repository.EventRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }

    public Event createEvent(Event event) {
        event.setAvailableSpots(event.getTotalSpots());
        return eventRepository.save(event);
    }

    public Event updateEvent(String id, Event updatedEvent) {
        Event event = eventRepository.findById(id).orElseThrow();
        event.setTitle(updatedEvent.getTitle());
        event.setCategory(updatedEvent.getCategory());
        event.setEventDate(updatedEvent.getEventDate());
        event.setLocation(updatedEvent.getLocation());
        event.setTotalSpots(updatedEvent.getTotalSpots());
        event.setAvailableSpots(updatedEvent.getAvailableSpots());
        event.setStatus(updatedEvent.getStatus());
        return eventRepository.save(event);
    }

    public void deleteEvent(String id) {
        eventRepository.deleteById(id);
    }

    public List<Event> searchByCategory(String category) {
        return eventRepository.findByCategoryIgnoreCase(category);
    }

    public List<Event> searchByLocation(String location) {
        return eventRepository.findByLocationContainingIgnoreCase(location);
    }

    public List<Event> searchByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return eventRepository.findByEventDateBetween(start, end);
    }

    /**
     * Applies category, location, and date filters together (AND). Any parameter may be null/blank to skip that filter.
     */
    public List<Event> searchEvents(String category, String location, LocalDate date) {
        List<Event> list = eventRepository.findAll();
        if (category != null && !category.isBlank()) {
            list = list.stream()
                    .filter(e -> category.equalsIgnoreCase(e.getCategory()))
                    .toList();
        }
        if (location != null && !location.isBlank()) {
            String loc = location.toLowerCase();
            list = list.stream()
                    .filter(e -> e.getLocation() != null
                            && e.getLocation().toLowerCase().contains(loc))
                    .toList();
        }
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);
            list = list.stream()
                    .filter(e -> e.getEventDate() != null
                            && !e.getEventDate().isBefore(start)
                            && !e.getEventDate().isAfter(end))
                    .toList();
        }
        return list;
    }
}