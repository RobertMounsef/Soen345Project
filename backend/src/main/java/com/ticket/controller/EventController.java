package com.ticket.controller;

import com.ticket.model.Event;
import com.ticket.service.EventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ---------------------------------------------------------------
    // GET /api/events – public (anyone can browse events)
    // ---------------------------------------------------------------
    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    // ---------------------------------------------------------------
    // GET /api/events/{id} – public
    // ---------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Integer id) {
        return eventService.getEventById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------------------------------------------------------
    // POST /api/events
    // ADMIN only. The organizer is set to the session user automatically.
    // ---------------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody Event event,
            HttpSession session) {

        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can create events"));
        }

        // Force the organizer to be the logged-in admin
        com.ticket.model.User organizer = new com.ticket.model.User();
        organizer.setUserId(sessionUserId);
        event.setOrganizer(organizer);

        return ResponseEntity.ok(eventService.createEvent(event));
    }

    // ---------------------------------------------------------------
    // PUT /api/events/{id}
    // ADMIN only, and only the organizer of that event.
    // ---------------------------------------------------------------
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
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can edit events"));
        }

        Event existing = eventService.getEventById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        // Only the organizer of this event may edit it
        if (!existing.getOrganizer().getUserId().equals(sessionUserId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "You can only edit your own events"));
        }

        return ResponseEntity.ok(eventService.updateEvent(id, updatedEvent));
    }

    // ---------------------------------------------------------------
    // DELETE /api/events/{id}
    // ADMIN only, and only the organizer of that event.
    // ---------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Integer id,
            HttpSession session) {

        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can delete events"));
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