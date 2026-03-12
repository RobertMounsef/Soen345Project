package com.ticket.controller;

import com.ticket.model.Reservation;
import com.ticket.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // ---------------------------------------------------------------
    // GET /api/reservations
    // ADMIN → all reservations | CUSTOMER → only their own
    // ---------------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> getAllReservations(HttpSession session) {
        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(reservationService.getAllReservations());
        } else {
            // CUSTOMERs only see their own reservations
            return ResponseEntity.ok(reservationService.getReservationsByUserId(sessionUserId));
        }
    }

    // ---------------------------------------------------------------
    // GET /api/reservations/{id}
    // ADMIN → any reservation | CUSTOMER → only if it belongs to them
    // ---------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Integer id, HttpSession session) {
        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        Reservation reservation = reservationService.getReservationById(id)
                .orElse(null);

        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }

        // CUSTOMER cannot view another user's reservation
        if ("CUSTOMER".equals(role) && !reservation.getUser().getUserId().equals(sessionUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        return ResponseEntity.ok(reservation);
    }

    // ---------------------------------------------------------------
    // POST /api/reservations
    // Must be logged in. The reservation is automatically linked to
    // the session user — the body's user field is ignored.
    // ---------------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody Reservation reservation,
            HttpSession session) {

        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        // Force the reservation to belong to the logged-in user
        com.ticket.model.User owner = new com.ticket.model.User();
        owner.setUserId(sessionUserId);
        reservation.setUser(owner);

        return ResponseEntity.ok(reservationService.createReservation(reservation));
    }

    // ---------------------------------------------------------------
    // DELETE /api/reservations/{id}
    // ADMIN → any | CUSTOMER → only their own
    // ---------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Integer id, HttpSession session) {
        Integer sessionUserId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_USER_ROLE);

        if (sessionUserId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        Reservation reservation = reservationService.getReservationById(id).orElse(null);

        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }

        // CUSTOMER cannot cancel another user's reservation
        if ("CUSTOMER".equals(role) && !reservation.getUser().getUserId().equals(sessionUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}