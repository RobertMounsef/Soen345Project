package com.ticket.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticket.model.Event;
import com.ticket.model.Reservation;
import com.ticket.service.EventService;
import com.ticket.service.ReservationService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final EventService eventService;

    public ReservationController(ReservationService reservationService, EventService eventService) {
        this.reservationService = reservationService;
        this.eventService = eventService;
    }

    // GET /api/reservations - CUSTOMER → all of their own reservations (across all events) - ADMIN → all reservations belonging to their own events
    @GetMapping
    public ResponseEntity<?> getReservations(HttpSession session) {
        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        if ("CUSTOMER".equals(role)) {
            // Customer sees all their own reservations
            return ResponseEntity.ok(reservationService.getReservationsByUserId(sessionUserId));
        } else {
            // Admin sees reservations only for events they organise
            List<Reservation> adminReservations = reservationService.getAllReservations()
                    .stream()
                    .filter(r -> r.getEvent().getOrganizer().getUserId().equals(sessionUserId))
                    .toList();
            return ResponseEntity.ok(adminReservations);
        }
    }


    // GET /api/reservations/event/{eventId} - CUSTOMER → their own reservation for that event (single object or 404) ADMIN → all reservations for that event, only if they organise it
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getReservationsForEvent(
            @PathVariable Integer eventId,
            HttpSession session) {

        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        if ("CUSTOMER".equals(role)) {
            // Customer: return their single reservation for this event, if any
            return reservationService.getReservationByEventAndUser(eventId, sessionUserId)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else {
            // Admin: must own the event
            Event event = eventService.getEventById(eventId).orElse(null);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }
            if (!event.getOrganizer().getUserId().equals(sessionUserId)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You can only view reservations for your own events"));
            }
            return ResponseEntity.ok(reservationService.getReservationsByEventId(eventId));
        }
    }

    // GET /api/reservations/{id} - CUSTOMER → only their own - ADMIN → only if they organise the related event
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Integer id, HttpSession session) {
        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        Reservation reservation = reservationService.getReservationById(id).orElse(null);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }

        if ("CUSTOMER".equals(role)) {
            if (!reservation.getUser().getUserId().equals(sessionUserId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
        } else {
            // Admin must organise the event this reservation belongs to
            if (!reservation.getEvent().getOrganizer().getUserId().equals(sessionUserId)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You can only view reservations for your own events"));
            }
        }

        return ResponseEntity.ok(reservation);
    }

    // POST /api/reservations — CUSTOMER only
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody Reservation reservation,
            HttpSession session) {

        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        if (!"CUSTOMER".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only customers can make reservations"));
        }

        // Force the reservation to belong to the logged-in customer
        com.ticket.model.User owner = new com.ticket.model.User();
        owner.setUserId(sessionUserId);
        reservation.setUser(owner);

        try {
            return ResponseEntity.ok(reservationService.createReservation(reservation));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/reservations/{id} — CUSTOMER only (their own)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Integer id, HttpSession session) {
        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        if (!"CUSTOMER".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only customers can cancel reservations"));
        }

        Reservation reservation = reservationService.getReservationById(id).orElse(null);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }
        if (!reservation.getUser().getUserId().equals(sessionUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only cancel your own reservations"));
        }

        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}