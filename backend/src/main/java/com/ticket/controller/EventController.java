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

    // GET /api/events – public, with optional search filters (combined when multiple params are set)
    @GetMapping
    public ResponseEntity<?> getAllEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String date) {

        LocalDate parsed = null;
        if (date != null && !date.isBlank()) {
            try {
                parsed = LocalDate.parse(date);
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid date format. Use yyyy-MM-dd"));
            }
        }

        boolean anyFilter = (category != null && !category.isBlank())
                || (location != null && !location.isBlank())
                || parsed != null;

        if (anyFilter) {
            return ResponseEntity.ok(eventService.searchEvents(category, location, parsed));
        }
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // GET /api/events/{id} – public
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable String id) {
        return eventService.getEventById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/events - ORGANIZER only
    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody Event event,
            HttpSession session) {

        String sessionUserId = (String) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        if (!"ORGANIZER".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only organizers can create events"));
        }

        event.setOrganizerId(sessionUserId);

        return ResponseEntity.ok(eventService.createEvent(event));
    }

    // PUT /api/events/{id} - ORGANIZER only, and only the organizer of that event.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable String id,
            @RequestBody Event updatedEvent,
            HttpSession session) {

        String sessionUserId = (String) session.getAttribute(AuthController.SESSION_USER_ID);
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
        if (!existing.getOrganizerId().equals(sessionUserId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "You can only edit your own events"));
        }

        return ResponseEntity.ok(eventService.updateEvent(id, updatedEvent));
    }

    // DELETE /api/events/{id} - ORGANIZER only, and only the organizer of that event.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable String id,
            HttpSession session) {

        String sessionUserId = (String) session.getAttribute(AuthController.SESSION_USER_ID);
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
        if (!existing.getOrganizerId().equals(sessionUserId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "You can only delete your own events"));
        }

        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}