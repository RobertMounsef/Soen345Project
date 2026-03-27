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

    // GET /api/reservations - CUSTOMER → all of their own reservations - ORGANIZER → reservations for their events
    @GetMapping
    public ResponseEntity<?> getReservations(HttpSession session) {
        String sessionUserId = (String) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        if ("CUSTOMER".equals(role)) {
            return ResponseEntity.ok(reservationService.getReservationsByUserId(sessionUserId));
        } else {
            // Organizer sees reservations only for events they organise
            List<Reservation> orgReservations = reservationService.getAllReservations()
                    .stream()
                    .filter(r -> {
                        Event event = eventService.getEventById(r.getEventId()).orElse(null);
                        return event != null && sessionUserId.equals(event.getOrganizerId());
                    })
                    .toList();
            return ResponseEntity.ok(orgReservations);
        }
    }

    // GET /api/reservations/event/{eventId}
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getReservationsForEvent(
            @PathVariable String eventId,
            HttpSession session) {

        String sessionUserId = (String) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        if ("CUSTOMER".equals(role)) {
            return reservationService.getReservationByEventAndUser(eventId, sessionUserId)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else {
            Event event = eventService.getEventById(eventId).orElse(null);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }
            if (!sessionUserId.equals(event.getOrganizerId())) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You can only view reservations for your own events"));
            }
            return ResponseEntity.ok(reservationService.getReservationsByEventId(eventId));
        }
    }

    // GET /api/reservations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable String id, HttpSession session) {
        String sessionUserId = (String) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        Reservation reservation = reservationService.getReservationById(id).orElse(null);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }

        if ("CUSTOMER".equals(role)) {
            if (!reservation.getUserId().equals(sessionUserId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
        } else {
            Event event = eventService.getEventById(reservation.getEventId()).orElse(null);
            if (event == null || !sessionUserId.equals(event.getOrganizerId())) {
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

        String sessionUserId = (String) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        if (!"CUSTOMER".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only customers can make reservations"));
        }

        // Force the reservation to belong to the logged-in customer
        reservation.setUserId(sessionUserId);

        try {
            return ResponseEntity.ok(reservationService.createReservation(reservation));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/reservations/{id} — CUSTOMER only (their own)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable String id, HttpSession session) {
        String sessionUserId = (String) session.getAttribute(AuthController.SESSION_USER_ID);
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
        if (!reservation.getUserId().equals(sessionUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only cancel your own reservations"));
        }

        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}