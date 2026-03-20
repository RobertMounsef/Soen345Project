package com.ticket.controller;

import com.ticket.model.Event;
import com.ticket.service.EventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // GET /api/events – public, with optional search filters
    @GetMapping
    public ResponseEntity<?> getAllEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String date) {

        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(eventService.searchByCategory(category));
        }
        if (location != null && !location.isBlank()) {
            return ResponseEntity.ok(eventService.searchByLocation(location));
        }
        if (date != null && !date.isBlank()) {
            try {
                LocalDate parsed = LocalDate.parse(date);
                return ResponseEntity.ok(eventService.searchByDate(parsed));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid date format. Use yyyy-MM-dd"));
            }
        }

        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // GET /api/events/{id} – public
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Integer id) {
        return eventService.getEventById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/events - ORGANIZER only
    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody Event event,
            HttpSession session) {

        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        if (!"ORGANIZER".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only organizers can create events"));
        }

        com.ticket.model.User organizer = new com.ticket.model.User();
        organizer.setUserId(sessionUserId);
        event.setOrganizer(organizer);

        return ResponseEntity.ok(eventService.createEvent(event));
    }

    // PUT /api/events/{id} - ORGANIZER only, and only the organizer of that event.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Integer id,
            @RequestBody Event updatedEvent,
            HttpSession session) {

        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        if (!"ORGANIZER".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only organizers can edit events"));
        }

        Event existing = eventService.getEventById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existing.getOrganizer().getUserId().equals(sessionUserId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "You can only edit your own events"));
        }

        return ResponseEntity.ok(eventService.updateEvent(id, updatedEvent));
    }

    // DELETE /api/events/{id} - ORGANIZER only, and only the organizer of that event.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Integer id,
            HttpSession session) {

        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        if (!"ORGANIZER".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only organizers can delete events"));
        }

        Event existing = eventService.getEventById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (!existing.getOrganizer().getUserId().equals(sessionUserId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "You can only delete your own events"));
        }

        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}